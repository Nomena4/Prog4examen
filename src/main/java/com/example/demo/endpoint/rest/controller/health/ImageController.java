package com.example.demo.endpoint.rest.controller;

import com.example.demo.entity.ImageSubmission;
import com.example.demo.service.ImageService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/images")
@AllArgsConstructor
public class ImageController {

  private final ImageService imageService;

  @PostMapping(consumes = "multipart/form-data")
  public ImageSubmission submit(
      @RequestParam("file") MultipartFile file, @RequestParam("email") String email) {
    return imageService.submit(file, email);
  }

  @GetMapping
  public List<ImageSubmission> findAll() {
    return imageService.findAll();
  }
}
