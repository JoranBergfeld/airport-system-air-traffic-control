package com.joranbergfeld.airportsystem.airtrafficcontrol;

import com.joranbergfeld.airport_system.airliner.client.api.AirlinerControllerApi;
import com.joranbergfeld.airport_system.airliner.client.model.Airliner;
import com.joranbergfeld.airport_system.arrival.client.api.ArrivalControllerApi;
import com.joranbergfeld.airport_system.arrival.client.api.ScheduleControllerApi;
import com.joranbergfeld.airport_system.arrival.client.model.ActualArrivalTimeRequest;
import com.joranbergfeld.airport_system.arrival.client.model.ArrivalDto;
import com.joranbergfeld.airport_system.arrival.client.model.Schedule;
import com.joranbergfeld.airport_system.arrival.client.model.ScheduleArrivalRequest;
import com.joranbergfeld.airport_system.plane.client.api.PlaneControllerApi;
import com.joranbergfeld.airport_system.plane.client.model.Plane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class AirTrafficManager {

    private final AirlinerControllerApi airlinerClient;
    private final PlaneControllerApi planeClient;
    private final ArrivalControllerApi arrivalClient;
    private final ScheduleControllerApi scheduleControllerApi;
    private final Logger log = LoggerFactory.getLogger(AirTrafficManager.class);

    @Value("${app.air-control.new-flight.expectation-time-delay}")
    long expectationTimeDelay;

    public AirTrafficManager(AirlinerControllerApi airlinerClient, PlaneControllerApi planeClient, ArrivalControllerApi arrivalClient, ScheduleControllerApi scheduleControllerApi) {
        this.airlinerClient = airlinerClient;
        this.planeClient = planeClient;
        this.arrivalClient = arrivalClient;
        this.scheduleControllerApi = scheduleControllerApi;
    }

    @Scheduled(fixedRateString = "${app.air-control.new-flight.schedule-rate}", initialDelayString = "${app.air-control.new-flight.schedule-initial-delay}")
    void scheduleNewFlights() {
        List<Airliner> allAirliners = airlinerClient.getAllAirliners();
        List<Plane> allPlanes = planeClient.getAllPlanes();

        int randomAirlinerIndex = (int) (Math.random() * allAirliners.size());
        int randomPlaneIndex = (int) (Math.random() * allPlanes.size());

        ScheduleArrivalRequest request = new ScheduleArrivalRequest();
        request.setAirlinerId(allAirliners.get(randomAirlinerIndex).getId());
        request.setPlaneId(allPlanes.get(randomPlaneIndex).getId());
        request.setExpectedTime(System.currentTimeMillis() + expectationTimeDelay);
        ArrivalDto arrivalSchedule = arrivalClient.createArrivalSchedule(request);
        log.info("Scheduled new arrival with id {}", arrivalSchedule.getArrivalId());
    }

    @Scheduled(fixedRateString = "${app.air-control.actual-arrival-time.schedule-rate}", initialDelayString = "${app.air-control.actual-arrival-time.schedule-initial-delay}")
    void propogateActualArrivalTime() {
        log.info("Starting schedule actual arrival time for flights.");

        List<Schedule> allScheduledArrivals = scheduleControllerApi.getAllSchedules();
        List<ArrivalDto> activeArrivals = arrivalClient.getActiveArrivals();

        allScheduledArrivals.forEach(schedule -> {
            log.debug("Checking schedule with id {}", schedule.getId());
            if (schedule.getActualArrivalTime() != 0)
                return;
            if (schedule.getExpectedAt() == null) {
                log.warn("Schedule with id {} does not have an expected arrival time", schedule.getId());
                return;
            }
            log.debug("Schedule with id {} has expected arrival time {}, and actual arrival time of {}", schedule.getId(), schedule.getExpectedAt(), schedule.getActualArrivalTime());
            log.info("Current time is {}, where expected time for schedule with ID {} is {}.", System.currentTimeMillis(), schedule.getId(), schedule.getExpectedAt());
            if (schedule.getExpectedAt() < System.currentTimeMillis()) {
                log.info("Processing schedule with id {}", schedule.getId());
                // We assume that the plane is planned to land, but does not have a gate assigned yet
                Optional<ArrivalDto> first = activeArrivals.stream().filter(arrival -> Objects.equals(arrival.getScheduleId(), schedule.getId())).findFirst();
                if (first.isPresent()) {
                    ArrivalDto arrivalDto = first.get();
                    ActualArrivalTimeRequest request = new ActualArrivalTimeRequest();
                    request.setActualArrivalTime(System.currentTimeMillis());
                    arrivalClient.receiveActualArrivalTime(arrivalDto.getArrivalId(), request);
                } else {
                    log.warn("Could not find active arrival for schedule with id {}", schedule.getId());
                }
            }
        });

    }
}
