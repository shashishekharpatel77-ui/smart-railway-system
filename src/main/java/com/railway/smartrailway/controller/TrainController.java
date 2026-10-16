package com.railway.smartrailway.controller;

import com.railway.smartrailway.service.TrainService;
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

    @GetMapping("/")
public String home(Model model) {
    System.out.println("HOME METHOD STARTED");
    model.addAttribute("stations", trainService.getAllStations());
    System.out.println("STATIONS LOADED");
    return "index";
}

    @GetMapping("/search")
    public String search(
            @RequestParam int sourceId,
            @RequestParam int destinationId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "time") String sortBy,
            Model model) {

        try {
            model.addAttribute("trains",
                    trainService.searchTrains(
                            sourceId, destinationId, date, sortBy));
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("trains", List.of());
        }

        model.addAttribute("sourceId", sourceId);
        model.addAttribute("destinationId", destinationId);
        model.addAttribute("date", date);

        return "results";
    }
}