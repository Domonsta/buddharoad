// src/main/java/com/buddharoad/service/storage/LocalStorageService.java
package com.buddharoad.service.storage;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service // Spring 빈으로 등록하여 다른 서비스에서 주입받아 사용할 수 있게 해줘!
public class LocalStorageService implements StorageService {

    // 💡 파일을 저장할 기본 디렉토리. 프로젝트 루트의 'uploads' 폴더에 저장될 거야.
    // Window 환경이라면 C:\\your-project-path\\uploads 처럼 절대경로로도 지정 가능하지만,
    // 프로젝트 내 상대경로가 개발 환경에서 더 유연해.
    private final String UPLOAD_BASE_DIR = "uploads/";

    public LocalStorageService() {
        // 💡 LocalStorageService 객체가 생성될 때, 업로드 디렉토리가 없으면 자동으로 생성해줘.
        try {
            Files.createDirectories(Paths.get(UPLOAD_BASE_DIR));
            System.out.println("DEBUG: Local file upload base directory created: " + Paths.get(UPLOAD_BASE_DIR).toAbsolutePath());
        } catch (IOException e) {
            System.err.println("ERROR: Local upload base directory creation failed: " + e.getMessage());
            // 실제 서비스에서는 이 시점에 애플리케이션을 시작하지 못하도록 예외를 던지는 것이 더 좋아.
            throw new RuntimeException("파일 업로드 기본 디렉토리 생성 실패", e);
        }
    }

    @Override
    public String uploadFile(MultipartFile file, String directory) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 비어있습니다.");
        }

        // 파일명 중복 방지를 위해 UUID를 사용하고, 원본 확장자를 유지해.
        String originalFileName = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

        // 최종 저장될 디렉토리 경로 (예: uploads/temple-photos/)
        Path uploadDirectoryPath = Paths.get(UPLOAD_BASE_DIR + directory);
        // 최종 파일 경로 (예: uploads/temple-photos/unique-filename.jpg)
        Path filePath = uploadDirectoryPath.resolve(uniqueFileName);

        try {
            // 해당 디렉토리가 없으면 생성 (예: uploads/temple-photos/ 부분이 없으면 만들어줘)
            Files.createDirectories(uploadDirectoryPath);
            file.transferTo(filePath); // 실제 파일 저장

            // 💡 저장된 파일의 '가상' URL을 반환해.
            // 이 URL은 클라이언트(프론트엔드)가 이미지를 요청할 때 사용할 경로야.
            // 나중에 Spring 설정에서 "/uploaded/**" 패턴을 실제 로컬 파일 경로와 연결해줘야 해!
            return "/uploaded/" + directory + uniqueFileName;

        } catch (IOException e) {
            System.err.println("ERROR: 로컬 파일 업로드 실패 - " + originalFileName + ", 이유: " + e.getMessage());
            throw new RuntimeException("로컬 파일 업로드 실패: " + originalFileName, e);
        }
    }

    @Override
    public List<String> uploadFiles(List<MultipartFile> files, String directory) {
        // 여러 파일을 순회하며 uploadFile 메서드를 호출하고, 그 결과 URL 목록을 반환해.
        return files.stream()
                .map(file -> uploadFile(file, directory))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteFile(String fileUrl) {
        // 💡 URL에서 실제 파일 시스템 경로를 역추적하여 삭제해.
        // "/uploaded/"라는 가상 경로를 제거하고, 실제 파일 경로를 찾아야 해.
        if (fileUrl.startsWith("/uploaded/")) {
            String relativePath = fileUrl.substring("/uploaded/".length());
            Path filePath = Paths.get(UPLOAD_BASE_DIR + relativePath);
            try {
                if (Files.exists(filePath)) { // 파일이 존재하는지 확인
                    Files.delete(filePath); // 파일 삭제
                    System.out.println("DEBUG: 로컬 파일 삭제 완료: " + filePath.toAbsolutePath());
                } else {
                    System.out.println("DEBUG: 삭제할 로컬 파일을 찾을 수 없음: " + filePath.toAbsolutePath());
                }
            } catch (IOException e) {
                System.err.println("ERROR: 로컬 파일 삭제 실패: " + filePath.toAbsolutePath() + " - " + e.getMessage());
                throw new RuntimeException("로컬 파일 삭제 실패: " + fileUrl, e);
            }
        } else {
            // "/uploaded/"로 시작하지 않는 URL은 로컬에서 관리하는 파일이 아니라고 판단해.
            System.out.println("DEBUG: 로컬 스토리지에서 관리하는 URL이 아님. 삭제 스킵: " + fileUrl);
        }
    }

    @Override
    public void deleteFiles(List<String> fileUrls) {
        // 여러 파일을 순회하며 deleteFile 메서드를 호출해.
        fileUrls.forEach(this::deleteFile);
    }
}