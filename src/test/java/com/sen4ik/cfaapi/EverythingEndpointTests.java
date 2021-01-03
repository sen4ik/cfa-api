package com.sen4ik.cfaapi;

import com.jayway.jsonpath.JsonPath;
import com.sen4ik.cfaapi.base.Constants;
import com.sen4ik.cfaapi.enums.ErrorMessagesCustom;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EverythingEndpointTests extends BaseTest{

    @Test
    void getEverything() {
        ValidatableResponse response = get(true, true,"/everything", 200);
        response
                .body("everything.categoryName", hasItems("Проповеди", "Пение"))
                .body("everything.categoryFolder", hasItems("songs", "sermons"));

        String jsonBody = response.extract().asString();
        List<String> sermons = JsonPath.read(jsonBody, "$.everything[?(@.categoryFolder=='sermons')].files.[*].fileTitle");
        assertTrue(sermons.contains("О жизни молодежи"));
        assertTrue(sermons.contains("О практическом целомудрии"));
    }

    @Test
    void getEverything_noToken() {
        ValidatableResponse response = get(false, null,"/everything", 403);
        response
                .statusCode(403)
                .contentType(ContentType.JSON)
                .body("timestamp", not(empty()))
                .body("status", equalTo(403))
                .body("error", equalTo("Forbidden"))
                .body("message", equalTo(ErrorMessagesCustom.access_denied.value))
                .body("path", equalTo(Constants.API_PREFIX + "/everything"));
    }

}
