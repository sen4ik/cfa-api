package com.sen4ik.cfaapi;

import com.sen4ik.cfaapi.enums.ErrorMessagesCustom;
import com.sen4ik.cfaapi.utilities.DatabaseUtility;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.sen4ik.utils.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.SQLException;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Slf4j
class TagTests extends BaseTest{

	@Autowired
	DatabaseUtility databaseUtility;

	@AfterAll
	public void tearDown() throws SQLException, ClassNotFoundException {
		// cleanup db
		databaseUtility.executeUpdate("DELETE FROM tags WHERE tag_name LIKE 'Test_%'");
	}

	@Test
	void getAllTags() {
		given()
				.baseUri(baseUrl)
				.log().everything()
		.when()
				.get("/tag/all")
		.then()
				.statusCode(200)
				.contentType(ContentType.JSON)
				.body("tagName", hasItem("Денис Самарин"))
				.log().everything();
	}

	@Test
	void getTag() {
		given()
				.baseUri(baseUrl)
				.log().everything()
		.when()
				.get("/tag/1")
		.then()
				.statusCode(200)
				.contentType(ContentType.JSON)
				.body("tagName", equalTo("Денис Самарин"))
				.body("id", equalTo(1))
				.log().everything();
	}

	@Test
	void addTag_positive() {
		createTag(getRandomTagName(), getAuthToken());
	}

	@Test
	void addTag_noAuth() {
		ValidatableResponse response = given()
				.baseUri(baseUrl)
				.header(jsonContentTypeHeader)
				.body("{\"tagName\": \"" + getRandomTagName() + "\"}")
				.log().everything()
		.when()
				.post("/tag/add")
		.then()
				.statusCode(403)
				.contentType(ContentType.JSON)
				.body("timestamp", not(empty()))
				.log().everything();

		assertEquals(JsonUtil.getStringFromJsonResponse(response, "$.message"), ErrorMessagesCustom.access_denied.value);
		assertEquals(JsonUtil.getStringFromJsonResponse(response, "$.error"), "Forbidden");
		assertEquals(JsonUtil.getIntFromJsonResponse(response, "$.status"), 403);
		assertEquals(JsonUtil.getStringFromJsonResponse(response, "$.path"), "/api/v1/tag/add");
	}

	private int createTag(String tagName, String token){
		ValidatableResponse response = given()
				.baseUri(baseUrl)
				.header("Authorization", "Bearer " + token)
				.header(jsonContentTypeHeader)
				.body("{\"tagName\": \"" + tagName + "\"}")
				.log().everything()
			.when()
				.post("/tag/add")
			.then()
				.statusCode(201)
				.contentType(ContentType.JSON)
				.body("tagName", equalTo(tagName))
				.body("id", not(empty()))
				.log().everything();

		return JsonUtil.getIntFromJsonResponse(response, "$.id");
	}

	private String getRandomTagName(){
		return "Test_" + faker.number().digits(5);
	}

	@Test
	void removeTag_positive() {
		int id = createTag(getRandomTagName(), getAuthToken());

		ValidatableResponse response = given()
				.baseUri(baseUrl)
				.header("Authorization", "Bearer " + getAuthToken())
				.log().everything()
		.when()
				.delete("/tag/" + id)
		.then()
				.statusCode(200)
				.contentType(ContentType.JSON)
				.log().everything();

		assertEquals(JsonUtil.getStringFromJsonResponse(response, "$.status"), "Deleted");
		assertEquals(JsonUtil.getIntFromJsonResponse(response, "$.id"), id);
	}

	@Test
	void removeTag_noAuth() {
		int id = createTag(getRandomTagName(), getAuthToken());

		ValidatableResponse response = given()
				.baseUri(baseUrl)
				.log().everything()
		.when()
				.delete("/tag/" + id)
		.then()
				.statusCode(403)
				.contentType(ContentType.JSON)
				.body("timestamp", not(empty()))
				.log().everything();

		assertEquals(JsonUtil.getStringFromJsonResponse(response, "$.message"), ErrorMessagesCustom.access_denied.value);
		assertEquals(JsonUtil.getStringFromJsonResponse(response, "$.error"), "Forbidden");
		assertEquals(JsonUtil.getIntFromJsonResponse(response, "$.status"), 403);
		assertEquals(JsonUtil.getStringFromJsonResponse(response, "$.path"),"/api/v1/tag/" + id);
	}

}
