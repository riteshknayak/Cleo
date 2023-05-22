package com.riteshknayak.cleo;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
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

    int credits;

    String uid;

    Dialog noCreditsDialog;


    //Initialising JSON
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");


    //Initialising OkHttpClient
    OkHttpClient client = new OkHttpClient.Builder()
            .readTimeout(15, TimeUnit.SECONDS)
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        noCreditsDialog = new Dialog(this);
        noCreditsDialog.setContentView(R.layout.no_credits_dialog);
        noCreditsDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);


        //Setting up ViewBinding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        //Initialise Firestore database
        database = FirebaseFirestore.getInstance();


        //Initialise Firebase Auth and get uid
        auth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = auth.getCurrentUser();
        assert firebaseUser != null;
        uid = firebaseUser.getUid();


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
            if (credits == 0) {

                noCreditsDialog.show();


//                PowerMenu powerMenu = new PowerMenu.Builder(MainActivity.this)
//                        .setHeaderView(R.layout.no_credits_dialog_header) // header used for title
//                        .setFooterView(R.layout.no_credits_dialog_footer) // footer used for yes and no buttons
//                        .addItem(new PowerMenuItem("This is DialogPowerMenu", false)) // this is body
//                        .setLifecycleOwner(lifecycleOwner)
//                        .setAnimation(MenuAnimation.SHOW_UP_CENTER)
//                        .setMenuRadius(10f)
//                        .setMenuShadow(10f)
//                        .setWidth(600)
//                        .setSelectedEffect(false)
//                        .build();
//
//
//
////                // Get the root view of your activity or the view that you want to use as the parent
////                View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
////
////                // Calculate the center coordinates of the screen
////                int centerX = rootView.getWidth() / 2;
////                int centerY = rootView.getHeight() / 2;
////
////                // Show the PowerMenu at the center coordinates
////                powerMenu.showAtLocation(rootView, Gravity.CENTER, centerX, centerY);


            } else {
                String question = binding.messageEditText.getText().toString().trim();
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



















