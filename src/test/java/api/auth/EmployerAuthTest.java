package api.auth;

import common.BaseApiTest;
import data.TestDataFactory;
import dto.EmployerRegistrationRequestDto;
import fixtures.ApiFixtures;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.Story;
import io.restassured.module.jsv.JsonSchemaValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static common.Specification.spec201;
import static common.Specification.spec400;
import static common.Specification.spec409;
import static common.Specification.specAuthEmployer;
import static data.TestDataFactory.generatePhone;
import static data.TestDataFactory.uniqueEmail;
import static io.qameta.allure.Allure.step;
import static io.qameta.allure.SeverityLevel.CRITICAL;
import static io.qameta.allure.SeverityLevel.NORMAL;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

@Epic("Auth")
@Feature("Регистрация работодателя")
@Owner("ivan.m")
@Tag("api")
public class EmployerAuthTest extends BaseApiTest {

    private static final String PASSWORD_OK = "12345678";
    private static final String PASSWORD_SHORT = "123";

    @Test
    @Story("Успешная регистрация")
    @Severity(CRITICAL)
    @DisplayName("Работодатель: 201 и выдача токенов")
    public void employerRegistration_201_tokensIssued() {
        EmployerRegistrationRequestDto employer = TestDataFactory.validEmployer();

        step("POST /auth/employer → 201, токены выданы", () -> {
            given().spec(specAuthEmployer())
                    .body(employer)
                    .when()
                    .post()
                    .then()
                    .spec(spec201())
                    .body("accessToken", notNullValue())
                    .body("refreshToken", notNullValue());
        });
    }

    @ParameterizedTest(name = "Неверный email → 400: \"{0}\"")
    @ValueSource(strings = {"tests@@gmaill.com", "@gmail.com", "tests@.com", "A..b@c.com"})
    @Story("Валидация email")
    @Severity(NORMAL)
    @DisplayName("Работодатель: 400 при неверном формате email")
    public void shouldReturn400WhenEmployerEmailIsInvalid(String email) {
        EmployerRegistrationRequestDto employer = buildEmployer(email, PASSWORD_OK, generatePhone());

        step("POST /auth/employer → 400, валидация email", () -> {
            given().spec(specAuthEmployer())
                    .body(employer)
                    .when()
                    .post()
                    .then()
                    .spec(spec400())
                    .assertThat()
                    .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/error.json"));
        });
    }

    @Test
    @Story("Валидация пароля")
    @Severity(NORMAL)
    @DisplayName("Работодатель: 400 при коротком пароле")
    public void shouldReturn400WhenEmployerPasswordInvalid() {
        EmployerRegistrationRequestDto employer = buildEmployer(uniqueEmail(), PASSWORD_SHORT, generatePhone());

        step("POST /auth/employer → 400, короткий пароль", () -> {
            given().spec(specAuthEmployer())
                    .body(employer)
                    .when()
                    .post()
                    .then()
                    .spec(spec400())
                    .assertThat()
                    .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/error.json"));
        });
    }

    @Test
    @Story("Валидация телефона")
    @Severity(NORMAL)
    @DisplayName("Работодатель: 400 при неверном формате телефона")
    public void shouldReturn400WhenEmployerPhoneInvalid() {
        EmployerRegistrationRequestDto employer = buildEmployer(uniqueEmail(), PASSWORD_OK, "123");

        step("POST /auth/employer → 400, неверный телефон", () -> {
            given().spec(specAuthEmployer())
                    .body(employer)
                    .when()
                    .post()
                    .then()
                    .spec(spec400())
                    .assertThat()
                    .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/error.json"));
        });
    }

    @Test
    @Story("Конфликт email")
    @Severity(CRITICAL)
    @DisplayName("Работодатель: 409 если email уже занят")
    public void shouldReturn409WhenEmployerEmailIsExist() {
        String existsEmail = ApiFixtures.createEmployerExists();

        EmployerRegistrationRequestDto employer = buildEmployer(existsEmail, PASSWORD_OK, generatePhone());

        step("POST /auth/employer → 409, email уже занят", () -> {
            given().spec(specAuthEmployer())
                    .body(employer)
                    .when()
                    .post()
                    .then()
                    .spec(spec409())
                    .assertThat()
                    .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/error.json"));
        });
    }

    private EmployerRegistrationRequestDto buildEmployer(String email, String password, String phone) {
        return EmployerRegistrationRequestDto.builder()
                .email(email).password(password)
                .name(TestDataFactory.generateName())
                .phone(phone)
                .build();
    }
}
