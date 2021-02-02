package com.sen4ik.cfaapi;

import com.sen4ik.cfaapi.base.Constants;
import com.sen4ik.cfaapi.entities.Playlist;
import com.sen4ik.cfaapi.enums.ErrorMessagesCustom;
import com.sen4ik.cfaapi.enums.PlaylistPaths;
import com.sen4ik.cfaapi.utilities.DatabaseUtility;
import io.restassured.response.ValidatableResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.sen4ik.utils.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.*;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Slf4j
public class PlaylistTests extends BaseTest {

    @Autowired
    DatabaseUtility databaseUtility;

    String playlistNameTestPrefix = "Test_";
    int testPlaylistId;
    String testPlaylistName;

    @BeforeAll
    public void beforeAll(){
        ValidatableResponse r = addPlaylistAndVerify(false, nonAdminUserId);
        testPlaylistId = JsonUtil.getIntFromJsonResponse(r, "$.id");
        testPlaylistName = JsonUtil.getStringFromJsonResponse(r, "$.playlistName");
    }

    @AfterAll
    public void tearDown() throws SQLException, ClassNotFoundException {
        // cleanup db
        databaseUtility.executeUpdate("DELETE FROM playlists WHERE playlist_name LIKE '" + playlistNameTestPrefix + "%'");
    }

    @Test
    void getAllPlaylists_adminRole() {
        ValidatableResponse response = get(true, true, PlaylistPaths.getAll.value, 200);
        response
                .body("id", hasItems(1, 2, 3))
                .body("userId", hasItems(adminUserId, nonAdminUserId))
                .body("playlistName", hasItems("first_playlist", "second_playlist", "third_playlist"))
                .body("find { it.id == 1 }.playlistFiles.fileId", hasItems(2, 21, 22))
                .body("find { it.id == 1 }.playlistFiles.file.fileTitle", hasItems("О жизни молодежи",
                        "Not my will_ but Yours be done", "God is Glorious and Mighty"));
    }

    @Test
    void getAllPlaylists_noToken() {
        ValidatableResponse response = get(false, null, PlaylistPaths.getAll.value, 403);
        response
                .body("message", containsString(ErrorMessagesCustom.access_denied.value))
                .body("error", equalTo("Forbidden"));
    }

    @Test
    void getAllPlaylists_userRole() {
        ValidatableResponse response = get(true, false, PlaylistPaths.getAll.value, 403);
        response
                .body("error", equalTo("Forbidden"))
                .body("message", equalTo("Forbidden"));
    }

    @Test
    void getPlaylistById_existingPlaylist() {
        ValidatableResponse response = get(true, true, PlaylistPaths.prefixWithSlash.value + 1, 200);
        response
                .body("id", equalTo(1))
                .body("userId", equalTo(adminUserId))
                .body("playlistName", equalTo("first_playlist"))
                .body("playlistFiles.fileId", hasItems(2, 21, 22))
                .body("playlistFiles.file.fileTitle", hasItems("О жизни молодежи",
                        "Not my will_ but Yours be done", "God is Glorious and Mighty"));
    }

    @Test
    void getPlaylistById_nonExistingPlaylist() {
        ValidatableResponse response = get(true, true, PlaylistPaths.prefixWithSlash.value + 99999, 404);
        response
                .body("message", containsString("Could not find playlist with ID: " + 99999))
                .body("status", equalTo("Error"));
    }

    @Test
    void getSomeonesPlaylistById() {
        ValidatableResponse response = get(true, true,PlaylistPaths.prefixWithSlash.value + 6, 403);
        verifyForbiddenResponse(response);
    }

    @Test
    void getPlaylistById_noToken() {
        ValidatableResponse response = get(false, null, PlaylistPaths.prefixWithSlash.value + 1, 403);
        verifyNoTokenResponse(response, Constants.API_PREFIX + PlaylistPaths.prefixWithSlash.value + 1);
    }

    @Test
    void getPlaylistForUser_asAdmin() {
        ValidatableResponse response = get(true, true,PlaylistPaths.getPlaylistsForUser.value + 1, 200);
        response
                .body("id", hasItems(1, 2, 3))
                .body("userId", hasItems(adminUserId))
                .body("playlistName", hasItems("first_playlist", "second_playlist", "third_playlist"))
                .body("find { it.id == 1 }.playlistFiles.fileId", hasItems(2, 21, 22));
    }

