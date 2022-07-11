package com.example.baitaplonandroid.profile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.baitaplonandroid.R;
import com.example.baitaplonandroid.login.LoginActivity;
import com.example.baitaplonandroid.model.User;
import com.example.baitaplonandroid.signup.ChangePasswordActivity;
import com.example.baitaplonandroid.signup.SignupActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {
    private TextInputEditText etxtEmail,etxtPhone,etxtName;
    private String email,name,phone;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;

    private StorageReference fileStorage;
    private Uri localFileUri,serverFileUri;
    private ImageView ivProfile;
    private FirebaseAuth firebaseAuth;
    private View progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        etxtEmail = (findViewById(R.id.etxtEmail));
        etxtName = findViewById(R.id.etxtName);
        etxtPhone = findViewById(R.id.etxtPhone);
        ivProfile = (ImageView) findViewById(R.id.ivProfile);
        progressBar = findViewById(R.id.progressBar);
        fileStorage = FirebaseStorage.getInstance("gs://baitaplonandroid-8036d.appspot.com").getReference();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        if(firebaseUser != null){
            etxtName.setText(firebaseUser.getDisplayName());
            etxtEmail.setText(firebaseUser.getEmail());
            serverFileUri = firebaseUser.getPhotoUrl();

            if(serverFileUri !=null){
                Glide.with(this).load(serverFileUri).placeholder(R.drawable.defaultpropic)
                        .error(R.drawable.defaultpropic)
                        .into(ivProfile);
            }

            databaseReference = FirebaseDatabase.getInstance("https://baitaplonandroid-8036d-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();

            Query phoneQuery = databaseReference.child("Users").orderByChild("email").equalTo(etxtEmail.getText().toString().trim());
            phoneQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                     for(DataSnapshot userSnapshot:snapshot.getChildren()){
                         User user = userSnapshot.getValue(User.class);
                         if(user != null){
                             etxtPhone.setText(user.getPhone());
                         }
                     }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    throw error.toException();
                }
            });
        }
    }

    private void removePhoto(){
        progressBar.setVisibility(View.VISIBLE);
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setDisplayName(etxtName.getText().toString().trim())
                .setPhotoUri(null)
                .build();

        firebaseUser.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    String userID = firebaseUser.getUid();
                    databaseReference = FirebaseDatabase.getInstance("https://baitaplonandroid-8036d-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Users");

                    HashMap<String, String> hashMap = new HashMap<>();

                    hashMap.put("photo", "");

                    databaseReference.child(userID).setValue(hashMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    Toast.makeText(ProfileActivity.this, R.string.photo_removed_successfully, Toast.LENGTH_SHORT).show();
                                }
                            });

                } else {
                    Toast.makeText(ProfileActivity.this,
                            getString(R.string.failed_to_update_profile, task.getException()), Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    public void changeImage(View view){
        if(serverFileUri == null){
            pickImage();
        }
        else{
            PopupMenu popupMenu = new PopupMenu(this,view);
            popupMenu.getMenuInflater().inflate(R.menu.menu_picture,popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    int id = menuItem.getItemId();
                    if(id==R.id.menuChangePic){
                        pickImage();
                    }else if(id==R.id.menuRemovePic){
                        removePhoto();
                    }
                    return false;
                }
            });
            popupMenu.show();
        }
    }

    public void btnSaveClick(View view){
        if(etxtName.getText().toString().trim().equals("")){
            etxtName.setError(getString(R.string.enter_name));
        }else{
            updateNameAndPhoto();
        }
    }

    private void pickImage(){

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent,101);
        }
        else{
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},102);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==101){
            if(resultCode==RESULT_OK){
                localFileUri = data.getData();
                ivProfile.setImageURI(localFileUri);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==102){
            if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent,101);
            }
            else{
                Toast.makeText(this,R.string.permission_required,Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateNameAndPhoto()
    {
        String strFileName= firebaseUser.getUid() + ".jpg";

        final  StorageReference fileRef = fileStorage.child("images/"+ strFileName);
        progressBar.setVisibility(View.VISIBLE);

        fileRef.putFile(localFileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                progressBar.setVisibility(View.GONE);

                if(task.isSuccessful())
                {
                    fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            serverFileUri = uri;

                            UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(etxtName.getText().toString().trim())
                                    .setPhotoUri(serverFileUri)
                                    .build();

                            firebaseUser.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        String userID = firebaseUser.getUid();
                                        databaseReference = FirebaseDatabase.getInstance("https://baitaplonandroid-8036d-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Users");

                                        HashMap<String, String> hashMap = new HashMap<>();
                                        hashMap.put("name", etxtName.getText().toString().trim());
                                        hashMap.put("phone", etxtPhone.getText().toString().trim());
                                        hashMap.put("photo", strFileName);
                                        hashMap.put("email", etxtEmail.getText().toString().trim());
                                        hashMap.put("online", "true");

                                        databaseReference.child(userID).setValue(hashMap)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        finish();
                                                    }
                                                });

                                    } else {
                                        Toast.makeText(ProfileActivity.this,
                                                getString(R.string.failed_to_update_profile, task.getException()), Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });
                        }
                    });
                }}});
    }

    public void btnLogoutClick (View view){
       final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        DatabaseReference rootRef = FirebaseDatabase.getInstance("https://baitaplonandroid-8036d-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
        DatabaseReference databaseReference = rootRef.child("Tokens").child(currentUser.getUid());

        databaseReference.setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    firebaseAuth.signOut();
                    startActivity(new Intent(ProfileActivity.this,LoginActivity.class));
                    finish();
                }else{
                    Toast.makeText(ProfileActivity.this, R.string.something_went_wrong, Toast.LENGTH_SHORT).show();
                }
            }
        });

//        firebaseAuth.signOut();
//        startActivity(new Intent(ProfileActivity.this,LoginActivity.class));
//        finish();
    }

    public void btnChangePasswordClick(View view){
        startActivity(new Intent(ProfileActivity.this, ChangePasswordActivity.class));
    }
}