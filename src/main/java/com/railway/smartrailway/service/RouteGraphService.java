
package com.railway.smartrailway.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RouteGraphService {

    private final JdbcTemplate jdbc;

    public RouteGraphService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private Map<Integer, String> getStations() {
        Map<Integer, String> stations = new HashMap<>();

        String sql = "SELECT station_id, station_name FROM stations";

        jdbc.query(sql, rs -> {
            stations.put(
                    rs.getInt("station_id"),
                    rs.getString("station_name")
            );
        });

        return stations;
    }

    private Map<Integer, List<Edge>> buildGraph() {
        Map<Integer, List<Edge>> graph = new HashMap<>();
        Map<Integer, String> stations = getStations();

        for (Integer stationId : stations.keySet()) {
            graph.put(stationId, new ArrayList<>());
        }

        String sql =
                "SELECT r1.station_id AS source_id, " +
                "r2.station_id AS destination_id, " +
                "CASE " +
                " WHEN r1.departure_time IS NULL " +
                "   OR r2.arrival_time IS NULL THEN 1 " +
                " WHEN TIME_TO_SEC(TIMEDIFF(" +
                "      r2.arrival_time, r1.departure_time)) <= 0 " +
                "   THEN TIME_TO_SEC(TIMEDIFF(" +
                "      r2.arrival_time, r1.departure_time)) / 60 + 1440 " +
                " ELSE TIME_TO_SEC(TIMEDIFF(" +
                "      r2.arrival_time, r1.departure_time)) / 60 " +
                "END AS travel_minutes " +
                "FROM train_routes r1 " +
                "JOIN train_routes r2 " +
                " ON r1.train_id = r2.train_id " +
                "AND r2.stop_number = r1.stop_number + 1";

        jdbc.query(sql, rs -> {
            int source = rs.getInt("source_id");
            int destination = rs.getInt("destination_id");
            int minutes = Math.max(
                    1,
                    (int) Math.round(rs.getDouble("travel_minutes"))
            );

            graph.computeIfAbsent(source, k -> new ArrayList<>())
                    .add(new Edge(destination, minutes));

            graph.computeIfAbsent(destination, k -> new ArrayList<>());
        });

        return graph;
    }

    // BFS: finds the route with the fewest station-to-station edges.
    public List<String> findRouteBFS(int source, int destination) {
        Map<Integer, List<Edge>> graph = buildGraph();
        Map<Integer, String> stations = getStations();

        if (source == destination
                || !stations.containsKey(source)
                || !stations.containsKey(destination)) {
            return Collections.emptyList();
        }

        Queue<Integer> queue = new LinkedList<>();
        Set<Integer> visited = new HashSet<>();
        Map<Integer, Integer> parent = new HashMap<>();

        queue.offer(source);
        visited.add(source);

        while (!queue.isEmpty()) {
            int current = queue.poll();

            if (current == destination) {
                break;
            }

            for (Edge edge :
                    graph.getOrDefault(current, Collections.emptyList())) {
                if (visited.add(edge.destination)) {
                    parent.put(edge.destination, current);
                    queue.offer(edge.destination);
                }
            }
        }

        return buildPath(source, destination, parent, visited, stations);
    }

    // Dijkstra: finds the route with the lowest estimated travel time.
    public List<String> findRouteDijkstra(int source, int destination) {
        Map<Integer, List<Edge>> graph = buildGraph();
        Map<Integer, String> stations = getStations();

        if (source == destination
                || !stations.containsKey(source)
                || !stations.containsKey(destination)) {
            return Collections.emptyList();
        }

        Map<Integer, Long> distance = new HashMap<>();
        Map<Integer, Integer> parent = new HashMap<>();

        for (Integer stationId : stations.keySet()) {
            distance.put(stationId, Long.MAX_VALUE);
        }

        distance.put(source, 0L);

        PriorityQueue<Node> queue = new PriorityQueue<>(
                Comparator.comparingLong(node -> node.cost)
        );

        queue.offer(new Node(source, 0L));

        while (!queue.isEmpty()) {
            Node current = queue.poll();

            if (current.cost != distance.get(current.station)) {
                continue;
            }

            if (current.station == destination) {
                break;
            }

            for (Edge edge :
                    graph.getOrDefault(current.station, Collections.emptyList())) {

                long newCost = current.cost + edge.weight;

                if (newCost < distance.getOrDefault(
                        edge.destination, Long.MAX_VALUE)) {
                    distance.put(edge.destination, newCost);
                    parent.put(edge.destination, current.station);
                    queue.offer(new Node(edge.destination, newCost));
                }
            }
        }

        if (distance.get(destination) == Long.MAX_VALUE) {
            return Collections.emptyList();
        }

        Set<Integer> visited = new HashSet<>();
        visited.add(source);
        visited.addAll(parent.keySet());

        return buildPath(source, destination, parent, visited, stations);
    }

    private List<String> buildPath(
            int source,
            int destination,
            Map<Integer, Integer> parent,
            Set<Integer> visited,
            Map<Integer, String> stations) {

        if (!visited.contains(destination)) {
            return Collections.emptyList();
        }

        LinkedList<String> path = new LinkedList<>();
        int current = destination;

        while (true) {
            String stationName = stations.get(current);

            if (stationName == null) {
                return Collections.emptyList();
            }

            path.addFirst(stationName);

            if (current == source) {
                break;
            }

            Integer previous = parent.get(current);

            if (previous == null) {
                return Collections.emptyList();
            }

            current = previous;
        }

        return path;
    }

    private static class Edge {
        private final int destination;
        private final int weight;

        private Edge(int destination, int weight) {
            this.destination = destination;
            this.weight = weight;
        }
    }

    private static class Node {
        private final int station;
        private final long cost;

        private Node(int station, long cost) {
            this.station = station;
            this.cost = cost;
        }
    }
}

