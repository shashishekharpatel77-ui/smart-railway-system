package com.railway.smartrailway.repository;

import com.railway.smartrailway.model.BookingRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;

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

    // Check whether the train is scheduled on the selected date
    public boolean isTrainScheduled(
            int trainId,
            java.time.LocalDate travelDate) {

        String sql = """
                SELECT COUNT(*)
                FROM schedules
                WHERE train_id = ?
                  AND travel_date = ?
                """;

        Integer count = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                trainId,
                Date.valueOf(travelDate)
        );

        return count != null && count > 0;
    }

    // Find a seat that is AVAILABLE and not already confirmed
    // for the same train and travel date.
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

    // Save a confirmed booking with class type
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
                    class_type
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'CONFIRMED', ?)
                """;

        return jdbcTemplate.update(
                sql,
                booking.getUserId(),
                booking.getTrainId(),
                booking.getSourceStationId(),
                booking.getDestinationStationId(),
                booking.getSeatId(),
                Date.valueOf(java.time.LocalDate.now()),
                Date.valueOf(booking.getTravelDate()),
                booking.getPassengerName(),
                classType
        );
    }

    // Generate the next waiting-list number for a train and date
    public int getNextWaitingNumber(
            int trainId,
            java.time.LocalDate travelDate) {

        String sql = """
                SELECT COALESCE(MAX(waiting_number), 0) + 1
                FROM waiting_list
                WHERE train_id = ?
                  AND travel_date = ?
                """;

        Integer number = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                trainId,
                Date.valueOf(travelDate)
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

    // Change waiting-list status only if the passenger is still WAITING
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
                    class_type
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'CONFIRMED', ?)
                """;

        return jdbcTemplate.update(
                sql,
                passenger.userId(),
                passenger.trainId(),
                passenger.sourceStationId(),
                passenger.destinationStationId(),
                seatId,
                Date.valueOf(java.time.LocalDate.now()),
                Date.valueOf(passenger.travelDate()),
                passenger.passengerName(),
                passenger.classType()
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
                    b.travel_date,
                    b.passenger_name,
                    b.booking_status,
                    b.class_type
                FROM bookings b
                JOIN users u
                    ON b.user_id = u.user_id
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

                    return booking;
                },
                username
        );
    }
}