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

public class LoginActivity extends AppCompatActivity {

    EditText email,pass;
    Button login;
    TextView txt_reg;
    ProgressBar progress_bar;

    FirebaseAuth mAuth;
    MainActivity mainActivity = new MainActivity();

    private void init(){
        email=findViewById(R.id.email);
        pass=findViewById(R.id.password);
        login=findViewById(R.id.login);
        txt_reg=findViewById(R.id.txt_reg);
        progress_bar=findViewById(R.id.progress_bar);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String e,p;
                e = email.getText().toString().trim();
                p = pass.getText().toString().trim();

                if (TextUtils.isEmpty(e)){
                    email.setError("Please Enter Email");
                    return;
                }
                if (TextUtils.isEmpty(p)){
                    pass.setError("Please Enter Valid Password");
                    return;
                }
                if (p.length()<8){
                    pass.setError("Password too short");
                    return;
                }
                progress_bar.setVisibility(View.VISIBLE);

                mAuth.signInWithEmailAndPassword(e, p)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(LoginActivity.this, "Logged in successfully", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(), MainActivity.class));

                                } else {
                                    Toast.makeText(LoginActivity.this, "Error :  "+ task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    progress_bar.setVisibility(View.GONE);
                                }

                                // ...
                            }
                        });
            }
        });

        txt_reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SignupActivity.class));
            }
        });
    }
}
