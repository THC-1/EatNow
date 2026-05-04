package com.eatnow.backend.file.controller;

import com.eatnow.backend.common.result.ApiResponse;
import com.eatnow.backend.file.service.FileUploadService;
import com.eatnow.backend.file.vo.UploadImageVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileUploadService fileUploadService;

    @PostMapping("/upload-image")
    public ApiResponse<UploadImageVo> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String type
    ) {
        return new ApiResponse<>(200, "上传成功", fileUploadService.uploadImage(file, type));
    }
}
