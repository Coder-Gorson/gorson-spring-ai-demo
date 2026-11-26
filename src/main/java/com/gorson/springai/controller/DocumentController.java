package com.gorson.springai.controller;

import com.gorson.springai.dto.response.DocumentSplitResponse;
import com.gorson.springai.dto.response.DocumentUploadResponse;
import com.gorson.springai.service.DocumentReaderService;

import com.gorson.springai.service.DocumentSplitService;
import org.springframework.ai.document.Document;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@RestController
@RequestMapping("/api/rag/documents")
public class DocumentController {

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("pdf", "doc", "docx", "ppt", "pptx", "txt", "html");

    private final DocumentReaderService documentReaderService;

    private final DocumentSplitService documentSplitService;

    public DocumentController(
            DocumentReaderService documentReaderService,
            DocumentSplitService documentSplitService) {

        this.documentReaderService = documentReaderService;
        this.documentSplitService = documentSplitService;
    }

    @PostMapping(
            value = "/upload-and-split",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public DocumentSplitResponse uploadAndSplit(
            @RequestParam("file") MultipartFile file) {

        // 1. 文件校验
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "上传文件不能为空"
            );
        }

        String filename = file.getOriginalFilename();

        String extension = StringUtils.getFilenameExtension(filename);

        if (extension == null ||
                !ALLOWED_EXTENSIONS.contains(
                        extension.toLowerCase(Locale.ROOT))) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "暂不支持该文件格式"
            );
        }

        try {
            // 2. 原始文件解析
            List<Document> documents =
                    documentReaderService.read(file);

            // 3. 文档切分
            List<Document> chunks =
                    documentSplitService.split(documents);

            // 4. 转换 Chunk DTO
            List<DocumentSplitResponse.ChunkItem> items =
                    chunks.stream()
                            .map(document -> {

                                String text = document.getText() == null
                                        ? ""
                                        : document.getText();

                                return new DocumentSplitResponse.ChunkItem(
                                        document.getId(),
                                        text.length(),
                                        text,
                                        document.getMetadata()
                                );
                            })
                            .toList();

            // 5. 返回结果
            return new DocumentSplitResponse(
                    filename,
                    file.getSize(),
                    documents.size(),
                    chunks.size(),
                    items
            );

        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "文件解析失败",
                    e
            );
        }
    }

    /**
     * 上传文件并解析为 Spring AI Document
     */
    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public DocumentUploadResponse upload(
            @RequestParam("file") MultipartFile file) {

        // 1. 文件非空校验
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "上传文件不能为空"
            );
        }

        // 2. 文件格式校验
        String filename = file.getOriginalFilename();

        String extension = StringUtils.getFilenameExtension(filename);

        if (extension == null ||
                !ALLOWED_EXTENSIONS.contains(
                        extension.toLowerCase(Locale.ROOT))) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "暂不支持该文件格式"
            );
        }

        // 3. 调用 Service 解析文件
        List<Document> documents;

        try {
            documents = documentReaderService.read(file);
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "文件解析失败",
                    e
            );
        }

        // 4. 转换为接口返回对象
        List<DocumentUploadResponse.DocumentItem> items =
                documents.stream()
                        .map(document -> {

                            String text = document.getText() == null
                                    ? ""
                                    : document.getText();

                            String preview = text.length() > 2000
                                    ? text.substring(0, 2000)
                                    : text;

                            return new DocumentUploadResponse.DocumentItem(
                                    document.getId(),
                                    text.length(),
                                    preview,
                                    document.getMetadata()
                            );
                        })
                        .toList();

        // 5. 返回解析结果
        return new DocumentUploadResponse(
                filename,
                file.getSize(),
                items.size(),
                items
        );
    }
}