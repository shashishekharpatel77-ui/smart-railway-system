package com.railway.smartrailway.repository;

import com.railway.smartrailway.model.TrainResult;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class TrainRepository {

    private final JdbcTemplate jdbc;

    public TrainRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<TrainResult> search(
            int sourceId,
            int destId,
            LocalDate date) {

        String sql =
                "SELECT " +
                " t.train_id, " +
                " t.train_number, " +
                " t.train_name, " +
                " t.train_type, " +
                " rs.direction AS route_direction, " +
                " rs.departure_time AS dep_time, " +
                " rd.arrival_time AS arr_time, " +

                " (SELECT MIN(f.fare) " +
                "  FROM fares f " +
                "  WHERE f.train_id = t.train_id " +
                "    AND f.source_station_id = rs.station_id " +
                "    AND f.destination_station_id = rd.station_id " +
                " ) AS min_fare, " +

                " (SELECT COUNT(*) " +
                "  FROM seats s " +
                "  JOIN coaches c ON c.coach_id = s.coach_id " +
                "  WHERE c.train_id = t.train_id " +
                "    AND c.coach_type = 'Sleeper' " +
                "    AND s.seat_status = 'AVAILABLE' " +
                "    AND NOT EXISTS ( " +
                "       SELECT 1 FROM bookings b " +
                "       WHERE b.seat_id = s.seat_id " +
                "         AND b.travel_date = ? " +
                "         AND b.booking_status = 'CONFIRMED' " +
                "    ) " +
                " ) AS sleeper_seats, " +

                " (SELECT COUNT(*) " +
                "  FROM seats s " +
                "  JOIN coaches c ON c.coach_id = s.coach_id " +
                "  WHERE c.train_id = t.train_id " +
                "    AND c.coach_type = 'AC' " +
                "    AND s.seat_status = 'AVAILABLE' " +
                "    AND NOT EXISTS ( " +
                "       SELECT 1 FROM bookings b " +
                "       WHERE b.seat_id = s.seat_id " +
                "         AND b.travel_date = ? " +
                "         AND b.booking_status = 'CONFIRMED' " +
                "    ) " +
                " ) AS ac_seats, " +

                " (SELECT COUNT(*) " +
                "  FROM waiting_list w " +
                "  WHERE w.train_id = t.train_id " +
                "    AND w.travel_date = ? " +
                "    AND w.status = 'WAITING' " +
                "    AND w.class_type = 'Sleeper' " +
                "    AND w.source_station_id = rs.station_id " +
                "    AND w.destination_station_id = rd.station_id " +
                "    AND w.direction = rs.direction " +
                " ) AS sleeper_waiting, " +

                " (SELECT COUNT(*) " +
                "  FROM waiting_list w " +
                "  WHERE w.train_id = t.train_id " +
                "    AND w.travel_date = ? " +
                "    AND w.status = 'WAITING' " +
                "    AND w.class_type = 'AC' " +
                "    AND w.source_station_id = rs.station_id " +
                "    AND w.destination_station_id = rd.station_id " +
                "    AND w.direction = rs.direction " +
                " ) AS ac_waiting " +

                "FROM trains t " +

                "JOIN train_routes rs " +
                "  ON rs.train_id = t.train_id " +

                "JOIN train_routes rd " +
                "  ON rd.train_id = t.train_id " +
                " AND rd.direction = rs.direction " +

                "WHERE rs.station_id = ? " +
                "  AND rd.station_id = ? " +
                "  AND rs.stop_number < rd.stop_number " +
                "  AND t.status = 'ACTIVE' " +

                "  AND EXISTS ( " +
                "      SELECT 1 " +
                "      FROM schedules sc " +
                "      WHERE sc.train_id = t.train_id " +
                "        AND ( " +
                "          UPPER(TRIM(sc.run_days)) = 'DAILY' " +
                "          OR FIND_IN_SET( " +
                "              UPPER(LEFT(DAYNAME(?), 3)), " +
                "              REPLACE(UPPER(sc.run_days), ' ', '') " +
                "          ) > 0 " +
                "        ) " +
                "  )";

        return jdbc.query(
                sql,
                (rs, rowNum) -> new TrainResult(
                        rs.getInt("train_id"),
                        rs.getString("train_number"),
                        rs.getString("train_name"),
                        rs.getString("train_type"),
                        rs.getString("route_direction"),
                        rs.getTime("dep_time").toLocalTime(),
                        rs.getTime("arr_time").toLocalTime(),
                        rs.getBigDecimal("min_fare"),
                        rs.getInt("sleeper_seats"),
                        rs.getInt("ac_seats"),
                        rs.getInt("sleeper_waiting"),
                        rs.getInt("ac_waiting")
                ),

                date,
                date,
                date,
                date,
                sourceId,
                destId,
                date
        );
    }
}