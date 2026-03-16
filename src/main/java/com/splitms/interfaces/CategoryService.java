package com.splitms.interfaces;

import com.splitms.models.CategoryModel;
import com.splitms.services.ServiceResult;
import java.util.List;

public interface CategoryService {
    ServiceResult<List<CategoryModel>> listCategories();

    ServiceResult<CategoryModel> getCategory(int categoryId);

    ServiceResult<CategoryModel> createCategory(String categoryName, String categoryType, String icon);

    ServiceResult<CategoryModel> updateCategory(int categoryId, String categoryName,
            String categoryType, String icon);

    ServiceResult<Void> deleteCategory(int categoryId);
}
