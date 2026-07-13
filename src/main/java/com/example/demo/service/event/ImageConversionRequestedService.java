package com.example.demo.service.event;

import com.example.demo.endpoint.event.model.ImageConversionRequested;
import com.example.demo.file.bucket.BucketComponent;
import com.example.demo.mail.Email;
import com.example.demo.mail.Mailer;
import jakarta.mail.internet.InternetAddress;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ImageConversionRequestedService implements Consumer<ImageConversionRequested> {

  private final BucketComponent bucketComponent;
  private final Mailer mailer;

  @SneakyThrows
  @Override
  public void accept(ImageConversionRequested event) {
    var extension = event.getBucketKey().endsWith(".png") ? ".png" : ".jpg";

    File originalFile = bucketComponent.download(event.getBucketKey());

    File bwFile = File.createTempFile("bw-", extension);
    convertToGrayscale(originalFile, bwFile, extension);

    var bwKey = "bw/" + event.getBucketKey().replace("originals/", "");
    bucketComponent.upload(bwFile, bwKey);

    var presignedUri = bucketComponent.presign(bwKey, Duration.ofDays(7));

    var recipient = new InternetAddress(event.getEmail());
    var email =
        new Email(
            recipient,
            List.of(),
            List.of(),
            "Votre image en noir et blanc",
            "Voici le lien vers votre image convertie : " + presignedUri,
            List.of());
    mailer.accept(email);
  }

  private void convertToGrayscale(File input, File output, String extension) throws Exception {
    BufferedImage original = ImageIO.read(input);
    BufferedImage grayscale =
        new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
    Graphics2D g = grayscale.createGraphics();
    g.drawImage(original, 0, 0, null);
    g.dispose();
    ImageIO.write(grayscale, extension.replace(".", ""), output);
  }
}
