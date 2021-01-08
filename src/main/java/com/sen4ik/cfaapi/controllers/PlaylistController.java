package com.sen4ik.cfaapi.controllers;

import com.sen4ik.cfaapi.base.Constants;
import com.sen4ik.cfaapi.entities.FileEntity;
import com.sen4ik.cfaapi.entities.Playlist;
import com.sen4ik.cfaapi.utilities.UserUtility;
import com.sen4ik.cfaapi.base.ResponseHelper;
import com.sen4ik.cfaapi.entities.PlaylistFile;
import com.sen4ik.cfaapi.exceptions.RecordNotFoundException;
import com.sen4ik.cfaapi.repositories.FileRepository;
import com.sen4ik.cfaapi.repositories.PlaylistFileRepository;
import com.sen4ik.cfaapi.repositories.PlaylistRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Controller
@Slf4j
@RequestMapping(path = Constants.API_PREFIX + "/playlist")
@Api(tags = "Playlists", description = " ")
public class PlaylistController {

    @Autowired
    private PlaylistRepository playlistRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private PlaylistFileRepository playlistFileRepository;

    private static String accessForbiddenMessage = "Users can only view/edit/delete playlists that belong to them";

    @GetMapping(path="/all")
    @ApiOperation(value = "Get all playlists")
    public @ResponseBody Iterable<Playlist> getAll() {
        // Only admins can do it
        return playlistRepository.findAll();
    }

    @GetMapping(path="/{id}")
    @ApiOperation(value = "Get playlist by id")
    public @ResponseBody ResponseEntity<?> getOne(@PathVariable("id") @Min(1) @Min(1) int id) {
        // users can only get playlists that belong to them
        Playlist playlist = checkIfPlaylistExists(id);

        if(UserUtility.getCurrentlyLoggedInUserId() == playlist.getUserId()){
            return ResponseHelper.success(playlist);
        }
        else{
            return ResponseHelper.actionIsForbidden(accessForbiddenMessage);
        }
    }

    @GetMapping(path="/user/{id}")
    @ApiOperation(value = "Get all playlists for specified user")
    public @ResponseBody ResponseEntity<?> getPlaylistsForUser(@PathVariable("id") @Min(1) int id) {
        // Users can only get their own playlists
        if(UserUtility.getCurrentlyLoggedInUserId() == id){
            return ResponseHelper.success(playlistRepository.findByUserId(id));
        }
        else{
            return ResponseHelper.actionIsForbidden(accessForbiddenMessage);
        }
    }

    @PostMapping(path="/add")
    @ApiOperation(value = "Add playlist")
    public @ResponseBody
    Playlist addOne(@Valid @RequestBody Playlist playlist) {
        playlist.setUserId(UserUtility.getCurrentlyLoggedInUserId());
        playlistRepository.save(playlist);
        return playlist;
    }

    @DeleteMapping(path="/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Delete playlist")
    public @ResponseBody ResponseEntity<?> deleteOne(@PathVariable("id") @Min(1) int id) {
        Playlist playlist = checkIfPlaylistExists(id);

        // user can only delete their own playlists
        if(UserUtility.getCurrentlyLoggedInUserId() != playlist.getUserId()) {
            return ResponseHelper.actionIsForbidden(accessForbiddenMessage);
        }

        try {
            playlistRepository.deleteById(id);
            return ResponseHelper.deleteSuccess(id);
        } catch (Exception e) {
            return ResponseHelper.deleteFailed(id, e);
        }
    }

    @PostMapping(path="/{playlistId}/file/{fileId}")
    @ApiOperation(value = "Add file to playlist")
    public @ResponseBody
    ResponseEntity<?> addFileToPlaylist(@PathVariable("playlistId") @Min(1) int playlistId, @PathVariable("fileId") @Min(1) int fileId) {
        Playlist playlist = checkIfPlaylistExists(playlistId);
        checkIfFileExists(fileId);

        if(UserUtility.getCurrentlyLoggedInUserId() == playlist.getUserId()){
            PlaylistFile playlistToFile = linkFileToPlaylist(playlist, fileId);
            Optional<PlaylistFile> p = playlistFileRepository.findById(playlistToFile.getPlaylistFileId());
            return ResponseHelper.success(p.get());
        }
        else{
            return ResponseHelper.actionIsForbidden(accessForbiddenMessage);
        }
    }

