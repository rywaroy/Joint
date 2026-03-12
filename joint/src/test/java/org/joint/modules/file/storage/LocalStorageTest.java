package org.joint.modules.file.storage;

import org.joint.config.FileStorageProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class LocalStorageTest {

    @TempDir
    Path tempDir;

    @Test
    void uploadWritesFileAndBuildsPublicUrl() throws Exception {
        FileStorageProperties properties = new FileStorageProperties();
        properties.setUploadDir(tempDir.toString());
        properties.setBaseUrl("http://localhost:8080");

        LocalStorage localStorage = new LocalStorage(properties);
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", "image".getBytes());

        FileInfo result = localStorage.upload(file);

        assertThat(result.getOriginalName()).isEqualTo("avatar.png");
        assertThat(result.getUrl()).startsWith("http://localhost:8080/uploads/");
        assertThat(Files.exists(Path.of(result.getPath()))).isTrue();
    }
}
