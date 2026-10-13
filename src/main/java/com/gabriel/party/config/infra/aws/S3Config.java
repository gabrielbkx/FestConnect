package com.gabriel.party.config.infra.aws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

import java.net.URI;

/**
 * Cliente S3. O mesmo código fala com a AWS ou com qualquer storage compatível com S3
 * (SeaweedFS em dev, Cloudflare R2 etc.); o que muda é só configuração.
 */
@Configuration
public class S3Config {

    @Value("${storage.s3.region}")
    private String region;

    // Vazio = AWS. Preenchido = storage compatível com S3 fora da AWS.
    @Value("${storage.s3.endpoint:}")
    private String endpoint;

    // Vazio = cadeia padrão da AWS (variáveis AWS_*, perfil, papel da EC2).
    @Value("${storage.s3.access-key:}")
    private String accessKey;

    @Value("${storage.s3.secret-key:}")
    private String secretKey;

    @Bean
    public S3Client s3Client() {
        S3ClientBuilder builder = S3Client.builder().region(Region.of(region));

        if (accessKey.isBlank()) {
            builder.credentialsProvider(DefaultCredentialsProvider.create());
        } else {
            builder.credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)));
        }

        if (!endpoint.isBlank()) {
            // Path-style (http://host/bucket/arquivo): os storages locais não resolvem o
            // estilo virtual-host da AWS (http://bucket.host/arquivo).
            builder.endpointOverride(URI.create(endpoint)).forcePathStyle(true);
        }

        return builder.build();
    }
}
