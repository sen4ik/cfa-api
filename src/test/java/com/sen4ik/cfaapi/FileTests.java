package com.sen4ik.cfaapi;

import com.google.common.base.Charsets;
import com.google.common.collect.MapDifference;
import com.sen4ik.cfaapi.base.Constants;
import com.sen4ik.cfaapi.entities.FileEntity;
import com.sen4ik.cfaapi.enums.FilePaths;
import com.sen4ik.cfaapi.utilities.DatabaseUtility;
import com.sen4ik.cfaapi.utilities.FileUtility;
import io.restassured.RestAssured;
import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.MultiPartSpecification;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.sen4ik.utils.FileUtil;
import org.sen4ik.utils.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;
import static io.restassured.config.DecoderConfig.decoderConfig;
import static io.restassured.config.EncoderConfig.encoderConfig;
import static io.restassured.config.MultiPartConfig.multiPartConfig;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Slf4j
public class FileTests extends BaseTest {

    @Autowired
    FileUtility fileUtility;

    @Autowired
    DatabaseUtility databaseUtility;

    String mp3MimeType = "audio/mpeg";
    String fileTitle = "Test_Файл";
    String testFile = "Antonuk O jizni molodeji.mp3";
    String testTitle = "О жизни молодежи";
    int categoryId = 23;

    @BeforeAll
    public void beforeAll(){
        // delete test sample files from upload location
        FileUtil.deleteFilesAndDirsWithPrefix(new File(fileUtility.getUploadDir()).listFiles(), "sample-");
    }

    @AfterAll
    public void tearDown() throws SQLException, ClassNotFoundException {
        // delete test sample files from test_data
        // fileUtility.deleteFilesAndDirsWithPrefix(new File(Constants.TEST_DATA_DIR).listFiles(), "sample-");
        FileUtil.deleteFilesAndDirsFromPath(Constants.TEST_DATA_DIR + "temp");

        // delete test sample files from upload location
        FileUtil.deleteFilesAndDirsWithPrefix(new File(fileUtility.getUploadDir()).listFiles(), Arrays.asList("sample-", "файлик"));

        // cleanup db
        databaseUtility.executeUpdate("DELETE FROM files WHERE file_title LIKE 'Test_%'");
    }

    private List<MultiPartSpecification> getMultiPartSpecificationList(File file, String fileMimeType, Integer categoryId, String fileTitle){

        List<MultiPartSpecification> multiPartSpecificationList = new ArrayList<>();

        if(file != null){
            multiPartSpecificationList.add(
                new MultiPartSpecBuilder(file)
                        .controlName("file")
                        .mimeType(fileMimeType)
                        .fileName(file.getName())
                        // .charset(Charsets.UTF_8)
                        .build()
            );
        }

        if(categoryId != null){
            multiPartSpecificationList.add(
                new MultiPartSpecBuilder(categoryId)
                        .controlName("categoryId")
                        .build()
            );
        }

        if(fileTitle != null){
            multiPartSpecificationList.add(
                new MultiPartSpecBuilder(fileTitle)
                        .charset(Charsets.UTF_8)
                        .controlName("fileTitle")
                        .build()
            );
        }

        return multiPartSpecificationList;
    }

    @Test
    void getAllFiles() {
        ValidatableResponse response = get(false, null, FilePaths.getAll.value, 200);
        response
                .body("fileTitle", hasItems(testTitle, "В руках великого Мастера"))
                .body("fileName", hasItems(testFile, "01-V rukah velikogo Mastera.mp3"));
    }

    @Test
    void getFileById() throws IOException {
        File sampleFile = getSampleFile();
        List<MultiPartSpecification> multiPartSpecificationList = getMultiPartSpecificationList(sampleFile, mp3MimeType, categoryId, fileTitle);
        ValidatableResponse response =  post(true, true, FilePaths.add.value, multiPartSpecificationList, null, 201);
        int fileId = JsonUtil.getIntFromJsonResponse(response, "$.id");
        ValidatableResponse singleResponse = get(false, null, FilePaths.prefixWithSlash.value + fileId, 200);
        verifyFileResponse(singleResponse, sampleFile.getName(), fileTitle, categoryId);
    }