    @Test
    void getPlaylistForUser_asUser() {
        ValidatableResponse response = get(true, false,PlaylistPaths.getPlaylistsForUser.value + 2, 200);
        response
                .body("id", hasItems(6))
                .body("userId", hasItems(nonAdminUserId))
                .body("playlistName", hasItems("fourth_playlist"))
                .body("playlistFiles[0].fileId", hasItems(22, 23));
    }

    @Test
    void getPlaylistForUser_noToken() {
        ValidatableResponse response = get(false, null,PlaylistPaths.getPlaylistsForUser.value + 1, 403);
        verifyNoTokenResponse(response, Constants.API_PREFIX + PlaylistPaths.getPlaylistsForUser.value + 1);
    }

    @Test
    void getPlaylistThatBelongsToSomeoneElse() {
        ValidatableResponse response = get(true, true,PlaylistPaths.getPlaylistsForUser.value + 2, 403);
        verifyForbiddenResponse(response);
    }

    @Test
    void addPlaylist_asAdmin() {
        addPlaylistAndVerify(true, adminUserId);
        // TODO: verify playlist shows when GET all playlist is hit
    }

    @Test
    void addPlaylist_asUser() {
        addPlaylistAndVerify(false, nonAdminUserId);
        // TODO: verify playlist shows when GET all playlist is hit
    }

    @Test
    void addPlaylist_noToken() {
        Playlist p = getPlaylistObj();
        ValidatableResponse response = post(false, null, PlaylistPaths.add.value, JsonUtil.objectToJson_withoutNulls(p), 403);
        verifyNoTokenResponse(response);
    }

    @Test
    void deletePlaylist_asAdmin() {
        ValidatableResponse response = addPlaylistAndVerify(true, adminUserId);
        int playlistId = JsonUtil.getIntFromJsonResponse(response, "$.id");
        ValidatableResponse deleteResponse = delete(true, true, PlaylistPaths.prefixWithSlash.value + playlistId, 200);
        deleteResponse
                .body("id", equalTo(playlistId))
                .body("status", equalTo("Deleted"));
    }

    @Test
    void deletePlaylist_asUser() {
        ValidatableResponse response = addPlaylistAndVerify(false, nonAdminUserId);
        int playlistId = JsonUtil.getIntFromJsonResponse(response, "$.id");
        ValidatableResponse deleteResponse = delete(true, false, PlaylistPaths.prefixWithSlash.value + playlistId, 200);
        deleteResponse
                .body("id", equalTo(playlistId))
                .body("status", equalTo("Deleted"));
    }

    @Test
    void deleteSomeonesPlaylist_asUser() {
        // create playlist as admin role user
        ValidatableResponse response = addPlaylistAndVerify(true, adminUserId);
        int playlistId = JsonUtil.getIntFromJsonResponse(response, "$.id");
        // delete playlist as user
        ValidatableResponse deleteResponse = delete(true, false, PlaylistPaths.prefixWithSlash.value + playlistId, 403);
        verifyForbiddenResponse(deleteResponse);
    }

    @Test
    void deleteSomeonesPlaylist_asAdmin() {
        // create playlist as user
        ValidatableResponse response = addPlaylistAndVerify(false, nonAdminUserId);
        int playlistId = JsonUtil.getIntFromJsonResponse(response, "$.id");
        // delete playlist as admin role user
        ValidatableResponse deleteResponse = delete(true, true, PlaylistPaths.prefixWithSlash.value + playlistId, 403);
        verifyForbiddenResponse(deleteResponse);
    }

    @Test
    void deletePlaylist_noToken() {
        ValidatableResponse response = addPlaylistAndVerify(false, nonAdminUserId);
        int playlistId = JsonUtil.getIntFromJsonResponse(response, "$.id");
        ValidatableResponse deleteResponse = delete(false, null, PlaylistPaths.prefixWithSlash.value + playlistId, 403);
        verifyNoTokenResponse(deleteResponse);
    }

