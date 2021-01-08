package com.sen4ik.cfaapi.base;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;

public class ResponseHelper {

    public static ResponseEntity<String> deleteSuccess(){
        return ResponseEntity.status( HttpStatus.OK)
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(getResponseObjectAsString(null, "Deleted", null));
    }

    public static ResponseEntity<String> deleteSuccess(int id){
        return new ResponseEntity<>(getResponseObjectAsString(id, "Deleted", null), HttpStatus.OK);
    }

    public static ResponseEntity<String> deleteFailed(int id, Exception e){
        return new ResponseEntity<>(getResponseObjectAsString(id, "Error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static ResponseEntity<String> error(Exception e, HttpStatus hs){
        return ResponseEntity.status(hs)
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(getResponseObjectAsString("Error", e.getLocalizedMessage()));
    }

    public static String getResponseObjectAsString(String status, String message){
        return getResponseObjectAsString(null, status, message);
    }

    public static String getResponseObjectAsString(Integer id, String status, String message){
        JSONObject entity = new JSONObject();

        if(id != null){
            entity.put("id", id);
        }
        if(status != null){
            entity.put("status", status);
        }
        if(message != null){
            entity.put("message", message);
        }

        return entity.toString();
    }

    public static ResponseEntity<?> actionIsForbidden(){
        return actionIsForbidden("Users can only view/edit/delete data that belong to them");
    }

    public static ResponseEntity<?> actionIsForbidden(String message){
        return ResponseEntity.status(403)
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(ResponseHelper.getResponseObjectAsString("Error", message));
    }

    public static <T> ResponseEntity<T> success(@Nullable T body){
        return ResponseEntity.status(200)
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(body);
    }
}
