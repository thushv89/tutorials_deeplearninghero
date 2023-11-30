package com.deeplearninghero.imageserver;

import com.google.api.gax.rpc.PermissionDeniedException;
import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

@Service
public class ObjectDetectionService {

    private final ImageAnnotatorClient visionClient;

    public ObjectDetectionService() throws PermissionDeniedException, IOException {
        // Initialize the Vision API client
        this.visionClient = ImageAnnotatorClient.create();
    }

    public List<String> detectObjects(byte[] imageBytes) throws IllegalArgumentException{

        // Check for invalid input
        if (imageBytes == null || imageBytes.length == 0) {
            throw new IllegalArgumentException("Image bytes cannot be null or empty.");
        }
        // Create an Image object
        Image image = Image.newBuilder().setContent(ByteString.copyFrom(imageBytes)).build();

        // Create a Feature for object detection
        Feature feature = Feature.newBuilder().setType(Feature.Type.OBJECT_LOCALIZATION).build();

        // Create a request with the image and feature
        AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                .addFeatures(feature)
                .setImage(image)
                .build();

        try {
            // Perform the object detection
            BatchAnnotateImagesResponse response = visionClient.batchAnnotateImages(List.of(request));
            // Get the annotations from the response
            List<AnnotateImageResponse> annotations = response.getResponsesList();
            // Close the Vision API client
            // visionClient.close();
            // Display the results
            return extractObjectNames(annotations);

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<String> extractObjectNames(List<AnnotateImageResponse> responses) {
        List<String> objectNames = new ArrayList<>();

        for (AnnotateImageResponse response : responses) {
            // Extract object names from each EntityAnnotation
            for (LocalizedObjectAnnotation annotation : response.getLocalizedObjectAnnotationsList()) {
                objectNames.add(annotation.getName());
            }
        }

        return objectNames;
    }
}