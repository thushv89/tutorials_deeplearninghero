package com.deeplearninghero.imageserver;

import com.google.api.gax.rpc.PermissionDeniedException;
import com.google.cloud.datastore.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DatastoreService {

    private final Datastore datastore;

    public DatastoreService()  throws PermissionDeniedException{
        this.datastore = DatastoreOptions.getDefaultInstance().getService();
    }

    public void persistImageTags(String imageId, String imageUri, List<String> imageObjectTags) {
        Key imageKey = datastore.newKeyFactory().setKind("Image").newKey(imageId);

        Set<Value<String>> tagValues = new HashSet<>();
        for (String tag: imageObjectTags){
            tagValues.add(new StringValue(tag));
        }

        // Create an Entity with the imageId as the key and store the tags as a repeated property
        Entity imageEntity = Entity.newBuilder(imageKey)
                .set("objectTags", tagValues.stream().toList())
                .set("uri", imageUri)
                .build();

        try {
            // Save the entity to Datastore
            datastore.put(imageEntity);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<String> readImageTags(String uuid) {
        Key imageKey = datastore.newKeyFactory().setKind("Image").newKey(uuid);
        Entity imageEntity = datastore.get(imageKey);

        Value<?> value = null;
        try {
            value = imageEntity.getValue("objectTags");
        } catch (NullPointerException e) {
            return List.of();
        }
        // Convert the Value to a ListValue
        if (value instanceof ListValue listValue) {
            // Convert the ListValue to a List<String>
            return listValue.get().stream()
                    .map(Value::get)
                    .map(Object::toString)
                    .collect(Collectors.toList());
        } else {
            throw new RuntimeException("Provided value isn't in the expected format");
        }
    }
}
