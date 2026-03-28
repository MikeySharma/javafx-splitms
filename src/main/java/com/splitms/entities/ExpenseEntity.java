package com.splitms.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "expenses")
public class ExpenseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expense_id")
    private int expenseId;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @ManyToOne(optional = false)
    @JoinColumn(name = "group_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_expense_group"))
    private GroupsEntity group;

    @ManyToOne(optional = false)
    @JoinColumn(name = "payer_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_expense_payer"))
    private UserEntity payer;

    @ManyToOne(optional = false)
    @JoinColumn(name = "category_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_expense_category"))
    private CategoryEntity category;

    @OneToMany(mappedBy = "expense")
    private List<ExpenseSplitEntity> splits = new ArrayList<>();

    protected ExpenseEntity() {
    }

    public ExpenseEntity(GroupsEntity group, UserEntity payer, CategoryEntity category,
            BigDecimal amount, LocalDate expenseDate, String title, String description) {
        this.group = group;
        this.payer = payer;
        this.category = category;
        this.amount = amount;
        this.expenseDate = expenseDate;
        this.title = title;
        this.description = description;
    }

    public int getExpenseId() {
        return expenseId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public GroupsEntity getGroup() {
        return group;
    }

    public UserEntity getPayer() {
        return payer;
    }

    public CategoryEntity getCategory() {
        return category;
    }

    public List<ExpenseSplitEntity> getSplits() {
        return Collections.unmodifiableList(splits);
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setExpenseDate(LocalDate expenseDate) {
        this.expenseDate = expenseDate;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategory(CategoryEntity category) {
        this.category = category;
    }
}
