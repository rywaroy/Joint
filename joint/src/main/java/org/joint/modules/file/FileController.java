package org.joint.modules.file;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.joint.common.annotation.Log;
import org.joint.common.enums.BusinessType;
import org.joint.modules.file.storage.FileInfo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
@Tag(name = "文件管理")
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    @Log(module = "文件管理", type = BusinessType.IMPORT, description = "单文件上传")
    @Operation(summary = "单文件上传")
    public FileInfo upload(@RequestParam("file") MultipartFile file) throws IOException {
        return fileService.uploadFile(file);
    }

    @PostMapping("/upload-files")
    @Log(module = "文件管理", type = BusinessType.IMPORT, description = "多文件上传")
    @Operation(summary = "多文件上传")
    public List<FileInfo> uploadFiles(@RequestParam("files") MultipartFile[] files) throws IOException {
        return fileService.uploadFiles(files);
    }
}
