package com.reservasalas.booking;

import com.reservasalas.booking.service.ReactiveRoomService;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CacheAndMetricsTest {

    @Autowired
    private ReactiveRoomService roomService;

    @Autowired
    private MeterRegistry meterRegistry;

    @Test
    void testMetricsRegistryInitialization() {
        assertThat(meterRegistry.find("reservas.criadas").counter()).isNotNull();
        assertThat(meterRegistry.find("reservas.conflitos").counter()).isNotNull();
    }

    @Test
    void testReactiveRoomServiceCacheAndFetch() {
        roomService.getAllActiveRooms().collectList().block();
        assertThat(roomService.getAllActiveRooms().collectList().block()).isNotEmpty();
    }
}
