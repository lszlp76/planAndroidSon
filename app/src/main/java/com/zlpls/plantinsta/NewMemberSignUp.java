package com.zlpls.plantinsta;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class NewMemberSignUp extends AppCompatActivity {
EditText emailText,passwordText;
Button button;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_member_sign_up);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setTitle("PlantInsta Yeni Üye");
        emailText = findViewById(R.id.mail);
        passwordText = findViewById(R.id.password);
        mAuth = FirebaseAuth.getInstance();

    }
    public void signUp(View view) {

        if (!emailText.getText().toString().trim().equals("") && !passwordText.getText().toString().trim().equals("")) {
            mAuth.createUserWithEmailAndPassword(emailText.getText()
                    .toString().trim(), passwordText.getText().toString().trim())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                showAlert("Yeni Üye Kaydı", "PlantInsta Bitki Günlüğüne hoşgeldiniz \n" + user.getEmail() + "\nadresiniz ile giriş yapabilirisiniz.");


                            } else {

                                Toast.makeText(getApplicationContext(), "e-mail geçersiz / şifre 6 haneli değil",
                                        Toast.LENGTH_SHORT).show();

                                //+task.getException()  eklersen firebase hatasını görürsün


                            }
                        }
                    });

        } else {
            Toast.makeText(getApplicationContext(), "Email veya Şifre boş bırakılmamalıdır",
                    Toast.LENGTH_SHORT).show();

        }

    }

    public void showAlert(String title, String message) {

        AlertDialog.Builder builder = new AlertDialog.Builder(NewMemberSignUp.this);
        builder
                .setTitle(title)
                .setMessage(message)

                .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        Intent intent = new Intent(NewMemberSignUp.this, SplashActivity.class);
                        startActivity(intent);
                        finish();
                    }
                })

                .show();


    }
}