package com.example.mediaservice;


import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/media")
public class MediaFileController {
    private final MediaFileService mediaFileService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaFileUrlDto> uploadImage(@RequestParam("image") MultipartFile file) throws IOException {
        return mediaFileService.uploadImage(file);
    }

    @GetMapping(path = "/get/{id}")
    public ResponseEntity<byte[]> getImageById(@PathVariable("id") String id) throws IOException {
        return mediaFileService.getImageById(id);
    }
}