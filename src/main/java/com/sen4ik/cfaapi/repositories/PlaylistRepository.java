package com.sen4ik.cfaapi.repositories;


import com.sen4ik.cfaapi.entities.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlaylistRepository extends JpaRepository<Playlist, Integer> {

    List<Playlist> findByUserId(@Param("userId") Integer userId);

}
