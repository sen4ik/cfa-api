package com.sen4ik.cfaapi.base;

import java.util.Arrays;
import java.util.List;

public class Constants {

    public static final String API_PREFIX       = "/api/v1";
    public static final String SCHEMA           = "cfa";
    public static final String TEST_DATA_DIR    = "./test_data/";
    public static final String APP_PROP         = "./src/main/resources/application.properties";
    public static List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "audio/mpeg", "audio/mp3"
    );

}
