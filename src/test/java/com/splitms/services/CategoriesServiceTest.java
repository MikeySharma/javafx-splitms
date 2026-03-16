package com.splitms.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.splitms.models.CategoryModel;
import com.splitms.repositories.CategoryRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

public class CategoriesServiceTest {

    private CategoriesService categoriesService;

    @Before
    public void setUp() {
    categoriesService = new CategoriesService(new InMemoryCategoryRepository());
    }

    @Test
    public void testCreateFindUpdateDeleteCategory() {
        ServiceResult<CategoryModel> createResult = categoriesService.createCategory(
        "Food and Drink", "expense", "food");
        assertTrue("Category create should succeed", createResult.success());
        assertNotNull("Created category should be returned", createResult.data());
        int categoryId = createResult.data().categoryId();
        assertTrue("Category id should be positive", categoryId > 0);
    assertEquals("Name should match", "Food and Drink", createResult.data().categoryName());

        ServiceResult<CategoryModel> findResult = categoriesService.getCategory(categoryId);
        assertTrue("Category should be found", findResult.success());
    assertEquals("Found category name should match", "Food and Drink", findResult.data().categoryName());

        ServiceResult<CategoryModel> updateResult = categoriesService.updateCategory(
        categoryId, "Food and Beverage", "expense", "plate");
        assertTrue("Category update should succeed", updateResult.success());
    assertEquals("Updated name should match", "Food and Beverage", updateResult.data().categoryName());

        ServiceResult<Void> deleteResult = categoriesService.deleteCategory(categoryId);
        assertTrue("Category delete should succeed", deleteResult.success());

        ServiceResult<CategoryModel> afterDelete = categoriesService.getCategory(categoryId);
        assertFalse("Category should not exist after delete", afterDelete.success());
    }

    @Test
    public void testListCategories() {
    ServiceResult<CategoryModel> create1 = categoriesService.createCategory("Travel", "expense", "plane");
    ServiceResult<CategoryModel> create2 = categoriesService.createCategory("Utilities", "expense", "light");
        assertTrue("First category create should succeed", create1.success());
        assertTrue("Second category create should succeed", create2.success());

        ServiceResult<List<CategoryModel>> listResult = categoriesService.listCategories();
        assertTrue("List should succeed", listResult.success());
        assertNotNull("List data should not be null", listResult.data());
        assertTrue("List should contain at least 2 categories", listResult.data().size() >= 2);
    }

    @Test
    public void testCreateCategoryWithBlankNameFails() {
    ServiceResult<CategoryModel> result = categoriesService.createCategory("  ", "expense", "unknown");
        assertFalse("Blank name should fail", result.success());
    }

    @Test
    public void testCreateCategoryWithBlankTypeFails() {
        ServiceResult<CategoryModel> result = categoriesService.createCategory("Transport", " ", "bus");
        assertFalse("Blank type should fail", result.success());
    }

    @Test
    public void testGetCategoryWithInvalidIdFails() {
        ServiceResult<CategoryModel> result = categoriesService.getCategory(0);
        assertFalse("Invalid id should fail", result.success());
    }

    @Test
    public void testDeleteCategoryWithInvalidIdFails() {
        ServiceResult<Void> result = categoriesService.deleteCategory(-1);
        assertFalse("Invalid id should fail", result.success());
    }

    private static final class InMemoryCategoryRepository implements CategoryRepository {
        private final Map<Integer, CategoryModel> categories = new HashMap<>();
        private int sequence = 1;

        @Override
        public int create(String categoryName, String categoryType, String icon) {
            int id = sequence++;
            categories.put(id, new CategoryModel(id, categoryName, categoryType, icon));
            return id;
        }

        @Override
        public Optional<CategoryModel> findById(int categoryId) {
            return Optional.ofNullable(categories.get(categoryId));
        }

        @Override
        public List<CategoryModel> findAll() {
            return new ArrayList<>(categories.values());
        }

        @Override
        public boolean update(int categoryId, String categoryName, String categoryType, String icon) {
            if (!categories.containsKey(categoryId)) {
                return false;
            }
            categories.put(categoryId, new CategoryModel(categoryId, categoryName, categoryType, icon));
            return true;
        }

        @Override
        public boolean delete(int categoryId) {
            return categories.remove(categoryId) != null;
        }
    }
}
