package com.gorson.springai.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DocumentReaderService {

    /**
     * 读取上传文件并转换为 Spring AI Document
     */
    public List<Document> read(MultipartFile file) throws IOException {

        // 1. 参数校验
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }

        // 2. 获取文件基本信息
        String filename = StringUtils.hasText(file.getOriginalFilename())
                ? file.getOriginalFilename()
                : "unknown";

        String contentType = StringUtils.hasText(file.getContentType())
                ? file.getContentType()
                : "application/octet-stream";

        // 3. MultipartFile 转换为 Spring Resource
        Resource resource = new ByteArrayResource(file.getBytes()) {

            @Override
            public String getFilename() {
                return filename;
            }
        };

        // 4. 使用 Tika 解析文件
        List<Document> documents =
                new TikaDocumentReader(resource).read();

        // 5. 补充业务 Metadata
        return documents.stream()
                .map(document -> {

                    Map<String, Object> metadata =
                            new HashMap<>(document.getMetadata());

                    metadata.put("original_filename", filename);
                    metadata.put("content_type", contentType);
                    metadata.put("file_size_bytes",
                            String.valueOf(file.getSize()));

                    return new Document(
                            document.getId(),
                            document.getText(),
                            metadata
                    );
                })
                .toList();
    }
}