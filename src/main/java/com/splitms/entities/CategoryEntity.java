package com.splitms.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "categories")
public class CategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private int categoryId;

    @Column(name = "category_name", nullable = false, length = 100)
    private String categoryName;

    @Column(name = "category_type", nullable = false, length = 50)
    private String categoryType;

    @Column(name = "icon", nullable = false, length = 100)
    private String icon;

    @OneToMany(mappedBy = "category")
    private List<ExpenseEntity> expenses = new ArrayList<>();

    protected CategoryEntity() {
    }

    public CategoryEntity(String categoryName, String categoryType, String icon) {
        this.categoryName = categoryName;
        this.categoryType = categoryType;
        this.icon = icon;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getCategoryType() {
        return categoryType;
    }

    public String getIcon() {
        return icon;
    }

    public List<ExpenseEntity> getExpenses() {
        return Collections.unmodifiableList(expenses);
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public void setCategoryType(String categoryType) {
        this.categoryType = categoryType;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
