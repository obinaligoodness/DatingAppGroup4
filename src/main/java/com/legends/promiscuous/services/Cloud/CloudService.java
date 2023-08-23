package com.legends.promiscuous.services.Cloud;

import com.legends.promiscuous.dtos.response.ApiResponse;
import org.springframework.web.multipart.MultipartFile;

public interface CloudService {
    String upload(MultipartFile file);
}
