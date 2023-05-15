package com.riteshknayak.cleo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.riteshknayak.cleo.databinding.ActivitySignInWithEmailBinding;

import java.util.Objects;

public class SignInWithEmailActivity extends AppCompatActivity {

    ActivitySignInWithEmailBinding binding;

    FirebaseAuth mAuth;

    private static final String TAG = "EmailPassword";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySignInWithEmailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        binding.signInBtn.setOnClickListener(v -> {
            String email, pass;


            if (isEmpty(binding.emailBox) || isEmpty(binding.passBox)) {
                Toast.makeText(getApplicationContext(), "Please provide the required data", Toast.LENGTH_SHORT).show();
            } else {
                email = Objects.requireNonNull(binding.emailBox.getText()).toString();
                pass = Objects.requireNonNull(binding.passBox.getText()).toString();

                mAuth.signInWithEmailAndPassword(email, pass)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithEmail:success");
                                //FirebaseUser user = mAuth.getCurrentUser();
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(SignInWithEmailActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });

            }
        });

        binding.signUpBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SignUpWithEmailActivity.class);
            startActivity(intent);
        });

    }

    private boolean isEmpty(EditText editText) {
        return editText.getText().toString().trim().length() == 0;
    }

}