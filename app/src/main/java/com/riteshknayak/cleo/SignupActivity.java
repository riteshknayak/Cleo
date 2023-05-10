package com.riteshknayak.cleo;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.auth.User;
import com.riteshknayak.cleo.databinding.ActivitySignupBinding;

import java.util.Objects;

public class SignupActivity extends AppCompatActivity {

    ActivitySignupBinding binding;

    private FirebaseAuth mAuth;

    private static final String TAG = "EmailPassword";


    SignInClient oneTapClient;
    BeginSignInRequest signUpRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        mAuth = FirebaseAuth.getInstance();
        binding.signupBtn.setOnClickListener(v -> {
            String email, pass, name;

            if (isEmpty(binding.emailBox) || isEmpty(binding.passBox) || isEmpty(binding.nameBox)) {
                Toast.makeText(SignupActivity.this, "Please provide the required data", Toast.LENGTH_SHORT).show();
            } else {

                Toast.makeText(SignupActivity.this, binding.emailBox.getText().toString(), Toast.LENGTH_SHORT).show();

                email = binding.emailBox.getText().toString();
                pass = binding.passBox.getText().toString();
                name = binding.nameBox.getText().toString();

                mAuth.createUserWithEmailAndPassword(email, pass)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "createUserWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();

                                    user.sendEmailVerification()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Log.d(TAG, "Email sent.");
                                                        Toast.makeText(getApplicationContext(),"Email Sent",Toast.LENGTH_LONG).show();

                                                    }
                                                }
                                            });
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                    Toast.makeText(SignupActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

            }
        });

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Toast toast= Toast.makeText(getApplicationContext(),"Sign in Successful",Toast.LENGTH_SHORT);
            toast.show();
        }


        binding.checkVerificationBtn.setOnClickListener(v -> {
            mAuth.getCurrentUser().reload();
            if(mAuth.getCurrentUser().isEmailVerified()){
                Toast.makeText(getApplicationContext(),"Email is Verified",Toast.LENGTH_LONG).show();
                binding.veriText.setText("Email is Verified");
            }else{
                Toast.makeText(getApplicationContext(),"Email is not Verified "+mAuth.getCurrentUser().getEmail(),Toast.LENGTH_LONG).show();
                binding.veriText.setText("Email is not Verified");
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

    }


    private boolean isEmpty(EditText editText) {
        return editText.getText().toString().trim().length() == 0;
    }




}