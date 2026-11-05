package com.railway.smartrailway.model;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalTime;

public class TrainResult {

    private final int trainId;
    private final String trainNumber;
    private final String trainName;
    private final String trainType;
    private final String direction;
    private final LocalTime departureTime;
    private final LocalTime arrivalTime;
    private final BigDecimal fare;

    private final int sleeperSeats;
    private final int acSeats;
    private final int sleeperWaiting;
    private final int acWaiting;

    public TrainResult(
            int trainId,
            String trainNumber,
            String trainName,
            String trainType,
            String direction,
            LocalTime departureTime,
            LocalTime arrivalTime,
            BigDecimal fare,
            int sleeperSeats,
            int acSeats,
            int sleeperWaiting,
            int acWaiting) {

        this.trainId = trainId;
        this.trainNumber = trainNumber;
        this.trainName = trainName;
        this.trainType = trainType;
        this.direction = direction;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.fare = fare;
        this.sleeperSeats = sleeperSeats;
        this.acSeats = acSeats;
        this.sleeperWaiting = sleeperWaiting;
        this.acWaiting = acWaiting;
    }

    public int getTrainId() {
        return trainId;
    }

    public String getTrainNumber() {
        return trainNumber;
    }

    public String getTrainName() {
        return trainName;
    }

    public String getTrainType() {
        return trainType;
    }

    public String getDirection() {
        return direction;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    public BigDecimal getFare() {
        return fare;
    }

    public int getAvailableSeats() {
        return sleeperSeats + acSeats;
    }

    public int getSleeperSeats() {
        return sleeperSeats;
    }

    public int getAcSeats() {
        return acSeats;
    }

    public int getSleeperWaiting() {
        return sleeperWaiting;
    }

    public int getAcWaiting() {
        return acWaiting;
    }

    public String getSleeperStatus() {
        return statusText(sleeperSeats, sleeperWaiting);
    }

    public String getAcStatus() {
        return statusText(acSeats, acWaiting);
    }

    private static String statusText(int seats, int waiting) {
        if (seats > 0) {
            return seats + " available";
        }

        return waiting > 0
                ? "Full - WL " + waiting
                : "Full - waiting list open";
    }

    public String getTravelTime() {
        long minutes = Duration.between(
                departureTime, arrivalTime
        ).toMinutes();

        if (minutes < 0) {
            minutes += 24 * 60;
        }

        return (minutes / 60) + "h " + (minutes % 60) + "m";
    }
}