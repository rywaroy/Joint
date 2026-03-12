package org.joint.modules.file.storage;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "文件上传结果")
public class FileInfo {

    @Schema(description = "存储文件名")
    private String filename;

    @Schema(description = "原始文件名")
    private String originalName;

    @Schema(description = "MIME 类型")
    private String mimeType;

    @Schema(description = "文件大小(字节)", example = "1024")
    private Long size;

    @Schema(description = "存储路径")
    private String path;

    @Schema(description = "可访问地址")
    private String url;

    @Schema(description = "文件扩展名")
    private String extension;
}
