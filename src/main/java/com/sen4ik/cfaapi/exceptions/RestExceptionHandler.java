package com.sen4ik.cfaapi.exceptions;

import com.sen4ik.cfaapi.base.ResponseHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUploadBase;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.ResponseEntity.*;

@RestControllerAdvice
@Slf4j
public class RestExceptionHandler {

    @ExceptionHandler(Exception.class)
    ResponseEntity sysError(Exception e){
        return ResponseHelper.error(e, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = {RecordNotFoundException.class})
    public ResponseEntity recordNotFound(WebRequest request, RecordNotFoundException e) {
        return ResponseHelper.error(e, NOT_FOUND);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity badCredentials(BadCredentialsException e) {
        return ResponseHelper.error(e, UNAUTHORIZED);
    }

    @ExceptionHandler(InvalidJwtAuthenticationException.class)
    public ResponseEntity invalidJwtAuthentication(InvalidJwtAuthenticationException ex) {
        return status(UNAUTHORIZED).build();
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity noHandlerFound(NoHandlerFoundException e) {
        return ResponseHelper.error(e, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    ResponseEntity handleMissingServletRequestPartException(Exception e){
        return ResponseHelper.error(e, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    ResponseEntity handleMissingServletRequestParameterException(Exception e){
        return ResponseHelper.error(e, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadRequestException.class)
    ResponseEntity handleBadRequestException(Exception e){
        return getResponseEntity(e, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        JSONObject entity = new JSONObject();
        entity.put("status", "Error");
        entity.put("message", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(entity.toString());
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<?> handleMultipartException(HttpServletRequest request, Throwable e) {

        Throwable cause = e.getCause();

        if (cause.getCause() instanceof FileUploadBase.FileSizeLimitExceededException) {
            return handleFileSizeLimitExceededException(request, cause.getCause());
        }

        if (cause instanceof IllegalStateException) {
            return handleIllegalStateException(request, cause);
        }

        return getResponseEntity(e, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleIllegalStateException(HttpServletRequest request, Throwable e) {
        return getResponseEntity(e, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(FileUploadBase.FileSizeLimitExceededException.class)
    public ResponseEntity<?> handleFileSizeLimitExceededException(HttpServletRequest request, Throwable e) {
        return getResponseEntity(e, HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<String> getResponseEntity(Throwable e, HttpStatus httpStatus){
        return ResponseEntity.status(httpStatus)
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(
                        ResponseHelper.getResponseObjectAsString("Error", e.getMessage())
                );
    }

    private HttpStatus getStatus(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        if (statusCode == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return HttpStatus.valueOf(statusCode);
    }
}
