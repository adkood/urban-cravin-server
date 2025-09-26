package com.ashutosh.urban_cravin.repositories.products;

import com.ashutosh.urban_cravin.models.product.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProductCategoryRepo extends JpaRepository<ProductCategory, UUID> {

    boolean existsBySlug(String slug);
}
