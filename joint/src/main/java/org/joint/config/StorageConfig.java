package org.joint.config;

import org.joint.modules.file.storage.LocalStorage;
import org.joint.modules.file.storage.OssStorage;
import org.joint.modules.file.storage.StorageStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class StorageConfig {

    @Bean
    public StorageStrategy storageStrategy(FileStorageProperties fileStorageProperties,
                                           OssProperties ossProperties) {
        if (StringUtils.hasText(ossProperties.getAccessKeyId())) {
            return new OssStorage(ossProperties);
        }
        return new LocalStorage(fileStorageProperties);
    }
}