    @Test
    void getFilesForCategory() {
        ValidatableResponse response = get(false, null, FilePaths.getFilesForCategory.value + 4, 200);
        response
                .body("fileTitle", hasItems(testTitle, "О практическом целомудрии"))
                .body("fileName", hasItems(testFile, "Antonuk O prakticheskom celomurdii.mp3"));
    }

    private File getSampleFile() throws IOException {
        return getSampleFile(Constants.TEST_DATA_DIR + "sample.mp3");
    }

    private File getSampleFile(String filename) throws IOException {
        return FileUtil.getCopyOfFile(
                filename,
                Constants.TEST_DATA_DIR + "temp/" + "sample-" + faker.number().digits(12) + ".mp3"
        );
    }

    private File getSampleFile_withSpacesInTheFilename() throws IOException {
        return FileUtil.getCopyOfFile(
                Constants.TEST_DATA_DIR + "sample.mp3",
                Constants.TEST_DATA_DIR + "temp/" + "sample sample " + faker.number().digits(12) + ".mp3"
        );
    }

    @Test
    void findFileByFileName() {
        ValidatableResponse response = get(false, null,FilePaths.findByFileName.value + testFile, 200);
        verifyResponseForFindEndpoints(response, testFile, testTitle, 4, 30010851);
    }

    private void verifyResponseForFindEndpoints(ValidatableResponse response, String fileName, String fileTitle, int categoryId, int fileSizeBytes){
        response
                .body("id", not(empty()))
                .body("fileTitle", hasItem(fileTitle))
                .body("fileName", hasItem(fileName))
                .body("categoryId", hasItem(categoryId))
                .body("fileSizeBytes", hasItem(fileSizeBytes))
                .body("downloaded", hasItem(0))
                .body("listened", hasItem(0))
                .body("hidden", hasItem(false))
                .body("addedBy", hasItem(1))
                .body("addedOn", not(empty()));
    }

    @Test
    void findFileByFileName_negative() {
        ValidatableResponse response = get(false, null,FilePaths.findByFileName.value + "BlaBlaBla.mp3", 200);
        response
                .body("", hasSize(0))
                .body("$", hasSize(0));
    }

    @Test
    void findFileByFileTitle() {
        ValidatableResponse response = get(false, null, FilePaths.findByFileTitle.value + testTitle, 200);
        verifyResponseForFindEndpoints(response, testFile, testTitle, 4, 30010851);
    }

    @Test
    void findFileByFileTitle_negative() {
        ValidatableResponse response = get(false, null, FilePaths.findByFileTitle.value + "БлаБлаБла", null);
        response
                .body("", hasSize(0))
                .body("$", hasSize(0));
    }

    @Test
    void addFile_toSubCategory() throws Exception {
        File sampleFile = getSampleFile();
        ValidatableResponse response = addFile(sampleFile);
        verifyFileResponse(response, sampleFile.getName(), fileTitle, categoryId);
        // verify file was uploaded
        String fileInUploadLocation = fileUtility.getCategoryFolderPath(categoryId) + sampleFile.getName();
        assertTrue(FileUtil.doesFileExists(fileInUploadLocation));
    }

    @Test
    void addFile_spaceInTheFilename() throws Exception {
        File sampleFile = getSampleFile_withSpacesInTheFilename();
        ValidatableResponse response = addFile(sampleFile);
        response
                .body("fileName", equalTo(sampleFile.getName().replaceAll(" ", "_")));
    }

    private ValidatableResponse addFile(File sampleFile) {
        log.info("CALLED: addFile(" + sampleFile.getName() + ")");
        List<MultiPartSpecification> multiPartSpecificationList = getMultiPartSpecificationList(sampleFile, mp3MimeType, categoryId, fileTitle);
        ValidatableResponse response =  post(true,true, FilePaths.add.value, multiPartSpecificationList, null, 201);
        return response;
    }

