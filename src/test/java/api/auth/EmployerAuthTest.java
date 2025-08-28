package api.auth;

import common.BaseApiTest;
import common.Specification;
import data.TestDataFactory;
import dto.EmployerRegistrationRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class EmployerAuthTest extends BaseApiTest {

    private static final String CORRECT_PASSWORD_SIZE = "12345678";
    private static final String NOT_CORRECT_PASSWORD_SIZE = "1234";

    @Test
    @DisplayName("Успешная регистрация работодателя (201)")
    public void successEmployerRegistration() {
        Specification.installSpecification(
                Specification.requestSpec("auth/employer"),
                Specification.responseSpec(201)
        );

        EmployerRegistrationRequestDto employer = EmployerRegistrationRequestDto.builder()
                .email(TestDataFactory.uniqueEmail())
                .password(CORRECT_PASSWORD_SIZE)
                .name(TestDataFactory.generateName())
                .phone(TestDataFactory.generatePhone())
                .build();

        given()
                .body(employer)
                .when()
                .post()
                .then()
                .statusCode(201)
                .log().all()
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue());
    }

    @ParameterizedTest(name = "invalidEmail")
    @ValueSource(strings = {
            "tesdts@@gmaill.com",
            "@gmaidl.com",
            "tesdtrs@.com",
            "AtesedtB@.com",
    })
    @DisplayName("Регистрация при неверном формате email (400)")
    public void shouldReturn400WhenEmployerEmailIsInvalid(String email) {
        Specification.installSpecification(
                Specification.requestSpec("auth/employer"),
                Specification.responseSpec(400)
        );

        EmployerRegistrationRequestDto employer = EmployerRegistrationRequestDto.builder()
                .email(email)
                .password(CORRECT_PASSWORD_SIZE)
                .name(TestDataFactory.generateName())
                .phone(TestDataFactory.generatePhone())
                .build();

        given()
                .body(employer)
                .when()
                .post()
                .then()
                .statusCode(400)
                .log().all()
                .body("status", equalTo(400))
                .body("errorCode", equalTo("Bad Request"))
                .body("message", equalTo("email: Неверный формат email"));
    }

    @Test
    @DisplayName("Регистрация при невреном формате password (400)")
    public void shouldReturn400WhenEmployerPasswordInvalid() {
        Specification.installSpecification(
                Specification.requestSpec("auth/employer"),
                Specification.responseSpec(400)
        );

        EmployerRegistrationRequestDto employer = EmployerRegistrationRequestDto.builder()
                .email(TestDataFactory.uniqueEmail())
                .password(NOT_CORRECT_PASSWORD_SIZE)
                .name(TestDataFactory.generateName())
                .phone(TestDataFactory.generatePhone())
                .build();

        given()
                .body(employer)
                .when()
                .post()
                .then()
                .statusCode(400)
                .log().all()
                .body("status", equalTo(400))
                .body("errorCode", equalTo("Bad Request"))
                .body("message", equalTo("password: Пароль должен содержать минимум 8 символов"));
    }

    @Test
    @DisplayName("Регистраця при неверном формате телефона")
    public void shouldReturn400WhenEmployerPhoneInvalid() {
        Specification.installSpecification(
                Specification.requestSpec("auth/employer"),
                Specification.responseSpec(400)
        );

        String invalidPhone = "123";

        EmployerRegistrationRequestDto employer = EmployerRegistrationRequestDto.builder()
                .email(TestDataFactory.uniqueEmail())
                .password(CORRECT_PASSWORD_SIZE)
                .name(TestDataFactory.generateName())
                .phone(invalidPhone)
                .build();

        given()
                .body(employer)
                .when()
                .post()
                .then()
                .statusCode(400)
                .log().all()
                .body("status", equalTo(400))
                .body("errorCode", equalTo("Bad Request"))
                .body("message", equalTo("Неверный формат телефона"));
    }

    @Test
    @DisplayName("Регистрация при уже занятом email (409)")
    public void shouldReturn409WhenEmployerEmailIsExist() {
        Specification.installSpecification(
                Specification.requestSpec("auth/employer"),
                Specification.responseSpec(409)
        );

        EmployerRegistrationRequestDto candidate = EmployerRegistrationRequestDto.builder()
                .email("employertest@gmail.com")
                .password(CORRECT_PASSWORD_SIZE)
                .name(TestDataFactory.generateName())
                .phone(TestDataFactory.generatePhone())
                .build();

        given().body(candidate).when().post();

        given()
                .body(candidate)
                .when()
                .post()
                .then()
                .statusCode(409)
                .log().ifValidationFails()
                .body("status", equalTo(409))
                .body("errorCode", equalTo("Conflict"))
                .body("message", equalTo("Пользователь с такой почтой уже зарегистрирован"));
    }
}
