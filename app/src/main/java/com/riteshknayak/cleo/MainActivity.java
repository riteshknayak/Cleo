package com.riteshknayak.cleo;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryPurchasesParams;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.riteshknayak.cleo.Adapters.MessageAdapter;
import com.riteshknayak.cleo.Models.Message;
import com.riteshknayak.cleo.Repo.DatabaseHelper;
import com.riteshknayak.cleo.Utils.Constants;
import com.riteshknayak.cleo.databinding.ActivityMainBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements ObservableScrollViewCallbacks {

    ActivityMainBinding binding;

    List<Message> messageList;
    MessageAdapter messageAdapter;

    FirebaseFirestore database;
    FirebaseAuth auth;
    String uid;

    int credits;

    Dialog noCreditsDialog;

//    private AdView mAdView;

    Button watchAdButton;
    Button getPremiumButton;

    BillingClient billingClient;
    Boolean isPremium = false;


    //Initialising JSON
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");


    //Initialising OkHttpClient
    OkHttpClient client = new OkHttpClient.Builder()
            .readTimeout(15, TimeUnit.SECONDS)
            .build();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Setting up ViewBinding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        isPremium = sharedPref.getBoolean("isPremium", false);

        if (isPremium){
            binding.creditsContainer.setVisibility(View.GONE);
        }

        //Initialise Firestore database
        database = FirebaseFirestore.getInstance();

        //Initialise Firebase Auth and get uid
        auth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = auth.getCurrentUser();
        assert firebaseUser != null;
        uid = firebaseUser.getUid();

        noCreditsDialog = new Dialog(this);
        noCreditsDialog.setContentView(R.layout.no_credits_dialog);
        noCreditsDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        watchAdButton = noCreditsDialog.findViewById(R.id.watchAdButton);
        getPremiumButton = noCreditsDialog.findViewById(R.id.getProButton);

        //Admob Initialisation
        MobileAds.initialize(this, initializationStatus -> {
        });

        RequestConfiguration requestConfiguration = MobileAds.getRequestConfiguration()
                .toBuilder()
                .setTagForChildDirectedTreatment(
                        RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE)
                .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G)
                .build();
        MobileAds.setRequestConfiguration(requestConfiguration);

        Constants.loadRewardedAd(MainActivity.this);

        watchAdButton.setOnClickListener(v -> {
            if (Constants.adLoaded) {
                Constants.rewardedAd.show(MainActivity.this, rewardItem -> {
                    // Handle the reward.
                    Log.d("admob", "The user earned the reward.");
                    int rewardAmount = rewardItem.getAmount();

                    //Provide the reward
                    credits = credits + rewardAmount;
                    Toast.makeText(getApplicationContext(), rewardAmount + " credits added", Toast.LENGTH_SHORT).show();
                    binding.credits.setText(Integer.toString(credits));

                    //change data in database
                    Map<String, Object> creditsData = new HashMap<>();
                    creditsData.put("credits", credits);
                    database.collection("users")
                            .document(uid)
                            .update(creditsData);


                    Constants.loadRewardedAd(MainActivity.this);

                    noCreditsDialog.dismiss();

                });
            } else {
                Toast.makeText(getApplicationContext(), "Sorry, ad is not loaded yet", Toast.LENGTH_SHORT).show();
            }
        });


        getPremiumButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SubscriptionActivity.class);
            startActivity(intent);

        });

        checkIfPremiumUser();

        PurchasesUpdatedListener purchasesUpdatedListener;
        purchasesUpdatedListener = (billingResult, list) -> {};

        billingClient = BillingClient.newBuilder(this)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();

        queryPurchase();


        //Set credits
        database.collection("users")
                .document(uid)
                .get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.get("credits") != null) {
                        credits = Integer.parseInt(Objects.requireNonNull(documentSnapshot.get("credits")).toString());

                        binding.credits.setText(Integer.toString(credits));

                    } else {
                        credits = 0;

                    }
                });


        //Initialising Room Database
        DatabaseHelper databaseHelper = DatabaseHelper.getDB(this);


        //Block Night mode
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);


        //The list of messages
        messageList = new ArrayList<>();
        ArrayList<Message> arrMessages = (ArrayList<Message>) databaseHelper.messagesDao().getMessages();
        messageList.addAll(arrMessages);


        //Setup ActionBar
        setSupportActionBar(binding.toolbar);


        //Setup Recycler view
        binding.recyclerView.setScrollViewCallbacks(this);
        messageAdapter = new MessageAdapter(messageList);
        binding.recyclerView.setAdapter(messageAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        binding.recyclerView.setLayoutManager(llm);


        //Send Button
        binding.sendBtn.setOnClickListener((v) -> {
            String question = binding.messageEditText.getText().toString().trim();

            if (!isPremium){
                if (credits == 0) {
                    noCreditsDialog.show();

                } else {
                    if (!question.equals("")) {
                        addToChat(question, Message.SENT_BY_ME);
                        binding.messageEditText.setText("");
                        callAPI(question);

                        databaseHelper.messagesDao().addMessage(new Message(question, Message.SENT_BY_ME));

                        credits = credits - 1;

                        Map<String, Object> creditsData = new HashMap<>();
                        creditsData.put("credits", credits);

                        database.collection("users")
                                .document(firebaseUser.getUid())
                                .update(creditsData);

                        binding.credits.setText(Integer.toString(credits));
                    } else {
                        Toast.makeText(getApplicationContext(), "Please write a message", Toast.LENGTH_SHORT).show();
                    }
                }

            } else {

                binding.creditsContainer.setVisibility(View.GONE);
                if (!question.equals("")) {
                    addToChat(question, Message.SENT_BY_ME);
                    binding.messageEditText.setText("");
                    callAPI(question);

                    databaseHelper.messagesDao().addMessage(new Message(question, Message.SENT_BY_ME));

                } else {
                    Toast.makeText(getApplicationContext(), "Please write a message", Toast.LENGTH_SHORT).show();
               }

            }

        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkIfPremiumUser();
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkIfPremiumUser();
    }

    void checkIfPremiumUser(){
        database.collection("users")
                .document(uid)
                .get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.get("premiumUser") != null) {
                        isPremium = Boolean.valueOf(Objects.requireNonNull(documentSnapshot.get("premiumUser")).toString());
                    } else {
                        isPremium = false;

                    }

                    if (isPremium){
                        binding.creditsContainer.setVisibility(View.GONE);
                        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putBoolean("isPremium", true);
                        editor.apply();
                    } else {
                        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putBoolean("isPremium", false);
                        editor.apply();

                    }

                });

    }

    void queryPurchase(){
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingServiceDisconnected() {

            }

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                ExecutorService executorService = Executors.newSingleThreadExecutor();
                executorService.execute(() -> {
                    billingClient.queryPurchasesAsync(
                            QueryPurchasesParams.newBuilder()
                                    .setProductType(BillingClient.ProductType.SUBS)
                                    .build(),
                            (billingResult1, purchases) -> {
                                for (Purchase purchase : purchases){
                                    if (purchase != null && purchase.isAcknowledged()){
                                        isPremium = true;

                                        Map<String, Object> isPremiumUser = new HashMap<>();
                                        isPremiumUser.put("premiumUser", true);

                                        FirebaseUser firebaseUser = auth.getCurrentUser();
                                        assert firebaseUser != null;
                                        database.collection("users")
                                                .document(firebaseUser.getUid())
                                                .update(isPremiumUser);

                                    } else {
                                        Map<String, Object> isPremiumUser = new HashMap<>();
                                        isPremiumUser.put("premiumUser", false);

                                        FirebaseUser firebaseUser = auth.getCurrentUser();
                                        assert firebaseUser != null;
                                        database.collection("users")
                                                .document(firebaseUser.getUid())
                                                .update(isPremiumUser);

                                    }
                                }

                            }
                    );


                });
            }
        });
    }

    void addToChat(String message, String sentBy) {
        runOnUiThread(() -> {
            messageList.add(new Message(message, sentBy));
            messageAdapter.notifyItemChanged(messageAdapter.getItemCount() - 1);
            binding.recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
        });
    }

    void addResponse(String response) {
        messageList.remove(messageList.size() - 1);
        addToChat(response, Message.SENT_BY_BOT);

        DatabaseHelper databaseHelper = DatabaseHelper.getDB(this);
        databaseHelper.messagesDao().addMessage(new Message(response, Message.SENT_BY_BOT));
    }

    void callAPI(String question) {
        //okhttp
        messageList.add(new Message("Typing... ", Message.SENT_BY_BOT));

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("model", "gpt-3.5-turbo");

            JSONArray messageArr = new JSONArray();
            JSONObject obj = new JSONObject();
            obj.put("role", "user");
            obj.put("content", question);
            messageArr.put(obj);

            jsonBody.put("messages", messageArr);


        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer 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")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                addResponse("Failed to load response due to " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    JSONObject jsonObject;
                    try {
                        assert response.body() != null;
                        jsonObject = new JSONObject(response.body().string());
                        JSONArray jsonArray = jsonObject.getJSONArray("choices");
                        String result = jsonArray.getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content");
                        addResponse(result.trim());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                } else {
                    assert response.body() != null;
                    addResponse("Failed to load response due to " + response.body());
                }
            }
        });


    }


    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {

    }

    @Override
    public void onDownMotionEvent() {
    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
    }

    int counter = 0;

    @Override
    public void onBackPressed() {
        counter++;
        if (counter == 2) {
            super.onBackPressed();
            counter = 0;
        }
    }
}



