    private void verifyFileResponse(ValidatableResponse response, String fileName, String fileTitle, int categoryId){
        response
                .body("id", not(empty()))
                .body("fileTitle", equalTo(fileTitle))
                .body("fileName", equalTo(fileName))
                .body("categoryId", equalTo(categoryId))
                .body("fileSizeBytes", greaterThan(764000))
                .body("downloaded", equalTo(0))
                .body("listened", equalTo(0))
                .body("hidden", equalTo(false))
                .body("addedBy", equalTo(1))
                .body("addedOn", not(empty()));
    }

    @Test
    void addFileToCategoryWithIdZero() throws IOException {
        File sampleFile = getSampleFile();
        List<MultiPartSpecification> multiPartSpecificationList = getMultiPartSpecificationList(sampleFile, mp3MimeType, 0, fileTitle);
        ValidatableResponse response =  post(true, true, FilePaths.add.value, multiPartSpecificationList, null, 201);
        verifyFileResponse(response, sampleFile.getName(), fileTitle, 0);

        // verify file was uploaded
        String fileInUploadLocation = fileUtility.getCategoryFolderPath(0) + sampleFile.getName();
        assertTrue(FileUtil.doesFileExists(fileInUploadLocation));
    }

    @Test
    void addFile_noFileParam() {
        List<MultiPartSpecification> multiPartSpecificationList = getMultiPartSpecificationList(null, mp3MimeType, categoryId, fileTitle);
        ValidatableResponse response = post(true, true, FilePaths.add.value, multiPartSpecificationList, null, 400);
        response
                .body("message", containsString("Required request part 'file' is not present"))
                .body("status", equalTo("Error"));
    }

    @Test
    void addFile_noFileTitleParam() throws IOException {
        File sampleFile = getSampleFile();
        List<MultiPartSpecification> multiPartSpecificationList = getMultiPartSpecificationList(sampleFile, mp3MimeType, categoryId, null);
        ValidatableResponse response = post(true, true, FilePaths.add.value, multiPartSpecificationList, null, 400);
        response
                .body("message", containsString("Required String parameter 'fileTitle' is not present"))
                .body("status", equalTo("Error"));
    }

    @Test
    void addFile_noCategoryIdParam() throws IOException {
        File sampleFile = getSampleFile();
        List<MultiPartSpecification> multiPartSpecificationList = getMultiPartSpecificationList(sampleFile, mp3MimeType, null, fileTitle);
        ValidatableResponse response = post(true, true, FilePaths.add.value, multiPartSpecificationList, null, 400);
        response
                .body("message", containsString("Required Integer parameter 'categoryId' is not present"))
                .body("status", equalTo("Error"));
    }

    @Test
    void addFile_fileSizeBiggerThanAllowed() {
        File sampleTextFile = new File(Constants.TEST_DATA_DIR + "sample.txt");
        String mimeType = FileUtil.getMimeTypeWithTika(sampleTextFile);
        List<MultiPartSpecification> multiPartSpecificationList = getMultiPartSpecificationList(sampleTextFile, mimeType, categoryId, fileTitle);
        ValidatableResponse response = post(true, true, FilePaths.add.value, multiPartSpecificationList, null, 400);
        response
                .body("message", containsString("The field file exceeds its maximum permitted size of"))
                .body("status", equalTo("Error"));
    }

