package api.auth;

import common.BaseApiTest;
import data.TestDataFactory;
import dto.CandidateRegistration;
import fixtures.ApiFixtures;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static common.Specification.spec201;
import static common.Specification.spec400;
import static common.Specification.spec409;
import static common.Specification.specAuthCandidate;
import static io.qameta.allure.Allure.parameter;
import static io.qameta.allure.Allure.step;
import static io.qameta.allure.SeverityLevel.CRITICAL;
import static io.qameta.allure.SeverityLevel.NORMAL;
import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;

@Epic("Auth")
@Feature("Регистрация кандидата")
@Owner("ivan.m")
@Tag("api")
class CandidateAuthTest extends BaseApiTest {

    private static final String PASSWORD_OK = "12345678";

    @Test
    @Story("Успешная регистрация")
    @Severity(CRITICAL)
    @DisplayName("201 и выдача токенов")
    void candidateRegistration_201_tokensIssued() {
        CandidateRegistration candidate = TestDataFactory.validCandidate();

        step("Регистрируем кандидата", () -> {
            given().spec(specAuthCandidate())
                    .body(candidate)
                    .when()
                    .post()
                    .then()
                    .spec(spec201())
                    .body("accessToken", notNullValue())
                    .body("refreshToken", notNullValue())
                    .body("accessToken.size()", greaterThan(10));
        });
    }

    @ParameterizedTest(name = "Неверный email → 400: \"{0}\"")
    @ValueSource(strings = {"tests@@gmaill.com", "@gmail.com", "tests@.com", "A..b@c.com"})
    @Story("Валидация email")
    @Severity(NORMAL)
    @DisplayName("400 при неверном формате email")
    void candidateRegistration_400_invalidEmail(String email) {
        CandidateRegistration candidate = CandidateRegistration.builder()
                .email(email)
                .password(PASSWORD_OK)
                .build();

        step("Пытаемся зарегистрировать кандидата с некорректным email", () -> {
            given().spec(specAuthCandidate())
                    .body(candidate)
                    .when()
                    .post()
                    .then()
                    .spec(spec400())
                    .body(matchesJsonSchemaInClasspath("schemas/error.json"));
        });
    }

    @Test
    @Story("Валидация пароля")
    @Severity(NORMAL)
    @DisplayName("400 при коротком пароле")
    void candidateRegistration_400_shortPassword() {
        CandidateRegistration candidate = CandidateRegistration.builder()
                .email(TestDataFactory.uniqueEmail())
                .password("1234")
                .build();

        step("Пытаемся зарегистрировать кандидата с коротким паролем", () -> {
            given().spec(specAuthCandidate())
                    .body(candidate)
                    .when()
                    .post()
                    .then()
                    .spec(spec400())
                    .body(matchesJsonSchemaInClasspath("schemas/error.json"));
        });
    }

    @Test
    @Story("Конфликт email")
    @Severity(CRITICAL)
    @DisplayName("Кандидат: 409 если email уже занят")
    void candidateRegistration_409_emailExists() {
        String email = TestDataFactory.uniqueEmail();

        parameter("email", email);

        step("Предусловие: создаём кандидата с email: %s".formatted(email), () -> {
            ApiFixtures.ensureCandidateExists(email);
        });

        CandidateRegistration candidate = CandidateRegistration.builder()
                .email(email)
                .password(PASSWORD_OK)
                .build();

        step("Пытаемся зарегистрировать кандидата с уже занятым email", () -> {
            given().spec(specAuthCandidate())
                    .body(candidate)
                    .when()
                    .post()
                    .then()
                    .spec(spec409())
                    .body(matchesJsonSchemaInClasspath("schemas/error.json"));
        });
    }
}
