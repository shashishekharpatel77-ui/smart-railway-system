package com.railway.smartrailway.controller;

import com.railway.smartrailway.service.TrainService;
import com.railway.smartrailway.util.IndianTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
public class TrainController {

    private final TrainService trainService;

    public TrainController(TrainService trainService) {
        this.trainService = trainService;
    }

    // Display the home page with available stations and today's date.
    @GetMapping("/")
    public String home(Model model) {

        model.addAttribute(
                "stations",
                trainService.getAllStations()
        );

        model.addAttribute(
                "today",
                IndianTime.today()
        );

        return "index";
    }

    // Search for trains between the selected stations.
    @GetMapping("/search")
    public String search(
            @RequestParam int sourceId,
            @RequestParam int destinationId,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,

            @RequestParam(defaultValue = "time")
            String sortBy,

            Model model) {

        try {

            model.addAttribute(
                    "trains",
                    trainService.searchTrains(
                            sourceId,
                            destinationId,
                            date,
                            sortBy
                    )
            );

        } catch (IllegalArgumentException e) {

            model.addAttribute("error", e.getMessage());
            model.addAttribute("trains", List.of());
        }

        // Preserve the search values on the results page.
        model.addAttribute("sourceId", sourceId);
        model.addAttribute("destinationId", destinationId);
        model.addAttribute("date", date);

        return "results";
    }
}