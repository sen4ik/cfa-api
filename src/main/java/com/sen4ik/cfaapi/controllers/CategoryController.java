package com.sen4ik.cfaapi.controllers;

import com.sen4ik.cfaapi.base.Constants;
import com.sen4ik.cfaapi.entities.Category;
import com.sen4ik.cfaapi.base.ResponseHelper;
import com.sen4ik.cfaapi.exceptions.BadRequestException;
import com.sen4ik.cfaapi.exceptions.RecordNotFoundException;
import com.sen4ik.cfaapi.repositories.CategoriesRepository;
import com.sen4ik.cfaapi.utilities.FileUtility;
import com.sen4ik.cfaapi.utilities.ObjectUtility;
import com.sen4ik.cfaapi.utilities.UserUtility;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.ResponseEntity.ok;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Controller
@RequestMapping(path = Constants.API_PREFIX + "/category")
@Slf4j
@Api(tags = "Categories", description = " ")
public class CategoryController {

    @Autowired
    private CategoriesRepository categoriesRepository;

    @Autowired
    FileUtility fileUtility;

    @GetMapping(path="/all")
    @ApiOperation(value = "Get all categories", response = Iterable.class)
    public @ResponseBody
    Iterable<Category> getAll() {
        return categoriesRepository.findAll();
    }

    @GetMapping(path="/{id}")
    @ApiOperation(value = "Get single category")
    public @ResponseBody
    ResponseEntity<Category> getOne(@PathVariable("id") @Min(1) int id) {
        return ok(categoriesRepository.findById(id).orElseThrow(() -> new RecordNotFoundException(id)));
    }

    @GetMapping(path="/parent/{id}")
    @ApiOperation(value = "Get categories with specified parent id")
    public @ResponseBody
    List<Category> getCategoriesByParentId(@PathVariable("id") int id) {
        return categoriesRepository.getCategoriesByParentId(id);
    }

    @GetMapping(path="/breadcrumbs/{id}")
    @ApiOperation(value = "Get breadcrumbs for category")
    public @ResponseBody void getBreadcrumbsForCategory(@PathVariable("id") int id, HttpServletResponse response) throws IOException {
        JSONArray jsonArr = new JSONArray();

        JSONObject root = new JSONObject();
        root.put("id", 0);
        root.put("name", "Главная");
        jsonArr.put(root);

        if(id > 0){
            getCategoryBreadcrumbsRecursively(id, jsonArr);
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().println(jsonArr);
    }

    private JSONArray getCategoryBreadcrumbsRecursively(int categoryId, JSONArray jsonArr) throws RecordNotFoundException {

        Optional<Category> category = categoriesRepository.findById(categoryId);
        if (!category.isPresent()){
            throw new RecordNotFoundException("Unable to find category with ID: " + categoryId);
        }

        String categoryName = category.get().getCategoryName();
        int currentCategoryParentId = category.get().getParentId();

        if(currentCategoryParentId != 0){
            getCategoryBreadcrumbsRecursively(currentCategoryParentId, jsonArr);
        }

        JSONObject catJsonObj = new JSONObject();
        catJsonObj.put("id", categoryId);
        catJsonObj.put("name", categoryName);

        jsonArr.put(catJsonObj);

        return jsonArr;
    }

    @GetMapping(path="/findByCategoryName")
    @ApiOperation(value = "Find category by name")
    public @ResponseBody
    List<Category> getCategoryByName(@NotBlank @Size(max = 100) @RequestParam(name = "categoryName") String categoryName) {
        return categoriesRepository.findByCategoryName(categoryName);
    }

    @PostMapping(path="/add", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Add category")
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody Category addOne(@Valid @RequestBody Category category/*, @RequestHeader (name = "Authorization") String token*/) throws Exception {

        // create category directory
        String newCategoryFolder = category.getCategoryFolder();
        log.info(CategoryController.class.getSimpleName() + ": newCategoryFolder: " + newCategoryFolder);

        String categoryFolderPath = fileUtility.getCategoryFolderPath(category.getParentId()) + newCategoryFolder + "/";

        // check if directory already exists
        File directory = new File(categoryFolderPath);
        if (directory.exists()){
            throw new BadRequestException(newCategoryFolder + " category folder already exists in the file system");
        }
        else{
            directory.mkdir();
            log.info(CategoryController.class.getSimpleName() + ": directory: " + directory.getAbsolutePath());
            // If you require it to make the entire directory path including parents, use directory.mkdirs(); here instead.
        }

        category.setAddedBy(UserUtility.getCurrentlyLoggedInUserId());
        categoriesRepository.save(category);

        // TODO: if save failed, we need to delete the directory we created above

        return category;
    }

    @DeleteMapping(path="/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Delete category by id")
    public @ResponseBody ResponseEntity<String> deleteOne(@PathVariable @Min(1) int id) {
        try {
            categoriesRepository.deleteById(id);
            return ResponseHelper.deleteSuccess(id);
        } catch (Exception e) {
            return ResponseHelper.deleteFailed(id, e);
        }
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "Update category")
    public ResponseEntity<Object> update(@Valid @RequestBody Category category, @PathVariable @Min(1) int id) {
        Optional<Category> categoryOptional = categoriesRepository.findById(id);
        if (!categoryOptional.isPresent()){
            // return ResponseEntity.notFound().build();
            // return ResponseHelper.notFound(id);
            throw new RecordNotFoundException(id);
        }
        // category.setId(id);

        BeanUtils.copyProperties(category, categoryOptional.get(), ObjectUtility.getNullPropertyNames(category));

        categoriesRepository.save(category);
        // categoriesRepository.flush();

        // quick and dirty hack to return json without null fields
        // String json = new Gson().toJson(category);

        return ResponseEntity.status(HttpStatus.OK)
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(categoryOptional.get());
    }

}
