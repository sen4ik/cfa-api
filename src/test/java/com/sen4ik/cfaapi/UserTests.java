package com.sen4ik.cfaapi;

import com.sen4ik.cfaapi.base.Constants;
import com.sen4ik.cfaapi.entities.User;
import com.sen4ik.cfaapi.enums.UserPaths;
import com.sen4ik.cfaapi.utilities.DatabaseUtility;
import io.restassured.response.ValidatableResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.SQLException;

import static org.hamcrest.Matchers.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Slf4j
public class UserTests extends BaseTest{

    @Autowired
    DatabaseUtility databaseUtility;

    String lastName = "Sentsov";
    String firstName = "Artur";
    String email = "asentsov@test.com";
    String roleAdmin = "ROLE_ADMIN";
    String roleUser = "ROLE_USER";
    boolean active = true;
    int id = 1;

    @AfterAll
    public void tearDown() throws SQLException, ClassNotFoundException {
        databaseUtility.executeUpdate("DELETE FROM user_role WHERE user_role.user_id in (SELECT users.id " +
                "FROM users WHERE users.username LIKE 'Test_%')");
        databaseUtility.executeUpdate("DELETE FROM users WHERE username LIKE 'Test_%'");
    }

    @Test
    void getAllUsers() {
        ValidatableResponse response = get(true, true, UserPaths.getAll.value, 200);
        response
                .body("username", hasItem(username))
                .body("firstname", hasItem(firstName))
                .body("lastname", hasItem(lastName))
                .body("email", hasItem(email))
                .body("find { it.id == 1 }.active", equalTo(active))
                .body("id", hasItem(id))
                .body("find { it.id == 1 }.roles[0].roleName", equalTo(roleAdmin));
    }

    @Test
    void getOneUser() {
        ValidatableResponse response = get(true, true, UserPaths.prefixWithSlash.value + id, 200);
        verifyUserResponse(response, username, firstName, lastName, email, active, id, roleAdmin);
    }

    private void verifyUserResponse(ValidatableResponse response, String username, String firstName, String lastName, String email, boolean active, Integer id, String role){
        response
                .body("username", equalTo(username))
                .body("firstname", equalTo(firstName))
                .body("lastname", equalTo(lastName))
                .body("email", equalTo(email))
                .body("active", equalTo(active));

        if(id != null){
            response.body("id", equalTo(id));
        }
        else{
            response.body("id", not(empty()));
        }

        if(role != null){
            response.body("roles[0].roleName", equalTo(role));
        }
    }

    private User getUserObject(){
        String username = "Test_" + faker.name().username();
        String firstName = "Test_" + faker.name().firstName();
        String lastName = "Test_" + faker.name().lastName();
        String email = faker.internet().safeEmailAddress();

        User u = new User();
        u.setUsername(username);
        u.setPassword(password);
        u.setFirstname(firstName);
        u.setLastname(lastName);
        u.setEmail(email);

        return u;
    }

    @Test
    void addUser() {
        User u = getUserObject();
        ValidatableResponse response = post(false, null, UserPaths.signUp.value, objectToJson_withoutNulls(u), 201);
        verifyUserResponse(response, u.getUsername(), u.getFirstname(), u.getLastname(), u.getEmail(), true, null, roleUser);

        // verify user can get token
        getAuthHeader(u.getUsername(), password);
    }

    @Test
    void addUser_existingUsername_existingEmail() {
        User u = getUserObject();
        post(false, null, UserPaths.signUp.value, objectToJson_withoutNulls(u), 201);

        ValidatableResponse response = post(false, null, UserPaths.signUp.value, objectToJson_withoutNulls(u), 400);
        response
                .body("message", equalTo("Username " + u.getUsername() + " is already taken!"))
                .body("status", equalTo("Error"));

        u.setUsername("Test_" + faker.name().username());
        response = post(false, null, UserPaths.signUp.value, objectToJson_withoutNulls(u), 400);
        response
                .body("message", equalTo("User with email " + u.getEmail() + " already exists!"))
                .body("status", equalTo("Error"));
    }

    @Test
    void deleteUser() {
        User u = getUserObject();
        ValidatableResponse userAddResponse = post(false, null, UserPaths.signUp.value, objectToJson_withoutNulls(u), 201);
        int userId = getIntFromJsonResponse(userAddResponse, "$.id");

        ValidatableResponse deleteResponse = delete(true, true, UserPaths.prefixWithSlash.value + userId, 200);
        deleteResponse
                .body("status", equalTo("Deleted"))
                .body("id", equalTo(userId));
    }

    @Test
    void updateUser() {
        User u = getUserObject();
        ValidatableResponse r = post(false, null, UserPaths.signUp.value, objectToJson_withoutNulls(u), 201);
        int userId = getIntFromJsonResponse(r, "$.id");

        User userUpdate = getUserObject();
        ValidatableResponse response = put(true, UserPaths.prefixWithSlash.value + userId, objectToJson_withoutNulls(userUpdate), 200);
        verifyUserResponse(response, userUpdate.getUsername(), userUpdate.getFirstname(), userUpdate.getLastname(), userUpdate.getEmail(), true, userId, null);
    }

    @Test
    void getAll_negative_noToken() {
        ValidatableResponse response =  get(false, null, UserPaths.getAll.value, 403);
        verifyNoTokenResponse(response, Constants.API_PREFIX + UserPaths.getAll.value);
    }

    @Test
    void getOne_negative_noToken() {
        ValidatableResponse response =  get(false, null, UserPaths.prefixWithSlash.value + 111, 403);
        verifyNoTokenResponse(response, Constants.API_PREFIX + UserPaths.prefixWithSlash.value + 111);
    }

    @Test
    void deleteUser_negative_noToken() {
        ValidatableResponse response =  delete(false, null, UserPaths.prefixWithSlash.value + 111, 403);
        verifyNoTokenResponse(response, Constants.API_PREFIX + UserPaths.prefixWithSlash.value + 111);
    }

    @Test
    void updateUser_negative_noToken() {
        ValidatableResponse response =  put(false, UserPaths.prefixWithSlash.value + 111, "", 403);
        verifyNoTokenResponse(response, Constants.API_PREFIX + UserPaths.prefixWithSlash.value + 111);
    }

    @Test
    void fieldLength() {
        User u = getUserObject();
        u.setLastname(faker.lorem().fixedString(51));
        ValidatableResponse response = post(false, null, UserPaths.signUp.value, objectToJson_withoutNulls(u), 400);
        response
                .body("status", equalTo("Error"))
                .body("message.lastname", equalTo("size must be between 0 and 50"));
    }

}
