package co.lemuel.academy.media.r2

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.net.URI
import java.time.Duration

@Configuration
class R2Config(
    @Value("\${academy.r2.endpoint}") private val endpoint: String,
    @Value("\${academy.r2.access-key}") private val accessKey: String,
    @Value("\${academy.r2.secret-key}") private val secretKey: String,
) {
    @Bean fun s3(): S3Client = S3Client.builder()
        .endpointOverride(URI.create(endpoint))
        .region(Region.of("auto"))
        .credentialsProvider(StaticCredentialsProvider.create(
            AwsBasicCredentials.create(accessKey, secretKey)))
        .forcePathStyle(true)
        .build()

    @Bean fun presigner(): S3Presigner = S3Presigner.builder()
        .endpointOverride(URI.create(endpoint))
        .region(Region.of("auto"))
        .credentialsProvider(StaticCredentialsProvider.create(
            AwsBasicCredentials.create(accessKey, secretKey)))
        .build()
}

class R2Service(
    private val presigner: S3Presigner,
    private val bucket: String,
) {
    /** 업로드용 presigned PUT URL (15분 유효) */
    fun presignPut(key: String, contentType: String): String {
        val req = PutObjectRequest.builder()
            .bucket(bucket).key(key).contentType(contentType).build()
        val presignReq = PutObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(15))
            .putObjectRequest(req).build()
        return presigner.presignPutObject(presignReq).url().toString()
    }

    fun objectKey(videoId: String, suffix: String = "") =
        "originals/${videoId}${suffix}"

    fun publicHlsKey(videoId: String) = "hls/${videoId}/master.m3u8"
}
