package com.sen4ik.cfaapi.helpers;

import com.jayway.jsonpath.JsonPath;
import com.sen4ik.cfaapi.BaseTest;
import com.sen4ik.cfaapi.enums.FilePaths;
import com.sen4ik.cfaapi.utilities.DatabaseUtility;
import com.sen4ik.cfaapi.utilities.FileUtility;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.sen4ik.utils.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static io.restassured.RestAssured.given;

@Slf4j
@SpringBootTest
public class UpdateFileSizesAndLength extends BaseTest {

    @Autowired
    FileUtility fileUtility;

    @Autowired
    DatabaseUtility databaseUtility;

    @Test
    @Disabled("This is helper test which was only needed once")
    void updateFileSizesAndMp3FileLength() throws Exception {

        String errors = "";

        ValidatableResponse response = given()
                .baseUri(baseUrl)
                .log().everything()
            .when()
                .get(FilePaths.getAll.value)
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .log().everything();

        String jsonBody = response.extract().asString();
        List<Integer> ids = JsonPath.read(jsonBody, "$.*.id");

        for(Integer id : ids){
            ValidatableResponse singleFileResponse = given()
                    .baseUri(baseUrl)
                    .log().everything()
                .when()
                    .get(FilePaths.prefixWithSlash.value + id)
                .then()
                    .statusCode(200)
                    .contentType(ContentType.JSON)
                    .log().everything();

            int fileId = JsonUtil.getIntFromJsonResponse(singleFileResponse, "$.id");
            int categoryId = JsonUtil.getIntFromJsonResponse(singleFileResponse, "$.categoryId");
            String fileName = JsonUtil.getStringFromJsonResponse(singleFileResponse, "$.fileName");
            String filePath = fileUtility.getCategoryFolderPath(categoryId) + fileName;
            log.info(" ==> " + fileName + ": " + filePath);

            Path path = Paths.get(filePath);
            if(Files.exists(path)){
                long fileSize = Files.size(path);
                databaseUtility.executeUpdate("UPDATE files SET file_size_bytes=" + fileSize + " WHERE id=" + fileId);

                if(fileName.toLowerCase().trim().endsWith(".mp3")){
                    int seconds = fileUtility.getDurationInSeconds(new File(filePath));
                    databaseUtility.executeUpdate("UPDATE files SET length_in_seconds=" + seconds + " WHERE id=" + fileId);
                }
            }
            else{
                String error = "-- =====> " + fileName + " DOES NOT EXISTS <=====";
                errors = errors + error;
                log.error(error);
            }
        }
        log.error(errors);
    }

}
