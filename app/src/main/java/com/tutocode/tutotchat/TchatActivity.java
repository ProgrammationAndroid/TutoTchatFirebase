package com.tutocode.tutotchat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tutocode.tutotchat.Entities.Message;

public class TchatActivity extends AppCompatActivity {

    private EditText etMessage;
    private ImageButton sendButton;
    private RecyclerView recycler;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mRef;
    private FirebaseAuth.AuthStateListener authStateListener;
    private ChildEventListener childEventListener;

    private SharedPreferences prefs;
    private String username;
    private String userId;

    private static final String TAG = "TCHAT";

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
                    Log.w(TAG, "onChildAdded: ");
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

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
        recycler = (RecyclerView) findViewById(R.id.recycler);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
    }

    private void sendMessage() {
        String content = etMessage.getText().toString();
        if(!TextUtils.isEmpty(content)){
            Message message = new Message(username, userId, content, null);
            mRef.child(Constants.MESSAGES_DB).push().setValue(message);
            etMessage.setText("");
        }
    }

    private void initFirebase(){
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReference();
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAuth.addAuthStateListener(authStateListener);
    }
}
