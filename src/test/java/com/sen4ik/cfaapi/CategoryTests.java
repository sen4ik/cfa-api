package com.sen4ik.cfaapi;

import com.sen4ik.cfaapi.base.Constants;
import com.sen4ik.cfaapi.entities.Category;
import com.sen4ik.cfaapi.enums.CategoryPaths;
import com.sen4ik.cfaapi.utilities.DatabaseUtility;
import com.sen4ik.cfaapi.utilities.FileUtility;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.sen4ik.utils.FileUtil;
import org.sen4ik.utils.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.sql.SQLException;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CategoryTests extends BaseTest{

    @Autowired
    FileUtility fileUtility;

    @Autowired
    DatabaseUtility databaseUtility;

    @AfterAll
    public void tearDown() throws SQLException, ClassNotFoundException {
        // delete test folders
        File[] files = new File(fileUtility.getUploadDir()).listFiles();
        FileUtil.deleteFilesAndDirsWithPrefix(files, "Test_");

        // cleanup db
        databaseUtility.executeUpdate("DELETE FROM categories WHERE category_name LIKE 'Test_%'");
    }

    private ValidatableResponse createCategory(Category c){
        return createCategory(JsonUtil.objectToJson_withoutNulls(c));
    }

    private ValidatableResponse createCategory(String body){
        return post(true, true, CategoryPaths.add.value, body, 201);
    }

    private ValidatableResponse updateCategory(int categoryId, Category c){
        return put(true, CategoryPaths.prefixWithSlash.value + categoryId, JsonUtil.objectToJson_withoutNulls(c), 200);
    }

    private ValidatableResponse deleteCategory(int catId){
        return delete(true, true,CategoryPaths.prefixWithSlash.value + catId, 200);
    }

    private Category getCategoryObject(){
        String random = faker.number().digits(10);
        String categoryName = "Test_Категория-" + random;

        Category c = new Category();
        c.setCategoryFolder("Test_Category-" + random);
        c.setCategoryName(categoryName);
        c.setParentId(4);
        c.setOrderBy("Test_title ASC");
        c.setZip(categoryName + ".zip");

        return c;
    }

    private void verifyCategoryResponse(ValidatableResponse response, Category c){
        response
                .body("id", not(empty()))
                .body("categoryName", equalTo(c.getCategoryName()))
                .body("categoryFolder", equalTo(c.getCategoryFolder()))
                .body("parentId", equalTo(c.getParentId()))
                .body("orderBy", equalTo(c.getOrderBy()))
                .body("zip", equalTo(c.getZip()))
                .body("dateAdded", not(empty()))
                .body("hidden", equalTo(false))
                .body("addedBy", equalTo(1));
    }

    @Test
    void getAllTest() {
        ValidatableResponse response = get(false, null, CategoryPaths.getAll.value, 200);
        response
                .body("categoryName", hasItems("Проповеди", "Пение"))
                .body("categoryFolder", hasItems("sermons", "songs"));
    }

    @Test
    void getCategoriesByParentId() {
        ValidatableResponse response = get(false, null,CategoryPaths.getCategoriesByParentId.value + 4, 200);
        response
                .body("categoryFolder", hasItems("denis_samarin", "alex_sentsov", "mihail_golubin"));
    }

    @Test
    void findCategoryByName() {
        ValidatableResponse response = get(false, null, CategoryPaths.findByCategoryName.value + "Проповеди", 200);
        response
                .body("categoryName", hasItem("Проповеди"))
                .body("categoryFolder", hasItem("sermons"))
                .body("id", hasItem(4));
    }

    @Test
    void findCategoryByName_negative() {
        ValidatableResponse response = get(false, null,CategoryPaths.findByCategoryName.value + "Блаблабла", 200);
        response
                .body("", hasSize(0))
                .body("$", hasSize(0));

        assertEquals(response.extract().asString(), "[]");
    }

    @Test
    void addAndViewCategory() throws Exception {
        // add
        Category c = getCategoryObject();
        ValidatableResponse response = createCategory(c);
        verifyCategoryResponse(response, c);
        int createdCatId = JsonUtil.getIntFromJsonResponse(response, "$.id");

        // verify it shows in /all payload
        ValidatableResponse allCategoriesResponse = get(false, null, CategoryPaths.getAll.value, 200);
        allCategoriesResponse
                .body("categoryName", hasItem(c.getCategoryName()))
                .body("categoryFolder", hasItem(c.getCategoryFolder()))
                .body("zip", hasItem(c.getZip()));

        // verify you can get category by id
        ValidatableResponse singleResponse = get(false, null, CategoryPaths.prefixWithSlash.value + createdCatId, 200);
        verifyCategoryResponse(singleResponse, c);

        // verify category directory is created
        verifyDirectoryCreated(c);
    }

    private void verifyDirectoryCreated(Category c) {
        String categoryFolderPath = fileUtility.getCategoryFolderPath(c.getParentId());
        File directory = new File(categoryFolderPath);
        assertTrue(directory.exists());
    }

    @Test
    void categoryFolderAlreadyExists() {
        Category c = getCategoryObject();
        ValidatableResponse response = createCategory(c);
        verifyCategoryResponse(response, c);

        response = post(true, true, CategoryPaths.add.value, JsonUtil.objectToJson_withoutNulls(c), 400);
        response
                .body("status", equalTo("Error"))
                .body("message", equalTo(c.getCategoryFolder() + " category folder already exists in the file system"));
    }

    @Test
    void createParentCategory() throws Exception {
        Category c = getCategoryObject();
        c.setParentId(0);
        ValidatableResponse response = createCategory(c);
        verifyCategoryResponse(response, c);
        verifyDirectoryCreated(c);
    }

    @Test
    void nonExistingParentCategory() {
        Category c = getCategoryObject();
        int parentId = 9999999;
        c.setParentId(parentId);

        ValidatableResponse response = post(true, true, CategoryPaths.add.value, JsonUtil.objectToJson_withoutNulls(c), 404);
        response
                .body("status", equalTo("Error"))
                .body("message", equalTo("Error while building category directory path. Parent directory ID: " + parentId  + " does not exist."));
    }

    @Test
    void addCategory_requiredFields() {
        Category c = new Category();
        c.setOrderBy("title ASC");
        c.setZip("Category.zip");

        ValidatableResponse response = post(true, true, CategoryPaths.add.value, JsonUtil.objectToJson_withoutNulls(c), 400);
        response
                .body("status", equalTo("Error"))
                .body("message.categoryName", equalTo("Please provide a category name"))
                .body("message.categoryFolder", equalTo("Please provide a category folder"))
                .body("message.parentId", equalTo("Please provide parent id"));
    }

    @Test
    void addCategory_fieldsMaxSize() {
        Category c = getCategoryObject();
        c.setCategoryName(faker.lorem().fixedString(101));
        c.setCategoryFolder(faker.lorem().fixedString(101));

        ValidatableResponse response = post(true, true, CategoryPaths.add.value, JsonUtil.objectToJson_withoutNulls(c), 400);
        response
                .body("status", equalTo("Error"))
                .body("message.categoryName", equalTo("size must be between 1 and 100"))
                .body("message.categoryFolder", equalTo("size must be between 1 and 100"));
    }

    @Test
    void addCategory_ignoreUnknown() {
        Category c = getCategoryObject();
        String jsonBody = JsonUtil.objectToJson_withoutNulls(c);
        jsonBody = jsonBody.replace("}", ",\"blabla\":\"blabla\"}");
        ValidatableResponse r = createCategory(jsonBody);
    }

    @Test
    void addHiddenCategory() {
        Category c = getCategoryObject();
        c.setHidden(true);
        ValidatableResponse response = createCategory(c);
        response.body("hidden", equalTo(true));
    }

    @Test
    void deleteCategory() {
        Category c = getCategoryObject();
        ValidatableResponse response = createCategory(c);
        int createdCatId = JsonUtil.getIntFromJsonResponse(response, "$.id");
        response = deleteCategory(createdCatId);
        response
                .body("status", equalTo("Deleted"))
                .body("id", equalTo(createdCatId));
    }

    @Test
    void updateCategory() {
        // add category
        Category c = getCategoryObject();
        ValidatableResponse response = createCategory(c);
        verifyCategoryResponse(response, c);
        int createdCatId = JsonUtil.getIntFromJsonResponse(response, "$.id");

        String random = faker.number().digits(10);
        String categoryName = "Test_Категория-" + random;
        c.setCategoryFolder("Test_Category-" + random);
        c.setCategoryName(categoryName);
        c.setParentId(3);
        c.setOrderBy("filename ASC");
        c.setZip(categoryName + ".zip");

        // update category
        ValidatableResponse updateResponse = updateCategory(createdCatId, c);
        verifyCategoryResponse(updateResponse, c);
    }

    @Test
    void updateCategory_requiredFields() {
        Category c = new Category();
        c.setOrderBy("title ASC");
        c.setZip("Category.zip");

        ValidatableResponse response = put(true, CategoryPaths.prefixWithSlash.value + 111, JsonUtil.objectToJson_withoutNulls(c), 400);
        response
                .body("status", equalTo("Error"))
                .body("message.categoryName", equalTo("Please provide a category name"))
                .body("message.categoryFolder", equalTo("Please provide a category folder"))
                .body("message.parentId", equalTo("Please provide parent id"));
    }

    @Test
    void updateNonExistingCategory() {
        int nonExistingCategory = 999999;
        ValidatableResponse response = put(true, CategoryPaths.prefixWithSlash.value + nonExistingCategory, JsonUtil.objectToJson_withoutNulls(getCategoryObject()), 404);
        response
                .body("status", equalTo("Error"))
                .body("message", equalTo("Could not find record with id " + nonExistingCategory));
    }

    @Test
    void addCategory_negative_noToken() {
        ValidatableResponse response = post(false, true, CategoryPaths.add.value, JsonUtil.objectToJson_withoutNulls(getCategoryObject()), 403);
        verifyNoTokenResponse(response, Constants.API_PREFIX + CategoryPaths.add.value);
    }

    @Test
    void deleteCategory_negative_noToken() {
        int createdCatId = JsonUtil.getIntFromJsonResponse(createCategory(getCategoryObject()), "$.id");
        ValidatableResponse response = delete(false, true,CategoryPaths.prefixWithSlash.value + createdCatId, 403);
        verifyNoTokenResponse(response, Constants.API_PREFIX + CategoryPaths.prefixWithSlash.value + createdCatId);
    }

    @Test
    void updateCategory_negative_noToken() {
        int createdCatId = JsonUtil.getIntFromJsonResponse(createCategory(getCategoryObject()), "$.id");
        ValidatableResponse response = put(false, CategoryPaths.prefixWithSlash.value + createdCatId, JsonUtil.objectToJson_withoutNulls(getCategoryObject()), 403);
        verifyNoTokenResponse(response, Constants.API_PREFIX + CategoryPaths.prefixWithSlash.value + createdCatId);
    }

    @Test
    void addCategory_cyrillicFolderName() {
        // TODO:
    }

    @Test
    void categoryBreadcrumbs() {
        ValidatableResponse response = get(false, null, CategoryPaths.breadcrumbsWithSlash.value + 28, 200);
        response
                .body("name", hasItems("Главная", "Проповеди", "Денис Самарин", "Послание к Филимону"));
    }

    @Test
    void categoryBreadcrumbs_negative() {
        int id = 999999;
        ValidatableResponse response = get(false, null, CategoryPaths.breadcrumbsWithSlash.value + id, 404);
        response
                .body("message", equalTo("Unable to find category with ID: " + id))
                .body("status", equalTo("Error" ));
    }

}
