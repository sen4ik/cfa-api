package com.sen4ik.cfaapi.repositories;


import com.sen4ik.cfaapi.entities.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FileRepository extends JpaRepository<FileEntity, Integer> {

    List<FileEntity> findByFileName(@Param("fileName") String fileName);

    List<FileEntity> findByFileTitle(@Param("fileTitle") String fileTitle);

    List<FileEntity> getFilesByCategoryId(@Param("categoryId") Integer categoryId);

}
