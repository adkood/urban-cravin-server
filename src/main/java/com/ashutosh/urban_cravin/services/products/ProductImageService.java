package com.ashutosh.urban_cravin.services.products;

import com.ashutosh.urban_cravin.helpers.dtos.product.request.CreateProductImageRequest;
import com.ashutosh.urban_cravin.models.product.Product;
import com.ashutosh.urban_cravin.models.product.ProductImage;
import com.ashutosh.urban_cravin.repositories.products.ProductImageRepo;
import com.ashutosh.urban_cravin.repositories.products.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class ProductImageService {

    @Autowired
    private ProductImageRepo imageRepo;

    @Autowired
    private ProductRepo productRepo;

    @Value("${upload.path}") // e.g., uploads/
    private String uploadPath;

    private void validateImage(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null ||
                !(contentType.equals("image/jpeg") || contentType.equals("image/png") || contentType.equals("image/webp"))) {
            throw new RuntimeException("Only JPG, PNG, or WEBP images are allowed");
        }

        if (file.getSize() > 5 * 1024 * 1024) { // 5MB max
            throw new RuntimeException("File size exceeds 5MB limit");
        }
    }

    public ProductImage addImage(UUID productId, CreateProductImageRequest req) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        ProductImage image = null;
        try {
            validateImage(req.getFile());

            String subFolder = req.isPrimaryImage() ? "primary" : "secondary";
            File dir = new File(uploadPath + File.separator + productId + File.separator + subFolder);

            // ✅ Ensure directory exists
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                System.out.println("Created directory: " + dir.getAbsolutePath() + " -> " + created);
            }

            // Generate unique filename
            String originalName = req.getFile().getOriginalFilename();
            System.out.println("Original filename: " + originalName);

            String fileName = UUID.randomUUID() + "_" + (originalName != null ? originalName : "unknown");
            File dest = new File(dir, fileName);

            // ✅ Log final save path
            System.out.println("Saving file to: " + dest.getAbsolutePath());

            // Save file locally

            // *********************************************Don't use this******************************
//            Spring’s StandardMultipartFile.transferTo() (inside Tomcat) ignores your dest path and instead tries to write to Tomcat’s own work directory:
//            req.getFile().transferTo(dest);

            FileCopyUtils.copy(req.getFile().getBytes(), dest);
            System.out.println("File saved successfully!");

            // Save DB record
            image = new ProductImage();
            image.setUrl("/uploads/" + productId + "/" + subFolder + "/" + fileName);
            image.setProduct(product);
            image.setPrimaryImage(req.isPrimaryImage());
            image.setAltText(originalName != null ? originalName : "No description");
            imageRepo.save(image);

        } catch (IOException e) {
            e.printStackTrace(); // ✅ log exact reason in console
            throw new RuntimeException("Failed to upload file: " + req.getFile().getOriginalFilename(), e);
        }

        return image;
    }

    public void deleteImage(UUID imageId) {
        ProductImage image = imageRepo.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found"));

        // Convert stored URL to file system path
        String relativePath = image.getUrl().replace("/uploads/", "");
        File file = new File(uploadPath + File.separator + relativePath);
        if (file.exists()) file.delete();

        imageRepo.delete(image);
    }


    public List<ProductImage> getImagesByProduct(UUID productId) {
        return imageRepo.findByProductId(productId);
    }
}
