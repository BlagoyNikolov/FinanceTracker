package com.financetracker.services;

import com.financetracker.model.Category;
import com.financetracker.model.PaymentType;
import com.financetracker.model.User;

import java.util.Set;

/**
 * Created by blagoy
 */
public interface CategoryService {

    void insertCategory(Category category);

    String getCategoryNameByCategoryId(long categoryId);

    Category getCategoryByCategoryId(long categoryId);

    Set<Category> getAllCategoriesByUserId(Long... userIdParam);

    Category getCategoryByCategoryName(String categoryName);

    Set<Category> getAllCategoriesByType(User user, PaymentType type);

    void postCategory(User user, Category category);

    Set<String> getCategories(String type, User user);
}
