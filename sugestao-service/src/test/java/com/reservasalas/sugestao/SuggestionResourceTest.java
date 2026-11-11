package com.reservasalas.sugestao;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class SuggestionResourceTest {

    @Test
    void testGetSuggestions() {
        String requestBody = """
                {
                    "minCapacity": 8,
                    "requiredLocation": "Bloco A",
                    "durationMinutes": 60
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/sugestoes")
                .then()
                .statusCode(200)
                .body("$", hasSize(greaterThanOrEqualTo(1)))
                .body("[0].score", greaterThanOrEqualTo(0.0F));
    }

    @Test
    void testAcceptSuggestion() {
        given()
                .contentType(ContentType.JSON)
                .queryParam("roomId", 1)
                .queryParam("roomName", "Sala Inovação")
                .queryParam("userEmail", "diretoria@empresa.com")
                .queryParam("score", 95.5)
                .when()
                .post("/api/sugestoes/aceitar")
                .then()
                .statusCode(200)
                .body("roomId", equalTo(1))
                .body("userEmail", equalTo("diretoria@empresa.com"))
                .body("eventId", notNullValue());
    }
}
