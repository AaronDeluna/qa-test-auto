package api.auth;

import common.BaseApiTest;
import data.TestDataFactory;
import dto.CandidateRegistration;
import fixtures.ApiFixtures;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Story;
import io.restassured.module.jsv.JsonSchemaValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static common.Specification.spec201;
import static common.Specification.spec400;
import static common.Specification.specAuthCandidate;
import static io.qameta.allure.Allure.step;
import static io.restassured.RestAssured.given;
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
    @DisplayName("Кандидат: 201 и выдача токенов")
    void candidateRegistration_201_tokensIssued() {
        CandidateRegistration candidate = TestDataFactory.validCandidate();

        step("Отправляем POST /auth/candidate", () -> {
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
    @DisplayName("Кандидат: 400 при неверном формате email")
    void candidateRegistration_400_invalidEmail(String email) {
        CandidateRegistration candidate = CandidateRegistration.builder()
                .email(email)
                .password(PASSWORD_OK)
                .build();

        given().spec(specAuthCandidate())
                .body(candidate)
                .when()
                .post()
                .then()
                .spec(spec400())
                .assertThat()
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("ErrorResponseSchema.json"));
    }

    @Test
    @Story("Валидация пароля")
    @DisplayName("Кандидат: 400 при коротком пароле")
    void candidateRegistration_400_shortPassword() {
        CandidateRegistration candidate = CandidateRegistration.builder()
                .email(TestDataFactory.uniqueEmail())
                .password("1234")
                .build();

        given().spec(specAuthCandidate())
                .body(candidate)
                .when()
                .post()
                .then()
                .spec(spec400())
                .assertThat()
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("ErrorResponseSchema.json"));
    }

    @Test
    @Story("Конфликт email")
    @DisplayName("Кандидат: 409 если email уже занят")
    void candidateRegistration_409_emailExists() {
        String email = TestDataFactory.uniqueEmail();

        ApiFixtures.createCandidateExists();

        CandidateRegistration candidate = CandidateRegistration.builder()
                .email(email)
                .password(PASSWORD_OK)
                .build();

        given().spec(specAuthCandidate())
                .body(candidate)
                .when()
                .post()
                .then()
                .statusCode(409)
                .assertThat()
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("ErrorResponseSchema.json"));
    }
}
