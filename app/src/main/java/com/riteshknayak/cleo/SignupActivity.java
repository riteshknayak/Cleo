package com.riteshknayak.cleo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.riteshknayak.cleo.databinding.ActivitySignupBinding;

public class SignupActivity extends AppCompatActivity {

    ActivitySignupBinding binding;

    private FirebaseAuth mAuth;

    private static final String TAG = "EmailPassword";

    GoogleSignInClient mGoogleSignInClient;

    int RC_SIGN_IN = 2;

//    private static final int REQ_ONE_TAP = 2;  // Can be any integer unique to the Activity.
//    private final boolean showOneTapUI = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);



        //Initialise FirebaseAuth
        mAuth = FirebaseAuth.getInstance();



        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null){
            Toast.makeText(getApplicationContext(),"Sign in Successful",Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Please sign in" , Toast.LENGTH_SHORT).show();
        }



        //Setting up Google Sign in
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        //Signup with Google Button
        binding.signInWithGoogle.setOnClickListener(v -> {
            googleSignIn();
        });


        //Sign up with email Button
        binding.signupBtn.setOnClickListener(v -> {
            String email, pass, name;


            if (isEmpty(binding.emailBox) || isEmpty(binding.passBox) || isEmpty(binding.nameBox)) {
                Toast.makeText(SignupActivity.this, "Please provide the required data", Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(SignupActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });

            }
        });


//        binding.checkVerificationBtn.setOnClickListener(v -> {
//            mAuth.getCurrentUser().reload();
//            if(mAuth.getCurrentUser().isEmailVerified()){
//                Toast.makeText(getApplicationContext(),"Email is Verified",Toast.LENGTH_LONG).show();
//                binding.veriText.setText("Email is Verified");
//            }else{
//                Toast.makeText(getApplicationContext(),"Email is not Verified "+mAuth.getCurrentUser().getEmail(),Toast.LENGTH_LONG).show();
//                binding.veriText.setText("Email is not Verified");
//            }
//        });
    }


    private void googleSignIn() {
        Intent intent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
            }
        }
    }
    // [END onactivityresult]


    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                    }
                });
    }
    // [END auth_with_google]


    private boolean isEmpty(EditText editText) {
        return editText.getText().toString().trim().length() == 0;
    }



}