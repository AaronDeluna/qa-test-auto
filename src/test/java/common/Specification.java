package common;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

public class Specification {

    private static final String BASE_URI = "http://localhost:8080/api"; // вынеси в конфиг/ENV

    // --- Request specs ---
    public static RequestSpecification specAuthCandidate() {
        return base()
                .setBasePath("/auth/candidate")
                .build();
    }

    public static RequestSpecification specAuthEmployer() {
        return base()
                .setBasePath("/auth/employer")
                .build();
    }

    public static RequestSpecification spec(String basePath) {
        return base()
                .setBasePath(basePath)
                .build();
    }

    private static RequestSpecBuilder base() {
        return new RequestSpecBuilder()
                .setBaseUri(BASE_URI)
                .setContentType(ContentType.JSON)
                .addFilter(new AllureRestAssured())
                // логируй "ALL" только в отладке; в CI лучше ifValidationFails
                .log(LogDetail.METHOD)
                .log(LogDetail.URI);
    }

    // --- Response specs ---
    public static ResponseSpecification spec201() {
        return new ResponseSpecBuilder()
                .expectStatusCode(201)
                .log(LogDetail.STATUS)
                .build();
    }

    public static ResponseSpecification spec400() {
        return new ResponseSpecBuilder()
                .expectStatusCode(400)
                .log(LogDetail.STATUS)
                .build();
    }

    public static ResponseSpecification spec409() {
        return new ResponseSpecBuilder()
                .expectStatusCode(409)
                .log(LogDetail.STATUS)
                .build();
    }
}
