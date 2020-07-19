package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignupActivity extends AppCompatActivity {

    EditText email, pass, conpass;
    Button signup_btn;
    ProgressBar progressBar;
    TextView txt_login;

    FirebaseAuth mAuth;
    FirebaseFirestore firebaseFirestore;
    String userID;

    private void init(){
        mAuth = FirebaseAuth.getInstance();
        email=findViewById(R.id.mail);
        pass=findViewById(R.id.pass);
        conpass=findViewById(R.id.conpass);
        progressBar=findViewById(R.id.progress);
        signup_btn=findViewById(R.id.signup_btn);
        txt_login=findViewById(R.id.txt_login);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        init();

        if ((mAuth.getCurrentUser() != null)){
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

        signup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String e,p,c;
                e = email.getText().toString().trim();
                p = pass.getText().toString().trim();
                c = conpass.getText().toString().trim();

                if (TextUtils.isEmpty(e)){
                    email.setError("Please Enter Email");
                    return;
                }
                if (TextUtils.isEmpty(p)){
                    pass.setError("Please Enter Password");
                    return;
                }

                if (TextUtils.isEmpty(c)){
                    pass.setError("Please Enter Confirm Password");
                    return;
                }

                if (p.length()<8){
                    pass.setError("Password too short");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                if (p.equals(c)) {

                    mAuth.createUserWithEmailAndPassword(e, p)
                            .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    progressBar.setVisibility(View.GONE);

                                    if (task.isSuccessful()) {
                                        Toast.makeText(SignupActivity.this, "Registration Completed", Toast.LENGTH_SHORT).show();
                                        userID = mAuth.getUid();
                                        DocumentReference documentReference = firebaseFirestore.collection("user").document(userID);
                                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));

                                    } else {
                                        Toast.makeText(SignupActivity.this, "Error : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.GONE);
                                    }

                                    // ...
                                }
                            });
                }
            }
        });
        txt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            }
        });

    }
}
