package com.example.sportkomchat.screens;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.sportkomchat.R;
import com.example.sportkomchat.adapters.MessageAdapter;
import com.example.sportkomchat.pojo.MessageModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MessageActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private RecyclerView recyclerView;
    private MessageAdapter adapter;

    private String userName;
    private EditText editTextMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        userName = "Артем Акимов";
        editTextMessage = findViewById(R.id.editTextTextMultiLine);
        recyclerView = findViewById(R.id.recyclerViewMessages);
        adapter = new MessageAdapter();
        recyclerView.setAdapter(adapter);
        db.collection("messages").orderBy("date").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value != null) {
                    List<MessageModel> messages = value.toObjects(MessageModel.class);
                    adapter.setMessages(messages);
                }
            }
        });
        if (mAuth.getCurrentUser() != null) {
            Toast.makeText(this, "Вы авторизованы", Toast.LENGTH_SHORT).show();
        }else {
            signOut();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.drawer_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.item_SignOut){
            mAuth.signOut();
            signOut();
        }
        return super.onOptionsItemSelected(item);
    }

    private void getMessage(){
        String message = editTextMessage.getText().toString().trim();
        if (!message.isEmpty()){
            db.collection("messages").add(new MessageModel(userName,message, System.currentTimeMillis())).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    editTextMessage.setText("");
                    recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MessageActivity.this, "Ошибка, проверьте интернет подключение и попробуйте снова", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    private void signOut(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void onClickSendMessage(View view) {
        getMessage();
    }


}