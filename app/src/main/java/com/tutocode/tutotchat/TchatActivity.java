package com.tutocode.tutotchat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.tutocode.tutotchat.Adapters.TchatAdapter;
import com.tutocode.tutotchat.Entities.Message;

import java.util.ArrayList;
import java.util.UUID;

public class TchatActivity extends AppCompatActivity {

    private EditText etMessage;
    private ImageButton sendButton, imageButton;
    private RecyclerView recycler;
    private ProgressBar imageLoader;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mRef;
    private FirebaseStorage mStorage;
    private StorageReference storageReference;
    private FirebaseAuth.AuthStateListener authStateListener;
    private ChildEventListener childEventListener;

    private UploadTask uploadTask;

    private SharedPreferences prefs;
    private String username;
    private String userId;

    private TchatAdapter adapter;

    private static final String TAG = "TCHAT";
    private static final int SELECT_PHOTO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tchat);

        //Initialisation de la toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Tuto::Tchat");
        setSupportActionBar(toolbar);

        //Initialisation des vues
        initViews();
        initFirebase();

        prefs = getSharedPreferences("tchat", MODE_PRIVATE);

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    attachChildListener();
                    username = prefs.getString("PSEUDO", null);
                    userId = user.getUid();
                    adapter.setUser(user);

                }else{
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    finish();
                }
            }
        };
    }

    private void attachChildListener() {
        if(childEventListener == null){
            childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Message message = dataSnapshot.getValue(Message.class);
                    message.setUid(dataSnapshot.getKey());
                    adapter.addMessage(message);
                    recycler.scrollToPosition(adapter.getItemCount() - 1);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Message message = dataSnapshot.getValue(Message.class);
                    message.setUid(dataSnapshot.getKey());
                    adapter.deleteMessage(message);
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            mRef.child(Constants.MESSAGES_DB).limitToLast(100).addChildEventListener(childEventListener);
        }
    }

    private void detachChildListener(){
        if(childEventListener != null){
            mRef.child(Constants.MESSAGES_DB).removeEventListener(childEventListener);
            childEventListener = null;
        }
    }
    private void initViews(){
        etMessage = (EditText) findViewById(R.id.etMessage);
        sendButton = (ImageButton) findViewById(R.id.sendButton);
        imageButton = (ImageButton) findViewById(R.id.imageButton);
        recycler = (RecyclerView) findViewById(R.id.recycler);
        imageLoader = (ProgressBar) findViewById(R.id.imageLoader);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setStackFromEnd(true);
        recycler.setLayoutManager(manager);
        ArrayList<Message> messages = new ArrayList<>();
        adapter = new TchatAdapter(messages);
        recycler.setAdapter(adapter);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage(null);
            }
        });
        etMessage.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if(i == EditorInfo.IME_ACTION_DONE){
                    sendMessage(null);
                    InputMethodManager imm = (InputMethodManager) textView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(textView.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickImage();
            }
        });
    }

    private void pickImage(){
        Intent picker = new Intent(Intent.ACTION_GET_CONTENT);
        picker.setType("image/jpeg");
        picker.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(Intent.createChooser(picker, "Sélectionner une image"), SELECT_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SELECT_PHOTO && resultCode == RESULT_OK){
            Uri imageUri = data.getData();
            Log.w(TAG, "onActivityResult: " + imageUri.toString());
            uploadImage(imageUri);
            //Upload image
        }
    }

    private void uploadImage(Uri imageUri){
        uploadTask = storageReference.child(UUID.randomUUID()+".jpg").putFile(imageUri);
        imageLoader.setVisibility(View.VISIBLE);
        imageButton.setEnabled(false);
        addUploadListener(uploadTask);
    }

    private void addUploadListener(final UploadTask task){
        OnCompleteListener<UploadTask.TaskSnapshot> completeListener = new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    Uri imageUrl = task.getResult().getDownloadUrl();
                    if(imageUrl != null){
                         sendMessage(imageUrl.toString());
                    }
                }else{
                    Toast.makeText(TchatActivity.this, "Impossible d'envoyer l'image", Toast.LENGTH_SHORT).show();
                }
                imageLoader.setVisibility(View.GONE);
                imageButton.setEnabled(true);
            }
        };
        OnProgressListener<UploadTask.TaskSnapshot> onProgressListener = new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
               double percent = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                imageLoader.setProgress((int) percent);
            }
        };
        task.addOnCompleteListener(this, completeListener).addOnProgressListener(this, onProgressListener);
    }

    private void sendMessage(String imageUrl) {
        Message message = null;
        if(imageUrl == null){
            String content = etMessage.getText().toString();
            if(!TextUtils.isEmpty(content)){
                message = new Message(username, userId, content, null);
            }
        }else{
            message = new Message(username, userId, null, imageUrl);
        }
        mRef.child(Constants.MESSAGES_DB).push().setValue(message);
        etMessage.setText("");

    }

    private void initFirebase(){
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReference();
        mStorage = FirebaseStorage.getInstance();
        storageReference = mStorage.getReferenceFromUrl(Constants.STORAGE_PATH).child(Constants.STORAGE_REF);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.logout){
            //TODO
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(authStateListener != null){
            mAuth.removeAuthStateListener(authStateListener);
        }
        detachChildListener();
        adapter.clearMessage();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAuth.addAuthStateListener(authStateListener);
    }
}