    @Test
    void addFile_cyrillicFileName() {

        String sampleFileName = "файлик.mp3";
        String filePath = Constants.TEST_DATA_DIR + sampleFileName;
        File file = new File(filePath);
        // String fileEncoding = System.getProperty("file.encoding");

        RestAssured.config = RestAssured.config()
                .encoderConfig(encoderConfig().defaultContentCharset("UTF-8"))
                .encoderConfig(encoderConfig().defaultCharsetForContentType("UTF-8", "multipart/form-data"))
                .multiPartConfig(multiPartConfig().defaultCharset("UTF-8"))
                .decoderConfig(decoderConfig().defaultContentCharset("UTF-8"))
                .decoderConfig(decoderConfig().defaultCharsetForContentType("UTF-8", "multipart/form-data"))
                // This is what actually fixed the issues I had with encoding for the file name
                // https://github.com/rest-assured/rest-assured/issues/844
                // https://stackoverflow.com/questions/59780811/rest-assured-post-call-with-multipart-file-that-contains-cyrillic-characters-in
                .httpClient(HttpClientConfig.httpClientConfig().httpMultipartMode(HttpMultipartMode.BROWSER_COMPATIBLE));

        List<MultiPartSpecification> multiPartSpecification = getMultiPartSpecificationList(file, mp3MimeType, categoryId, fileTitle);

        RequestSpecification rs = given()
                .baseUri(baseUrl)
                .header(getAuthHeader_asAdmin())
                .config(RestAssured.config)
                .log().everything();

        if(multiPartSpecification != null && multiPartSpecification.size() > 0){
            for(MultiPartSpecification m : multiPartSpecification){
                rs.multiPart(m);
            }
        }

        ValidatableResponse response = rs
                .when()
                    .post(FilePaths.add.value)
                .then()
                    .log().everything()
                    .statusCode(201)
                    .contentType(ContentType.JSON);

        verifyFileResponse(response, sampleFileName, fileTitle, categoryId);

        // verify file was uploaded
        String fileInUploadLocation = fileUtility.getCategoryFolderPath(categoryId) + sampleFileName;
        assertTrue(FileUtil.doesFileExists(fileInUploadLocation));
    }

    @Test
    void addFile_prohibitedMimeType() {
        List<MultiPartSpecification> mpl = getMultiPartSpecificationList(new File("./test_data/sample.sh"), mp3MimeType, categoryId, "Test_Shell_File");
        ValidatableResponse response = post(true, true, FilePaths.add.value, mpl, null, 400);
        response
                .body("message", equalTo("Only files with following content type are allowed: "
                        + Constants.ALLOWED_MIME_TYPES))
                .body("status", equalTo("Error"));
    }

    @Test
    void addFile_alreadyExists() throws IOException {
        File sampleFile = getSampleFile();
        addFile(sampleFile);

        List<MultiPartSpecification> multiPartSpecificationList = getMultiPartSpecificationList(sampleFile, mp3MimeType, categoryId, fileTitle);
        ValidatableResponse response =  post(true, true, FilePaths.add.value, multiPartSpecificationList, null, 400);
        response
                .body("message", equalTo(sampleFile.getName() + " file already exists for category " + categoryId))
                .body("status", equalTo("Error"));
    }

    @Test
    void addFileToNonExistentCategory() throws IOException {
        int categoryId = 9999999;
        File sampleFile = getSampleFile();
        List<MultiPartSpecification> multiPartSpecificationList = getMultiPartSpecificationList(sampleFile, mp3MimeType, categoryId, fileTitle);
        ValidatableResponse response = post(true, true, FilePaths.add.value, multiPartSpecificationList, null, 404);
        response
                .body("message", equalTo(
                        "Error while building category directory path. Parent directory ID: " + categoryId + " does not exist."
                ))
                .body("status", equalTo("Error"));
    }

    @Test
    void addFile_zeroSize() {

    }

    @Test
    void deleteFile() throws Exception {
        File sampleFile = getSampleFile();
        ValidatableResponse response =  addFile(sampleFile);
        int fileId = JsonUtil.getIntFromJsonResponse(response, "$.id");
        response = delete(true, true, FilePaths.prefixWithSlash.value + fileId, 200);
        response
                .body("status", equalTo("Deleted"))
                .body("id", equalTo(fileId));

        // verify file is deleted from the file system
        String fileLocation = fileUtility.getCategoryFolderPath(categoryId) + sampleFile.getName();
        assertFalse(FileUtil.doesFileExists(fileLocation));
    }

