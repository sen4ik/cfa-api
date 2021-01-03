package com.sen4ik.cfaapi.utilities;

import com.sen4ik.cfaapi.entities.Category;
import com.sen4ik.cfaapi.repositories.CategoriesRepository;
import com.sen4ik.cfaapi.exceptions.RecordNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.io.TikaInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.tritonus.share.sampled.file.TAudioFileFormat;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Component
@Slf4j
public class FileUtility {

    private String directoryPath = "";
    private String operatingSystem = System.getProperty("os.name").toLowerCase();

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

    public void deleteFile(String path){
        log.info("CALLED: deleteFile()");
        try {
            File file = new File(path);
            if(file.exists()){
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Boolean doesFileExists(String path){
        log.info("CALLED: doesFileExists()");
        try {
            File file = new File(path);
            return file.exists();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void deleteFileFromUploadLocation(int categoryId, String filename){
        try {
            String path = getCategoryFolderPath(categoryId) + filename;
            deleteFile(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteFilesAndDirsWithPrefix(File[] files, String prefix) {
        log.info("CALLED: deleteFilesAndDirsWithPrefix()");
        for (File file : files) {
            if (file.isDirectory()) {
                deleteFilesAndDirsWithPrefix(file.listFiles(), prefix);
                if(file.getName().startsWith(prefix)){
                    file.delete();
                }
            } else {
                if(file.getName().startsWith(prefix)){
                    file.delete();
                }
            }
        }
    }

    public void deleteFilesAndDirsWithPrefix(File[] files, List<String> prefixes) {
        log.info("CALLED: deleteFilesAndDirsWithPrefix()");
        for (File file : files) {
            if (file.isDirectory()) {
                deleteFilesAndDirsWithPrefix(file.listFiles(), prefixes);
            }

            String fileName = file.getName();
            boolean res = prefixes.stream().anyMatch(fileName::startsWith);
            if(res){
                log.info(file.getAbsolutePath());
                file.delete();
            }
        }
    }

    public void deleteFilesAndDirsFromPath(File directoryToBeDeleted) {
        log.info("CALLED: deleteFilesAndDirsFromPath()");

        File[] files = directoryToBeDeleted.listFiles();

        for (File file : files) {
            if (file.isDirectory()) {
                deleteFilesAndDirsFromPath(file);
            }
            log.info(file.getAbsolutePath());
            if(!file.getName().equals(".gitignore")){
                file.delete();
            }
        }
    }

    public void deleteFilesAndDirsFromPath(String path) {
        deleteFilesAndDirsFromPath(new File(path));
    }

    public File getCopyOfFile(String filePathToCopy, String copyToPath) throws IOException {
        Path fileToCopy = Paths.get(filePathToCopy);
        Path copied = Paths.get(copyToPath);
        return getCopyOfFile(fileToCopy, copied);
    }

    public File getCopyOfFile(Path fileToCopy, Path copied) throws IOException {
        log.info("CALLED: getCopyOfFile()");
        Files.copy(fileToCopy, copied, StandardCopyOption.REPLACE_EXISTING);
        return copied.toFile();
    }

    private File createFile(final String filename) throws IOException {
        log.info("CALLED: createFile(" + filename + ")");
        File file = new File(filename);
        file.createNewFile();
        return file;
    }


    public File createFileOfGivenSize(final String filename, int sizeInMegabytes) throws IOException {
        log.info("CALLED: createFileOfGivenSize()");
        File file = createFile(filename);
        try (FileOutputStream out = new FileOutputStream(file)) {
            byte[] bytes = new byte[1000000 * sizeInMegabytes];
            new SecureRandom().nextBytes(bytes);
            out.write(bytes);
        }
        return file;
    }

    public String getMimeTypeWithTika(File file){
        log.info("CALLED: getMimeTypeWithTika(" + file.getAbsolutePath() + ")");
        Tika tika = new Tika();
        try {
            TikaInputStream tis = TikaInputStream.get(file);
            String mimeType = tika.detect(tis);
            log.info("Mime for file " + file.getName() + " is " + mimeType);
            return mimeType;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getUploadDir(){
        log.info("CALLED: getUploadDir()");
        String UPLOAD_DIR = env.getProperty("file.upload-dir");
        log.info("UPLOAD_DIR: " + UPLOAD_DIR);
        return UPLOAD_DIR;
    }

    private String getAbsoluteFilePath(String relativeFilePath) {
        log.info("CALLED: getFilePath(" + relativeFilePath + ")");
        File file = new File(relativeFilePath);
        if (file.exists()) {
            log.info("Absolute file path for " + relativeFilePath + " is: " + file.getAbsolutePath());
            return file.getAbsolutePath() + "/";
        } else {
            throw new IllegalArgumentException(
                    "Invalid relative file path. File does not exist: " + file.getAbsolutePath());
        }
    }

    public boolean isWindows() {
        log.info("CALLED: isWindows()");
        boolean result = (operatingSystem.indexOf("win") >= 0);
        log.info("isWindows: " + result);
        return result;
    }

    public boolean isMac() {
        log.info("CALLED: isMac()");
        boolean result = (operatingSystem.indexOf("mac") >= 0);
        log.info("isMac: " + result);
        return result;
    }

    public boolean isUnix() {
        log.info("CALLED: isUnix()");
        boolean result = (operatingSystem.indexOf("nix") >= 0 || operatingSystem.indexOf("nux") >= 0 || operatingSystem.indexOf("aix") > 0 );
        log.info("isUnix: " + result);
        return result;
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
