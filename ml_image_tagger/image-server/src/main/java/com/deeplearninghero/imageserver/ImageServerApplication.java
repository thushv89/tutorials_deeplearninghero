package com.deeplearninghero.imageserver;


import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.apache.commons.imaging.ImageFormat;
import org.apache.commons.imaging.ImageFormats;
import org.apache.commons.imaging.Imaging;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Base64;
import java.util.UUID;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@SpringBootApplication
public class ImageServerApplication {

    private final GCSService gcsService;
    private final ObjectDetectionService objectDetectionService;
    private final DatastoreService datastoreService;
    private static final Logger logger = LoggerFactory.getLogger(ImageServerApplication.class);

    @Autowired
    public ImageServerApplication(GCSService gcsService, ObjectDetectionService objectDetectionService, DatastoreService datastoreService) {
        // Make sure you've run
        // gcloud application-default login with service account impersonation
        this.gcsService = gcsService;
        this.objectDetectionService = objectDetectionService;
        this.datastoreService = datastoreService;
    }

    @RequestMapping("/status")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.status(HttpStatus.OK).body("Service is running.");
    }

    @RequestMapping("/upload_image")
    public ResponseEntity<String> uploadImage(@RequestBody String base64Image) {

        String uuid = UUID.randomUUID().toString();
        byte[] imageBytes = Base64.getDecoder().decode(base64Image);
        // TODO detect the filetype and use it automatically

        String fileName = "";
        try {
            ImageFormat imageFormat = Imaging.guessFormat(imageBytes);
            fileName = uuid + "." + imageFormat.getDefaultExtension();
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }

        String gcsPath = gcsService.writeToBucket(imageBytes, fileName);

        // Now, you can use the detected objects to save tags to GCP Datastore
        CompletableFuture<Void> saveTagsFuture = CompletableFuture.supplyAsync(() -> objectDetectionService.detectObjects(imageBytes)).thenAccept(objectTags -> datastoreService.persistImageTags(uuid, gcsPath, objectTags));

        saveTagsFuture.exceptionally(ex -> {
            // Handle the exception here
            logger.error(ex.getMessage());
            throw new RuntimeException(ex.getMessage());
        });
        return ResponseEntity.status(HttpStatus.OK).body(String.format("{\"image_id\":\"%s\"}", uuid));
    }

    @RequestMapping("/get_image_tags")
    public List<String> getImageTags(@RequestParam String uuid) {
        // TODO: Implement logic
        return datastoreService.readImageTags(uuid);
    }

    public static void main(String[] args) {
        SpringApplication.run(ImageServerApplication.class, args);
    }
}
