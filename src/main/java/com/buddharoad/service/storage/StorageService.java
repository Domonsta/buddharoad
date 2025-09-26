package com.buddharoad.service.storage;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/**
 * 파일을 클라우드 스토리지 또는 로컬에 업로드하고 삭제하는 기능을 정의하는 인터페이스.
 * 이 인터페이스를 통해 실제 저장 방식에 상관없이 파일 관리 로직을 일관되게 처리할 수 있습니다.
 */
public interface StorageService {

    /**
     * 단일 파일을 스토리지에 업로드하고 해당 파일의 접근 가능한 URL을 반환합니다.
     *
     * @param file      업로드할 MultipartFile 객체. 클라이언트로부터 받은 파일 데이터.
     * @param directory 스토리지 내에서 파일을 저장할 디렉토리 경로 (예: "temple-photos/", "review-photos/").
     * 마지막에 '/'를 포함하는 것이 일반적입니다.
     * @return          업로드된 파일의 공개 접근 URL.
     * @throws RuntimeException 파일 업로드 중 예외 발생 시. (예: IO Exception, 스토리지 서비스 에러)
     */
    String uploadFile(MultipartFile file, String directory);

    /**
     * 여러 파일을 스토리지에 업로드하고 각 파일의 접근 가능한 URL 목록을 반환합니다.
     * 이 메서드는 내부적으로 {@code uploadFile(MultipartFile file, String directory)}을 호출하여 각 파일을 처리합니다.
     *
     * @param files     업로드할 MultipartFile 객체들의 목록.
     * @param directory 스토리지 내에서 파일을 저장할 디렉토리 경로.
     * @return          업로드된 파일들의 공개 접근 URL 목록.
     * @throws RuntimeException 파일 업로드 중 예외 발생 시.
     */
    List<String> uploadFiles(List<MultipartFile> files, String directory);

    /**
     * 특정 URL에 해당하는 파일을 스토리지에서 삭제합니다.
     * 파일 URL에서 스토리지 내의 파일 경로를 추출하여 삭제를 수행합니다.
     *
     * @param fileUrl 삭제할 파일의 전체 URL.
     * @throws RuntimeException 파일 삭제 중 예외 발생 시.
     */
    void deleteFile(String fileUrl);

    /**
     * 여러 URL에 해당하는 파일들을 스토리지에서 삭제합니다.
     * 이 메서드는 내부적으로 {@code deleteFile(String fileUrl)}을 호출하여 각 파일을 처리합니다.
     *
     * @param fileUrls 삭제할 파일들의 URL 목록.
     * @throws RuntimeException 파일 삭제 중 예외 발생 시.
     */
    void deleteFiles(List<String> fileUrls);
}