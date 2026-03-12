package org.joint.modules.file;

import lombok.RequiredArgsConstructor;
import org.joint.common.exception.BusinessException;
import org.joint.modules.file.storage.FileInfo;
import org.joint.modules.file.storage.StorageStrategy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
