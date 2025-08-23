package api.auth;

import common.Specification;
import dto.AuthenticationResponse;
import dto.CandidateRegistration;
import dto.EmployerRegistrationRequestDto;
import dto.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.testng.annotations.DataProvider;
import utils.DataGeneration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AuthenticationTest {

    private static final String CORRECT_PASSWORD_SIZE = "12345678";
    private static final String NOT_CORRECT_PASSWORD_SIZE = "1234";

    @BeforeEach
    public void iniTestCandidate() {
        CandidateRegistration candidate = CandidateRegistration.builder()
                .email("test@mail.ru")
                .password("12345678")
                .build();

        given().body(candidate).when().post();
    }

    @Test
    @DisplayName("Успешная регистрация кандидата")
    public void successCandidateRegistration() {
        Specification.installSpecification(
                Specification.requestSpec("auth/candidate"),
                Specification.responseSpec(201)
        );

        CandidateRegistration candidate = CandidateRegistration.builder()
                .email(DataGeneration.generateEmail())
                .password(CORRECT_PASSWORD_SIZE)
                .build();

        AuthenticationResponse authResponse = given()
                .body(candidate)
                .when()
                .post()
                .then().log().all()
                .extract().as(AuthenticationResponse.class);

        assertNotNull(authResponse.getAccessToken());
        assertNotNull(authResponse.getRefreshToken());
    }

    @ParameterizedTest(name = "invalidEmail")
    @ValueSource(strings = {
            "tests@@gmaill.com",
            "@gmail.com",
            "tests@.com",
            "AtestB@.com",
    })
    @DisplayName("Ошибка регистрации при некорректном формате почты")
    public void shouldReturn400WhenEmailIsInvalid(String email) {
        Specification.installSpecification(
                Specification.requestSpec("auth/candidate"),
                Specification.responseSpec(400)
        );

        CandidateRegistration candidate = CandidateRegistration.builder()
                .email("tests@@gmaill.com")
                .password(CORRECT_PASSWORD_SIZE)
                .build();

        ErrorResponse errorResponse = given()
                .body(candidate)
                .when()
                .post()
                .then().log().all()
                .extract().as(ErrorResponse.class);

        assertEquals(400, errorResponse.getStatus());
        assertEquals("Bad Request", errorResponse.getErrorCode());
        assertEquals("Неверный формат email", errorResponse.getMessage());
    }

    @Test
    @DisplayName("Ошибка регистрации длина пароля меньше 8 символов")
    public void shouldReturn400WhenPasswordLengthInvalid() {
        Specification.installSpecification(
                Specification.requestSpec("auth/candidate"),
                Specification.responseSpec(400)
        );

        CandidateRegistration candidate = CandidateRegistration.builder()
                .email(DataGeneration.generateEmail())
                .password(NOT_CORRECT_PASSWORD_SIZE)
                .build();

        ErrorResponse errorResponse = given()
                .body(candidate)
                .when()
                .post()
                .then().log().all()
                .extract().as(ErrorResponse.class);

        assertEquals(400, errorResponse.getStatus());
        assertEquals("Bad Request", errorResponse.getErrorCode());
        assertEquals("Пароль должен быть минимум 8 символов", errorResponse.getMessage());
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
