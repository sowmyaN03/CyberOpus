package com.cyberopus.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BrandRequest {

    @NotBlank(message = "Brand name is required")
    @Size(max = 200)
    private String name;

    @Size(max = 500)
    private String description;
}
