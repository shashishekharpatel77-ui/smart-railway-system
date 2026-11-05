package com.railway.smartrailway.repository;

import com.railway.smartrailway.model.BookingRecord;
import com.railway.smartrailway.model.WaitingRecord;
import com.railway.smartrailway.util.IndianTime;
import com.railway.smartrailway.util.RunDays;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;
import java.util.UUID;

@Repository
public class BookingRepository {

    private final JdbcTemplate jdbcTemplate;

    public BookingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Find user ID using username
    public Integer findUserId(String username) {

        String sql = """
                SELECT user_id
                FROM users
                WHERE username = ?
                """;

        List<Integer> ids = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getInt("user_id"),
                username
        );

        return ids.isEmpty() ? null : ids.get(0);
    }

    // Check whether the train is active and runs on the selected date.
    public boolean isTrainRunning(
            int trainId,
            java.time.LocalDate travelDate) {

        String sql = """
                SELECT sc.run_days
                FROM schedules sc
                JOIN trains t ON t.train_id = sc.train_id
                WHERE sc.train_id = ?
                  AND t.status = 'ACTIVE'
                """;

        List<String> rows = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getString("run_days"),
                trainId
        );

        for (String runDays : rows) {
            if (RunDays.runsOn(runDays, travelDate)) {
                return true;
            }
        }

        return false;
    }

    // Create the user on first use and return the user ID.
    public Integer createUserIfAbsent(String username) {

        Integer existing = findUserId(username);

        if (existing != null) {
            return existing;
        }

        String sql = """
                INSERT IGNORE INTO users (name, username, password)
                VALUES (?, ?, ?)
                """;

        jdbcTemplate.update(
                sql,
                username,
                username,
                UUID.randomUUID().toString()
        );

        return findUserId(username);
    }

    // Check whether passenger already has a confirmed ticket or waiting entry.
    public boolean hasActiveTicket(
            int userId,
            int trainId,
            java.time.LocalDate travelDate,
            String passengerName) {

        String sql = """
                SELECT
                    (SELECT COUNT(*) FROM bookings
                      WHERE user_id = ? AND train_id = ? AND travel_date = ?
                        AND booking_status = 'CONFIRMED'
                        AND LOWER(passenger_name) = LOWER(?))
                  + (SELECT COUNT(*) FROM waiting_list
                      WHERE user_id = ? AND train_id = ? AND travel_date = ?
                        AND status = 'WAITING'
                        AND LOWER(passenger_name) = LOWER(?))
                """;

        Date sqlDate = Date.valueOf(travelDate);

        Integer count = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                userId, trainId, sqlDate, passengerName,
                userId, trainId, sqlDate, passengerName
        );

        return count != null && count > 0;
    }

    // Seat label such as S1-12 for messages
    public String findSeatNumber(int seatId) {

        List<String> rows = jdbcTemplate.query(
                "SELECT seat_number FROM seats WHERE seat_id = ?",
                (rs, rowNum) -> rs.getString("seat_number"),
                seatId
        );

        return rows.isEmpty() ? String.valueOf(seatId) : rows.get(0);
    }

    // Segment-wise seat availability.
    // A seat can be reused only when the existing journey does not overlap.
    public Integer findFreeSeat(
            int trainId,
            String classType,
            java.time.LocalDate travelDate,
            int sourceId,
            int destinationId) {

        String sql = """
                SELECT s.seat_id
                FROM seats s
                JOIN coaches c
                    ON s.coach_id = c.coach_id

                JOIN train_routes req_src
                    ON req_src.train_id = c.train_id
                   AND req_src.station_id = ?

                JOIN train_routes req_dest
                    ON req_dest.train_id = req_src.train_id
                   AND req_dest.direction = req_src.direction
                   AND req_dest.station_id = ?
                   AND req_src.stop_number < req_dest.stop_number

                WHERE c.train_id = ?
                  AND c.coach_type = ?
                  AND s.seat_status = 'AVAILABLE'

                  AND NOT EXISTS (
                      SELECT 1
                      FROM bookings b

                      JOIN train_routes booked_src
                          ON booked_src.train_id = b.train_id
                         AND booked_src.direction = b.direction
                         AND booked_src.station_id = b.source_station_id

                      JOIN train_routes booked_dest
                          ON booked_dest.train_id = b.train_id
                         AND booked_dest.direction = b.direction
                         AND booked_dest.station_id = b.destination_station_id
                         AND booked_src.stop_number < booked_dest.stop_number

                      WHERE b.seat_id = s.seat_id
                        AND b.train_id = c.train_id
                        AND b.travel_date = ?
                        AND b.booking_status = 'CONFIRMED'
                        AND b.direction = req_src.direction

                        AND booked_src.stop_number < req_dest.stop_number
                        AND req_src.stop_number < booked_dest.stop_number
                  )

                ORDER BY s.seat_id
                LIMIT 1
                """;

        List<Integer> ids = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getInt("seat_id"),
                sourceId,
                destinationId,
                trainId,
                classType,
                Date.valueOf(travelDate)
        );

        return ids.isEmpty() ? null : ids.get(0);
    }

    // Backward-compatible method for existing callers.
    // This checks availability for the complete train journey.
    public Integer findFreeSeat(
            int trainId,
            String classType,
            java.time.LocalDate travelDate) {

        String sql = """
                SELECT s.seat_id
                FROM seats s
                JOIN coaches c
                    ON s.coach_id = c.coach_id
                WHERE c.train_id = ?
                  AND c.coach_type = ?
                  AND s.seat_status = 'AVAILABLE'
                  AND NOT EXISTS (
                      SELECT 1
                      FROM bookings b
                      WHERE b.seat_id = s.seat_id
                        AND b.travel_date = ?
                        AND b.booking_status = 'CONFIRMED'
                  )
                ORDER BY s.seat_id
                LIMIT 1
                """;

        List<Integer> ids = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getInt("seat_id"),
                trainId,
                classType,
                Date.valueOf(travelDate)
        );

        return ids.isEmpty() ? null : ids.get(0);
    }

    // Find fare for the selected train, route and class
    public Double findFare(
            int trainId,
            int sourceId,
            int destinationId,
            String classType) {

        String sql = """
                SELECT fare
                FROM fares
                WHERE train_id = ?
                  AND source_station_id = ?
                  AND destination_station_id = ?
                  AND class_type = ?
                LIMIT 1
                """;

        List<Double> fares = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getDouble("fare"),
                trainId,
                sourceId,
                destinationId,
                classType
        );

        return fares.isEmpty() ? null : fares.get(0);
    }

    // Save a confirmed booking
    public int saveBooking(
            BookingRecord booking,
            double fare) {

        return saveBooking(
                booking,
                fare,
                booking.getClassType()
        );
    }

    // Save a confirmed booking with class type and direction
    public int saveBooking(
            BookingRecord booking,
            double fare,
            String classType) {

        String sql = """
                INSERT INTO bookings (
                    user_id,
                    train_id,
                    source_station_id,
                    destination_station_id,
                    seat_id,
                    booking_date,
                    travel_date,
                    passenger_name,
                    booking_status,
                    class_type,
                    direction
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'CONFIRMED', ?,
                    (
                        SELECT src.direction
                        FROM train_routes src
                        JOIN train_routes dest
                            ON dest.train_id = src.train_id
                           AND dest.direction = src.direction
                        WHERE src.train_id = ?
                          AND src.station_id = ?
                          AND dest.station_id = ?
                          AND src.stop_number < dest.stop_number
                        LIMIT 1
                    )
                )
                """;

        return jdbcTemplate.update(
                sql,
                booking.getUserId(),
                booking.getTrainId(),
                booking.getSourceStationId(),
                booking.getDestinationStationId(),
                booking.getSeatId(),
                Date.valueOf(IndianTime.today()),
                Date.valueOf(booking.getTravelDate()),
                booking.getPassengerName(),
                classType,
                booking.getTrainId(),
                booking.getSourceStationId(),
                booking.getDestinationStationId()
        );
    }

    // Generate the next waiting-list number
    public int getNextWaitingNumber(
            int trainId,
            java.time.LocalDate travelDate,
            String classType) {

        String sql = """
                SELECT COALESCE(MAX(waiting_number), 0) + 1
                FROM waiting_list
                WHERE train_id = ?
                  AND travel_date = ?
                  AND class_type = ?
                """;

        Integer number = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                trainId,
                Date.valueOf(travelDate),
                classType
        );

        return number == null ? 1 : number;
    }

    // Backward-compatible waiting-list method
    public int addToWaitingList(
            int userId,
            int trainId,
            int sourceStationId,
            int destinationStationId,
            java.time.LocalDate travelDate,
            String passengerName,
            int waitingNumber) {

        return addToWaitingList(
                userId,
                trainId,
                sourceStationId,
                destinationStationId,
                travelDate,
                passengerName,
                waitingNumber,
                "Sleeper"
        );
    }

    // Add a passenger to the waiting list
    public int addToWaitingList(
            int userId,
            int trainId,
            int sourceStationId,
            int destinationStationId,
            java.time.LocalDate travelDate,
            String passengerName,
            int waitingNumber,
            String classType) {

        String sql = """
                INSERT INTO waiting_list (
                    user_id,
                    train_id,
                    source_station_id,
                    destination_station_id,
                    travel_date,
                    passenger_name,
                    waiting_number,
                    status,
                    class_type
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, 'WAITING', ?)
                """;

        return jdbcTemplate.update(
                sql,
                userId,
                trainId,
                sourceStationId,
                destinationStationId,
                Date.valueOf(travelDate),
                passengerName,
                waitingNumber,
                classType
        );
    }

    // Find a confirmed booking by booking ID
    public BookingRecord findConfirmedBooking(int bookingId) {

        String sql = """
                SELECT booking_id, user_id, train_id,
                       source_station_id, destination_station_id,
                       seat_id, travel_date, passenger_name,
                       booking_status, class_type
                FROM bookings
                WHERE booking_id = ?
                  AND booking_status = 'CONFIRMED'
                """;

        List<BookingRecord> bookings = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {

                    BookingRecord booking = new BookingRecord();

                    booking.setBookingId(rs.getInt("booking_id"));
                    booking.setUserId(rs.getInt("user_id"));
                    booking.setTrainId(rs.getInt("train_id"));

                    booking.setSourceStationId(
                            rs.getInt("source_station_id")
                    );

                    booking.setDestinationStationId(
                            rs.getInt("destination_station_id")
                    );

                    int seatId = rs.getInt("seat_id");
                    booking.setSeatId(
                            rs.wasNull() ? null : seatId
                    );

                    booking.setTravelDate(
                            rs.getDate("travel_date").toLocalDate()
                    );

                    booking.setPassengerName(
                            rs.getString("passenger_name")
                    );

                    booking.setBookingStatus(
                            rs.getString("booking_status")
                    );

                    booking.setClassType(
                            rs.getString("class_type")
                    );

                    return booking;
                },
                bookingId
        );

        return bookings.isEmpty() ? null : bookings.get(0);
    }

    // Cancel a confirmed booking
    public int cancelBooking(int bookingId) {

        String sql = """
                UPDATE bookings
                SET booking_status = 'CANCELLED'
                WHERE booking_id = ?
                  AND booking_status = 'CONFIRMED'
                """;

        return jdbcTemplate.update(sql, bookingId);
    }

    // Find all waiting passengers in queue order for the requested class.
    public List<WaitingPassenger> findWaitingPassengers(
            int trainId,
            java.time.LocalDate travelDate,
            String classType) {

        String sql = """
                SELECT waiting_id, user_id, train_id,
                       source_station_id, destination_station_id,
                       travel_date, passenger_name, class_type
                FROM waiting_list
                WHERE train_id = ?
                  AND travel_date = ?
                  AND status = 'WAITING'
                  AND class_type = ?
                ORDER BY waiting_number ASC, waiting_id ASC
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new WaitingPassenger(
                        rs.getInt("waiting_id"),
                        rs.getInt("user_id"),
                        rs.getInt("train_id"),
                        rs.getInt("source_station_id"),
                        rs.getInt("destination_station_id"),
                        rs.getDate("travel_date").toLocalDate(),
                        rs.getString("passenger_name"),
                        rs.getString("class_type")
                ),
                trainId,
                Date.valueOf(travelDate),
                classType
        );
    }

    // Find the first waiting passenger for a train and date
    public WaitingPassenger findFirstWaitingPassenger(
            int trainId,
            java.time.LocalDate travelDate) {

        String sql = """
                SELECT waiting_id, user_id, train_id,
                       source_station_id, destination_station_id,
                       travel_date, passenger_name, class_type
                FROM waiting_list
                WHERE train_id = ?
                  AND travel_date = ?
                  AND status = 'WAITING'
                ORDER BY waiting_number ASC, waiting_id ASC
                LIMIT 1
                """;

        List<WaitingPassenger> passengers = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new WaitingPassenger(
                        rs.getInt("waiting_id"),
                        rs.getInt("user_id"),
                        rs.getInt("train_id"),
                        rs.getInt("source_station_id"),
                        rs.getInt("destination_station_id"),
                        rs.getDate("travel_date").toLocalDate(),
                        rs.getString("passenger_name"),
                        rs.getString("class_type")
                ),
                trainId,
                Date.valueOf(travelDate)
        );

        return passengers.isEmpty() ? null : passengers.get(0);
    }

    // Find the first waiting passenger for the requested class
    public WaitingPassenger findFirstWaitingPassenger(
            int trainId,
            java.time.LocalDate travelDate,
            String classType) {

        String sql = """
                SELECT waiting_id, user_id, train_id,
                       source_station_id, destination_station_id,
                       travel_date, passenger_name, class_type
                FROM waiting_list
                WHERE train_id = ?
                  AND travel_date = ?
                  AND status = 'WAITING'
                  AND class_type = ?
                ORDER BY waiting_number ASC, waiting_id ASC
                LIMIT 1
                """;

        List<WaitingPassenger> passengers = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new WaitingPassenger(
                        rs.getInt("waiting_id"),
                        rs.getInt("user_id"),
                        rs.getInt("train_id"),
                        rs.getInt("source_station_id"),
                        rs.getInt("destination_station_id"),
                        rs.getDate("travel_date").toLocalDate(),
                        rs.getString("passenger_name"),
                        rs.getString("class_type")
                ),
                trainId,
                Date.valueOf(travelDate),
                classType
        );

        return passengers.isEmpty() ? null : passengers.get(0);
    }

    // Change waiting-list status only if passenger is still WAITING
    public int updateWaitingStatus(
            int waitingId,
            String status) {

        String sql = """
                UPDATE waiting_list
                SET status = ?
                WHERE waiting_id = ?
                  AND status = 'WAITING'
                """;

        return jdbcTemplate.update(
                sql,
                status,
                waitingId
        );
    }

    // Save a waiting-list passenger as a confirmed booking
    public int savePromotedBooking(
            WaitingPassenger passenger,
            int seatId,
            double fare) {

        String sql = """
                INSERT INTO bookings (
                    user_id,
                    train_id,
                    source_station_id,
                    destination_station_id,
                    seat_id,
                    booking_date,
                    travel_date,
                    passenger_name,
                    booking_status,
                    class_type,
                    direction
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'CONFIRMED', ?,
                    (
                        SELECT src.direction
                        FROM train_routes src
                        JOIN train_routes dest
                            ON dest.train_id = src.train_id
                           AND dest.direction = src.direction
                        WHERE src.train_id = ?
                          AND src.station_id = ?
                          AND dest.station_id = ?
                          AND src.stop_number < dest.stop_number
                        LIMIT 1
                    )
                )
                """;

        return jdbcTemplate.update(
                sql,
                passenger.userId(),
                passenger.trainId(),
                passenger.sourceStationId(),
                passenger.destinationStationId(),
                seatId,
                Date.valueOf(IndianTime.today()),
                Date.valueOf(passenger.travelDate()),
                passenger.passengerName(),
                passenger.classType(),
                passenger.trainId(),
                passenger.sourceStationId(),
                passenger.destinationStationId()
        );
    }

    // Waiting-list passenger data
    public record WaitingPassenger(
            int waitingId,
            int userId,
            int trainId,
            int sourceStationId,
            int destinationStationId,
            java.time.LocalDate travelDate,
            String passengerName,
            String classType) {
    }

    // Get booking history with train and station names
    public List<BookingRecord> findBookingHistory(String username) {

        String sql = """
                SELECT
                    b.booking_id,
                    b.user_id,
                    b.train_id,
                    t.train_name,
                    t.train_number,
                    b.source_station_id,
                    src.station_name AS source_station_name,
                    b.destination_station_id,
                    dest.station_name AS destination_station_name,
                    b.seat_id,
                    st.seat_number,
                    b.travel_date,
                    b.passenger_name,
                    b.booking_status,
                    b.class_type
                FROM bookings b
                JOIN users u
                    ON b.user_id = u.user_id
                LEFT JOIN seats st
                    ON b.seat_id = st.seat_id
                JOIN trains t
                    ON b.train_id = t.train_id
                JOIN stations src
                    ON b.source_station_id = src.station_id
                JOIN stations dest
                    ON b.destination_station_id = dest.station_id
                WHERE u.username = ?
                ORDER BY b.booking_id DESC
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {

                    BookingRecord booking = new BookingRecord();

                    booking.setBookingId(rs.getInt("booking_id"));
                    booking.setUserId(rs.getInt("user_id"));
                    booking.setTrainId(rs.getInt("train_id"));

                    booking.setTrainName(
                            rs.getString("train_name")
                    );

                    booking.setTrainNumber(
                            rs.getString("train_number")
                    );

                    booking.setSourceStationId(
                            rs.getInt("source_station_id")
                    );

                    booking.setSourceStationName(
                            rs.getString("source_station_name")
                    );

                    booking.setDestinationStationId(
                            rs.getInt("destination_station_id")
                    );

                    booking.setDestinationStationName(
                            rs.getString("destination_station_name")
                    );

                    int seatId = rs.getInt("seat_id");
                    booking.setSeatId(
                            rs.wasNull() ? null : seatId
                    );

                    booking.setTravelDate(
                            rs.getDate("travel_date").toLocalDate()
                    );

                    booking.setPassengerName(
                            rs.getString("passenger_name")
                    );

                    booking.setBookingStatus(
                            rs.getString("booking_status")
                    );

                    booking.setClassType(
                            rs.getString("class_type")
                    );

                    booking.setSeatNumber(rs.getString("seat_number"));

                    return booking;
                },
                username
        );
    }

    // Cancel a waiting-list entry (only the owner can cancel it)
    public int cancelWaitingEntry(int waitingId, int userId) {

        String sql = """
                UPDATE waiting_list
                SET status = 'CANCELLED'
                WHERE waiting_id = ?
                  AND user_id = ?
                  AND status = 'WAITING'
                """;

        return jdbcTemplate.update(sql, waitingId, userId);
    }

    // Waiting-list entries of a user with the current queue position.
    public List<WaitingRecord> findWaitingHistory(String username) {

        String sql = """
                SELECT
                    w.waiting_id,
                    t.train_number,
                    t.train_name,
                    src.station_name AS source_station_name,
                    dest.station_name AS destination_station_name,
                    w.travel_date,
                    w.passenger_name,
                    w.class_type,
                    w.waiting_number,
                    CASE
                        WHEN w.status = 'WAITING' AND w.travel_date < ?
                            THEN 'EXPIRED'
                        ELSE w.status
                    END AS display_status,
                    (SELECT COUNT(*) + 1
                       FROM waiting_list w2
                      WHERE w2.train_id = w.train_id
                        AND w2.travel_date = w.travel_date
                        AND w2.class_type = w.class_type
                        AND w2.status = 'WAITING'
                        AND w2.waiting_number < w.waiting_number) AS queue_position
                FROM waiting_list w
                JOIN users u ON w.user_id = u.user_id
                JOIN trains t ON w.train_id = t.train_id
                JOIN stations src ON w.source_station_id = src.station_id
                JOIN stations dest ON w.destination_station_id = dest.station_id
                WHERE u.username = ?
                ORDER BY w.waiting_id DESC
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new WaitingRecord(
                        rs.getInt("waiting_id"),
                        rs.getString("train_number"),
                        rs.getString("train_name"),
                        rs.getString("source_station_name"),
                        rs.getString("destination_station_name"),
                        rs.getDate("travel_date").toLocalDate(),
                        rs.getString("passenger_name"),
                        rs.getString("class_type"),
                        rs.getInt("waiting_number"),
                        rs.getInt("queue_position"),
                        rs.getString("display_status")
                ),
                Date.valueOf(IndianTime.today()),
                username
        );
    }
}