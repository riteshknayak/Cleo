package com.riteshknayak.cleo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.riteshknayak.cleo.databinding.ActivitySignUpWithEmailBinding;
import com.riteshknayak.cleo.databinding.ActivitySignupBinding;

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

        binding.signupBtn.setOnClickListener(v -> {
            String email, pass, name;


            if (isEmpty(binding.emailBox) || isEmpty(binding.passBox) || isEmpty(binding.nameBox)) {
                Toast.makeText(getApplicationContext(), "Please provide the required data", Toast.LENGTH_SHORT).show();
            } else {
                email = binding.emailBox.getText().toString();
                pass = binding.passBox.getText().toString();
                name = binding.nameBox.getText().toString();

                mAuth.createUserWithEmailAndPassword(email, pass)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "createUserWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();

                                user.sendEmailVerification()
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                Log.d(TAG, "Email sent.");
                                                Toast.makeText(getApplicationContext(),"Email Sent",Toast.LENGTH_LONG).show();
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

    }

    private boolean isEmpty(EditText editText) {
        return editText.getText().toString().trim().length() == 0;
    }

}