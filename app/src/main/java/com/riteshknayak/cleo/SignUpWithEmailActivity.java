package com.riteshknayak.cleo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.riteshknayak.cleo.Models.User;
import com.riteshknayak.cleo.databinding.ActivitySignUpWithEmailBinding;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SignUpWithEmailActivity extends AppCompatActivity {

    ActivitySignUpWithEmailBinding binding;

    FirebaseAuth mAuth;

    private static final String TAG = "EmailPassword";

    FirebaseFirestore database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Initialise ViewBinding
        binding = ActivitySignUpWithEmailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Initialise firebase auth
        mAuth = FirebaseAuth.getInstance();

        //Initialise Firestore database
        database = FirebaseFirestore.getInstance();

        binding.signUpBtn.setOnClickListener(v -> {
            String email, password, name;  // name; //TODO save these data to firebase


            if (isEmpty(binding.emailBox) || isEmpty(binding.passBox) || isEmpty(binding.nameBox)) {
                Toast.makeText(getApplicationContext(), "Please provide the required data", Toast.LENGTH_SHORT).show();
            } else {
                email = Objects.requireNonNull(binding.emailBox.getText()).toString();
                password = Objects.requireNonNull(binding.passBox.getText()).toString();
                name = Objects.requireNonNull(binding.nameBox.getText()).toString();

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "createUserWithEmail:success");

                                //get the new FirebaseUser
                                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                assert firebaseUser != null;
                                User user = new User(name, firebaseUser.getUid(), email, password, 5, true, false);

                                //Add new user to database
                                database.collection("users")
                                        .document(firebaseUser.getUid())
                                        .set(user).addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                finish();
                                            } else {
                                                Toast.makeText(SignUpWithEmailActivity.this, Objects.requireNonNull(task1.getException()).getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                            }

                                        });

                                Map<String, Object> isPremiumUser = new HashMap<>();
                                isPremiumUser.put("premiumUser", false);
                                database.collection("users")
                                        .document(firebaseUser.getUid())
                                        .update(isPremiumUser);

                                Map<String, Object> name1 = new HashMap<>();
                                name1.put("name", name);
                                database.collection("users")
                                        .document(firebaseUser.getUid())
                                        .update(name1);



                                //Send verification email
                                firebaseUser.sendEmailVerification()
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