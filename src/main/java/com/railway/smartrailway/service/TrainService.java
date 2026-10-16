package com.railway.smartrailway.service;

import com.railway.smartrailway.model.Station;
import com.railway.smartrailway.model.TrainResult;
import com.railway.smartrailway.repository.StationRepository;
import com.railway.smartrailway.repository.TrainRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class TrainService {

    private final TrainRepository trainRepository;
    private final StationRepository stationRepository;

    public TrainService(TrainRepository trainRepository,
                        StationRepository stationRepository) {
        this.trainRepository = trainRepository;
        this.stationRepository = stationRepository;
    }

    public List<Station> getAllStations() {
        return stationRepository.findAll();
    }

    public List<TrainResult> searchTrains(
            int sourceId, int destId,
            java.time.LocalDate date, String sortBy) {

        if (sourceId == destId) {
            throw new IllegalArgumentException(
                    "Source and destination cannot be the same");
        }

        List<TrainResult> list =
                trainRepository.search(sourceId, destId, date);

        switch (sortBy) {
            case "fare":
                list.sort(Comparator.comparing(
                        TrainResult::getFare,
                        Comparator.nullsLast(Comparator.naturalOrder())));
                break;

            case "seats":
                list.sort(Comparator.comparingInt(
                        TrainResult::getAvailableSeats).reversed());
                break;

            default:
                list.sort(Comparator.comparing(
                        TrainResult::getDepartureTime));
        }

        return list;
    }
}