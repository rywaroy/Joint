package org.joint.modules.file.storage;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import jakarta.annotation.PreDestroy;
import org.joint.config.OssProperties;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class OssStorage implements StorageStrategy {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final OssProperties ossProperties;
    private OSS ossClient;

    public OssStorage(OssProperties ossProperties) {
        this.ossProperties = ossProperties;
    }

    @Override
    public FileInfo upload(MultipartFile file, String module) throws IOException {
        String dateDir = LocalDate.now().format(DATE_FORMATTER);
        String extension = getExtension(file.getOriginalFilename());
        String filename = extension.isEmpty()
                ? UUID.randomUUID().toString()
                : UUID.randomUUID() + "." + extension;
        String key = ossProperties.getDir() + "/" + module + "/" + dateDir + "/" + filename;

        getClient().putObject(ossProperties.getBucket(), key, file.getInputStream());

        FileInfo fileInfo = new FileInfo();
        fileInfo.setFilename(filename);
        fileInfo.setOriginalName(file.getOriginalFilename());
        fileInfo.setMimeType(file.getContentType());
        fileInfo.setSize(file.getSize());
        fileInfo.setPath(key);
        fileInfo.setExtension(extension);
        fileInfo.setUrl("https://" + ossProperties.getBucket() + "." + ossProperties.getEndpoint() + "/" + key);
        return fileInfo;
    }

    @PreDestroy
    public void destroy() {
        if (ossClient != null) {
            ossClient.shutdown();
        }
    }

    private OSS getClient() {
        if (ossClient == null) {
            ossClient = new OSSClientBuilder().build(
                    ossProperties.getEndpoint(),
                    ossProperties.getAccessKeyId(),
                    ossProperties.getAccessKeySecret()
            );
        }
        return ossClient;
    }

    private String getExtension(String filename) {
        if (!StringUtils.hasText(filename) || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
