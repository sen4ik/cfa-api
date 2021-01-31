package com.sen4ik.cfaapi.utilities;

import com.sen4ik.cfaapi.entities.Category;
import com.sen4ik.cfaapi.repositories.CategoriesRepository;
import com.sen4ik.cfaapi.exceptions.RecordNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.sen4ik.utils.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.tritonus.share.sampled.file.TAudioFileFormat;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Component
@Slf4j
public class FileUtility {

    private String directoryPath = "";

    @Autowired
    private CategoriesRepository categoriesRepository;

    @Autowired
    private Environment env;

    /*
    @PostConstruct
    public void init() {
        System.out.println("Inside init method);
    }

    @PreDestroy
    public void destroy() {
        System.out.println("Inside destroy method");
    }
    */

    private String getCategoryFolderPathRecursively(int parentCategoryId) throws RecordNotFoundException {
        Optional<Category> category = categoriesRepository.findById(parentCategoryId);
        if (!category.isPresent()){
            // something is bad with parent directory, it does not exists probably
            throw new RecordNotFoundException("Error while building category directory path. Parent directory ID: " + parentCategoryId + " does not exist.");
        }

        int currentCategoryParentId = category.get().getParentId();
        String currentCategoryFolder = category.get().getCategoryFolder();

        if(currentCategoryParentId != 0){
            getCategoryFolderPathRecursively(currentCategoryParentId);
        }

        directoryPath = directoryPath + currentCategoryFolder + "/";
        log.info("directoryPath: " + directoryPath);
        return directoryPath;
    }

    public String getCategoryFolderPath(int categoryId) {
        directoryPath = "";
        String parentDirectoriesPath = "";
        if(categoryId != 0){
            parentDirectoriesPath = getCategoryFolderPathRecursively(categoryId);
        }
        String builtDirectoryPath = getUploadDir() + parentDirectoriesPath;
        return builtDirectoryPath;
    }

    public void deleteFileFromUploadLocation(int categoryId, String filename){
        try {
            String path = getCategoryFolderPath(categoryId) + filename;
            FileUtil.deleteFile(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getUploadDir(){
        log.info("CALLED: getUploadDir()");
        String UPLOAD_DIR = env.getProperty("file.upload-dir");
        log.info("UPLOAD_DIR: " + UPLOAD_DIR);
        return UPLOAD_DIR;
    }

    public int getDurationInSeconds(File file) throws UnsupportedAudioFileException, IOException {
        AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(file);
        if (fileFormat instanceof TAudioFileFormat) {
            Map<?, ?> properties = ((TAudioFileFormat) fileFormat).properties();
            String key = "duration";
            Long microseconds = (Long) properties.get(key);
            int mili = (int) (microseconds / 1000);
            int sec = mili / 1000;
            log.info("sec = " + sec);

            // int sec = (mili / 1000) % 60;
            // int min = (mili / 1000) / 60;
            // log.info("time = " + min + ":" + sec);

            return sec;
        } else {
            throw new UnsupportedAudioFileException();
        }
    }

}
