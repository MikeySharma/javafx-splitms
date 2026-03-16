package com.splitms.repositories;

import com.splitms.lib.Database;
import com.splitms.models.ExpenseModel;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcExpenseRepository implements ExpenseRepository {

    @Override
    public int create(int groupId, int payerId, int categoryId, BigDecimal amount,
            LocalDate expenseDate, String description) {
        String sql = """
                INSERT INTO expenses (group_id, payer_id, category_id, amount, expense_date, description)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps = connection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, groupId);
            ps.setInt(2, payerId);
            ps.setInt(3, categoryId);
            ps.setBigDecimal(4, amount);
            ps.setDate(5, Date.valueOf(expenseDate));
            ps.setString(6, description);

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
            throw new RuntimeException("Failed to create expense", e);
        }
    }

    @Override
    public Optional<ExpenseModel> findById(int expenseId) {
        String sql = """
                SELECT expense_id, group_id, payer_id, category_id, amount, expense_date, description
                FROM expenses WHERE expense_id = ?
                """;

        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, expenseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find expense", e);
        }
    }

    @Override
    public List<ExpenseModel> findByGroup(int groupId) {
        String sql = """
                SELECT expense_id, group_id, payer_id, category_id, amount, expense_date, description
                FROM expenses WHERE group_id = ? ORDER BY expense_date DESC
                """;

        List<ExpenseModel> expenses = new ArrayList<>();
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, groupId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    expenses.add(map(rs));
                }
            }
            return expenses;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list expenses", e);
        }
    }

    @Override
    public Optional<BigDecimal> findAmountById(int expenseId) {
        String sql = "SELECT amount FROM expenses WHERE expense_id = ?";

        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, expenseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(rs.getBigDecimal("amount"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load expense amount", e);
        }
    }

    @Override
    public boolean update(int expenseId, int categoryId, BigDecimal amount,
            LocalDate expenseDate, String description) {
        String sql = """
                UPDATE expenses SET category_id = ?, amount = ?, expense_date = ?, description = ?
                WHERE expense_id = ?
                """;

        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            ps.setBigDecimal(2, amount);
            ps.setDate(3, Date.valueOf(expenseDate));
            ps.setString(4, description);
            ps.setInt(5, expenseId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update expense", e);
        }
    }

    @Override
    public boolean delete(int expenseId) {
        String sql = "DELETE FROM expenses WHERE expense_id = ?";

        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, expenseId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete expense", e);
        }
    }

    private static ExpenseModel map(ResultSet rs) throws SQLException {
        return new ExpenseModel(
                rs.getInt("expense_id"),
                rs.getInt("group_id"),
                rs.getInt("payer_id"),
                rs.getInt("category_id"),
                rs.getBigDecimal("amount"),
                rs.getDate("expense_date").toLocalDate(),
                rs.getString("description"));
    }

    private static Connection connection() throws SQLException {
        return Database.getConnection();
    }
}
