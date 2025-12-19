package com.cooking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class FileUploadController {

    private static final String UPLOAD_DIR = "uploads";

    @PostMapping("/image")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("文件为空");
            }

            // 创建上传目录
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : ".jpg";
            String filename = UUID.randomUUID().toString().substring(0, 8) + "_" + 
                (originalFilename != null ? originalFilename : "image") + extension;

            // 保存文件
            Path filePath = Paths.get(UPLOAD_DIR, filename);
            Files.write(filePath, file.getBytes());

            // 返回相对路径
            return ResponseEntity.ok("/uploads/" + filename);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("文件上传失败: " + e.getMessage());
        }
    }
}

