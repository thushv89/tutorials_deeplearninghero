# ML Image Tagger

## Introduction 
In this project we'll be building an ML based image tagger which uses several GCP services:
* GCS to store the user uploaded images
* Cloud vision API to detect objects in the image
* Datastore to store the detected object tags for fast lookup

To implement most of the services we'll be using Java and the [Spring Framework](https://docs.spring.io/spring-framework/reference/index.html)

## Testing in the local environment

1. Grant the service account token creator role on the user:

`gcloud iam service-accounts add-iam-policy-binding  <service account> --member=user:<user account> --role=roles/iam.serviceAccountTokenCreator --format=json`

2. Authenticate the client library to impersonate the service account

`gcloud auth application-default login --impersonate-service-account <service account>`

3. Build the maven project
4. Run the webserver
5. Make a request:

`echo "$(base64 -w 0 <path to image>)" | curl -X POST -H "Content-Type: application/json" -d @- http://localhost:8080/upload_image`

6. Remember to revoke the ADC credentials after the project

`gcloud auth application-default revoke`