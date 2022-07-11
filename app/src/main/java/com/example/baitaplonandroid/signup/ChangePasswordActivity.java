package com.example.baitaplonandroid.signup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.baitaplonandroid.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    private TextInputEditText etxtPassword,etxtConfirmPassword;
    private View progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        etxtPassword = findViewById(R.id.etxtPassword);
        etxtConfirmPassword = findViewById(R.id.etxtConfirmPassword);
        progressBar = findViewById(R.id.progressBar);
    }

    public void btnChangePasswordClick(View view){
        String password = etxtPassword.getText().toString().trim();
        String confirmPassword = etxtConfirmPassword.getText().toString().trim();

        if(password.equals("")){
            etxtPassword.setError(getString(R.string.enter_password));
        }else if(confirmPassword.equals("")){
            etxtConfirmPassword.setError(getString(R.string.confirm_password));
        }else if(!password.equals(confirmPassword)){
            etxtConfirmPassword.setError(getString(R.string.error_matching_password));
        }else{
            progressBar.setVisibility(View.VISIBLE);
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

            if(firebaseUser!=null){
                firebaseUser.updatePassword(password).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressBar.setVisibility(View.GONE);
                        if(task.isSuccessful()){
                            Toast.makeText(ChangePasswordActivity.this, R.string.password_changed_successfully, Toast.LENGTH_SHORT).show();
                            finish();
                        }else{
                            Toast.makeText(ChangePasswordActivity.this, getString(R.string.something_went_wrong,task.getException()), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    }
}