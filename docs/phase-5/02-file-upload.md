# 5.2 文件上传

## 学习目标

- 使用 MultipartFile 处理文件上传
- 实现本地存储和 OSS 存储（策略模式）
- 对照 Nexus file 模块实现

## 与 Nexus 对照

Nexus 文件上传使用策略模式：

```typescript
// 存储接口
interface IStorageStrategy {
    upload(file: Express.Multer.File): Promise<UploadResult>;
}

// 根据配置自动选择 LocalStorage 或 OssStorage
```

## 实践步骤

### 步骤 1：存储策略接口

```java
package com.joint.modules.common.file.storage;

public interface StorageStrategy {

    /**
     * 上传文件
     */
    FileInfo upload(MultipartFile file) throws IOException;
}

@Data
public class FileInfo {
    private String filename;       // 存储文件名
    private String originalName;   // 原始文件名
    private String mimeType;       // MIME 类型
    private Long size;             // 文件大小（字节）
    private String path;           // 存储路径
    private String url;            // 访问 URL
    private String extension;      // 文件扩展名
}
```

### 步骤 2：本地存储实现

```java
package com.joint.modules.common.file.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class LocalStorage implements StorageStrategy {

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public FileInfo upload(MultipartFile file) throws IOException {
        // 按日期分目录：uploads/20240101/
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Path dirPath = Paths.get(uploadDir, dateDir);
        Files.createDirectories(dirPath);

        // 生成文件名：UUID.ext
        String extension = getExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + "." + extension;
        Path filePath = dirPath.resolve(filename);

        // 写入文件
        Files.write(filePath, file.getBytes());

        // 构建返回信息
        FileInfo info = new FileInfo();
        info.setFilename(filename);
        info.setOriginalName(file.getOriginalFilename());
        info.setMimeType(file.getContentType());
        info.setSize(file.getSize());
        info.setPath(filePath.toString());
        info.setExtension(extension);
        info.setUrl(baseUrl + "/" + uploadDir + "/" + dateDir + "/" + filename);
        return info;
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
```

### 步骤 3：OSS 存储实现

添加依赖：

```xml
<dependency>
    <groupId>com.aliyun.oss</groupId>
    <artifactId>aliyun-sdk-oss</artifactId>
    <version>3.17.4</version>
</dependency>
```

```java
package com.joint.modules.common.file.storage;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class OssStorage implements StorageStrategy {

    @Value("${oss.endpoint}")
    private String endpoint;

    @Value("${oss.access-key-id}")
    private String accessKeyId;

    @Value("${oss.access-key-secret}")
    private String accessKeySecret;

    @Value("${oss.bucket}")
    private String bucket;

    @Value("${oss.dir:uploads}")
    private String dir;

    private OSS ossClient;

    private OSS getClient() {
        if (ossClient == null) {
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        }
        return ossClient;
    }

    @PreDestroy
    public void destroy() {
        if (ossClient != null) {
            ossClient.shutdown();
        }
    }

    @Override
    public FileInfo upload(MultipartFile file) throws IOException {
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String extension = getExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + "." + extension;
        String key = dir + "/" + dateDir + "/" + filename;

        getClient().putObject(bucket, key, file.getInputStream());

        String url = "https://" + bucket + "." + endpoint + "/" + key;

        FileInfo info = new FileInfo();
        info.setFilename(filename);
        info.setOriginalName(file.getOriginalFilename());
        info.setMimeType(file.getContentType());
        info.setSize(file.getSize());
        info.setPath(key);
        info.setExtension(extension);
        info.setUrl(url);
        return info;
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
```

### 步骤 4：配置自动选择

```java
package com.joint.config;

import com.joint.modules.common.file.storage.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfig {

    @Bean
    @ConditionalOnProperty(name = "oss.access-key-id", havingValue = "", matchIfMissing = true)
    public StorageStrategy localStorage() {
        return new LocalStorage();
    }

    @Bean
    @ConditionalOnProperty(name = "oss.access-key-id")
    public StorageStrategy ossStorage() {
        return new OssStorage();
    }
}
```

### 步骤 5：配置文件

```yaml
# application.yml
file:
  upload-dir: uploads
  max-size-mb: 10

app:
  base-url: http://localhost:8080

# 配置 OSS 后自动切换（不配置则使用本地存储）
# oss:
#   endpoint: oss-cn-hangzhou.aliyuncs.com
#   access-key-id: your-key
#   access-key-secret: your-secret
#   bucket: your-bucket
#   dir: uploads

spring:
  servlet:
    multipart:
      max-file-size: ${file.max-size-mb:10}MB
      max-request-size: ${file.max-size-mb:10}MB
```

### 步骤 6：配置静态资源访问（本地存储）

```java
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }
}
```

### 步骤 7：Service 和 Controller

```java
// FileService.java
@Service
@RequiredArgsConstructor
public class FileService {

    private final StorageStrategy storageStrategy;

    public FileInfo uploadFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }
        return storageStrategy.upload(file);
    }

    public List<FileInfo> uploadFiles(MultipartFile[] files) throws IOException {
        List<FileInfo> results = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                results.add(storageStrategy.upload(file));
            }
        }
        return results;
    }
}

// FileController.java
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
@Tag(name = "文件管理")
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    @Operation(summary = "单文件上传")
    public FileInfo upload(@RequestParam("file") MultipartFile file) throws IOException {
        return fileService.uploadFile(file);
    }

    @PostMapping("/upload-files")
    @Operation(summary = "多文件上传")
    public List<FileInfo> uploadFiles(
            @RequestParam("files") MultipartFile[] files) throws IOException {
        return fileService.uploadFiles(files);
    }
}
```

## 架构图

```
FileController
    │
    ├── POST /file/upload        → FileService.uploadFile()
    └── POST /file/upload-files  → FileService.uploadFiles()
                                        │
                                        ▼
                              StorageStrategy (接口)
                                   │
                     ┌─────────────┴─────────────┐
                     │                           │
                LocalStorage                OssStorage
              (未配置 OSS 时)             (配置 OSS 后)
```

## 与 Nexus 对照

| Nexus | Joint |
|-------|-------|
| `IStorageStrategy` 接口 | `StorageStrategy` 接口 |
| `memoryStorage()` (Multer) | `MultipartFile` (Spring) |
| 工厂函数选择存储 | `@ConditionalOnProperty` 自动选择 |
| `ali-oss` npm 包 | `aliyun-sdk-oss` Maven 包 |

## 知识点总结

| 概念 | 说明 |
|------|------|
| MultipartFile | Spring 文件上传抽象 |
| 策略模式 | 接口 + 多实现切换存储方式 |
| @ConditionalOnProperty | 根据配置条件注册 Bean |
| 静态资源映射 | WebMvcConfigurer 映射本地文件目录 |

## 练习任务

1. 实现本地文件上传
2. 配置静态资源访问，验证上传后的文件可以通过 URL 访问
3. （可选）配置 OSS 并测试云端上传
