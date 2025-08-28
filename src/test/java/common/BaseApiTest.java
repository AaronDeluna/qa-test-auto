package common;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;

public abstract class BaseApiTest {

    private static final String CORRECT_PASSWORD_SIZE = "12345678";
    private static final String NOT_CORRECT_PASSWORD_SIZE = "1234";

    protected static String BASE_URL;
    protected static RequestSpecification JSON_SPEC;

    @BeforeAll
    static void beforeAll() {
        BASE_URL = System.getProperty("api.baseUrl",
                System.getenv().getOrDefault("API_BASE_URL", "http://localhost:8081"));
        JSON_SPEC = new RequestSpecBuilder()
                .setBaseUri(BASE_URL)
                .setContentType(ContentType.JSON)
                .build();
    }

    protected RequestSpecification jsonSpec(String basePath) {
        return new RequestSpecBuilder()
                .addRequestSpecification(JSON_SPEC)
                .setBasePath(basePath)
                .build();
    }

    protected ResponseSpecification expectStatus(int code) {
        return new ResponseSpecBuilder()
                .expectStatusCode(code)
                .build();
    }

    @AfterEach
    void afterEach() { RestAssured.reset(); }

}
