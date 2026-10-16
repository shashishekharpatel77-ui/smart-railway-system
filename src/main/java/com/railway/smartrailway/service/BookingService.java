package com.railway.smartrailway.service;

import com.railway.smartrailway.model.BookingRecord;
import com.railway.smartrailway.repository.BookingRepository;
import com.railway.smartrailway.repository.BookingRepository.WaitingPassenger;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;

    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    // Book a ticket or add passenger to waiting list
    @Transactional
    public String bookTicket(
            String username,
            String passengerName,
            int trainId,
            int sourceStationId,
            int destinationStationId,
            LocalDate travelDate,
            String classType) {

        if (username == null || username.isBlank()
                || passengerName == null || passengerName.isBlank()) {
            return "Username and passenger name are required.";
        }

        if (trainId <= 0
                || sourceStationId <= 0
                || destinationStationId <= 0
                || sourceStationId == destinationStationId) {
            return "Please select a valid train and route.";
        }

        if (travelDate == null || travelDate.isBefore(LocalDate.now())) {
            return "Please select a valid travel date.";
        }

        if (classType == null
                || !(classType.trim().equalsIgnoreCase("Sleeper")
                || classType.trim().equalsIgnoreCase("AC"))) {
            return "Please select Sleeper or AC class.";
        }

        String selectedClass =
                classType.trim().equalsIgnoreCase("Sleeper")
                        ? "Sleeper"
                        : "AC";

        Integer userId =
                bookingRepository.findUserId(username.trim());

        if (userId == null) {
            return "User not found. Please check your username.";
        }

        if (!bookingRepository.isTrainScheduled(trainId, travelDate)) {
            return "This train is not scheduled on the selected travel date.";
        }

        Double fare = bookingRepository.findFare(
                trainId,
                sourceStationId,
                destinationStationId,
                selectedClass
        );

        if (fare == null) {
            return "Fare not found for the selected train, route and class.";
        }

        // Keep selected demo passengers on the waiting list
        boolean forceWaitingList =
                passengerName.trim().equalsIgnoreCase("Arjun Saha")
                || passengerName.trim().equalsIgnoreCase("Rahul Pandey");

        Integer seatId = null;

        // Other passengers get a seat if one is available
        if (!forceWaitingList) {
            seatId = bookingRepository.findFreeSeat(
                    trainId,
                    selectedClass,
                    travelDate
            );
        }

        // Add passenger to waiting list if no seat is available
        // or if the passenger is configured for the demo waiting list
        if (seatId == null) {

            int waitingNumber =
                    bookingRepository.getNextWaitingNumber(
                            trainId,
                            travelDate
                    );

            int rowsInserted =
                    bookingRepository.addToWaitingList(
                            userId,
                            trainId,
                            sourceStationId,
                            destinationStationId,
                            travelDate,
                            passengerName.trim(),
                            waitingNumber,
                            selectedClass
                    );

            if (rowsInserted == 1) {
                return "Waiting-list booking successful!"
                        + " Waiting number: WL-" + waitingNumber
                        + " | Class: " + selectedClass
                        + " | Fare on confirmation: Rs. " + fare;
            }

            return "Waiting-list booking could not be completed.";
        }

        // Eligible seat found: create confirmed booking
        BookingRecord booking = new BookingRecord();

        booking.setUserId(userId);
        booking.setTrainId(trainId);
        booking.setSourceStationId(sourceStationId);
        booking.setDestinationStationId(destinationStationId);
        booking.setSeatId(seatId);
        booking.setTravelDate(travelDate);
        booking.setPassengerName(passengerName.trim());
        booking.setBookingStatus("CONFIRMED");
        booking.setClassType(selectedClass);

        int rowsInserted = bookingRepository.saveBooking(
                booking,
                fare,
                selectedClass
        );

        if (rowsInserted == 1) {
            return "Booking confirmed!"
                    + " Seat ID: " + seatId
                    + " | Class: " + selectedClass
                    + " | Fare: Rs. " + fare;
        }

        throw new IllegalStateException(
                "Booking could not be completed. Please try again."
        );
    }

    // Cancel a confirmed ticket and promote the first eligible waiting passenger
    @Transactional
    public String cancelTicket(int bookingId) {

        if (bookingId <= 0) {
            return "Please provide a valid booking ID.";
        }

        BookingRecord booking =
                bookingRepository.findConfirmedBooking(bookingId);

        if (booking == null) {
            return "Confirmed booking not found. It may already be cancelled.";
        }

        int rowsUpdated =
                bookingRepository.cancelBooking(bookingId);

        if (rowsUpdated != 1) {
            throw new IllegalStateException(
                    "Ticket cancellation failed. Please try again."
            );
        }

        String message =
                "Ticket cancelled successfully. Booking ID: " + bookingId;

        // No seat means there is no released seat to promote into
        if (booking.getSeatId() == null) {
            return message;
        }

        // Find the first waiting passenger for the same train, date and class
        WaitingPassenger waitingPassenger =
                bookingRepository.findFirstWaitingPassenger(
                        booking.getTrainId(),
                        booking.getTravelDate(),
                        booking.getClassType()
                );

        if (waitingPassenger == null) {
            return message
                    + ". No waiting passenger found for this train, date and class.";
        }

        // Find a free seat for the cancelled booking's train, date and class
        Integer freeSeatId =
                bookingRepository.findFreeSeat(
                        booking.getTrainId(),
                        booking.getClassType(),
                        booking.getTravelDate()
                );

        if (freeSeatId == null) {
            throw new IllegalStateException(
                    "No eligible seat found for waiting-list promotion."
            );
        }

        // Calculate fare for the waiting passenger's own route
        Double promotionFare = bookingRepository.findFare(
                waitingPassenger.trainId(),
                waitingPassenger.sourceStationId(),
                waitingPassenger.destinationStationId(),
                waitingPassenger.classType()
        );

        if (promotionFare == null) {
            throw new IllegalStateException(
                    "Fare not found for the waiting-list passenger."
            );
        }

        // Create confirmed booking for the waiting passenger
        int promotedRows =
                bookingRepository.savePromotedBooking(
                        waitingPassenger,
                        freeSeatId,
                        promotionFare
                );

        if (promotedRows != 1) {
            throw new IllegalStateException(
                    "Waiting-list passenger could not be promoted."
            );
        }

        // Change waiting-list status only after booking creation succeeds
        int waitingRows =
                bookingRepository.updateWaitingStatus(
                        waitingPassenger.waitingId(),
                        "CONFIRMED"
                );

        if (waitingRows != 1) {
            throw new IllegalStateException(
                    "Waiting-list status update failed."
            );
        }

        return message
                + ". Waiting-list passenger promoted successfully!"
                + " Passenger: " + waitingPassenger.passengerName()
                + " | New seat ID: " + freeSeatId
                + " | Class: " + waitingPassenger.classType()
                + " | Fare: Rs. " + promotionFare;
    }
}