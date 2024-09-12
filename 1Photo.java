package com.example.fyp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.util.HashMap;
import java.util.Map;

public class Photo extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 100;
    private ImageView imageView;
    private Button btnOpenGallery;
    private Button btnUploadImage;
    private Uri mImageUri;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mStorageRef;
    private String mImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        imageView = findViewById(R.id.imageView);
        btnOpenGallery = findViewById(R.id.btnOpenGallery);
        btnUploadImage = findViewById(R.id.btnUploadImage);

        mFirebaseStorage = FirebaseStorage.getInstance();
        mStorageRef = mFirebaseStorage.getReference();

        btnOpenGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        btnUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mImageUri != null) {
                    uploadImage();
                } else {
                    Toast.makeText(Photo.this, "No image selected", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE_REQUEST && data != null && data.getData() != null) {
            mImageUri = data.getData();
            imageView.setImageURI(mImageUri);
        }
    }

    private void uploadImage() {
        if (mImageUri != null) {
            StorageReference fileRef = mStorageRef.child("images/" + System.currentTimeMillis() + ".jpg");
            fileRef.putFile(mImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Log.d("PhotoActivity", "Image uploaded successfully");
                            Toast.makeText(Photo.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();

                            taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    mImageUrl = uri.toString();
                                    saveImageMetadata(mImageUrl);
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Log.e("PhotoActivity", "Image upload failed", exception);
                            Toast.makeText(Photo.this, "Image upload failed", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void saveImageMetadata(String imageUrl) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> imageMetadata = new HashMap<>();
        imageMetadata.put("url", imageUrl);
        imageMetadata.put("description", "Uploaded from gallery");

        db.collection("images").add(imageMetadata)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("PhotoActivity", "Image metadata added successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("PhotoActivity", "Error adding image metadata", e);
                    }
                });
    }
}
