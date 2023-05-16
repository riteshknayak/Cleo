package com.riteshknayak.cleo;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements ObservableScrollViewCallbacks {



    ObservableRecyclerView recyclerView;

    TextView welcomeTextView;
    EditText messageEditText;
    ImageButton sendButton;
    List<Message> messageList;
    MessageAdapter messageAdapter;
    Toolbar toolbar;



    //Initialising JSON
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");


    //Initialising OkHttpClient
    OkHttpClient client = new OkHttpClient.Builder()
                                          .readTimeout(15, TimeUnit.SECONDS)
                                          .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialising Database
        DatabaseHelper databaseHelper = DatabaseHelper.getDB(this);

        //Block Night mode
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        //The list of messages
        messageList = new ArrayList<>();
        ArrayList<Message> arrMessages = (ArrayList<Message>) databaseHelper.messagesDao().getMessages();
        messageList.addAll(arrMessages);

        recyclerView = findViewById(R.id.recycler_view);
        messageEditText = findViewById(R.id.message_edit_text);
        sendButton = findViewById(R.id.send_btn);
        toolbar = findViewById(R.id.toolbar);
        recyclerView.setScrollViewCallbacks(this);

        setSupportActionBar(toolbar);

        //Setup Recycler view
        messageAdapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(messageAdapter);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        recyclerView.setLayoutManager(llm);

        sendButton.setOnClickListener((v)->{
            String question = messageEditText.getText().toString().trim();
            addToChat(question,Message.SENT_BY_ME);
            databaseHelper.messagesDao().addMessage(new Message(question, Message.SENT_BY_ME));
            messageEditText.setText("");
            callAPI(question);
        });
    }

    void addToChat(String message,String sentBy){
        runOnUiThread(() -> {
            messageList.add(new Message(message,sentBy));
            messageAdapter.notifyItemChanged(messageAdapter.getItemCount()-1);
            recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
        });
    }

    void addResponse(String response){
        messageList.remove(messageList.size()-1);
        addToChat(response,Message.SENT_BY_BOT);

        DatabaseHelper databaseHelper = DatabaseHelper.getDB(this);
        databaseHelper.messagesDao().addMessage(new Message(response, Message.SENT_BY_BOT));
    }

    void callAPI(String question){
        //okhttp
        messageList.add(new Message("Typing... ",Message.SENT_BY_BOT));

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("model","gpt-3.5-turbo");

            JSONArray messageArr = new JSONArray();
            JSONObject obj = new JSONObject();
            obj.put("role","user");
            obj.put("content",question);
            messageArr.put(obj);

            jsonBody.put("messages",messageArr);


        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(jsonBody.toString(),JSON);
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization","Bearer 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")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                addResponse("Failed to load response due to "+e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.isSuccessful()){
                    JSONObject  jsonObject;
                    try {
                        jsonObject = new JSONObject(response.body().string());
                        JSONArray jsonArray = jsonObject.getJSONArray("choices");
                        String result = jsonArray.getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content");
                        addResponse(result.trim());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }else{
                    addResponse("Failed to load response due to "+response.body().toString());
                }
            }
        });





    }


    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
//        ActionBar ab = getSupportActionBar();
//        if (dragging) {
//            assert ab != null;
//            if (scrollY > ab.getHeight()) {
//                ab.hide();
//            } else {
//                ab.show();
//            }
//        }

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
        if (counter==2){
            super.onBackPressed();
            counter = 0;
        };
    }
}



















