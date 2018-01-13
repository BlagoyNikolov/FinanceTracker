package com.financetracker.services;

import com.financetracker.model.Category;
import com.financetracker.model.PaymentType;
import com.financetracker.model.User;
import com.financetracker.repositories.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public void insertCategory(Category category) {
        categoryRepository.save(category);
    }

    public String getCategoryNameByCategoryId(long categoryId) {
        Category category = categoryRepository.findByCategoryId(categoryId);
        return category.getName();
    }

    public Category getCategoryByCategoryId(long categoryId) {
        return categoryRepository.findByCategoryId(categoryId);
    }

    public Set<Category> getAllCategoriesByUserId(Long... userIdParam) {
        return (userIdParam.length == 0) ? categoryRepository.findByUserIsNull() : categoryRepository.findByUserUserId(userIdParam[0]);
    }

    public Category getCategoryByCategoryName(String categoryName) {
        return categoryRepository.findByName(categoryName);
    }

    public Set<Category> getAllCategoriesByType(User user, PaymentType type) {
        return categoryRepository.getCategories(user, type);
    }

    public void postCategory(User user, Category category) {
        category.setUser(user);
        insertCategory(category);
    }

    public Set<String> getCategories(String type, User user) {
        Set<Category> categories = getAllCategoriesByType(user, PaymentType.valueOf(type));
        Set<String> categoryNames = categories
            .stream()
            .map(category -> category.getName())
            .collect(Collectors.toSet());
        return categoryNames;
    }
}
