package fixtures;

import common.Specification;
import data.TestDataFactory;
import dto.CandidateRegistration;
import dto.EmployerRegistrationRequestDto;

import static data.TestDataFactory.generatePassword;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;

public class ApiFixtures {

    public static String ensureCandidateExists(String email) {
        CandidateRegistration candidate = CandidateRegistration.builder()
                .email(email)
                .password(generatePassword())
                .build();

        given()
                .spec(Specification.specAuthCandidate())
                .body(candidate)
                .when()
                .post()
                .then()
                .statusCode(anyOf(is(201), is(409)));

        return candidate.getEmail();
    }

    public static String createEmployerExists() {
        EmployerRegistrationRequestDto employer = TestDataFactory.validEmployer();

        given()
                .spec(Specification.specAuthEmployer())
                .body(employer)
                .when()
                .post()
                .then()
                .statusCode(anyOf(is(201), is(409)));

        return employer.getEmail();
    }

    public static String createUniqueCandidate() {
        CandidateRegistration candidate = TestDataFactory.validCandidate();

        given()
                .spec(Specification.specAuthCandidate())
                .body(candidate)
                .when()
                .post()
                .then()
                .statusCode(201);

        return candidate.getEmail();
    }
}
