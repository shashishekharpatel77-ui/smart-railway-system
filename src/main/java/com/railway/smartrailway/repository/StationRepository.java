package com.railway.smartrailway.repository;

import com.railway.smartrailway.model.Station;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StationRepository {

    private final JdbcTemplate jdbc;

    public StationRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Station> findAll() {

        String sql = "SELECT station_id, station_code, station_name, city "
                   + "FROM stations ORDER BY station_name";

        return jdbc.query(sql, (rs, i) -> new Station(
                rs.getInt("station_id"),
                rs.getString("station_code"),
                rs.getString("station_name"),
                rs.getString("city")
        ));
    }
}