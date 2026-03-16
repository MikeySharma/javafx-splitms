package com.splitms.repositories;

import com.splitms.lib.Database;
import com.splitms.models.TransactionModel;
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

public class JdbcTransactionRepository implements TransactionRepository {

    @Override
    public int create(int groupId, int fromUserId, int toUserId, BigDecimal amount,
            LocalDate transactionDate) {
        String sql = """
                INSERT INTO transactions (group_id, from_user_id, to_user_id, amount, transaction_date)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps = connection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, groupId);
            ps.setInt(2, fromUserId);
            ps.setInt(3, toUserId);
            ps.setBigDecimal(4, amount);
            ps.setDate(5, Date.valueOf(transactionDate));

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
            throw new RuntimeException("Failed to create transaction", e);
        }
    }

    @Override
    public Optional<TransactionModel> findById(int transactionId) {
        String sql = """
                SELECT transaction_id, group_id, from_user_id, to_user_id, amount, transaction_date, settled
                FROM transactions WHERE transaction_id = ?
                """;

        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, transactionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find transaction", e);
        }
    }

    @Override
    public List<TransactionModel> findByGroup(int groupId) {
        String sql = """
                SELECT transaction_id, group_id, from_user_id, to_user_id, amount, transaction_date, settled
                FROM transactions WHERE group_id = ? ORDER BY transaction_date DESC
                """;

        List<TransactionModel> transactions = new ArrayList<>();
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, groupId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    transactions.add(map(rs));
                }
            }
            return transactions;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list transactions", e);
        }
    }

    @Override
    public boolean settle(int transactionId) {
        String sql = "UPDATE transactions SET settled = true WHERE transaction_id = ?";

        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, transactionId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to settle transaction", e);
        }
    }

    @Override
    public boolean delete(int transactionId) {
        String sql = "DELETE FROM transactions WHERE transaction_id = ?";

        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, transactionId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete transaction", e);
        }
    }

    private static TransactionModel map(ResultSet rs) throws SQLException {
        return new TransactionModel(
                rs.getInt("transaction_id"),
                rs.getInt("group_id"),
                rs.getInt("from_user_id"),
                rs.getInt("to_user_id"),
                rs.getBigDecimal("amount"),
                rs.getDate("transaction_date").toLocalDate(),
                rs.getBoolean("settled"));
    }

    private static Connection connection() throws SQLException {
        return Database.getConnection();
    }
}
