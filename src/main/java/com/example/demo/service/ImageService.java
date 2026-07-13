package com.example.demo.service;

import com.example.demo.endpoint.event.EventProducer;
import com.example.demo.endpoint.event.model.ImageConversionRequested;
import com.example.demo.entity.ImageSubmission;
import com.example.demo.file.bucket.BucketComponent;
import com.example.demo.repository.ImageSubmissionRepository;
import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@AllArgsConstructor
public class ImageService {

  private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png");

  private final ImageSubmissionRepository repository;
  private final BucketComponent bucketComponent;
  private final EventProducer<ImageConversionRequested> eventProducer;

  @SneakyThrows
  public ImageSubmission submit(MultipartFile file, String email) {
    if (file.isEmpty() || !ALLOWED_TYPES.contains(file.getContentType())) {
      throw new IllegalArgumentException("Seuls les fichiers JPEG et PNG sont acceptés");
    }

    var extension = "image/png".equals(file.getContentType()) ? ".png" : ".jpg";
    var bucketKey = "originals/" + UUID.randomUUID() + extension;

    File tempFile = File.createTempFile("upload", extension);
    file.transferTo(tempFile);
    bucketComponent.upload(tempFile, bucketKey);

    var submission = new ImageSubmission(null, bucketKey, email);
    submission = repository.save(submission);

    var event = ImageConversionRequested.builder().bucketKey(bucketKey).email(email).build();
    eventProducer.accept(List.of(event));

    return submission;
  }

  public List<ImageSubmission> findAll() {
    return repository.findAll();
  }
}
