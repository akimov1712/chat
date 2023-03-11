package com.example.sportkomchat.screens;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class MessageActivity extends AppCompatActivity {

    private static final int RC_GET_IMAGE = 120;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    // Get a non-default Storage bucket
    private FirebaseStorage storage;
    private StorageReference reference;

    private RecyclerView recyclerView;
    private MessageAdapter adapter;

    private String userName;
    private EditText editTextMessage;
    private ImageView btnAddImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        // Create a storage reference from our app
        reference = storage.getReference();
        userName = "Артем Акимов";
        editTextMessage = findViewById(R.id.editTextTextMultiLine);
        recyclerView = findViewById(R.id.recyclerViewMessages);
        adapter = new MessageAdapter();
        recyclerView.setAdapter(adapter);
        if (mAuth.getCurrentUser() != null) { // проверяет войден пользователь или нет, если нет перенаправляет его в активити с входом
            Toast.makeText(this, "Вы авторизованы", Toast.LENGTH_SHORT).show();
            userName = mAuth.getCurrentUser().getEmail();
        }else {
            signOut();
        }
    }

    @Override
    protected void onResume() {
        super.onResume(); // при запуске экрана подписывается на коллекцию, при изменении обновляет данные на экране
        db.collection("messages").orderBy("date").addSnapshotListener(new EventListener<QuerySnapshot>() { // сортируем по полю date
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value != null) {
                    List<MessageModel> messages = value.toObjects(MessageModel.class);
                    adapter.setMessages(messages);
                    recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { // получает разметку menu toolbar
        getMenuInflater().inflate(R.menu.drawer_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) { // отслеживает нажатие на кнопку из toolbar
        if (item.getItemId() == R.id.item_SignOut){
            mAuth.signOut();
            signOut();
        }
        return super.onOptionsItemSelected(item);
    }

    private void getMessage(){ // получаем из инпута сообщение и добавляем его в коллекцию, вместе с ней передаем время(по времени сортируются сообщения)
        String message = editTextMessage.getText().toString().trim();
        if (!message.isEmpty()){
            db.collection("messages").add(new MessageModel(userName,message, System.currentTimeMillis())).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    editTextMessage.setText("");
                    recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
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


    public void onClickImageSend(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(intent,RC_GET_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GET_IMAGE && resultCode == RESULT_OK){
            if (data != null) {
                Uri uri = data.getData();
                if (uri != null){
                    StorageReference referenceToImage = reference.child("images/" + uri.getLastPathSegment());
                    referenceToImage.putFile(uri);
                }
            }
        }
    }
}