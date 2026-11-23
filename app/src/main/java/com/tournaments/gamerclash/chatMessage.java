package com.tournaments.gamerclash;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.tournaments.gamerclash.Adapters.ChatAdapter;
import com.tournaments.gamerclash.Models.chatModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class chatMessage extends AppCompatActivity {

    RecyclerView recyclerView;
    EditText messageInput;
    Button sendButton;
    Toolbar toolbar;
    ChatAdapter adapter;
    List<chatModel> messageList;
    DatabaseReference chatRef;
    LinearLayout bottomBar;
    ImageView back_btn;
    FirebaseAuth mAuth;
    String currentUserId, currentUserName = "Anonymous";

    List<String> bannedWords = Arrays.asList(
            "mc", "bc", "bkl", "chutiya", "saala", "gaand", "madarchod", "behenchod"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_message);
        back_btn = findViewById(R.id.back_button);
        back_btn.setOnClickListener(v -> {
            onBackPressed();
        });

        toolbar = findViewById(R.id.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        int statusBarHeight = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (statusBarHeight > 0) {
            toolbar.setPadding(0, getResources().getDimensionPixelSize(statusBarHeight), 0, 0);
            toolbar.setClipToPadding(false);
        }

        recyclerView = findViewById(R.id.recyclerViewMessages);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        bottomBar = findViewById(R.id.bottomBar);

        ViewCompat.setOnApplyWindowInsetsListener(bottomBar, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), systemBars.bottom);
            return insets;
        });

        messageList = new ArrayList<>();
        adapter = new ChatAdapter(messageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        FirebaseDatabase.getInstance().getReference("users").child(currentUserId).child("username")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            currentUserName = snapshot.getValue(String.class);
                            adapter.setCurrentUserName(currentUserName); // Set username in adapter
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

        chatRef = FirebaseDatabase.getInstance().getReference("ChatMessages");

        sendButton.setOnClickListener(v -> {
            String text = messageInput.getText().toString().trim();

            if (TextUtils.isEmpty(text)) {
                Toast.makeText(this, "Please type a message", Toast.LENGTH_SHORT).show();
                return;
            }

            String filteredText = sanitizeText(text);

            String messageId = chatRef.push().getKey();
            chatModel message = new chatModel(currentUserName, filteredText);
            chatRef.child(messageId).setValue(message);
            messageInput.setText("");
        });

        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    chatModel msg = data.getValue(chatModel.class);
                    messageList.add(msg);
                }
                adapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(messageList.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private String sanitizeText(String message) {
        String[] words = message.split("\\s+");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            boolean replaced = false;
            for (String bad : bannedWords) {
                if (word.equalsIgnoreCase(bad)) {
                    result.append("*".repeat(bad.length())).append(" ");
                    replaced = true;
                    break;
                }
            }
            if (!replaced) {
                result.append(word).append(" ");
            }
        }

        return result.toString().trim();
    }
}
