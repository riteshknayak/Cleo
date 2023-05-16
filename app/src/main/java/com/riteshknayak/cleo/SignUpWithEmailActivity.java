package com.riteshknayak.cleo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.riteshknayak.cleo.databinding.ActivitySignUpWithEmailBinding;

import java.util.Objects;

public class SignUpWithEmailActivity extends AppCompatActivity {

    ActivitySignUpWithEmailBinding binding;

    FirebaseAuth mAuth;

    private static final String TAG = "EmailPassword";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySignUpWithEmailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        binding.signUpBtn.setOnClickListener(v -> {
            String email, pass;  // name; //TODO save these data to firebase


            if (isEmpty(binding.emailBox) || isEmpty(binding.passBox) || isEmpty(binding.nameBox)) {
                Toast.makeText(getApplicationContext(), "Please provide the required data", Toast.LENGTH_SHORT).show();
            } else {
                email = Objects.requireNonNull(binding.emailBox.getText()).toString();
                pass = Objects.requireNonNull(binding.passBox.getText()).toString();
                //name = Objects.requireNonNull(binding.nameBox.getText()).toString();

                mAuth.createUserWithEmailAndPassword(email, pass)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "createUserWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();

                                assert user != null;
                                user.sendEmailVerification()
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                Log.d(TAG, "Verification email sent");
                                                Toast.makeText(getApplicationContext(),"Verification email sent",Toast.LENGTH_LONG).show();

                                                Intent intent = new Intent(getApplicationContext(), CheckVerificationActivity.class);
                                                startActivity(intent);
                                            }
                                        });

                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(getApplicationContext(), "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });

            }
        });

        binding.signInBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SignInWithEmailActivity.class);
            startActivity(intent);
        });

    }

    private boolean isEmpty(EditText editText) {
        return editText.getText().toString().trim().length() == 0;
    }

}