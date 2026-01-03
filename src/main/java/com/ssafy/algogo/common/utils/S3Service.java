package com.ssafy.algogo.common.utils;

import com.ssafy.algogo.common.advice.CustomException;
import com.ssafy.algogo.common.advice.ErrorCode;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${cloud.aws.cloudfront.domain}")
    private String cloudfrontDomain;

    @Value("${file.upload.profile-image-path}")
    private String profileImagePath;

    @Value("${file.upload.submission-code-path}")
    private String submissionCodePath;

    @Value("${file.upload.allowed-extensions}")
    private String allowedExtensions;

    @Value("${file.upload.max-size}")
    private long maxSize;

    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
        "image/jpeg", "image/png", "image/jpg", "image/webp"
    );

    public String uploadProfileImage(MultipartFile file, Long userId) {

        // 1. 파일 검증
        validateFile(file);

        // 2. S3 key 생성 (profile/{userId}/{UUID}.{확장자})
        String s3Key = generateS3Key(file, userId, profileImagePath);

        // 3. S3 업로드
        uploadToS3(file, s3Key);
        log.info("S3 업로드 완료: {}", s3Key);
        log.info("S3 업로드 유저 PK: {}", userId);

        // 4. CloudFront URL 반환
        return convertToCloudFrontUrl(s3Key);
    }

    public void deleteImage(String imageUrl) {
        if (imageUrl == null | imageUrl.isEmpty()) {
            return;
        }

        try {
            String s3Key = extractS3Key(imageUrl);

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("S3 이미지 삭제 완료: {}", s3Key);
        } catch (Exception e) {
            log.error("S3 이미지 삭제 실패: {}", imageUrl, e);
            // 실패해도 비즈니스 로직은 진행,
        }
    }

    public String uploadText(Long userId, String text) {
        if (text.isBlank()) {
            throw new CustomException("제출 code text가 비어있습니다.", ErrorCode.FAILED_FILE_UPLOAD);
        }

        String s3Key = generateS3Key(userId, submissionCodePath);
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(s3Key)
            .contentType("text/plain; charset=UTF-8")
            .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromString(text));
        return convertToCloudFrontUrl(s3Key);
    }

    // TODO : FileUploadException 발생 vs CustomException 발생 ?
    private void validateFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new CustomException("파일이 비어있습니다.", ErrorCode.EMPTY_FILE);
        }

        if (file.getSize() > maxSize) {
            throw new CustomException(
                String.format("파일 크기는 %dMB를 초과할 수 없습니다.", maxSize / 1024 / 1024),
                ErrorCode.OVERSIZE_FILE);
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType)) {
            throw new CustomException("지원하지 않는 파일 형식입니다.", ErrorCode.INVALID_FILE_TYPE);
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !hasValidExtension(originalFilename)) {
            throw new CustomException("허용되지 않은 파일 확장자입니다.", ErrorCode.INVALID_FILE_EXTENSION);
        }

    }

    // S3 key 생성
    private String generateS3Key(MultipartFile file, Long userId, String path) {
        String originalFilename = file.getOriginalFilename();
        String extension = extractExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID().toString() + "." + extension;

        return path + userId + "/" + uniqueFilename;
    }

    private String generateS3Key(Long userId, String path) {
        return path + userId + "/" + UUID.randomUUID().toString() + ".txt";
    }

    private void uploadToS3(MultipartFile file, String s3Key) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build();
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
        } catch (IOException e) {
            log.error("S3 업로드 실패: {}", s3Key, e);
            throw new CustomException("파일 업로드에 실패했습니다.", ErrorCode.FAILED_FILE_UPLOAD);
        }
    }

    // S3 key를 CloudFront URL로 변환
    private String convertToCloudFrontUrl(String s3Key) {
        return "https://" + cloudfrontDomain + "/" + s3Key;
    }

    // CloudFront URL에서 S3 key 추출
    private String extractS3Key(String cloudFrontUrl) {
        return cloudFrontUrl.replace("https://" + cloudfrontDomain + "/", "");
    }

    // 파일 확장자 추출
    private String extractExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            throw new CustomException("파일 확장자가 없습니다.", ErrorCode.NOT_FOUND_FILE_EXTENSION);
        }
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }

    // 허용된 확장자인지 확인
    private boolean hasValidExtension(String filename) {
        String extension = extractExtension(filename);
        return Arrays.asList(allowedExtensions.split(",")).contains(extension);
    }

    /**
     * CloudFront URL에서 코드 텍스트 다운로드
     * 
     * @param cloudFrontUrl CloudFront URL
     * @return 코드 텍스트 내용
     */
    public String downloadText(String cloudFrontUrl) {
        if (cloudFrontUrl == null || cloudFrontUrl.isEmpty()) {
            throw new CustomException("URL이 비어있습니다.", ErrorCode.INVALID_PARAMETER);
        }

        try {
            String s3Key = extractS3Key(cloudFrontUrl);
            
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();

            ResponseInputStream<GetObjectResponse> response = s3Client.getObject(getObjectRequest);
            String content = new String(response.readAllBytes());
            response.close();
            
            return content;
        } catch (Exception e) {
            log.error("S3 코드 다운로드 실패: {}", cloudFrontUrl, e);
            throw new CustomException("코드 다운로드에 실패했습니다.", ErrorCode.FAILED_FILE_UPLOAD);
        }
    }
}
