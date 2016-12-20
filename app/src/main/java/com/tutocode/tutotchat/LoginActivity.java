package com.tutocode.tutotchat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tutocode.tutotchat.Entities.User;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText etPseudo;
    private ProgressBar loader;
    private Button btnLogin;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mRef;
    private SharedPreferences prefs;

    private static final String TAG = "TCHAT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Initialisation des vues
        etPseudo = (EditText) findViewById(R.id.etPseudo);
        loader = (ProgressBar) findViewById(R.id.loader);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(this);

        //Init de firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReference();

        prefs = getSharedPreferences("tchat", MODE_PRIVATE);

        if(mAuth.getCurrentUser() != null && prefs.getString("PSEUDO", null) != null ){
            startActivity(new Intent(getApplicationContext(), TchatActivity.class));
            finish();
        }

    }

    private void checkUsername(final String username, final CheckUsernameCallback callback){
        mRef.child(Constants.USERNAMES_DB).child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null){
                    callback.isTaken();
                }else{
                    callback.isValid(username);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "onCancelled: " +databaseError.getMessage() );
                loader.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void registerUser(final String username){
        mAuth.signInAnonymously().addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(!task.isSuccessful()){
                    Toast.makeText(LoginActivity.this, "Connexion impossible, Veuillez réessayer", Toast.LENGTH_SHORT).show();
                }

                final String userId = task.getResult().getUser().getUid();

                checkUsername(username, new CheckUsernameCallback() {
                    @Override
                    public void isValid(final String username) {

                        User newUser = new User(username, userId);
                        mRef.child(Constants.USERS_DB).child(userId).setValue(newUser).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    mRef.child(Constants.USERNAMES_DB).child(username).setValue(userId);
                                    prefs.edit().putString("PSEUDO", username).apply();
                                    startActivity(new Intent(getApplicationContext(), TchatActivity.class));
                                    finish();
                                }
                            }
                        });

                    }

                    @Override
                    public void isTaken() {
                        Toast.makeText(LoginActivity.this, "Veuillez choisir un autre pseudo", Toast.LENGTH_SHORT).show();
                        loader.setVisibility(View.INVISIBLE);
                    }
                });
            }
        });
    }

    @Override
    public void onClick(View view) {
        loader.setVisibility(View.VISIBLE);
        String username = etPseudo.getText().toString();
        if(!TextUtils.isEmpty(username)){
            registerUser(username);
        }

    }

    interface CheckUsernameCallback{
        void isValid(String username);
        void isTaken();
    }

}
