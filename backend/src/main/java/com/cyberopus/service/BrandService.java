package com.cyberopus.service;

import com.cyberopus.dto.request.BrandRequest;
import com.cyberopus.dto.response.BrandResponse;
import com.cyberopus.entity.Brand;
import com.cyberopus.exception.DuplicateResourceException;
import com.cyberopus.exception.ResourceNotFoundException;
import com.cyberopus.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository brandRepository;

    public List<BrandResponse> getAll() {
        return brandRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public BrandResponse getById(Long id) {
        return toResponse(findById(id));
    }

    @Transactional
    public BrandResponse create(BrandRequest request) {
        if (brandRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Brand already exists: " + request.getName());
        }
        Brand saved = brandRepository.save(Brand.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build());
        return toResponse(saved);
    }

    @Transactional
    public BrandResponse update(Long id, BrandRequest request) {
        Brand brand = findById(id);
        brand.setName(request.getName());
        brand.setDescription(request.getDescription());
        return toResponse(brandRepository.save(brand));
    }

    @Transactional
    public void delete(Long id) {
        findById(id);
        brandRepository.deleteById(id);
    }

    public Brand findById(Long id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found: " + id));
    }

    public BrandResponse toResponse(Brand b) {
        return BrandResponse.builder()
                .id(b.getId())
                .name(b.getName())
                .description(b.getDescription())
                .createdAt(b.getCreatedAt())
                .build();
    }
}
