package com.example.mediaservice;


import com.example.commondto.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor

public class MediaFileService {
    private final MediaFileRepository mediaFileRepository;
    private static final String IMAGES_PATH = "/api/media/get/";

    public ResponseEntity<MediaFileUrlDto> uploadImage(MultipartFile file) throws IOException {
        if (isValidFile(file)) {
            byte[] fileData;

            // Nếu là ảnh thì nén lại, còn file văn bản thì lưu nguyên gốc
            if (file.getContentType() != null && file.getContentType().startsWith("image/")) {
                fileData = ImageUtils.compressImage(file.getBytes());
            } else {
                fileData = file.getBytes();
            }

            MediaFile savedFile = mediaFileRepository.save(MediaFile.builder()
                    .name(file.getOriginalFilename())
                    .type(file.getContentType())
                    .data(fileData)
                    .build());

            String fileUrl = IMAGES_PATH + savedFile.getId();

            return ResponseEntity.ok(MediaFileUrlDto.builder()
                    .pathUrl(fileUrl)
                    .build());
        } else {
            throw new BadRequestException("Invalid file type");
        }
    }
    @Cacheable(value = "mediaFile", key = "#id")
    public MediaFile getFileFromCache(String id) {
        return mediaFileRepository.findById(id).orElse(null);
    }

    public ResponseEntity<byte[]> getImageById(String id) throws IOException {

        MediaFile mediaFile = getFileFromCache(id);

        if (mediaFile == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        byte[] fileData;

        if (mediaFile.getType() != null && mediaFile.getType().startsWith("image/")) {
            fileData = ImageUtils.decompressImage(mediaFile.getData());
        } else {
            fileData = mediaFile.getData();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.valueOf(mediaFile.getType()))
                .header("Content-Disposition", "inline; filename=\"" + mediaFile.getName() + "\"")
                .body(fileData);
    }




    private boolean isValidFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) return false;

        // Cho phép ảnh
        if (contentType.startsWith("image/")) return true;

        // Cho phép các file văn bản
        switch (contentType) {
            case "application/pdf":
            case "application/msword": // .doc
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document": // .docx
            case "text/plain": // .txt
                return true;
            default:
                return false;
        }
    }


}