    @Test
    void updateFileInfo() throws IOException {
        File sampleFile = getSampleFile();
        ValidatableResponse response = addFile(sampleFile);
        verifyFileResponse(response, sampleFile.getName(), fileTitle, categoryId);

        // String json = responseToJson(response);
        // FileEntity createdFile = (FileEntity) objectFromJson(json, FileEntity.class);

        FileEntity updateWith = new FileEntity();
        updateWith.setId(JsonUtil.getIntFromJsonResponse(response, "$.id"));
        updateWith.setFileTitle("Test_File_1");
        updateWith.setFileName("file.mp3");
        updateWith.setCategoryId(4);
        updateWith.setFileSizeBytes(999999L);
        updateWith.setDownloaded(4);
        updateWith.setListened(4);
        updateWith.setHidden(true);

        ValidatableResponse updateResponse = put(
                true,
                FilePaths.updateFileInfo.value + JsonUtil.getIntFromJsonResponse(response, "$.id"), JsonUtil.objectToJson_withoutNulls(updateWith),
                200);
        String updateJson = JsonUtil.responseToJson(updateResponse);
        // FileEntity updatedFile = (FileEntity) objectFromJson(updateJson, FileEntity.class);

        MapDifference<String, Object> diff = JsonUtil.compareTwoJsonsAndGetTheDifference(JsonUtil.objectToJson_withoutNulls(updateWith), updateJson);
        assertTrue(diff.entriesOnlyOnRight().containsKey("addedBy"));
        assertTrue(diff.entriesOnlyOnRight().containsKey("addedOn"));
    }

    @Test
    void replaceFile() throws Exception {
        File sampleFile = getSampleFile();
        ValidatableResponse response = addFile(sampleFile);
        int fileId = JsonUtil.getIntFromJsonResponse(response, "$.id");
        String sampleFilePath = fileUtility.getCategoryFolderPath(categoryId) + sampleFile.getName();

        File replacementFile = getSampleFile(Constants.TEST_DATA_DIR + "sample 2.mp3");
        List<MultiPartSpecification> multiPartSpecificationList = getMultiPartSpecificationList(replacementFile, mp3MimeType, null, null);
        response =  put(true, FilePaths.replaceFile.value + fileId, multiPartSpecificationList, null, 200);
        String replacementFilePath = fileUtility.getCategoryFolderPath(categoryId) + replacementFile.getName();

        // verify db updated file name and file size
        response
                .body("id", equalTo(fileId))
                .body("fileName", equalTo(replacementFile.getName()))
                .body("fileSizeBytes", equalTo((int) replacementFile.length()));

        // verify old file is deleted
        assertFalse(new File(sampleFilePath).exists());

        // verify new file is created in file system
        assertTrue(new File(replacementFilePath).exists());
    }

    @Test
    void addFile_noToken() {
        // File sampleFile = getSampleFile();
        // List<MultiPartSpecification> multiPartSpecificationList = getMultiPartSpecificationList(sampleFile, mp3MimeType, categoryId, fileTitle);
        ValidatableResponse response =  post(false, true, FilePaths.add.value, null, null, 403);
        verifyNoTokenResponse(response, Constants.API_PREFIX + FilePaths.add.value);
    }

    @Test
    void deleteFile_noToken() {
        // File sampleFile = getSampleFile();
        // ValidatableResponse response =  addFile(sampleFile);
        // String deletePath = FilePaths.prefixWithSlash.value + getIntFromJsonResponse(response, "$.id");
        String deletePath = FilePaths.prefixWithSlash.value + 111;
        ValidatableResponse response = delete(false, null, deletePath, 403);
        verifyNoTokenResponse(response, Constants.API_PREFIX + deletePath);
    }

    @Test
    void updateFileInfo_noToken() {
        ValidatableResponse updateResponse = put(false, FilePaths.updateFileInfo.value + 111, null, 403);
        verifyNoTokenResponse(updateResponse, Constants.API_PREFIX + FilePaths.updateFileInfo.value + 111);
    }

    @Test
    void replaceFile_noToken() {
        ValidatableResponse response = put(false, FilePaths.replaceFile.value + 111, null, null, 403);
        verifyNoTokenResponse(response, Constants.API_PREFIX + FilePaths.replaceFile.value + 111);
    }

    // TODO: add verification for file length to existing tests
    // TODO: add test for file/download/{id}

}
