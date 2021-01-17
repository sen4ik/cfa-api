package com.sen4ik.cfaapi;

import com.github.javafaker.Faker;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.jayway.jsonpath.JsonPath;
import com.sen4ik.cfaapi.base.Constants;
import com.sen4ik.cfaapi.enums.AuthPaths;
import com.sen4ik.cfaapi.enums.ErrorMessagesCustom;
import com.sen4ik.cfaapi.utilities.PropertiesFileUtility;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.MultiPartSpecification;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.EqualsBuilder;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.empty;

@Slf4j
public class BaseTest {

    public static String baseUrl = null;
    int adminUserId = 1;
    String username = "sen4ik";
    String password = "12345";
    int nonAdminUserId = 2;
    String nonAdminUser = "user";
    String nonAdminPassword = "12345";
    String nonAdminFirstname = "Vasia";
    String nonAdminLastname = "Pupkin";
    String nonAdminEmail = "user@test.com";

    public BaseTest() {
        try {
            baseUrl = "http://localhost:" + new PropertiesFileUtility(Constants.APP_PROP).getProperty("server.port") + Constants.API_PREFIX;
            // baseUrl = "http://192.168.1.7:" + new PropertiesFileUtility(Constants.APP_PROP).getProperty("server.port") + Constants.API_PREFIX;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    Faker faker = new Faker();

    Header jsonAcceptHeader = new Header("Accept", "application/json");
    Header jsonContentTypeHeader = new Header("Content-Type", "application/json");
    Header multipartContentTypeHeader = new Header("Content-Type", "multipart/form-data");
    Header xmlAcceptHeader = new Header("Accept", "application/xml");
    Header xmlContentTypeHeader = new Header("Content-Type", "application/xml");

    public RequestSpecification getSimpleRs(){
        return given()
                .baseUri(baseUrl)
                .header(jsonContentTypeHeader)
                .log().everything();
    }

    public ValidatableResponse get(boolean authNeeded, Boolean isAdmin, String path, Integer expectedStatusCode){

        RequestSpecification rs = getSimpleRs();

        if(authNeeded){
            if(isAdmin != null){
                if(isAdmin){
                    rs.header(getAuthHeader(username, password));
                }
                else{
                    rs.header(getAuthHeader(nonAdminUser, nonAdminPassword));
                }
            }
        }

        Response r = rs
                .when()
                .get(path);

        return validateResponse(r, expectedStatusCode);
    }

    public ValidatableResponse post(boolean authNeeded, Boolean isAdmin, String path, String body, Integer expectedStatusCode){
        return post(authNeeded, isAdmin, path, null, body, expectedStatusCode);
    }

    public ValidatableResponse post(boolean authNeeded, Boolean isAdmin, String path, List<MultiPartSpecification> multiPartSpecificationList, String body, Integer expectedStatusCode){
        RequestSpecification rs = getSimpleRs();

        if(authNeeded){
            if(isAdmin != null){
                if(isAdmin){
                    rs.header(getAuthHeader(username, password));
                }
                else{
                    rs.header(getAuthHeader(nonAdminUser, nonAdminPassword));
                }
            }
        }

        if(multiPartSpecificationList != null && multiPartSpecificationList.size() > 0){
            for(MultiPartSpecification m : multiPartSpecificationList){
                rs.multiPart(m);
            }
            rs.header(multipartContentTypeHeader);
        }

        if(body != null){
            rs.body(body);
        }

        Response r = rs
                .when()
                .post(path);

        return validateResponse(r, expectedStatusCode);
    }

    public ValidatableResponse put(boolean authNeeded, String path, String body, Integer expectedStatusCode){
        return put(authNeeded, path, null, body, expectedStatusCode);
    }

    public ValidatableResponse put(boolean authNeeded, String path, List<MultiPartSpecification> multiPartSpecificationList, String body, Integer expectedStatusCode){

        RequestSpecification rs = getSimpleRs();

        if(authNeeded){
            rs.header(getAuthHeader_asAdmin());
        }

        if(multiPartSpecificationList != null && multiPartSpecificationList.size() > 0){
            for(MultiPartSpecification m : multiPartSpecificationList){
                rs.multiPart(m);
            }
            rs.header(multipartContentTypeHeader);
        }

        if(body != null){
            rs.body(body);
        }

        Response r = rs
                .when()
                .put(path);

        return validateResponse(r, expectedStatusCode);
    }

    public ValidatableResponse delete(boolean authNeeded, Boolean isAdmin, String path, Integer expectedStatusCode){

        RequestSpecification rs = getSimpleRs();

        if(authNeeded){
            if(isAdmin != null){
                if(isAdmin){
                    rs.header(getAuthHeader(username, password));
                }
                else{
                    rs.header(getAuthHeader(nonAdminUser, nonAdminPassword));
                }
            }
        }

        Response r = rs
                .when()
                .delete(path);

        return validateResponse(r, expectedStatusCode);
    }

    private ValidatableResponse validateResponse(Response r, Integer expectedStatusCode){
        ValidatableResponse response;
        if(expectedStatusCode != null){
            response = r.then()
                    .log().everything()
                    .statusCode(expectedStatusCode)
                    .contentType(ContentType.JSON);
        }
        else{
            response = r.then().log().everything();
        }
        return response;
    }

    public Header getAuthHeader_asAdmin(){
        return new Header("Authorization", "Bearer " + getAuthToken());
    }

    public Header getAuthHeader(String username, String password){
        return new Header("Authorization", "Bearer " + getAuthToken(username, password));
    }

    public String getAuthToken(String username, String password){
        log.info("CALLED: getAuthToken(" + username + ", " + password + ")");
        ValidatableResponse response =
                post(false, null, AuthPaths.signIn.value, "{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}", 200);
        response
                .body("username", equalTo(username))
                .body("token", not(empty()));

        return getStringFromJsonResponse(response, "$.token");
    }

    public String getAuthToken(){
        return getAuthToken(username, password);
    }

    public String getStringFromJsonResponse(ValidatableResponse response, String jsonPath){
        String jsonBody = response.extract().asString();
        String str = JsonPath.read(jsonBody, jsonPath);
        log.info(jsonPath + ": " + str);
        return str;
    }

    public void verifyNoTokenResponse(ValidatableResponse response, String path){
        verifyNoTokenResponse(response, path, ErrorMessagesCustom.access_denied.value);
    }

    public void verifyNoTokenResponse(ValidatableResponse response, String path, String message){
        response
                .statusCode(403)
                .contentType(ContentType.JSON)
                .body("timestamp", not(empty()))
                .body("status", equalTo(403))
                .body("error", equalTo("Forbidden"))
                .body("message", equalTo(message))
                .body("path", equalTo(path));
    }

    public String responseToJson(ValidatableResponse response){
        return response.extract().asString();
    }

    public int getIntFromJsonResponse(ValidatableResponse response, String jsonPath){
        String jsonBody = response.extract().asString();
        return getIntFromJsonResponse(jsonBody, jsonPath);
    }

    public int getIntFromJsonResponse(String json, String jsonPath){
        int i = JsonPath.read(json, jsonPath);
        log.info(jsonPath + ": " + i);
        return i;
    }

    public static String objectToJson_withNulls(Object obj){
        Gson gson = new GsonBuilder().serializeNulls().create();
        String json = gson.toJson(obj);
        log.info("json: " + json);
        return json;
    }

    public static String objectToJson_withoutNulls(Object obj){
        String json = new Gson().toJson(obj);
        log.info("json: " + json);
        return json;
    }

    public static Object objectFromJson(String json, Class<?> cl){
        Gson gson = new Gson();
        return gson.fromJson(json, cl);
    }

    private boolean compareResponseWithExpected(ValidatableResponse response, Object expectedObj){
        String json = response.extract().asString();
        Object resultObj = new Gson().fromJson(json, expectedObj.getClass());
        boolean result = EqualsBuilder.reflectionEquals(resultObj, expectedObj);
        return result;
    }

    public JsonElement jsonToJsonElement(String json){
        JsonElement jsonElement =  new JsonParser().parse(json).getAsJsonArray();
        return jsonElement;
    }

    public JsonElement objectToJsonElement(Object obj){
        return jsonToJsonElement(objectToJson_withNulls(obj));
    }

    public boolean compare(ValidatableResponse response, Object obj){
        String json = response.extract().asString();
        JsonElement responseArray =  jsonToJsonElement(json);
        JsonElement expectedArray =   objectToJsonElement(obj);
        // assertEquals(responseArray, expectedArray);
        return responseArray.equals(expectedArray);
    }

    public MapDifference<String, Object> compareTwoJsonsAndGetTheDifference(String json1, String json2) {
        Gson g = new Gson();
        Type mapType = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> firstMap = g.fromJson(json1, mapType);
        Map<String, Object> secondMap = g.fromJson(json2, mapType);
        log.info(Maps.difference(firstMap, secondMap).toString());
        return Maps.difference(firstMap, secondMap);
    }

}
