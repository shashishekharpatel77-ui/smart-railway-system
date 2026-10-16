package com.railway.smartrailway.controller;

import com.railway.smartrailway.model.Station;
import com.railway.smartrailway.repository.StationRepository;
import com.railway.smartrailway.service.RouteGraphService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

    @GetMapping("/routes")
    public String findRoutes(
            @RequestParam(defaultValue = "1") int sourceId,
            @RequestParam(defaultValue = "2") int destinationId,
            Model model) {

        List<Station> stations = stationRepository.findAll();

        List<String> bfsRoute =
                routeGraphService.findRouteBFS(sourceId, destinationId);

        List<String> dijkstraRoute =
                routeGraphService.findRouteDijkstra(sourceId, destinationId);

        model.addAttribute("stations", stations);
        model.addAttribute("bfsRoute", bfsRoute);
        model.addAttribute("dijkstraRoute", dijkstraRoute);
        model.addAttribute("sourceId", sourceId);
        model.addAttribute("destinationId", destinationId);

        return "routes";
    }
}