package com.sen4ik.cfaapi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sen4ik.cfaapi.entities.Category;
import com.sen4ik.cfaapi.entities.FileEntity;
import com.sen4ik.cfaapi.base.Constants;
import com.sen4ik.cfaapi.repositories.CategoriesRepository;
import com.sen4ik.cfaapi.repositories.FileRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Controller
@RequestMapping(path = Constants.API_PREFIX)
@Api(tags = "Everything", description = " ")
public class EverythingController {

    @Autowired
    private CategoriesRepository categoriesRepository;

    @Autowired
    private FileRepository fileRepository;

    ObjectMapper objectMapper;
    List<Category> categories;
    List<FileEntity> files;
    List<Category> rootCategories;
    Map<Integer, List<FileEntity>> filesMap;
    Map<Integer, List<Category>> categoriesMap;

    // I dont remember why i built it, looks like i dont need it anymore
    @GetMapping(path="/everything")
    @ApiOperation(value = "Get all categories and files in single payload", response = Iterable.class)
    public ResponseEntity<ObjectNode> getAll() {

        categories = categoriesRepository.findAll();
        files = fileRepository.findAll();
        rootCategories = categoriesRepository.getCategoriesByParentId(0);
        filesMap = files.stream().collect(Collectors.groupingBy(FileEntity::getCategoryId));
        categoriesMap = categories.stream().collect(Collectors.groupingBy(Category::getParentId));

        objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.registerModule(new JavaTimeModule());
        // objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);

        DateFormat myDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        myDateFormat.setTimeZone(TimeZone.getDefault());
        objectMapper.setDateFormat(myDateFormat);

        ObjectNode rootArrayNode = objectMapper.createObjectNode();
        buildEverythingJson(objectMapper, rootCategories, rootArrayNode);

        return ResponseEntity.status(HttpStatus.OK)
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(rootArrayNode);
    }

    private ObjectNode buildEverythingJson(ObjectMapper objectMapper, List<Category> categories, ObjectNode rootArrayNode){

        for(Category category : categories){

            ObjectNode currentCategory = objectMapper.convertValue(category, ObjectNode.class);

            int categoryId = category.getId();
            List<FileEntity> files = filesMap.get(categoryId);
            List<Category> subCategories = categoriesMap.get(categoryId);

            ArrayNode filesArrayNode = objectMapper.createArrayNode();
            if(files != null){
                for(FileEntity file : files){
                    // if(file.getHidden() != true){
                        ObjectNode fileObjectNode = objectMapper.convertValue(file, ObjectNode.class);
                        filesArrayNode.add(fileObjectNode);
                    // }
                }
            }
            currentCategory.put("files", filesArrayNode);

            if(subCategories != null){

                ObjectNode traversedCategory = buildEverythingJson(objectMapper, subCategories, currentCategory);

                // at this point looks like traversedCategory and currentCategory are equal
                // we need to add traversedCategory/currentCategory to rootArrayNode.subcategories
                ArrayNode subCategoriesElement = ((ArrayNode) rootArrayNode.get("subCategories"));
                if(subCategoriesElement != null){
                    subCategoriesElement.add(traversedCategory);
                }
                else{
                    addObjectNodeToField(rootArrayNode, "everything", traversedCategory);
                }
            }
            else{
                ArrayNode empty = objectMapper.createArrayNode();
                currentCategory.put("subCategories", empty);

                if(category.getParentId() == 0){
                    addObjectNodeToField(rootArrayNode, "everything", currentCategory);
                }
                else{
                    addObjectNodeToField(rootArrayNode, "subCategories", currentCategory);
                }
            }
        }

        return rootArrayNode;
    }

    private void addObjectNodeToField(ObjectNode addTo, String fieldToAddTo, ObjectNode objectNodeToAdd){
        ArrayNode elem = ((ArrayNode) addTo.get(fieldToAddTo));
        if(elem == null){
            addTo.putArray(fieldToAddTo).add(objectNodeToAdd);
        }
        else{
            elem.add(objectNodeToAdd);
        }
    }

}
