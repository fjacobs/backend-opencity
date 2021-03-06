package com.dynacore.livemap.traveltime;

import com.dynacore.livemap.core.tools.FileToGeojson;
import com.dynacore.livemap.core.service.GeoJsonAdapter;
import com.dynacore.livemap.traveltime.service.TravelTimeServiceConfig;
import io.rsocket.transport.netty.client.WebsocketClientTransport;
import org.geojson.Feature;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.test.StepVerifier;

import java.time.Duration;


@SpringBootTest( properties = "spring.main.web-application-type=reactive",
                 webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class RSocketIntegrationTest {

    @Value("${spring.rsocket.server.port}")
    private int serverPort;

    @Autowired
    private RSocketRequester.Builder builder;

    @Test
    public void testRSocketRequestStream() {
        Hooks.onOperatorDebug();
        RSocketRequester webSocket = builder.connect( WebsocketClientTransport
                                                .create(serverPort))
                                                .block();

        webSocket.route("TRAVELTIME_STREAM")
            .retrieveFlux(Feature.class)
            .as(StepVerifier::create)
            .expectNextCount(1011)
            .expectNextMatches(feature-> feature.getProperties().get("Velocity").equals(50) )
            .expectNextCount(1011)
            .expectNextMatches(feature-> feature.getProperties().get("Velocity").equals(120) )
            .expectNextCount(1011)
            .expectNextMatches(feature-> feature.getProperties().get("Velocity").equals(50) )
            .expectNextCount(1011)
            .expectNextMatches(feature-> feature.getProperties().get("Velocity").equals(120) )
            .thenCancel()
            .verify();
    }

    @TestConfiguration
    static class MyTestConfiguration {

        @Bean
        @Primary
        TravelTimeServiceConfig createServiceConfig(){
            TravelTimeServiceConfig serviceConfig = new TravelTimeServiceConfig();
            serviceConfig.setInitialDelay(Duration.ZERO);
            serviceConfig.setRequestInterval(Duration.ofSeconds(2));
            serviceConfig.setElementDelay(Duration.ofMillis(2));
            serviceConfig.setSaveToDbEnabled(false);
            return serviceConfig;
        }

        @Bean(name= "testFileBean")
        @Primary
        GeoJsonAdapter fileReaderRepeat(){
            return (interval) -> Flux.fromIterable(FileToGeojson.readCollection("/traveltimedata/blinking/"))
                    .delayElements(interval)
                    .repeat(1);
        }
    }
}
