package com.sen4ik.cfaapi.helpers;

import org.apache.tika.Tika;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileTests {

    void fe() throws IOException {
        String sampleFile = "./test_data/sample.mp3";
        File f = new File(sampleFile);
        System.out.println(f.exists());

        byte[] fb = Files.readAllBytes(Paths.get(sampleFile));

        Tika tika = new Tika();
        String mimeType = tika.detect(fb);
        assertTrue(mimeType.equals("audio/mpeg"));
    }

    void t(){
        List<String> prefixes = Arrays.asList("sample-", "файлик", "Test_");
        File[] files = new File("./test_data/").listFiles();

        for (File file : files) {
            String fileName = file.getName();
            boolean res = prefixes.stream().anyMatch(fileName::startsWith);
            System.out.println(res);
        }
    }

}
