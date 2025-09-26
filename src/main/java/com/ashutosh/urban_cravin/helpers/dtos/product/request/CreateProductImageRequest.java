package com.ashutosh.urban_cravin.helpers.dtos.product.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CreateProductImageRequest {
    private MultipartFile file;
    private boolean primaryImage = false;
    private String altText;
}
