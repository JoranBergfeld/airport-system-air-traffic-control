package com.joranbergfeld.airportsystem.airtrafficcontrol;

import com.joranbergfeld.airport_system.airliner.client.api.AirlinerControllerApi;
import com.joranbergfeld.airport_system.arrival.client.api.ArrivalControllerApi;
import com.joranbergfeld.airport_system.arrival.client.api.ScheduleControllerApi;
import com.joranbergfeld.airport_system.plane.client.api.PlaneControllerApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiClientConfiguration {

    private final AppConfigProperties appConfigProperties;

    public ApiClientConfiguration(AppConfigProperties appConfigProperties) {
        this.appConfigProperties = appConfigProperties;
    }

    @Bean
    AirlinerControllerApi airlinerControllerApi() {
        com.joranbergfeld.airport_system.airliner.client.invoker.ApiClient client = new com.joranbergfeld.airport_system.airliner.client.invoker.ApiClient();
        client.setBasePath(appConfigProperties.getClientProtocol() + "://" + appConfigProperties.getAirlinerClient().getUrl() + ":" + appConfigProperties.getAirlinerClient().getPort());
        return new AirlinerControllerApi(client);
    }
    @Bean
    PlaneControllerApi planeControllerApi() {
        com.joranbergfeld.airport_system.plane.client.invoker.ApiClient client = new com.joranbergfeld.airport_system.plane.client.invoker.ApiClient();
        client.setBasePath(appConfigProperties.getClientProtocol() + "://" + appConfigProperties.getPlaneClient().getUrl() + ":" + appConfigProperties.getPlaneClient().getPort());
        return new PlaneControllerApi(client);
    }

    @Bean
    ArrivalControllerApi arrivalControllerApi() {
        com.joranbergfeld.airport_system.arrival.client.invoker.ApiClient client = new com.joranbergfeld.airport_system.arrival.client.invoker.ApiClient();
        client.setBasePath(appConfigProperties.getClientProtocol() + "://" + appConfigProperties.getArrivalClient().getUrl() + ":" + appConfigProperties.getArrivalClient().getPort());
        return new ArrivalControllerApi(client);
    }

    @Bean
    ScheduleControllerApi scheduleControllerApi() {
        com.joranbergfeld.airport_system.arrival.client.invoker.ApiClient client = new com.joranbergfeld.airport_system.arrival.client.invoker.ApiClient();
        client.setBasePath(appConfigProperties.getClientProtocol() + "://" + appConfigProperties.getArrivalClient().getUrl() + ":" + appConfigProperties.getArrivalClient().getPort());
        return new ScheduleControllerApi(client);
    }
}
