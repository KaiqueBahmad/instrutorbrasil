package kaiquebt.dev.instrutorbrasil.service;

import kaiquebt.dev.instrutorbrasil.config.AwsProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.time.Duration;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

	private final S3Client s3Client;
	private final S3Presigner s3Presigner;
	private final AwsProperties awsProperties;
	private final DefaultCredentialsProvider credentialsProvider;

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneOffset.UTC);
	private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC);

	/**
	 * Generates a presigned POST for uploading a file to S3 with enforced size limit
	 * The size limit is enforced by S3 BEFORE accepting the upload
	 *
	 * @param s3Key S3 object key
	 * @return PresignedPostData containing URL and form fields
	 */
	public PresignedPostData generatePresignedPost(String s3Key) {
		try {
			AwsCredentials credentials = credentialsProvider.resolveCredentials();
			String region = awsProperties.getRegion();
			String bucket = awsProperties.getS3().getBucket();
			long maxFileSizeBytes = awsProperties.getS3().getMaxFileSizeMb() * 1024L * 1024L;

			Instant now = Instant.now();
			Instant expiration = now.plusSeconds(awsProperties.getS3().getPresignedUrlExpirationMinutes() * 60L);

			String dateStamp = DATE_FORMATTER.format(now);
			String amzDateTime = DATETIME_FORMATTER.format(now);
			String credential = String.format("%s/%s/%s/s3/aws4_request", credentials.accessKeyId(), dateStamp, region);

			// Build policy
			String policy = buildPolicy(bucket, s3Key, credential, amzDateTime, expiration, maxFileSizeBytes);
			String policyBase64 = Base64.getEncoder().encodeToString(policy.getBytes(StandardCharsets.UTF_8));

			// Generate signature
			String signature = generateSignature(credentials.secretAccessKey(), dateStamp, region, policyBase64);

			// Build form fields
			Map<String, String> formFields = new LinkedHashMap<>();
			formFields.put("key", s3Key);
			formFields.put("x-amz-algorithm", "AWS4-HMAC-SHA256");
			formFields.put("x-amz-credential", credential);
			formFields.put("x-amz-date", amzDateTime);
			formFields.put("policy", policyBase64);
			formFields.put("x-amz-signature", signature);

			String url = String.format("https://%s.s3.%s.amazonaws.com/", bucket, region);

			log.debug("Generated presigned POST for key: {} with max size: {} MB", s3Key, awsProperties.getS3().getMaxFileSizeMb());

			return new PresignedPostData(url, formFields);

		} catch (Exception e) {
			log.error("Error generating presigned POST for key: {}", s3Key, e);
			throw new RuntimeException("Failed to generate presigned POST", e);
		}
	}

	private String buildPolicy(String bucket, String key, String credential, String amzDateTime, Instant expiration, long maxFileSizeBytes) {
		String expirationStr = expiration.toString();

		return String.format("""
				{
				  "expiration": "%s",
				  "conditions": [
				    {"bucket": "%s"},
				    {"key": "%s"},
				    {"x-amz-algorithm": "AWS4-HMAC-SHA256"},
				    {"x-amz-credential": "%s"},
				    {"x-amz-date": "%s"},
				    ["content-length-range", 1, %d]
				  ]
				}
				""", expirationStr, bucket, key, credential, amzDateTime, maxFileSizeBytes);
	}

	private String generateSignature(String secretKey, String dateStamp, String region, String policyBase64) throws Exception {
		byte[] kDate = hmacSHA256(("AWS4" + secretKey).getBytes(StandardCharsets.UTF_8), dateStamp);
		byte[] kRegion = hmacSHA256(kDate, region);
		byte[] kService = hmacSHA256(kRegion, "s3");
		byte[] kSigning = hmacSHA256(kService, "aws4_request");
		byte[] signature = hmacSHA256(kSigning, policyBase64);

		return bytesToHex(signature);
	}

	private byte[] hmacSHA256(byte[] key, String data) throws Exception {
		Mac mac = Mac.getInstance("HmacSHA256");
		mac.init(new SecretKeySpec(key, "HmacSHA256"));
		return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
	}

	private String bytesToHex(byte[] bytes) {
		StringBuilder result = new StringBuilder();
		for (byte b : bytes) {
			result.append(String.format("%02x", b));
		}
		return result.toString();
	}

	/**
	 * Generates a presigned GET URL for downloading a file from S3
	 * Overrides the Content-Type header to ensure correct MIME type
	 *
	 * @param s3Key S3 object key
	 * @param contentType MIME type to override (from database)
	 * @return Presigned GET URL as string
	 */
	public String generatePresignedGetUrl(String s3Key, String contentType) {
		try {
			GetObjectRequest getObjectRequest = GetObjectRequest.builder()
					.bucket(awsProperties.getS3().getBucket())
					.key(s3Key)
					.responseContentType(contentType)  // Override Content-Type
					.build();

			GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
					.signatureDuration(Duration.ofMinutes(awsProperties.getS3().getPresignedUrlExpirationMinutes()))
					.getObjectRequest(getObjectRequest)
					.build();

			PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);

			String url = presignedRequest.url().toString();
			log.debug("Generated presigned GET URL for key: {} with Content-Type: {}", s3Key, contentType);
			return url;

		} catch (Exception e) {
			log.error("Error generating presigned GET URL for key: {}", s3Key, e);
			throw new RuntimeException("Failed to generate presigned GET URL", e);
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

	/**
	 * Record class to hold presigned POST data
	 */
	public record PresignedPostData(String url, Map<String, String> formFields) {}
}
