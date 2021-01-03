package com.sen4ik.cfaapi.repositories;


import com.sen4ik.cfaapi.entities.PlaylistFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlaylistFileRepository extends JpaRepository<PlaylistFile, Integer> {
    List<PlaylistFile> findByPlaylistIdAndFileId(Integer playlistId, Integer fileId);
    List<PlaylistFile> findByPlaylistId(Integer playlistId);
    List<PlaylistFile> findByFileId(Integer fileId);
    Optional<PlaylistFile> findById(Integer id);
}