    @Test
    void addFileToPlaylist_asUser() {
        // create playlist
        ValidatableResponse r = addPlaylistAndVerify(false, nonAdminUserId);
        int playlistId = JsonUtil.getIntFromJsonResponse(r, "$.id");
        String playlistName = JsonUtil.getStringFromJsonResponse(r, "$.playlistName");

        // add file to playlist
        ValidatableResponse response = post(true, false, PlaylistPaths.prefixWithSlash.value + playlistId + "/file/" + 2, null,200);
        response
                .body("playlistFileId", isA(Integer.class))
                .body("playlistId", equalTo(playlistId))
                .body("fileId", equalTo(2))
                .body("file.id", equalTo(2))
                .body("file.fileTitle", equalTo("О жизни молодежи"));

        // verify file is in the playlist
        ValidatableResponse playlistResponse = get(true, false, PlaylistPaths.prefixWithSlash.value + playlistId, 200);
        playlistResponse
                .body("id", equalTo(playlistId))
                .body("userId", equalTo(nonAdminUserId))
                .body("playlistName", equalTo(playlistName))
                .body("playlistFiles[0].playlistId", equalTo(playlistId))
                .body("playlistFiles[0].playlistFileId", isA(Integer.class))
                .body("playlistFiles[0].fileId", equalTo(2))
                .body("playlistFiles[0].file.fileTitle", equalTo("О жизни молодежи"));
    }

    @Test
    void addFileToPlaylist_nonExistingPlaylist() {
        ValidatableResponse response = post(true, false, PlaylistPaths.prefixWithSlash.value + 99999999 + "/file/" + 2, null,404);
        verifyNonExistingPlaylistResponse(response, 99999999);
    }

    @Test
    void addFileToPlaylist_nonExistingFile() {
        ValidatableResponse response = post(true, false, PlaylistPaths.prefixWithSlash.value + testPlaylistId + "/file/" + 9999999, null,404);
        verifyNonExistingFileResponse(response, 9999999);
    }

    @Test
    void addFileToSomeonesPlaylist() {
        ValidatableResponse response = post(true, false, PlaylistPaths.prefixWithSlash.value + 1 + "/file/" + 2, null,403);
        verifyForbiddenResponse(response);
    }

    @Test
    void addFileToPlaylist_noToken() {
        ValidatableResponse response = post(false, null, PlaylistPaths.prefixWithSlash.value + testPlaylistId + "/file/" + 2, null,403);
        verifyNoTokenResponse(response);
    }

    @Test
    void addFilesToPlaylist_asUser() {
        ValidatableResponse response = post(true, false, PlaylistPaths.prefixWithSlash.value + testPlaylistId + "/file/add?fileIDs=3,4,5", null,200);
        response
                .body("playlistFileId[0]", isA(Integer.class))
                .body("playlistId", hasItems(testPlaylistId, testPlaylistId, testPlaylistId))
                .body("fileId", hasItems(3, 4, 5))
                .body("file.id", hasItems(3, 4, 5));
    }

    @Test
    void addFilesToPlaylist_nonExistingPlaylist() {
        ValidatableResponse response = post(true, false, PlaylistPaths.prefixWithSlash.value + 99999999 + "/file/add?fileIDs=3,4,5", null,404);
        verifyNonExistingPlaylistResponse(response, 99999999);
    }

    @Test
    void addFilesToPlaylist_nonExistingFiles() {
        ValidatableResponse response = post(true, false, PlaylistPaths.prefixWithSlash.value + testPlaylistId + "/file/add?fileIDs=9999999,8888888,5", null,404);
        response
                .body("message", equalTo("Could not find file(s) with the following IDs: [9999999, 8888888]"))
                .body("status", equalTo("Error"));
    }

    @Test
    void addFilesToPlaylist_noToken() {
        ValidatableResponse response = post(false, null, PlaylistPaths.prefixWithSlash.value + testPlaylistId + "/file/add?fileIDs=6,7", null,403);
        verifyNoTokenResponse(response);
    }

