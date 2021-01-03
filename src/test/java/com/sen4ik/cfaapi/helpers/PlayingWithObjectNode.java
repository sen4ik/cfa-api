package com.sen4ik.cfaapi.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.sen4ik.cfaapi.BaseTest;
import com.sen4ik.cfaapi.entities.Category;
import com.sen4ik.cfaapi.entities.FileEntity;
import com.sen4ik.cfaapi.repositories.CategoriesRepository;
import com.sen4ik.cfaapi.repositories.FileRepository;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.fail;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Slf4j
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
class PlayingWithObjectNode extends BaseTest {

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

	@BeforeAll
	@Disabled
	public void ba(){
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
	}

	@Test
	@Disabled
	void gsonDateAndTimeTypeAdapters(){
		Optional<Category> categoryOptional = categoriesRepository.findById(4);
		if(!categoryOptional.isPresent()){
			fail();
		}
		Category category = categoryOptional.get();
		// Gson gsonOne = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		Gson gsonOne = new GsonBuilder()
				.setPrettyPrinting()
				.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter().nullSafe())
				.create();
		String json = gsonOne.toJson(category);
		System.out.println(json);

		Gson gsonTwo = new GsonBuilder()
				.setPrettyPrinting()
				.registerTypeAdapter(LocalDateTime.class, new LocalDateAdapter())
				.create();
		String jsonTwo = gsonTwo.toJson(category);
		System.out.println(jsonTwo);
	}

	private static final class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
		@Override
		public void write(final JsonWriter jsonWriter, final LocalDateTime localDateTime ) throws IOException {
			jsonWriter.value(localDateTime.toString());
		}

		@Override
		public LocalDateTime read( final JsonReader jsonReader ) throws IOException {
			return LocalDateTime.parse(jsonReader.nextString());
		}
	}

	private static final class LocalDateAdapter implements JsonSerializer<LocalDateTime> {
		public JsonElement serialize(LocalDateTime date, Type typeOfSrc, JsonSerializationContext context) {
			return new JsonPrimitive(date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
		}
	}

	@Test
	// @Disabled
	void playingWithJasonObjects(){

		JSONObject fileOne = new JSONObject();
		fileOne.put("id", 1);
		fileOne.put("fileName", "testFile1");

		JSONObject fileTwo = new JSONObject();
		fileTwo.put("id", 2);
		fileTwo.put("fileName", "testFile2");

		JSONArray filesArr = new JSONArray();
		filesArr.put(fileOne);
		filesArr.put(fileTwo);

		JSONObject categoryTen = new JSONObject();
		categoryTen.put("id", 10);
		categoryTen.put("categoryName", "testCat10");
		categoryTen.put("categoryFolder", "testFolder10");
		categoryTen.put("childrenCategories", (Collection<?>) null);
		categoryTen.put("files", filesArr);

		JSONObject categoryTwenty = new JSONObject();
		categoryTwenty.put("id", 20);
		categoryTwenty.put("categoryName", "testCat20");
		categoryTwenty.put("categoryFolder", "testFolder20");
		categoryTwenty.put("childrenCategories", (Collection<?>) null);
		categoryTwenty.put("files", (Collection<?>) null);

		JSONArray catArr = new JSONArray();
		catArr.put(categoryTen);
		catArr.put(categoryTwenty);

		JSONObject fileForCatFour = new JSONObject();
		fileForCatFour.put("id", 87);
		fileForCatFour.put("fileName", "fileName87");

		JSONArray filesArrForCatFour = new JSONArray();
		filesArrForCatFour.put(fileForCatFour);

		JSONObject categoryFour = new JSONObject();
		categoryFour.put("id", 4);
		categoryFour.put("categoryName", "testCat4");
		categoryFour.put("categoryFolder", "testFolder4");
		categoryFour.put("childrenCategories", catArr);
		categoryFour.put("files", filesArrForCatFour);

		System.out.println(categoryFour.toString());
	}

	@Test
	@Disabled
	void playingWithObjNodes(){

		ObjectMapper objectMapper = new ObjectMapper();

		ObjectNode fileOne = objectMapper.createObjectNode();
		fileOne.put("id", 1);
		fileOne.put("fileName", "testFile1");

		ObjectNode fileTwo = objectMapper.createObjectNode();
		fileTwo.put("id", 2);
		fileTwo.put("fileName", "testFile2");

		ObjectNode fileThree = objectMapper.createObjectNode();
		fileThree.put("id", 3);
		fileThree.put("fileName", "testFile3");

		ObjectNode categoryFour = objectMapper.createObjectNode();
		categoryFour.put("id", 4);
		categoryFour.put("categoryName", "testCategory4");
		categoryFour.put("categoryFolder", "testFolder4");
		categoryFour.putArray("files").add(fileOne).add(fileTwo);

		System.out.println(categoryFour.toString());
	}

	@Test
	@Disabled
	void playingWithArrayNodes(){

		ObjectMapper objectMapper = new ObjectMapper();
		ArrayNode rootArrayNode = objectMapper.createArrayNode();

		ObjectNode categoryOne = objectMapper.createObjectNode();
		categoryOne.put("id", 1);
		categoryOne.put("categoryName", "testCategory1");
		categoryOne.put("parentId", 0);

		ObjectNode categoryTwo = objectMapper.createObjectNode();
		categoryTwo.put("id", 2);
		categoryTwo.put("categoryName", "testCategory2");
		categoryTwo.put("parentId", 0);

		ObjectNode subCategoryOne = objectMapper.createObjectNode();
		subCategoryOne.put("id", 11);
		subCategoryOne.put("categoryName", "testCategory11");
		subCategoryOne.put("parentId", 1);

		ObjectNode subCategoryTwo = objectMapper.createObjectNode();
		subCategoryTwo.put("id", 12);
		subCategoryTwo.put("categoryName", "testCategory12");
		subCategoryTwo.put("parentId", 1);

		ArrayNode subCategoriesArrayNode = objectMapper.createArrayNode();
		subCategoriesArrayNode.add(subCategoryOne);

		// categoryOne.put("subCategories", subCategoriesArrayNode);
		categoryOne.putArray("subCategories").add(subCategoryOne);
		ArrayNode elem0 = ((ArrayNode) categoryOne.get("subCategories"));
		elem0.add(subCategoryTwo);

		rootArrayNode.add(categoryOne).add(categoryTwo);

		System.out.println(rootArrayNode);
	}

	@Test
	@Disabled
	void emptyArray(){
		ObjectNode rootArrayNode = objectMapper.createObjectNode();

		ObjectNode categoryOne = objectMapper.createObjectNode();
		categoryOne.put("id", 1);
		categoryOne.put("categoryName", "testCategory1");
		categoryOne.put("parentId", 0);

		ArrayNode arrayNode = objectMapper.createArrayNode();
		arrayNode.add(categoryOne);
		System.out.println(arrayNode);

		rootArrayNode.putArray("everything").add(arrayNode);

		System.out.println(rootArrayNode);
	}

}
