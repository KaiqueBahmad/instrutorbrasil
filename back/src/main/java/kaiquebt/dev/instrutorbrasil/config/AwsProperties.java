package kaiquebt.dev.instrutorbrasil.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.aws")
public class AwsProperties {

    private String region;
    private S3Properties s3 = new S3Properties();

    @Data
    public static class S3Properties {

        private String bucket;
        private Integer presignedUrlExpirationMinutes;
        private Integer maxFileSizeMb;
        private String endpointOverride;
    }
}
