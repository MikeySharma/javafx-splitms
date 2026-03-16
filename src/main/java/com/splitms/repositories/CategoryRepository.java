package com.splitms.repositories;

import com.splitms.models.CategoryModel;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository {
    int create(String categoryName, String categoryType, String icon);

    Optional<CategoryModel> findById(int categoryId);

    List<CategoryModel> findAll();

    boolean update(int categoryId, String categoryName, String categoryType, String icon);

    boolean delete(int categoryId);
}
