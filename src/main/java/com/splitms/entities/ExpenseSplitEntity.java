package com.splitms.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;

@Entity
@Table(name = "expense_splits",
        uniqueConstraints = @UniqueConstraint(name = "uq_expense_split",
                columnNames = { "expense_id", "user_id" }))
public class ExpenseSplitEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "split_id")
    private int splitId;

    @Column(name = "share_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal shareAmount;

    @Column(name = "share_percentage", nullable = false)
    private float sharePercentage;

    @ManyToOne(optional = false)
    @JoinColumn(name = "expense_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_split_expense"))
    private ExpenseEntity expense;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_split_user"))
    private UserEntity user;

    protected ExpenseSplitEntity() {
    }

    public ExpenseSplitEntity(ExpenseEntity expense, UserEntity user,
            BigDecimal shareAmount, float sharePercentage) {
        this.expense = expense;
        this.user = user;
        this.shareAmount = shareAmount;
        this.sharePercentage = sharePercentage;
    }

    public int getSplitId() {
        return splitId;
    }

    public BigDecimal getShareAmount() {
        return shareAmount;
    }

    public float getSharePercentage() {
        return sharePercentage;
    }

    public ExpenseEntity getExpense() {
        return expense;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setShareAmount(BigDecimal shareAmount) {
        this.shareAmount = shareAmount;
    }

    public void setSharePercentage(float sharePercentage) {
        this.sharePercentage = sharePercentage;
    }
}