    @PostMapping(path="/{playlistId}/file/add")
    @ApiOperation(value = "Add files to playlist")
    public @ResponseBody
    ResponseEntity<?> addFilesToPlaylist(@PathVariable("playlistId") @Min(1) int playlistId, @RequestParam(name="fileIDs") List<Integer> fileIDs) {

        Playlist playlist = checkIfPlaylistExists(playlistId);
        if(UserUtility.getCurrentlyLoggedInUserId() != playlist.getUserId()){
            return ResponseHelper.actionIsForbidden(accessForbiddenMessage);
        }

        List<Integer> nonFoundFiles = new ArrayList<>();
        for(Integer fileID : fileIDs){
            // checkIfFileExists(fileID);
            Optional<FileEntity> fileOptional = fileRepository.findById(fileID);
            if (!fileOptional.isPresent()){
                nonFoundFiles.add(fileID);
            }
        }
        if(nonFoundFiles.size() > 0){
            throw new RecordNotFoundException("Could not find file(s) with the following IDs: " + nonFoundFiles.toString());
        }

        List<PlaylistFile> playlistFiles = new ArrayList<>();
        for(Integer fileID : fileIDs){
            PlaylistFile pf = linkFileToPlaylist(playlist, fileID);
            Optional<PlaylistFile> npf = playlistFileRepository.findById(pf.getPlaylistFileId());
            playlistFiles.add(npf.get());
        }

        return ResponseHelper.success(playlistFiles);
    }

    private FileEntity checkIfFileExists(Integer fileID){
        Optional<FileEntity> fileOptional = fileRepository.findById(fileID);
        if (!fileOptional.isPresent()){
            throw new RecordNotFoundException("Could not find file with ID: " + fileID);
        }
        return fileOptional.get();
    }

    private Playlist checkIfPlaylistExists(Integer playlistId){
        Optional<Playlist> playlistOptional = playlistRepository.findById(playlistId);
        if (!playlistOptional.isPresent()){
            throw new RecordNotFoundException("Could not find playlist with ID: " + playlistId);
        }
        return playlistOptional.get();
    }

    private PlaylistFile linkFileToPlaylist(Playlist playlist, Integer fileID){
        Optional<FileEntity> fileOptional = fileRepository.findById(fileID);
        if (!fileOptional.isPresent()){
            throw new RecordNotFoundException("Could not find file with ID: " + fileID);
        }

        PlaylistFile playlistToFile = new PlaylistFile();
        playlistToFile.setFileId(fileOptional.get().getId());
        playlistToFile.setPlaylistId(playlist.getId());
        playlistFileRepository.save(playlistToFile);

        return playlistToFile;
    }

    @DeleteMapping(path="/playlistFile/{playlistFileId}")
    @ApiOperation(value = "Delete file and playlist association record") // TODO: update the description and explain which id needs to be passed in
    public @ResponseBody
    ResponseEntity<?> deleteFileFromPlaylist(@PathVariable("playlistFileId") @Min(1) int playlistFileId) {

        Optional<PlaylistFile> pf = playlistFileRepository.findById(playlistFileId);
        if (!pf.isPresent()){
            throw new RecordNotFoundException("Could not find playlist to file association using ID " + playlistFileId);
        }

        Optional<Playlist> playlist = playlistRepository.findById(pf.get().getPlaylistId());
        if(UserUtility.getCurrentlyLoggedInUserId() != playlist.get().getUserId()){
            return ResponseHelper.actionIsForbidden(accessForbiddenMessage);
        }

        playlistFileRepository.delete(pf.get());
        return ResponseHelper.deleteSuccess();
    }

}
