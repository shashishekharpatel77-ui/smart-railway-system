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

    public List<TrainResult> search(int sourceId, int destId, LocalDate date) {

        String sql =
                "SELECT t.train_id, t.train_number, t.train_name, t.train_type, "
              + "       rs.departure_time AS dep_time, rd.arrival_time AS arr_time, "
              + "       (SELECT MIN(f.fare) FROM fares f "
              + "         WHERE f.train_id = t.train_id "
              + "           AND f.source_station_id = rs.station_id "
              + "           AND f.destination_station_id = rd.station_id) AS min_fare, "
              + "       (t.total_seats - (SELECT COUNT(*) FROM bookings b "
              + "         WHERE b.train_id = t.train_id AND b.travel_date = ? "
              + "           AND b.booking_status = 'CONFIRMED')) AS available_seats "
              + "FROM trains t "
              + "JOIN train_routes rs ON rs.train_id = t.train_id "
              + "JOIN train_routes rd ON rd.train_id = t.train_id "
              + "JOIN schedules sc ON sc.train_id = t.train_id AND sc.travel_date = ? "
              + "WHERE rs.station_id = ? AND rd.station_id = ? "
              + "  AND rs.stop_number < rd.stop_number "
              + "  AND t.status = 'ACTIVE'";

        return jdbc.query(sql, (rs, i) -> new TrainResult(
                rs.getInt("train_id"),
                rs.getString("train_number"),
                rs.getString("train_name"),
                rs.getString("train_type"),
                rs.getTime("dep_time").toLocalTime(),
                rs.getTime("arr_time").toLocalTime(),
                rs.getBigDecimal("min_fare"),
                rs.getInt("available_seats")
        ), date, date, sourceId, destId);
    }
}