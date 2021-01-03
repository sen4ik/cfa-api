package com.sen4ik.cfaapi.controllers;

import com.sen4ik.cfaapi.base.Constants;
import com.sen4ik.cfaapi.base.ResponseHelper;
import com.sen4ik.cfaapi.entities.FileEntity;
import com.sen4ik.cfaapi.exceptions.BadRequestException;
import com.sen4ik.cfaapi.exceptions.RecordNotFoundException;
import com.sen4ik.cfaapi.repositories.FileRepository;
import com.sen4ik.cfaapi.utilities.FileUtility;
import com.sen4ik.cfaapi.utilities.ObjectUtility;
import com.sen4ik.cfaapi.utilities.UserUtility;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Controller
@Slf4j
@RequestMapping(path = Constants.API_PREFIX + "/file")
@Api(tags = "Files", description = " ")
public class FileController {

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    FileUtility fileUtility;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @GetMapping(path="/all")
    @ApiOperation(value = "Get all files")
    public @ResponseBody Iterable<FileEntity> getAll() {
        return fileRepository.findAll();
    }

    @GetMapping(path="/{id}")
    @ApiOperation(value = "Get file by id")
    public @ResponseBody
    FileEntity getOne(@PathVariable("id") @Min(1) int id) {
        return fileRepository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException(id));
    }

    @GetMapping(path="/findByFileName")
    @ApiOperation(value = "Find file by file name")
    public @ResponseBody
    List<FileEntity> getFileByName(@NotBlank @Size(max = 100) @RequestParam(name = "fileName") String fileName) {
        return fileRepository.findByFileName(fileName);
    }

    @GetMapping(path="/findByFileTitle")
    @ApiOperation(value = "Find file by file title")
    public @ResponseBody
    List<FileEntity> getFileByTitle(@NotBlank @Size(max = 100) @RequestParam(name = "fileTitle") String fileTitle) {
        return fileRepository.findByFileTitle(fileTitle);
    }

    @GetMapping(path="/category/{id}")
    @ApiOperation(value = "Get all files for specified category")
    public @ResponseBody
    List<FileEntity> getFilesForCategory(@PathVariable("id") int id) {
        return fileRepository.getFilesByCategoryId(id);
    }

    /*
    // VERSION 1
    @GetMapping(path="/download/{id}")
    @ApiOperation(value = "Download file by id")
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable("id") int id) throws FileNotFoundException {

        Optional<FileEntity> fileOptional = fileRepository.findById(id);
        if (!fileOptional.isPresent()){
            throw new RecordNotFoundException(id);
        }

        String filePath = fileUtility.getCategoryFolderPath(fileOptional.get().getCategoryId()) + fileOptional.get().getFileName();
        File file = new File(filePath);

        HttpHeaders responseHeaders = new HttpHeaders();
        // responseHeaders.setContentType(MediaType.valueOf("audio/mpeg"));
        // responseHeaders.setContentType(MediaType.valueOf("application/zip"));
        responseHeaders.set("Charset", "UTF-8");
        responseHeaders.setContentType(MediaType.valueOf("application/force-download"));
        responseHeaders.setContentLength(file.length());
        responseHeaders.setContentDispositionFormData("attachment", file.getName());

        InputStreamResource isr = new InputStreamResource(new FileInputStream(file));
        return new ResponseEntity<>(isr, responseHeaders, HttpStatus.OK);
    }
    */

    // VERSION 2
    // This works better because with version 1 seek was not working
    // https://www.callicoder.com/spring-boot-file-upload-download-rest-api-example/
    @GetMapping(path="/download/{id}")
    @ApiOperation(value = "Download file by id")
    public ResponseEntity<Resource> downloadFile(@PathVariable("id") int id, HttpServletRequest request) throws FileNotFoundException, MalformedURLException {

        Optional<FileEntity> fileOptional = fileRepository.findById(id);
        if (!fileOptional.isPresent()){
            throw new RecordNotFoundException(id);
        }

        String fpath = fileUtility.getCategoryFolderPath(fileOptional.get().getCategoryId()) + fileOptional.get().getFileName();
        Path fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path filePath = fileStorageLocation.resolve(fpath).normalize();
        Resource resource = new UrlResource(filePath.toUri());

        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            log.info("Could not determine file type.");
        }

        if(contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileOptional.get().getFileName() + "\"")
                .body(resource);
    }

    // // https://www.mkyong.com/spring-boot/spring-boot-file-upload-example-ajax-and-rest/
    @PostMapping(path = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Add file")
    public @ResponseBody ResponseEntity<?> addOne(
            @RequestParam("file") MultipartFile file,
            @RequestParam("fileTitle") String fileTitle,
            @RequestParam("categoryId") Integer categoryId) throws Exception {

        if(!isContentTypeOk(file)){
            return badContentType();
        }
        Path savedFilePath = null;
        try {
            savedFilePath = saveUploadedFiles(file, categoryId);
        } catch (IOException e) {
            return new ResponseEntity<>(
                    ResponseHelper.getResponseObjectAsString("Error", e.getMessage()),
                    HttpStatus.BAD_REQUEST
            );
        }

        FileEntity fileToSave = new FileEntity();

        if(savedFilePath != null){
            int seconds = fileUtility.getDurationInSeconds(
                    new File(
                            savedFilePath.toAbsolutePath().toString()
                    )
            );
            fileToSave.setLengthInSeconds(seconds);
        }

        fileToSave.setAddedBy(UserUtility.getCurrentlyLoggedInUserId());
        fileToSave.setFileTitle(fileTitle);
        fileToSave.setFileName(file.getOriginalFilename());
        fileToSave.setCategoryId(categoryId);
        fileToSave.setFileSizeBytes(file.getSize());

        fileRepository.save(fileToSave);

        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(fileToSave);
    }

    private boolean isContentTypeOk(MultipartFile file){
        // check the content type
        String contentType = file.getContentType();
        log.info("contentType: " + contentType);
        if(!Constants.ALLOWED_MIME_TYPES.contains(contentType)){
            return false;
        }

        // recheck the content type with apache tika, because
        // i found a way to send shell script with audio/mpeg mime using rest-assured.
        Tika tika = new Tika();
        try {
            String mimeType = tika.detect(file.getBytes());
            log.info("mimeType: " + mimeType);
            if(!Constants.ALLOWED_MIME_TYPES.contains(mimeType)){
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    private ResponseEntity<String> badContentType(){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(
                        ResponseHelper.getResponseObjectAsString(
                                "Error",
                                "Only files with following content type are allowed: " + Constants.ALLOWED_MIME_TYPES.toString()
                        )
                );
    }

    @DeleteMapping(path="/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Delete file by id")
    public @ResponseBody ResponseEntity<String> deleteOne(@PathVariable @Min(1) int id) {

        try {
            Optional<FileEntity> fileOptional = fileRepository.findById(id);
            if (!fileOptional.isPresent()){
                throw new RecordNotFoundException(id);
            }

            // delete file from file system
            String filePath = fileUtility.getCategoryFolderPath(fileOptional.get().getCategoryId()) + fileOptional.get().getFileName();
            fileUtility.deleteFile(filePath);

            fileRepository.deleteById(id);

            return ResponseHelper.deleteSuccess(id);
        } catch (Exception e) {
            return ResponseHelper.deleteFailed(id, e);
        }
    }

    @PutMapping("/updateFileInfo/{id}")
    @ApiOperation(value = "Update file info by id")
    public ResponseEntity<Object> updateFileInfo(@Valid @RequestBody FileEntity file, @PathVariable @Min(1) int id) {
        Optional<FileEntity> fileOptional = fileRepository.findById(id);
        if (!fileOptional.isPresent()){
            throw new RecordNotFoundException(id);
        }

        BeanUtils.copyProperties(file, fileOptional.get(), ObjectUtility.getNullPropertyNames(file));
        fileRepository.save(file);

        return ResponseEntity.status(HttpStatus.OK)
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(fileOptional.get());
    }

    @PutMapping(path = "/replaceFile/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Replace file")
    public @ResponseBody ResponseEntity<?> replaceFile(
            @RequestParam("file") MultipartFile file,
            @PathVariable @Min(1) int id) throws Exception {

        // check content type
        if(!isContentTypeOk(file)){
            return badContentType();
        }

        // attempt to find a file using id
        Optional<FileEntity> fileOptional = fileRepository.findById(id);
        if (!fileOptional.isPresent()){
            throw new RecordNotFoundException(id);
        }

        // delete old file from file system
        String filePath = fileUtility.getCategoryFolderPath(fileOptional.get().getCategoryId()) + fileOptional.get().getFileName();
        fileUtility.deleteFile(filePath);

        // store new file in the file system
        try {
            saveUploadedFiles(file, fileOptional.get().getCategoryId());
        } catch (IOException e) {
            return new ResponseEntity<>(
                    ResponseHelper.getResponseObjectAsString("Error", e.getMessage()),
                    HttpStatus.BAD_REQUEST
            );
        }

        // update filename and file size in database
        fileOptional.get().setFileName(file.getOriginalFilename());
        fileOptional.get().setFileSizeBytes(file.getSize());
        fileRepository.save(fileOptional.get());

        return ResponseEntity.status(HttpStatus.OK)
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(fileOptional.get());
    }

    private Path saveUploadedFiles(MultipartFile file, Integer categoryId) throws Exception {

        String fileName = file.getOriginalFilename();
        byte[] bytes = file.getBytes();

        // get path based on category file belongs to
        String uploadPath = fileUtility.getCategoryFolderPath(categoryId) + fileName; // throws Exception
        Path path = Paths.get(uploadPath);

        // check if file already exists
        if(Files.exists(path)){
            throw new BadRequestException(fileName + " file already exists for category " + categoryId);
        }
        else{
            return Files.write(path, bytes);
        }
    }

    // TODO: endpoints to set downloaded and listened

}
