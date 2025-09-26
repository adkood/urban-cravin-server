package com.ashutosh.urban_cravin.services.products;

import com.ashutosh.urban_cravin.helpers.dtos.product.request.CreateProductCategoryRequest;
import com.ashutosh.urban_cravin.helpers.dtos.product.request.UpdateProductCategoryRequest;
import com.ashutosh.urban_cravin.models.product.ProductCategory;
import com.ashutosh.urban_cravin.repositories.products.ProductCategoryRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProductCategoryService {

    @Autowired
    private ProductCategoryRepo productCategoryRepo;

    public ProductCategory addProductCategory(CreateProductCategoryRequest req) {
        if (productCategoryRepo.existsBySlug(req.getSlug())) {
            throw new RuntimeException("Product Category with this slug already exists");
        }

        ProductCategory category = new ProductCategory();
        category.setName(req.getName());
        category.setDescription(req.getDescription());
        category.setSlug(req.getSlug());
        category.setActive(req.getActive() != null ? req.getActive() : true);
        category.setImageUrl(req.getImageUrl());
        category.setSortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0);

        if (req.getParentCategoryId() != null) {
            ProductCategory parent = productCategoryRepo.findById(req.getParentCategoryId())
                    .orElseThrow(() -> new RuntimeException("Parent category not found with id: " + req.getParentCategoryId()));
            category.setParentCategory(parent);
        }

        return productCategoryRepo.save(category);
    }

    public List<ProductCategory> getAllCategories() {
        return productCategoryRepo.findAll();
    }

    public ProductCategory getCategoryById(UUID id) {
        return productCategoryRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
    }

    public ProductCategory updateCategory(UUID id, UpdateProductCategoryRequest req) {
        ProductCategory category = getCategoryById(id);

        if (req.getName() != null) category.setName(req.getName());
        if (req.getDescription() != null) category.setDescription(req.getDescription());
        if (req.getSlug() != null) {
            if (!req.getSlug().equals(category.getSlug()) && productCategoryRepo.existsBySlug(req.getSlug())) {
                throw new RuntimeException("Category with this slug already exists");
            }
            category.setSlug(req.getSlug());
        }
        if (req.getActive() != null) category.setActive(req.getActive());
        if (req.getImageUrl() != null) category.setImageUrl(req.getImageUrl());
        if (req.getSortOrder() != null) category.setSortOrder(req.getSortOrder());

        if (req.getParentCategoryId() != null) {
            ProductCategory parent = productCategoryRepo.findById(req.getParentCategoryId())
                    .orElseThrow(() -> new RuntimeException("Parent category not found with id: " + req.getParentCategoryId()));
            category.setParentCategory(parent);
        }

        return productCategoryRepo.save(category);
    }

    public void deleteCategory(UUID id) {
        ProductCategory category = getCategoryById(id);
        productCategoryRepo.delete(category);
    }
}
