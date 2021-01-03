package com.sen4ik.cfaapi;

import com.sen4ik.cfaapi.enums.AuthPaths;
import com.sen4ik.cfaapi.enums.ErrorMessagesCustom;
import com.sen4ik.cfaapi.enums.TagPaths;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest
public class AuthTests extends BaseTest {

    @Test
    void negative_invalidBearerToken() {
        given()
                .baseUri(baseUrl)
                .header(jsonContentTypeHeader)
                .header("Authorization", "Bearer BlaBlaBla.BlaBlaBla.BlaBlaBla")
                .log().everything()
            .when()
                .post(TagPaths.add.value)
            .then()
                .statusCode(401)
                .contentType(ContentType.JSON)
                .body("status", equalTo("Error"))
                .body("message", equalTo(ErrorMessagesCustom.expired_or_invalid_jwt_token.value))
                .log().everything();
    }

    private void signInHelper(String username, String password){
        ValidatableResponse response = post(false, null, AuthPaths.signIn.value, "{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}", 401);
        response
                .body("status", equalTo("Error"))
                .body("message", equalTo(ErrorMessagesCustom.invalid_username_password_supplied.value));
    }

    @Test
    void signIn_negative_emptyBody() {
        ValidatableResponse response = post(false, null, AuthPaths.signIn.value, "", 500);
        response
                .body("status", equalTo("Error"))
                .body("message", containsString(ErrorMessagesCustom.required_request_body_is_missing.value));
    }

    @Test
    void signIn_negative_badUsername() {
        signInHelper("sen4ik111", "12345");
    }

    @Test
    void signIn_negative_badPassword() {
        signInHelper("sen4ik", "12345111111");
    }
}
