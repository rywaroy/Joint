package org.joint.modules.file.storage;

import org.joint.config.FileStorageProperties;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class LocalStorage implements StorageStrategy {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final FileStorageProperties fileStorageProperties;

    public LocalStorage(FileStorageProperties fileStorageProperties) {
        this.fileStorageProperties = fileStorageProperties;
    }

    @Override
    public FileInfo upload(MultipartFile file, String module) throws IOException {
        String dateDir = LocalDate.now().format(DATE_FORMATTER);
        Path root = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();
        Path directory = root.resolve(module).resolve(dateDir);
        Files.createDirectories(directory);

        String extension = getExtension(file.getOriginalFilename());
        String filename = extension.isEmpty()
                ? UUID.randomUUID().toString()
                : UUID.randomUUID() + "." + extension;
        Path filePath = directory.resolve(filename);
        Files.write(filePath, file.getBytes());

        FileInfo fileInfo = new FileInfo();
        fileInfo.setFilename(filename);
        fileInfo.setOriginalName(file.getOriginalFilename());
        fileInfo.setMimeType(file.getContentType());
        fileInfo.setSize(file.getSize());
        fileInfo.setPath(filePath.toString());
        fileInfo.setExtension(extension);
        fileInfo.setUrl(buildUrl(module, dateDir, filename));
        return fileInfo;
    }

    private String buildUrl(String module, String dateDir, String filename) {
        return fileStorageProperties.getBaseUrl() + "/uploads/" + module + "/" + dateDir + "/" + filename;
    }

    private String getExtension(String filename) {
        if (!StringUtils.hasText(filename) || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