    @Test
    void deleteFileFromPlaylist() {
        // create playlist
        ValidatableResponse r = addPlaylistAndVerify(false, nonAdminUserId);
        int playlistId = JsonUtil.getIntFromJsonResponse(r, "$.id");
        String playlistName = JsonUtil.getStringFromJsonResponse(r, "$.playlistName");

        // add file to playlist
        ValidatableResponse response = post(true, false, PlaylistPaths.prefixWithSlash.value + playlistId + "/file/" + 2, null,200);
        response
                .body("playlistFileId", isA(Integer.class))
                .body("playlistId", equalTo(playlistId))
                .body("fileId", equalTo(2))
                .body("file.id", equalTo(2))
                .body("file.fileTitle", equalTo("О жизни молодежи"));
        int playlistFileId = JsonUtil.getIntFromJsonResponse(response, "$.playlistFileId");

        // verify file is in the playlist
        ValidatableResponse playlistResponse = get(true, false, PlaylistPaths.prefixWithSlash.value + playlistId, 200);
        playlistResponse
                .body("id", equalTo(playlistId))
                .body("userId", equalTo(nonAdminUserId))
                .body("playlistName", equalTo(playlistName))
                .body("playlistFiles[0].playlistFileId", isA(Integer.class))
                .body("playlistFiles[0].fileId", equalTo(2))
                .body("playlistFiles[0].playlistId", equalTo(playlistId))
                .body("playlistFiles[0].file.id", equalTo(2))
                .body("playlistFiles[0].file.fileTitle", equalTo("О жизни молодежи"));

        // delete file from playlist
        ValidatableResponse deleteResponse = delete(true, false, PlaylistPaths.prefixWithSlash.value + "playlistFile/" + playlistFileId, 200);
        deleteResponse
                .body("status", equalTo("Deleted"));

        // verify file is not in the playlist
        playlistResponse = get(true, false, PlaylistPaths.prefixWithSlash.value + playlistId, 200);
        playlistResponse
                .body("id", equalTo(playlistId))
                .body("userId", equalTo(nonAdminUserId))
                .body("playlistName", equalTo(playlistName))
                .body("playlistFiles", not(hasItems(2)))
                .body("playlistFiles.size()", is(0));
    }

    @Test
    void deleteFileFromPlaylistThatDoesNotBelongToTheUser() {
        // create playlist
        ValidatableResponse r = addPlaylistAndVerify(true, adminUserId);
        int playlistId = JsonUtil.getIntFromJsonResponse(r, "$.id");

        // add file to playlist
        ValidatableResponse response = post(true, true, PlaylistPaths.prefixWithSlash.value + playlistId + "/file/" + 2, null,200);
        int playlistFileId = JsonUtil.getIntFromJsonResponse(response, "$.playlistFileId");

        ValidatableResponse deleteResponse = delete(true, false, PlaylistPaths.prefixWithSlash.value + "playlistFile/" + playlistFileId, 403);
        verifyForbiddenResponse(deleteResponse);
    }

    @Test
    void deleteNonExistingPlaylistFileRecord() {
        ValidatableResponse deleteResponse = delete(true, true, PlaylistPaths.prefixWithSlash.value + "playlistFile/9999999", 404);
        deleteResponse
                .body("message", equalTo("Could not find playlist to file association using ID 9999999"))
                .body("status", equalTo("Error"));
    }

    private Playlist getPlaylistObj(){
        Playlist p = new Playlist();
        p.setPlaylistName(playlistNameTestPrefix + faker.letterify("????? ????? ????????"));
        return p;
    }

    private ValidatableResponse addPlaylistAndVerify(boolean isAdmin, int userIdToVerify){
        Playlist p = getPlaylistObj();
        ValidatableResponse response = post(true, isAdmin, PlaylistPaths.add.value, JsonUtil.objectToJson_withoutNulls(p), 200);
        response
                .body("id", isA(Integer.class))
                .body("userId", equalTo(userIdToVerify))
                .body("playlistName", equalTo(p.getPlaylistName()));
        return response;
    }

    private void verifyForbiddenResponse(ValidatableResponse response){
        response
                .body("message", containsString("Users can only view/edit/delete playlists that belong to them"))
                .body("status", equalTo("Error"));
    }

    private void verifyNoTokenResponse(ValidatableResponse response){
        response
                .body("error", equalTo("Forbidden"))
                .body("status", equalTo(403))
                .body("message", equalTo(ErrorMessagesCustom.access_denied.value));
    }

    private void verifyNonExistingPlaylistResponse(ValidatableResponse response, int playlistId){
        response
                .body("message", equalTo("Could not find playlist with ID: " + playlistId))
                .body("status", equalTo("Error"));
    }

    private void verifyNonExistingFileResponse(ValidatableResponse response, int fileId){
        response
                .body("message", equalTo("Could not find file with ID: " + fileId))
                .body("status", equalTo("Error"));
    }

}
