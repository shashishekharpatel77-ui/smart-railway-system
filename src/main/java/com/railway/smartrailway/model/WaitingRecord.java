package com.railway.smartrailway.model;

import java.time.LocalDate;

public class WaitingRecord {

    private final int waitingId;
    private final String trainNumber;
    private final String trainName;
    private final String sourceStationName;
    private final String destinationStationName;
    private final LocalDate travelDate;
    private final String passengerName;
    private final String classType;
    private final int waitingNumber;
    private final int queuePosition;
    private final String status;

    public WaitingRecord(int waitingId, String trainNumber,
                         String trainName, String sourceStationName,
                         String destinationStationName, LocalDate travelDate,
                         String passengerName, String classType,
                         int waitingNumber, int queuePosition,
                         String status) {

        this.waitingId = waitingId;
        this.trainNumber = trainNumber;
        this.trainName = trainName;
        this.sourceStationName = sourceStationName;
        this.destinationStationName = destinationStationName;
        this.travelDate = travelDate;
        this.passengerName = passengerName;
        this.classType = classType;
        this.waitingNumber = waitingNumber;
        this.queuePosition = queuePosition;
        this.status = status;
    }

    public int getWaitingId() {
        return waitingId;
    }

    public String getTrainNumber() {
        return trainNumber;
    }

    public String getTrainName() {
        return trainName;
    }

    public String getSourceStationName() {
        return sourceStationName;
    }

    public String getDestinationStationName() {
        return destinationStationName;
    }

    public LocalDate getTravelDate() {
        return travelDate;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public String getClassType() {
        return classType;
    }

    public int getWaitingNumber() {
        return waitingNumber;
    }

    public int getQueuePosition() {
        return queuePosition;
    }

    public String getStatus() {
        return status;
    }

    // Returns the waiting-list label for the booking history page
    public String getWaitingLabel() {
        if ("WAITING".equals(status)) {
            return "WL-" + waitingNumber
                    + " (position " + queuePosition + ")";
        }

        return "WL-" + waitingNumber;
    }
}