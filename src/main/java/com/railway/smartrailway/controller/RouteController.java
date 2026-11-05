package com.railway.smartrailway.controller;

import com.railway.smartrailway.model.Station;
import com.railway.smartrailway.repository.StationRepository;
import com.railway.smartrailway.service.RouteGraphService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;

@Controller
public class RouteController {

    private final RouteGraphService routeGraphService;
    private final StationRepository stationRepository;

    public RouteController(
            RouteGraphService routeGraphService,
            StationRepository stationRepository) {

        this.routeGraphService = routeGraphService;
        this.stationRepository = stationRepository;
    }

    // Display available stations and compare BFS and Dijkstra routes.
    @GetMapping("/routes")
    public String findRoutes(
            @RequestParam(defaultValue = "1") int sourceId,
            @RequestParam(defaultValue = "2") int destinationId,
            Model model) {

        // Fetch stations dynamically from the database.
        List<Station> stations = stationRepository.findAll();

        // Check whether both selected stations exist.
        boolean validSource = stations.stream()
                .anyMatch(station -> station.getStationId() == sourceId);

        boolean validDestination = stations.stream()
                .anyMatch(station -> station.getStationId() == destinationId);

        List<String> bfsRoute = Collections.emptyList();
        List<String> dijkstraRoute = Collections.emptyList();

        String errorMessage = null;

        if (!validSource || !validDestination) {

            errorMessage = "Please select valid source and destination stations.";

        } else if (sourceId == destinationId) {

            errorMessage = "Source and destination stations must be different.";

        } else {

            // Find the route with the fewest station-to-station connections.
            bfsRoute = routeGraphService.findRouteBFS(
                    sourceId,
                    destinationId
            );

            // Find the route with the lowest estimated travel time.
            dijkstraRoute = routeGraphService.findRouteDijkstra(
                    sourceId,
                    destinationId
            );
        }

        // Send all required attributes to the Thymeleaf template.
        model.addAttribute("stations", stations);
        model.addAttribute("bfsRoute", bfsRoute);
        model.addAttribute("dijkstraRoute", dijkstraRoute);
        model.addAttribute("sourceId", sourceId);
        model.addAttribute("destinationId", destinationId);
        model.addAttribute("errorMessage", errorMessage);

        return "routes";
    }
}