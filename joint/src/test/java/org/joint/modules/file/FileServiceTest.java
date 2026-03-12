package org.joint.modules.file;

import org.joint.common.exception.BusinessException;
import org.joint.modules.file.storage.FileInfo;
import org.joint.modules.file.storage.StorageStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FileServiceTest {

    private StorageStrategy storageStrategy;
    private FileService fileService;

    @BeforeEach
    void setUp() {
        storageStrategy = mock(StorageStrategy.class);
        fileService = new FileService(storageStrategy);
    }

    @Test
    void uploadFileRejectsEmptyFile() {
        MultipartFile file = new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);

        assertThatThrownBy(() -> fileService.uploadFile(file))
                .isInstanceOf(BusinessException.class)
                .hasMessage("文件不能为空");
    }

    @Test
    void uploadFileDelegatesToStorageStrategy() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", "abc".getBytes());
        FileInfo fileInfo = new FileInfo();
        fileInfo.setFilename("stored.png");

        when(storageStrategy.upload(file)).thenReturn(fileInfo);

        FileInfo result = fileService.uploadFile(file);

        assertThat(result.getFilename()).isEqualTo("stored.png");
        verify(storageStrategy).upload(file);
    }

    @Test
    void uploadFilesSkipsEmptyEntriesAndKeepsNonEmptyResults() throws IOException {
        MockMultipartFile emptyFile = new MockMultipartFile("files", "empty.txt", "text/plain", new byte[0]);
        MockMultipartFile firstFile = new MockMultipartFile("files", "a.txt", "text/plain", "a".getBytes());
        MockMultipartFile secondFile = new MockMultipartFile("files", "b.txt", "text/plain", "b".getBytes());
        FileInfo firstInfo = new FileInfo();
        firstInfo.setFilename("a.txt");
        FileInfo secondInfo = new FileInfo();
        secondInfo.setFilename("b.txt");

        when(storageStrategy.upload(firstFile)).thenReturn(firstInfo);
        when(storageStrategy.upload(secondFile)).thenReturn(secondInfo);

        List<FileInfo> result = fileService.uploadFiles(new MultipartFile[]{emptyFile, firstFile, secondFile});

        assertThat(result).extracting(FileInfo::getFilename).containsExactly("a.txt", "b.txt");
        verify(storageStrategy).upload(firstFile);
        verify(storageStrategy).upload(secondFile);
    }
}
