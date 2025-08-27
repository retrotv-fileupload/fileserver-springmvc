package dev.retrotv.fileserver.common.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Getter
@Component
@ConfigurationProperties(prefix = "fileserver")
public class FileServerProperties {
    private int chunkSize = 8 * 1024;
    private String uploadDir = "uploads/";
    private String tempDir = "tmp/";
}
