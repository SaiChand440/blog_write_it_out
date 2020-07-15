package com.chandhu.firstapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    TextView alreadyAccount;
    EditText signUpMail;
    EditText originalPassword;
    EditText confirmPassword;
    Button register;
    ProgressBar progressBar;

    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(),R.color.colorPrimaryDark));

        signUpMail = findViewById(R.id.SignUpEmailAddress);
        originalPassword = findViewById(R.id.SignUpTextPassword);
        confirmPassword = findViewById(R.id.TextConfirmPassword);
        progressBar = findViewById(R.id.SignUpProgressBar);

        register = findViewById(R.id.RegisterButton);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(signUpMail.getText().toString()) && !TextUtils.isEmpty(originalPassword.getText().toString()) && !TextUtils.isEmpty(confirmPassword.getText().toString()) ){
                    if (originalPassword.getText().toString().equals(confirmPassword.getText().toString())){
                        progressBar.setVisibility(View.VISIBLE);
                        registerUser(signUpMail.getText().toString(),confirmPassword.getText().toString());
                    }else{
                        Toast.makeText(RegisterActivity.this, "Password do not match", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(RegisterActivity.this, "Please Fill the form", Toast.LENGTH_SHORT).show();
                }
            }
        });


        alreadyAccount = findViewById(R.id.loginRequest);
        String text = "<font color='black'>Already have an account? </font><font color='red'>Login from here</font>";
        alreadyAccount.setText(Html.fromHtml(text), TextView.BufferType.SPANNABLE);
        alreadyAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        mAuth = FirebaseAuth.getInstance();
    }

    private void registerUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("Registration status", "createUserWithEmail:success");
                            Intent intent = new Intent(getApplicationContext(), SetupActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("Registration status", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });
    }
}
