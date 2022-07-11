package com.example.baitaplonandroid.signup;

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
import android.util.Patterns;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.baitaplonandroid.R;
import com.example.baitaplonandroid.login.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class SignupActivity extends AppCompatActivity {

    private TextInputEditText etxtEmail,etxtPassword,etxtPhone,etxtName,etxtConfirmPassword;
    private String email,name,phone,password,confirmPassword,Role;
    private RadioButton rdiRolebenhnhan,rdiRolebacsi;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;

    private StorageReference fileStorage;
    private Uri localFileUri,serverFileUri;
    private ImageView ivProfile;
    private View progressBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etxtEmail = (findViewById(R.id.etxtEmail));
        etxtName = findViewById(R.id.etxtName);
        etxtPassword = findViewById(R.id.etxtPassword);
        etxtConfirmPassword = findViewById(R.id.etxtConfirmPassword);
        etxtPhone = findViewById(R.id.etxtPhone);
        ivProfile = (ImageView) findViewById(R.id.ivProfile);
        progressBar = findViewById(R.id.progressBar);
        rdiRolebacsi = findViewById(R.id.rdiRolebacsi);
        rdiRolebenhnhan = findViewById(R.id.rdiRolebenhnhan);

        fileStorage = FirebaseStorage.getInstance("gs://baitaplonandroid-8036d.appspot.com").getReference();
    }

    public void pickImage(View v){

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
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
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
                                        hashMap.put("email", etxtEmail.getText().toString().trim());
                                        hashMap.put("phone", etxtPhone.getText().toString().trim());
                                        hashMap.put("online", "true");
                                        hashMap.put("photo", strFileName);
                                        if (rdiRolebenhnhan.isChecked()){
                                            hashMap.put("Role", rdiRolebenhnhan.getText().toString());
                                        } else if (rdiRolebacsi.isChecked()){
                                            hashMap.put("Role", rdiRolebacsi.getText().toString());
                                        }

                                        databaseReference.child(userID).setValue(hashMap)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        Toast.makeText(SignupActivity.this, R.string.user_created_successfully, Toast.LENGTH_SHORT).show();
                                                        startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                                                    }
                                                });

                                    } else {
                                        Toast.makeText(SignupActivity.this,
                                                getString(R.string.failed_to_update_profile, task.getException()), Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });
                        }
                    });
                }}});

    }

    public void btnSignupClick(View v){
        email = etxtEmail.getText().toString().trim();
        name = etxtName.getText().toString().trim();
        password = etxtPassword.getText().toString().trim();
        phone = etxtPhone.getText().toString().trim();
        confirmPassword = etxtConfirmPassword.getText().toString().trim();
        if (rdiRolebenhnhan.isChecked()){
            Role = rdiRolebenhnhan.getText().toString();
        } else if (rdiRolebacsi.isChecked()){
            Role = rdiRolebacsi.getText().toString();
        }


        if(email.equals("")){
            etxtEmail.setError(getString(R.string.enter_email));
        }else if(name.equals("")){
            etxtName.setError(getString(R.string.enter_name));
        }else if(password.equals("")){
            etxtPassword.setError(getString(R.string.enter_password));
        }else if(phone.equals("")){
            etxtPhone.setError(getString(R.string.enter_phone));
        }else if(confirmPassword.equals("")){
            etxtConfirmPassword.setError(getString(R.string.confirm_password));
        }else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            etxtEmail.setError(getString(R.string.enter_correct_email));
        }else if(!password.equals(confirmPassword)){
            etxtConfirmPassword.setError(getString(R.string.error_matching_password));
        }else{

            progressBar.setVisibility(View.VISIBLE);
            final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

            firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    progressBar.setVisibility(View.GONE);
                    if(task.isSuccessful()){
                        firebaseUser = firebaseAuth.getCurrentUser();

                        if(localFileUri != null) updateNameAndPhoto();

                    }
                    else{
                        Toast.makeText(SignupActivity.this, getString(R.string.signup_failed,task.getException()),Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}