package api.auth;

import common.BaseApiTest;
import common.Specification;
import data.TestDataFactory;
import dto.CandidateRegistration;
import dto.ErrorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CandidateAuthTest extends BaseApiTest {

    private static final String CORRECT_PASSWORD_SIZE = "12345678";
    private static final String NOT_CORRECT_PASSWORD_SIZE = "1234";

    @Test
    @DisplayName("Кандидат: успешная регистрация (201) — выдаются access/refresh")
    public void successCandidateRegistration() {
        Specification.installSpecification(
                Specification.requestSpec("auth/candidate"),
                Specification.responseSpec(201)
        );

        given()
                .body(TestDataFactory.validCandidate("12345678"))
                .when()
                .post()
                .then()
                .statusCode(201)
                .log().ifValidationFails()
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue());
    }

    @ParameterizedTest(name = "Кандидат: некорректный email → 400 (\"{0}\")")
    @ValueSource(strings = {"tests@@gmaill.com", "@gmail.com", "tests@.com", "AtestB@.com"})
    @DisplayName("Кандидат: неверный формат email (400)")
    public void shouldReturn400WhenEmailIsInvalid(String email) {
        Specification.installSpecification(
                Specification.requestSpec("auth/candidate"),
                Specification.responseSpec(400)
        );

        CandidateRegistration candidate = CandidateRegistration.builder()
                .email(email)
                .password(CORRECT_PASSWORD_SIZE)
                .build();

        given()
                .body(candidate)
                .when()
                .post()
                .then()
                .statusCode(400)
                .log().ifValidationFails()
                .body("status", equalTo(400))
                .body("errorCode", equalTo("Bad Request"))
                .body("message", equalTo("email: Неверный формат email"));
    }

    @Test
    @DisplayName("Кандидат: невреная длина пароля (400)")
    public void shouldReturn400WhenPasswordLengthInvalid() {
        Specification.installSpecification(
                Specification.requestSpec("auth/candidate"),
                Specification.responseSpec(400)
        );

        CandidateRegistration candidate = CandidateRegistration.builder()
                .email(TestDataFactory.uniqueEmail())
                .password(NOT_CORRECT_PASSWORD_SIZE)
                .build();

        given()
                .body(candidate)
                .when()
                .post()
                .then()
                .statusCode(400)
                .log().ifValidationFails()
                .body("status", equalTo(400))
                .body("errorCode", equalTo("Bad Request"))
                .body("message", equalTo("Пароль должен быть минимум 8 символов"));
    }

    @Test
    @DisplayName("Ошибка при регистрации на уже занятый email")
    public void shouldReturn409WhenEmailIsExist() {
        Specification.installSpecification(
                Specification.requestSpec("auth/candidate"),
                Specification.responseSpec(409)
        );

        CandidateRegistration candidate = CandidateRegistration.builder()
                .email("test@mail.ru")
                .password(CORRECT_PASSWORD_SIZE)
                .build();

        ErrorResponse errorResponse = given()
                .body(candidate)
                .when()
                .post()
                .then().log().all()
                .extract().as(ErrorResponse.class);

        assertEquals(409, errorResponse.getStatus());
        assertEquals("Conflict", errorResponse.getErrorCode());
        assertEquals("Пользователь с такой почтой уже зарегистрирован", errorResponse.getMessage());
    }

}
