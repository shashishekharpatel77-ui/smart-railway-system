package com.railway.smartrailway.controller;

import com.railway.smartrailway.service.BookingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class BookingHistoryController {

    private final BookingService bookingService;

    public BookingHistoryController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    // Display confirmed, cancelled and other booking records for a username.
    @GetMapping("/booking-history")
    public String bookingHistory(
            @RequestParam(required = false) String username,
            Model model) {

        // Normalize the username using the same rule as BookingService.
        String user = BookingService.normalizeUsername(username);

        // Show an empty history page if no valid username is provided.
        if (user.isEmpty()) {
            model.addAttribute(
                    "error",
                    "Please enter your username."
            );

            model.addAttribute("bookings", List.of());
            model.addAttribute("waitingList", List.of());

            return "booking-history";
        }

        // Load both regular bookings and waiting-list records.
        model.addAttribute("username", user);

        model.addAttribute(
                "bookings",
                bookingService.getBookingHistory(user)
        );

        model.addAttribute(
                "waitingList",
                bookingService.getWaitingHistory(user)
        );

        return "booking-history";
    }
}