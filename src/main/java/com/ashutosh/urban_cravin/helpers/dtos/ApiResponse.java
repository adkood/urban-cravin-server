package com.ashutosh.urban_cravin.helpers.dtos;

import com.ashutosh.urban_cravin.helpers.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class ApiResponse {
    private Status status;
    private String message;
    private Map<String, Object> data;
}
