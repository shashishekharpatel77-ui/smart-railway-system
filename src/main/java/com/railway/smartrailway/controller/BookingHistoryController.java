package com.railway.smartrailway.controller;

import com.railway.smartrailway.model.BookingRecord;
import com.railway.smartrailway.repository.BookingRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class BookingHistoryController {

    private final BookingRepository bookingRepository;

    public BookingHistoryController(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @GetMapping("/booking-history")
    public String bookingHistory(
            @RequestParam(required = false) String username,
            Model model) {

        if (username == null || username.isBlank()) {
            model.addAttribute("error", "Please enter your username.");
            model.addAttribute("bookings", List.of());
            return "booking-history";
        }

        List<BookingRecord> bookings =
                bookingRepository.findBookingHistory(username.trim());

        model.addAttribute("username", username.trim());
        model.addAttribute("bookings", bookings);

        return "booking-history";
    }
}