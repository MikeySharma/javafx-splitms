package com.splitms.repositories;

import com.splitms.lib.Database;
import com.splitms.models.ExpenseSplitModel;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcExpenseSplitRepository implements ExpenseSplitRepository {

    @Override
    public int create(int expenseId, int userId, BigDecimal shareAmount, float sharePercentage) {
        String sql = "INSERT INTO expense_splits (expense_id, user_id, share_amount, share_percentage) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = connection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, expenseId);
            ps.setInt(2, userId);
            ps.setBigDecimal(3, shareAmount);
            ps.setFloat(4, sharePercentage);

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
            throw new RuntimeException("Failed to create expense split", e);
        }
    }

    @Override
    public Optional<ExpenseSplitModel> findById(int splitId) {
        String sql = "SELECT split_id, expense_id, user_id, share_amount, share_percentage FROM expense_splits WHERE split_id = ?";

        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, splitId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find expense split", e);
        }
    }

    @Override
    public List<ExpenseSplitModel> findByExpense(int expenseId) {
        String sql = "SELECT split_id, expense_id, user_id, share_amount, share_percentage FROM expense_splits WHERE expense_id = ?";

        List<ExpenseSplitModel> splits = new ArrayList<>();
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, expenseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    splits.add(map(rs));
                }
            }
            return splits;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list expense splits", e);
        }
    }

    @Override
    public List<ExpenseSplitModel> findByUser(int userId) {
        String sql = "SELECT split_id, expense_id, user_id, share_amount, share_percentage FROM expense_splits WHERE user_id = ?";

        List<ExpenseSplitModel> splits = new ArrayList<>();
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    splits.add(map(rs));
                }
            }
            return splits;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list expense splits for user", e);
        }
    }

    @Override
    public BigDecimal sumShareAmountByExpense(int expenseId) {
        String sql = "SELECT COALESCE(SUM(share_amount), 0) AS total FROM expense_splits WHERE expense_id = ?";

        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, expenseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("total");
                }
                return BigDecimal.ZERO;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to sum expense splits", e);
        }
    }

    @Override
    public boolean deleteAllForExpense(int expenseId) {
        String sql = "DELETE FROM expense_splits WHERE expense_id = ?";

        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, expenseId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete splits for expense", e);
        }
    }

    @Override
    public boolean delete(int splitId) {
        String sql = "DELETE FROM expense_splits WHERE split_id = ?";

        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, splitId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete expense split", e);
        }
    }

    private static ExpenseSplitModel map(ResultSet rs) throws SQLException {
        return new ExpenseSplitModel(
                rs.getInt("split_id"),
                rs.getInt("expense_id"),
                rs.getInt("user_id"),
                rs.getBigDecimal("share_amount"),
                rs.getFloat("share_percentage"));
    }

    private static Connection connection() throws SQLException {
        return Database.getConnection();
    }
}
