package com.reservasalas.booking;

import com.reservasalas.booking.dto.ReservationRequestDTO;
import com.reservasalas.booking.dto.ReservationResponseDTO;
import com.reservasalas.booking.dto.RoomDTO;
import com.reservasalas.booking.model.ReservationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class ReactiveBookingControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void testGetAllActiveRooms() {
        webTestClient.get().uri("/api/salas")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(RoomDTO.class)
                .value(rooms -> assertThat(rooms).isNotEmpty());
    }

    @Test
    void testCheckAvailability() {
        webTestClient.get().uri("/api/salas/disponibilidade?minCapacity=5")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Object.class)
                .value(list -> assertThat(list).isNotEmpty());
    }

    @Test
    void testCreateAndCancelReservation() {
        LocalDateTime start = LocalDateTime.now().plusHours(10);
        LocalDateTime end = start.plusHours(2);

        ReservationRequestDTO request = ReservationRequestDTO.builder()
                .roomId(2L)
                .userEmail("teste.unitario@empresa.com")
                .startTime(start)
                .endTime(end)
                .build();

        ReservationResponseDTO created = webTestClient.post().uri("/api/reservas")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ReservationResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertThat(created).isNotNull();
        assertThat(created.getRoomId()).isEqualTo(2L);
        assertThat(created.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);

        webTestClient.post().uri("/api/reservas")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(409);

        webTestClient.delete().uri("/api/reservas/" + created.getId())
                .exchange()
                .expectStatus().isNoContent();
    }
}
