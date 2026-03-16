package com.splitms.repositories;

import com.splitms.lib.Database;
import com.splitms.models.CategoryModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcCategoryRepository implements CategoryRepository {

    @Override
    public int create(String categoryName, String categoryType, String icon) {
        String sql = "INSERT INTO categories (category_name, category_type, icon) VALUES (?, ?, ?)";

        try (PreparedStatement ps = connection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, categoryName);
            ps.setString(2, categoryType);
            ps.setString(3, icon);

            int rows = ps.executeUpdate();
            if (rows <= 0) {
                return -1;
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            return -1;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create category", e);
        }
    }

    @Override
    public Optional<CategoryModel> findById(int categoryId) {
        String sql = "SELECT category_id, category_name, category_type, icon FROM categories WHERE category_id = ?";

        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find category", e);
        }
    }

    @Override
    public List<CategoryModel> findAll() {
        String sql = "SELECT category_id, category_name, category_type, icon FROM categories ORDER BY category_name";

        List<CategoryModel> categories = new ArrayList<>();
        try (PreparedStatement ps = connection().prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                categories.add(map(rs));
            }
            return categories;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list categories", e);
        }
    }

    @Override
    public boolean update(int categoryId, String categoryName, String categoryType, String icon) {
        String sql = "UPDATE categories SET category_name = ?, category_type = ?, icon = ? WHERE category_id = ?";

        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setString(1, categoryName);
            ps.setString(2, categoryType);
            ps.setString(3, icon);
            ps.setInt(4, categoryId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update category", e);
        }
    }

    @Override
    public boolean delete(int categoryId) {
        String sql = "DELETE FROM categories WHERE category_id = ?";

        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete category", e);
        }
    }

    private static CategoryModel map(ResultSet rs) throws SQLException {
        return new CategoryModel(
                rs.getInt("category_id"),
                rs.getString("category_name"),
                rs.getString("category_type"),
                rs.getString("icon"));
    }

    private static Connection connection() throws SQLException {
        return Database.getConnection();
    }
}
