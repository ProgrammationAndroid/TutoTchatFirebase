package com.tutocode.tutotchat;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText etPseudo;
    private ProgressBar loader;
    private Button btnLogin;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mRef;

    private FirebaseAuth.AuthStateListener authStateListener;

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

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() != null){
                    //Rediriger vers la tchat Activity
                    Log.w(TAG, "onAuthStateChanged: " + firebaseAuth.getCurrentUser().getUid());
                }
            }
        };


    }

    @Override
    public void onClick(View view) {
        loader.setVisibility(View.VISIBLE);
        mAuth.signInAnonymously().addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(!task.isSuccessful()){
                    Toast.makeText(LoginActivity.this, "Connexion impossible, Veuillez réessayer", Toast.LENGTH_SHORT).show();
                }
                loader.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(authStateListener != null){
            mAuth.removeAuthStateListener(authStateListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAuth.addAuthStateListener(authStateListener);
    }
}
