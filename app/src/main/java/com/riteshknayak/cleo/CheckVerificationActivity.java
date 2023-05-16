package com.riteshknayak.cleo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.auth.FirebaseAuth;
import com.riteshknayak.cleo.databinding.ActivityCheckVerificationBinding;

import java.util.Objects;

public class CheckVerificationActivity extends AppCompatActivity {

    ActivityCheckVerificationBinding binding;

    private FirebaseAuth mAuth;

    private static final String TAG = "EmailPassword";

    GoogleSignInClient mGoogleSignInClient;

    int RC_SIGN_IN = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCheckVerificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        binding.checkVerificationBtn.setOnClickListener(v -> {

            Objects.requireNonNull(mAuth.getCurrentUser()).reload();
            if(mAuth.getCurrentUser().isEmailVerified()){
                Toast.makeText(getApplicationContext(),"Email is Verified",Toast.LENGTH_LONG).show();

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);

            }else{
                Toast.makeText(getApplicationContext(),"Email is not Verified",Toast.LENGTH_LONG).show();
            }
        });



    }
}