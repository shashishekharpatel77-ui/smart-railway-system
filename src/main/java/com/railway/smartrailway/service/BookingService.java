package com.railway.smartrailway.service;

import com.railway.smartrailway.model.BookingRecord;
import com.railway.smartrailway.model.WaitingRecord;
import com.railway.smartrailway.repository.BookingRepository;
import com.railway.smartrailway.util.IndianTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class BookingService {

    // Usernames: 3 to 20 characters, lowercase letters, digits or underscore.
    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[a-z0-9_]{3,20}$");

    // Passenger names: letters, spaces, dot, apostrophe and hyphen.
    private static final Pattern PASSENGER_PATTERN =
            Pattern.compile("^[\\p{L}][\\p{L} .'-]{1,99}$");

    private final BookingRepository bookingRepository;

    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    // Normalize username
    public static String normalizeUsername(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }

    // Book a ticket using segment-wise seat availability.
    @Transactional
    public String bookTicket(
            String username,
            String passengerName,
            int trainId,
            int sourceId,
            int destinationId,
            LocalDate travelDate,
            String classType) {

        String user = normalizeUsername(username);

        if (!USERNAME_PATTERN.matcher(user).matches()) {
            return "Username must be 3 to 20 characters: letters, numbers or underscore.";
        }

        String passenger = passengerName == null ? "" : passengerName.trim();

        if (!PASSENGER_PATTERN.matcher(passenger).matches()) {
            return "Passenger name should contain only letters and spaces (2 to 100 characters).";
        }

        if (travelDate == null || travelDate.isBefore(IndianTime.today())) {
            return "Please select today's date or a future date.";
        }

        if (!"Sleeper".equals(classType) && !"AC".equals(classType)) {
            return "Please select a valid travel class.";
        }

        if (!bookingRepository.isTrainRunning(trainId, travelDate)) {
            return "This train does not run on the selected date. Please choose another date.";
        }

        Double fare = bookingRepository.findFare(
                trainId, sourceId, destinationId, classType);

        if (fare == null) {
            return "Selected class is not available for this route.";
        }

        Integer userId = bookingRepository.createUserIfAbsent(user);

        if (userId == null) {
            return "Could not create the user. Please try again.";
        }

        if (bookingRepository.hasActiveTicket(
                userId, trainId, travelDate, passenger)) {

            return "This passenger already has a ticket or waiting-list entry "
                    + "for this train and date.";
        }

        // Check seat availability only for the requested journey segment.
        Integer seatId = bookingRepository.findFreeSeat(
                trainId,
                classType,
                travelDate,
                sourceId,
                destinationId
        );

        if (seatId != null) {

            BookingRecord booking = new BookingRecord();

            booking.setUserId(userId);
            booking.setTrainId(trainId);
            booking.setSourceStationId(sourceId);
            booking.setDestinationStationId(destinationId);
            booking.setSeatId(seatId);
            booking.setTravelDate(travelDate);
            booking.setPassengerName(passenger);
            booking.setClassType(classType);

            int saved = bookingRepository.saveBooking(
                    booking, fare, classType);

            if (saved == 0) {
                return "Booking could not be completed. Please try again.";
            }

            return "Booking confirmed! Seat: "
                    + bookingRepository.findSeatNumber(seatId)
                    + " | Class: " + classType
                    + " | Fare: Rs. " + fare;
        }

        // No seat available for this segment: add passenger to waiting list.
        int waitingNumber = bookingRepository.getNextWaitingNumber(
                trainId, travelDate, classType);

        bookingRepository.addToWaitingList(
                userId,
                trainId,
                sourceId,
                destinationId,
                travelDate,
                passenger,
                waitingNumber,
                classType
        );

        return "Waiting-list booking successful! Waiting number: WL-"
                + waitingNumber
                + " | Class: " + classType
                + " | Fare on confirmation: Rs. " + fare;
    }

    // Cancel a confirmed ticket and check waiting-list promotion.
    @Transactional
    public String cancelTicket(int bookingId, String username) {

        String user = normalizeUsername(username);
        Integer userId = bookingRepository.findUserId(user);

        if (userId == null) {
            return "User not found. Please enter the username used for the booking.";
        }

        BookingRecord booking = bookingRepository.findConfirmedBooking(bookingId);

        if (booking == null) {
            return "Confirmed booking not found. It may already be cancelled.";
        }

        if (booking.getUserId() != userId) {
            return "This booking does not belong to the given username.";
        }

        if (booking.getTravelDate().isBefore(IndianTime.today())) {
            return "The journey date has already passed, so this ticket cannot be cancelled.";
        }

        int cancelled = bookingRepository.cancelBooking(bookingId);

        if (cancelled == 0) {
            return "Booking could not be cancelled. Please try again.";
        }

        if (booking.getSeatId() == null) {
            return "Ticket cancelled successfully. Booking ID: "
                    + bookingId + ".";
        }

        // Get all waiting passengers of the same train, date and class.
        List<BookingRepository.WaitingPassenger> waitingPassengers =
                bookingRepository.findWaitingPassengers(
                        booking.getTrainId(),
                        booking.getTravelDate(),
                        booking.getClassType()
                );

        if (waitingPassengers.isEmpty()) {
            return "Ticket cancelled successfully. Booking ID: "
                    + bookingId + ".";
        }

        // Check waiting passengers one by one in queue order.
        for (BookingRepository.WaitingPassenger waiting : waitingPassengers) {

            // Check availability for this passenger's own route segment.
            Integer freeSeatId = bookingRepository.findFreeSeat(
                    waiting.trainId(),
                    waiting.classType(),
                    waiting.travelDate(),
                    waiting.sourceStationId(),
                    waiting.destinationStationId()
            );

            // If no seat is available for this passenger, check the next one.
            if (freeSeatId == null) {
                continue;
            }

            Double fare = bookingRepository.findFare(
                    waiting.trainId(),
                    waiting.sourceStationId(),
                    waiting.destinationStationId(),
                    waiting.classType()
            );

            // Skip entries for which fare data is unavailable.
            if (fare == null) {
                continue;
            }

            // Confirm the waiting passenger.
            int promoted = bookingRepository.savePromotedBooking(
                    waiting, freeSeatId, fare);

            if (promoted == 0) {
                continue;
            }

            // Update waiting status only after successful booking.
            int statusUpdated = bookingRepository.updateWaitingStatus(
                    waiting.waitingId(), "CONFIRMED");

            if (statusUpdated == 0) {
                throw new IllegalStateException(
                        "Waiting-list status could not be updated. Transaction rolled back."
                );
            }

            return "Ticket cancelled successfully. Booking ID: " + bookingId
                    + ". Waiting-list passenger promoted successfully! Passenger: "
                    + waiting.passengerName()
                    + " | New seat: "
                    + bookingRepository.findSeatNumber(freeSeatId)
                    + " | Class: " + waiting.classType()
                    + " | Fare: Rs. " + fare;
        }

        return "Ticket cancelled successfully. Booking ID: "
                + bookingId
                + ". No waiting-list passenger could be promoted for the available seat.";
    }

    // Cancel a waiting-list entry.
    @Transactional
    public String cancelWaiting(int waitingId, String username) {

        Integer userId = bookingRepository.findUserId(
                normalizeUsername(username));

        if (userId == null) {
            return "User not found. Please enter the username used for the booking.";
        }

        int rows = bookingRepository.cancelWaitingEntry(waitingId, userId);

        if (rows == 0) {
            return "Waiting-list entry not found, already cancelled or already confirmed.";
        }

        return "Waiting-list entry cancelled successfully.";
    }

    // Get confirmed and cancelled booking history.
    public List<BookingRecord> getBookingHistory(String username) {
        return bookingRepository.findBookingHistory(
                normalizeUsername(username));
    }

    // Get waiting-list history.
    public List<WaitingRecord> getWaitingHistory(String username) {
        return bookingRepository.findWaitingHistory(
                normalizeUsername(username));
    }
}