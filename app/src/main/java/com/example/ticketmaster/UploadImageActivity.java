package com.example.ticketmaster;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContentInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class UploadImageActivity extends AppCompatActivity {

    private ProgressBar pb4;
    private ImageView imageviewUploadPic;
    private FirebaseAuth authProfile;
    private StorageReference storageReference;
    private FirebaseUser firebaseUser;
    private Uri uriImage;
    private ActivityResultLauncher<Intent> openFileLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_image);

        // Toolbar setup
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Upload Image");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        // Initialize UI elements
        Button buttonUploadChoose = findViewById(R.id.button_upload_pic_choose);
        Button uploadPic = findViewById(R.id.upload_pic_button);
        imageviewUploadPic = findViewById(R.id.imageview_profile_dp);
        pb4 = findViewById(R.id.pb);
        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference("DisplayPics");

        Uri uri = firebaseUser.getPhotoUrl();
        if (uri != null) {
            Picasso.with(UploadImageActivity.this).load(uri).into(imageviewUploadPic);
        }

        // Initialize the ActivityResultLauncher for picking images
        openFileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        uriImage = result.getData().getData();
                        imageviewUploadPic.setImageURI(uriImage);
                    } else {
                        Log.e("UploadImageActivity", "Image selection failed");
                    }
                }
        );

        // Open file chooser on button click
        buttonUploadChoose.setOnClickListener(v -> openFileChooser());

        // Upload image on button click
        uploadPic.setOnClickListener(v -> {
            pb4.setVisibility(View.VISIBLE);
            uploadPic();
        });
    }

    // Method to open the file chooser
    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        openFileLauncher.launch(intent);
    }

    // Upload the selected image to Firebase Storage
    private void uploadPic() {
        if (uriImage != null) {
            String fileExtension = getFileExtension(uriImage);
            StorageReference fileReference = storageReference.child(authProfile.getCurrentUser().getUid() + "." + fileExtension);

            fileReference.putFile(uriImage).addOnSuccessListener(taskSnapshot -> {
                fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    Uri downloadUri = uri;
                    firebaseUser = authProfile.getCurrentUser();

                    // Update Firebase User profile with the new photo URL
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setPhotoUri(downloadUri).build();
                    firebaseUser.updateProfile(profileUpdates);

                    pb4.setVisibility(View.GONE);
                    Toast.makeText(UploadImageActivity.this, "Upload Successful!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(UploadImageActivity.this, UserProfileActivity.class);
                    startActivity(intent);
                    finish(); // Close the activity
                });
            }).addOnFailureListener(e -> {
                pb4.setVisibility(View.GONE);
                Toast.makeText(UploadImageActivity.this, "Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    // Get the file extension of the selected image
    private String getFileExtension(Uri uri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.common_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        else if (id == R.id.menu_logout) {
            authProfile.signOut();
            Toast.makeText(UploadImageActivity.this, "Logged Out",Toast.LENGTH_LONG).show();
            Intent intent = new Intent(UploadImageActivity.this, MainActivity.class);
            //check stack to prevent coming back to the UserprofileActivity
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();//close Userprofile
        }else{
            Toast.makeText(UploadImageActivity.this,"Something went wrong",Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

}
