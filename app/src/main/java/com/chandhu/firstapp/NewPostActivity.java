package com.chandhu.firstapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.developers.imagezipper.ImageZipper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class NewPostActivity extends AppCompatActivity {

    private ImageView newPostImage;
    private EditText newPostDesc;
    private Button newPostBtn;
    private Uri postImageUri;
    private ProgressBar newPostProgress;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private String user_id;
    private Bitmap compressedImageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(),R.color.colorPrimaryDark));
        Toolbar toolbar = findViewById(R.id.post_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Create post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        user_id = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        newPostImage = findViewById(R.id.place_holder_img);
        newPostDesc = findViewById(R.id.new_post_desc);
        newPostBtn = findViewById(R.id.new_post_btn);
        newPostProgress = findViewById(R.id.new_post_progress);

        newPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512,512)
                        .setAspectRatio(5,4)
                        .start(NewPostActivity.this);
            }
        });

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();

        newPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String desc = newPostDesc.getText().toString();
                if (!desc.isEmpty() && postImageUri != null){

                    newPostProgress.setVisibility(View.VISIBLE);
                    newPostBtn.setEnabled(false);

                    final String randomName = random();
                    final String[] download_uri = new String[1];
                    final StorageReference post_path = storageReference.child("post_images").child(randomName + ".jpg");
                    post_path.putFile(postImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()){

                                File newImageFile = new File(Objects.requireNonNull(postImageUri.getPath()));

                                try {
                                    compressedImageFile = new ImageZipper(NewPostActivity.this)
                                                                .setQuality(1)
                                                                .setMaxWidth(100)
                                                                .setMaxHeight(100)
                                                                .compressToBitmap(newImageFile);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                compressedImageFile.compress(Bitmap.CompressFormat.JPEG,100,baos);
                                byte[] thumbData = baos.toByteArray();

                                UploadTask uploadTask = storageReference.child("post_images/thumbs").child(randomName + ".jpg").putBytes(thumbData);
                                final StorageReference thumb_path = storageReference.child("post_images/thumbs").child(randomName + ".jpg");
                                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                        final String[] thumb_uri = new String[1];
                                        thumb_path.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                thumb_uri[0] = uri.toString();
                                                post_path.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        download_uri[0] = uri.toString();
                                                        Map<String,Object> postMap = new HashMap<>();
                                                        postMap.put("image_url",download_uri[0]);
                                                        postMap.put("thumb_url",thumb_uri[0]);
                                                        postMap.put("desc",desc);
                                                        postMap.put("user_id",user_id);
                                                        postMap.put("timeStamp",FieldValue.serverTimestamp());

                                                        firebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                                                if (task.isSuccessful()){
                                                                    Toast.makeText(NewPostActivity.this, "Post was added", Toast.LENGTH_LONG).show();
                                                                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                                                                    startActivity(intent);
                                                                    finish();
                                                                }else{
                                                                    String error = Objects.requireNonNull(task.getException()).getMessage();
                                                                    Toast.makeText(NewPostActivity.this, "Error : " + error, Toast.LENGTH_LONG).show();
                                                                }
                                                                newPostProgress.setVisibility(View.INVISIBLE);
                                                            }
                                                        });
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(NewPostActivity.this, "Error : " + e, Toast.LENGTH_LONG).show();
                                                        newPostProgress.setVisibility(View.INVISIBLE);
                                                    }
                                                });
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.i("uri of thumb exception","" + e);
                                            }
                                        });
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        String exception = e.toString();
                                        Toast.makeText(NewPostActivity.this, "There's an error while uploading img to thumbs", Toast.LENGTH_SHORT).show();
                                        Log.i("Error",exception);
                                    }
                                });

                            }else{
                                String error = Objects.requireNonNull(task.getException()).getMessage();
                                Toast.makeText(NewPostActivity.this, "Error : " + error, Toast.LENGTH_LONG).show();
                                newPostProgress.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
                }

               newPostBtn.setEnabled(true);
            }
        });
    }

    public static String random() {
        String AlphaNumericString = "abcdefghijklmnopqrstuvxyz";
        StringBuilder sb = new StringBuilder(20);

        for (int i = 0; i < 20; i++) {
            int index
                    = (int)(AlphaNumericString.length()
                    * Math.random());
            sb.append(AlphaNumericString
                    .charAt(index));
        }

        return sb.toString();
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                assert result != null;
                postImageUri = result.getUri();
                newPostImage.setImageURI(postImageUri);

            } else if (BuildConfig.DEBUG && !(resultCode != CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE || result != null)) {
                throw new AssertionError("Assertion failed");
            }
        }
    }
}