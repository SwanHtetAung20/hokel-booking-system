import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.notNullValue;

public class ClientIntegrationTest {

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://localhost:8083";
    }

    @Test
    public void shouldReturnClientsWithValidToken() {
        String loginPayload = """
                {
                    "email": "testuser@test.com",
                    "password": "password123"
                }
                """;
        String token = RestAssured.given()
                .contentType("application/json")
                .body(loginPayload)
                .when()
                .post("/api/v1/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .get("token");

        RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/v1/clients")
                .then()
                .statusCode(200)
                .body("clients", notNullValue());

    }
}
