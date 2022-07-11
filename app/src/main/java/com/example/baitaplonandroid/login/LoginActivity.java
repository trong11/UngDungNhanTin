package com.example.baitaplonandroid.login;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.baitaplonandroid.MainActivity;
import com.example.baitaplonandroid.MainActivityDoctor;
import com.example.baitaplonandroid.MessageActivity;
import com.example.baitaplonandroid.R;
import com.example.baitaplonandroid.Util;
import com.example.baitaplonandroid.model.User;
import com.example.baitaplonandroid.signup.ResetPasswordActivity;
import com.example.baitaplonandroid.signup.SignupActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceIdReceiver;
import com.google.firebase.iid.internal.FirebaseInstanceIdInternal;
import com.google.firebase.messaging.FirebaseMessaging;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText etxtEmail,etxtPassword;
    private String email,password;
    private View progressBar;
    private DatabaseReference databaseReference;
    private Button btnGoogleLogin;
    GoogleSignInOptions gso;
    GoogleSignInClient gsc;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etxtEmail = findViewById(R.id.etxtEmail);
        etxtPassword = findViewById(R.id.etxtPassword);

        progressBar = findViewById(R.id.progressBar);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);

        databaseReference = FirebaseDatabase.getInstance("https://baitaplonandroid-8036d-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Users");

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        gsc = GoogleSignIn.getClient(this,gso);


        btnGoogleLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SignIn();
            }
        });
    }

    private void SignIn(){
        Intent intent = gsc.getSignInIntent();
        startActivityForResult(intent,100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 100){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try{
                  task.getResult(ApiException.class);
                  MainActivity();
            }catch (Exception e){
                Toast.makeText(this,"Error",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void MainActivity(){
        finish();
        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        startActivity(intent);
    }

    public void SignupClick(View v){
        startActivity(new Intent(this, SignupActivity.class));
    }

    public void btnLoginClick(View v){
        email = etxtEmail.getText().toString().trim();
        password = etxtPassword.getText().toString().trim();

        if(email.equals("")){
            etxtEmail.setError(getString(R.string.enter_email));
        }
        else if (password.equals("")){
            etxtPassword.setError(getString(R.string.enter_password));
        }
        else {
            if (Util.connectionAvaialble(this)) {
                progressBar.setVisibility(View.VISIBLE);
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Query query = databaseReference.orderByChild("email").equalTo(email);
                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for(DataSnapshot userSnapshot:snapshot.getChildren()){
                                        User user = userSnapshot.getValue(User.class);
                                        String role = user.getRole();

                                        if(role.equals("Benhnhan")){
                                            FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
                                                @Override
                                                public void onSuccess(String s) {
                                                    Util.updateDeviceToken(LoginActivity.this,s);
                                                }
                                            });
                                            startActivity(new Intent(LoginActivity.this,MainActivity.class));
                                            finish();
                                        }else if(role.equals("Bacsi")){
                                            FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
                                                @Override
                                                public void onSuccess(String s) {
                                                    Util.updateDeviceToken(LoginActivity.this,s);
                                                }
                                            });
                                            startActivity(new Intent(LoginActivity.this,MainActivityDoctor.class));
                                            finish();
                                        }
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    throw error.toException();
                                }
                            });
//                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
//                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Login failed : " + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            else{
                startActivity(new Intent(LoginActivity.this, MessageActivity.class));
            }
        }
    }

    public void resetPasswordClick(View view){
        startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if(firebaseUser!=null){
            Query query = databaseReference.orderByChild("email").equalTo(firebaseUser.getEmail());
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot userSnapshot:snapshot.getChildren()){
                        User user = userSnapshot.getValue(User.class);
                        String role = user.getRole();

                        if(role.equals("Benhnhan")){
                            FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
                                @Override
                                public void onSuccess(String s) {
                                    Util.updateDeviceToken(LoginActivity.this,s);
                                }
                            });
                            startActivity(new Intent(LoginActivity.this,MainActivity.class));
                            finish();
                        }else if(role.equals("Bacsi")){
                            FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
                                @Override
                                public void onSuccess(String s) {
                                    Util.updateDeviceToken(LoginActivity.this,s);
                                }
                            });
                            startActivity(new Intent(LoginActivity.this,MainActivityDoctor.class));
                            finish();
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
}