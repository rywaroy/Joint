package org.joint.modules.file.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface StorageStrategy {

    FileInfo upload(MultipartFile file, String module) throws IOException;
}
