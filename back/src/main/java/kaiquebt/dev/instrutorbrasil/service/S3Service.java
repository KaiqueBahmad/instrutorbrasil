package kaiquebt.dev.instrutorbrasil.service;

import kaiquebt.dev.instrutorbrasil.config.AwsProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

	private final S3Client s3Client;
	private final S3Presigner s3Presigner;
	private final AwsProperties awsProperties;

	/**
	 * Generates a presigned URL for uploading a file to S3
	 *
	 * @param s3Key S3 object key
	 * @param contentType MIME type of the file
	 * @return Presigned URL as string
	 */
	public String generatePresignedUploadUrl(String s3Key, String contentType) {
		try {
			PutObjectRequest putObjectRequest = PutObjectRequest.builder()
					.bucket(awsProperties.getS3().getBucket())
					.key(s3Key)
					.contentType(contentType)
					.build();

			PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
					.signatureDuration(Duration.ofMinutes(awsProperties.getS3().getPresignedUrlExpirationMinutes()))
					.putObjectRequest(putObjectRequest)
					.build();

			PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

			String url = presignedRequest.url().toString();
			log.debug("Generated presigned upload URL for key: {}", s3Key);
			return url;

		} catch (Exception e) {
			log.error("Error generating presigned URL for key: {}", s3Key, e);
			throw new RuntimeException("Failed to generate presigned URL", e);
		}
	}

	/**
	 * Retrieves file metadata from S3
	 *
	 * @param s3Key S3 object key
	 * @return S3FileMetadata containing file size and MIME type
	 * @throws IllegalArgumentException if file doesn't exist in S3
	 */
	public S3FileMetadata getFileMetadata(String s3Key) {
		try {
			HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
					.bucket(awsProperties.getS3().getBucket())
					.key(s3Key)
					.build();

			HeadObjectResponse headObjectResponse = s3Client.headObject(headObjectRequest);

			Long fileSize = headObjectResponse.contentLength();
			String mimeType = headObjectResponse.contentType();

			log.debug("Retrieved metadata for key: {} - Size: {} bytes, Type: {}", s3Key, fileSize, mimeType);

			return new S3FileMetadata(fileSize, mimeType);

		} catch (NoSuchKeyException e) {
			log.error("File not found in S3: {}", s3Key);
			throw new IllegalArgumentException("File not found in S3: " + s3Key);
		} catch (Exception e) {
			log.error("Error retrieving metadata for key: {}", s3Key, e);
			throw new RuntimeException("Failed to retrieve file metadata from S3", e);
		}
	}

	/**
	 * Record class to hold S3 file metadata
	 */
	public record S3FileMetadata(Long fileSize, String mimeType) {}
}
