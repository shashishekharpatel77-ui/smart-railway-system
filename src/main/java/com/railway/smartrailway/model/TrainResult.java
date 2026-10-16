package com.railway.smartrailway.model;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalTime;

public class TrainResult {

    private final int trainId;
    private final String trainNumber;
    private final String trainName;
    private final String trainType;
    private final LocalTime departureTime;
    private final LocalTime arrivalTime;
    private final BigDecimal fare;
    private final int availableSeats;

    public TrainResult(int trainId, String trainNumber, String trainName, String trainType,
                       LocalTime departureTime, LocalTime arrivalTime,
                       BigDecimal fare, int availableSeats) {
        this.trainId = trainId;
        this.trainNumber = trainNumber;
        this.trainName = trainName;
        this.trainType = trainType;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.fare = fare;
        this.availableSeats = availableSeats;
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
        return availableSeats;
    }

    public String getTravelTime() {
        long m = Duration.between(departureTime, arrivalTime).toMinutes();

        if (m < 0) {
            m += 24 * 60;
        }

        return (m / 60) + "h " + (m % 60) + "m";
    }
}