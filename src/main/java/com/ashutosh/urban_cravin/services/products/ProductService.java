package com.ashutosh.urban_cravin.services.products;

import com.ashutosh.urban_cravin.helpers.dtos.product.request.CreateProductRequest;
import com.ashutosh.urban_cravin.helpers.dtos.product.request.UpdateProductRequest;
import com.ashutosh.urban_cravin.models.product.Product;
import com.ashutosh.urban_cravin.models.product.ProductCategory;
import com.ashutosh.urban_cravin.repositories.products.ProductCategoryRepo;
import com.ashutosh.urban_cravin.repositories.products.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class ProductService {

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private ProductCategoryRepo productCategoryRepo;


    public Product addProduct(CreateProductRequest req) {
        if (productRepo.existsBySku(req.getSku())) {
            throw new RuntimeException("Product with SKU already exists");
        }

        ProductCategory category = productCategoryRepo.findById(req.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Product product = new Product();
        product.setName(req.getName());
        product.setDescription(req.getDescription());
        product.setPrice(req.getPrice());
        product.setDiscountAmount(req.getDiscountAmount());
        product.setDiscountPercentage(req.getDiscountPercentage());
        product.setTaxPercentage(req.getTaxPercentage());
        product.setActive(req.isActive());
        product.setStockQuantity(req.getStockQuantity());
        product.setWeight(req.getWeight());
        product.setDimensions(req.getDimensions());
        product.setSize(req.getSize());
        product.setSku(req.getSku());
        product.setProductCategory(category);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        return productRepo.save(product);
    }


    public List<Product> getAllProducts() {
        return productRepo.findAll();
    }


    public Product getProductById(UUID id) {
        return productRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Product not found with id: " + id));
    }


    public Product updateProduct(UUID id, UpdateProductRequest req) {
        Product product = getProductById(id);

        if (req.getName() != null) product.setName(req.getName());
        if (req.getDescription() != null) product.setDescription(req.getDescription());
        if (req.getPrice() != null) product.setPrice(req.getPrice());
        if (req.getDiscountAmount() != null) product.setDiscountAmount(req.getDiscountAmount());
        if (req.getDiscountPercentage() != null) product.setDiscountPercentage(req.getDiscountPercentage());
        if (req.getTaxPercentage() != null) product.setTaxPercentage(req.getTaxPercentage());
        if (req.getStockQuantity() != null) product.setStockQuantity(req.getStockQuantity());
        if (req.getWeight() != null) product.setWeight(req.getWeight());
        if (req.getDimensions() != null) product.setDimensions(req.getDimensions());
        if (req.getSize() != null) product.setSize(req.getSize());
        if (req.getActive() != null) product.setActive(req.getActive());

        if (req.getSku() != null) {
            // prevent duplicate SKU conflicts
            if (!req.getSku().equals(product.getSku()) && productRepo.existsBySku(req.getSku())) {
                throw new RuntimeException("Product with SKU already exists");
            }
            product.setSku(req.getSku());
        }

        if (req.getCategoryId() != null) {
            ProductCategory category = productCategoryRepo.findById(req.getCategoryId())
                    .orElseThrow(() -> new NoSuchElementException("Category not found with id: " + req.getCategoryId()));
            product.setProductCategory(category);
        }

        product.setUpdatedAt(LocalDateTime.now());

        return productRepo.save(product);
    }


    public void deleteProduct(UUID id) {
        Product product = getProductById(id);
        productRepo.delete(product);
    }
}
