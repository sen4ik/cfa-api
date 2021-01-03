package com.sen4ik.cfaapi.repositories;

import com.sen4ik.cfaapi.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoriesRepository extends JpaRepository<Category, Integer> {

    List<Category> findByCategoryName(@Param("categoryName") String categoryName);

    List<Category> getCategoriesByParentId(@Param("parentId") Integer parentId);

}
