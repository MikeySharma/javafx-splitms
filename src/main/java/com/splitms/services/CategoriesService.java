package com.splitms.services;

import com.splitms.interfaces.CategoryService;
import com.splitms.models.CategoryModel;
import com.splitms.repositories.CategoryRepository;
import com.splitms.repositories.JdbcCategoryRepository;
import com.splitms.utils.Normalize;
import java.util.List;

public class CategoriesService implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoriesService() {
        this(new JdbcCategoryRepository());
    }

    public CategoriesService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public ServiceResult<List<CategoryModel>> listCategories() {
        List<CategoryModel> categories = categoryRepository.findAll();
        return ServiceResult.ok("Categories loaded.", categories);
    }

    @Override
    public ServiceResult<CategoryModel> getCategory(int categoryId) {
        if (categoryId <= 0) {
            return ServiceResult.fail("Invalid category id.");
        }

        return categoryRepository.findById(categoryId)
                .map(c -> ServiceResult.ok("Category found.", c))
                .orElseGet(() -> ServiceResult.fail("Category not found."));
    }

    @Override
    public ServiceResult<CategoryModel> createCategory(String categoryName, String categoryType,
            String icon) {
        String normalizedName = Normalize.normalizeText(categoryName);
        String normalizedType = Normalize.normalizeText(categoryType);
        String normalizedIcon = Normalize.normalizeText(icon);

        if (normalizedName.isBlank() || normalizedType.isBlank()) {
            return ServiceResult.fail("Category name and type are required.");
        }

        String safeIcon = normalizedIcon.isBlank() ? "" : normalizedIcon;

        int categoryId = categoryRepository.create(normalizedName, normalizedType, safeIcon);
        if (categoryId <= 0) {
            return ServiceResult.fail("Could not create category.");
        }

        return categoryRepository.findById(categoryId)
                .map(c -> ServiceResult.ok("Category created.", c))
                .orElseGet(() -> ServiceResult.fail("Category created but could not be loaded."));
    }

    @Override
    public ServiceResult<CategoryModel> updateCategory(int categoryId, String categoryName,
            String categoryType, String icon) {
        String normalizedName = Normalize.normalizeText(categoryName);
        String normalizedType = Normalize.normalizeText(categoryType);
        String normalizedIcon = Normalize.normalizeText(icon);

        if (categoryId <= 0 || normalizedName.isBlank() || normalizedType.isBlank()) {
            return ServiceResult.fail("Category name and type are required.");
        }

        String safeIcon = normalizedIcon.isBlank() ? "" : normalizedIcon;

        boolean updated = categoryRepository.update(categoryId, normalizedName, normalizedType, safeIcon);
        if (!updated) {
            return ServiceResult.fail("Category update failed.");
        }

        return categoryRepository.findById(categoryId)
                .map(c -> ServiceResult.ok("Category updated.", c))
                .orElseGet(() -> ServiceResult.fail("Category updated but could not be reloaded."));
    }

    @Override
    public ServiceResult<Void> deleteCategory(int categoryId) {
        if (categoryId <= 0) {
            return ServiceResult.fail("Invalid category id.");
        }

        boolean deleted = categoryRepository.delete(categoryId);
        if (!deleted) {
            return ServiceResult.fail("Category delete failed.");
        }

        return ServiceResult.ok("Category deleted.", null);
    }
}
