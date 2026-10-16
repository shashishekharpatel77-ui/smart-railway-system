package com.railway.smartrailway.controller;

import com.railway.smartrailway.service.BookingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    // Book a ticket
    @PostMapping("/book-ticket")
    public String bookTicket(
            @RequestParam String username,
            @RequestParam String passengerName,
            @RequestParam int trainId,
            @RequestParam int sourceStationId,
            @RequestParam int destinationStationId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate travelDate,
            @RequestParam String classType,
            Model model) {

        String message = bookingService.bookTicket(
                username,
                passengerName,
                trainId,
                sourceStationId,
                destinationStationId,
                travelDate,
                classType
        );

        model.addAttribute("message", message);
        return "booking-result";
    }

    // Cancel a confirmed ticket
    @PostMapping("/cancel-ticket")
    public String cancelTicket(
            @RequestParam int bookingId,
            Model model) {

        String message = bookingService.cancelTicket(bookingId);

        model.addAttribute("message", message);
        return "booking-result";
    }
}