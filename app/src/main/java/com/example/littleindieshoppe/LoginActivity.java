package com.example.littleindieshoppe;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.littleindieshoppe.Model.Users;
import com.example.littleindieshoppe.Prevalent.Prevalent;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.paperdb.Paper;

public class LoginActivity extends AppCompatActivity {
    private EditText inputPhoneNumber, inputPassword;
    private Button loginButton;
    private ProgressDialog loadingBar;
    private TextView adminLink, notAdminLink;

    private String parentDbName = "Users";
    private CheckBox chkBoxRememberMe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginButton = findViewById(R.id.login_btn);

        inputPassword = findViewById(R.id.login_password_input);
        inputPhoneNumber = findViewById(R.id.login_phone_number_input);
        adminLink = findViewById(R.id.admin_panel_link);
        notAdminLink = findViewById(R.id.not_admin_panel_link);

        loadingBar = new ProgressDialog(this);

        chkBoxRememberMe = findViewById(R.id.remember_me_chkb);
        Paper.init(this);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        adminLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputPhoneNumber.setHint("Retailer ID Number");
                loginButton.setText("Retailer Login");
                adminLink.setVisibility(View.INVISIBLE);
                notAdminLink.setVisibility(View.VISIBLE);

                parentDbName = "Admins";
            }
        });

        notAdminLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputPhoneNumber.setHint("Phone Number");
                loginButton.setText("Login");
                adminLink.setVisibility(View.VISIBLE);
                notAdminLink.setVisibility(View.INVISIBLE);

                parentDbName = "Users";
            }
        });
    }

    private void loginUser() {
        String phone = inputPhoneNumber.getText().toString();
        String password = inputPassword.getText().toString();

        if (TextUtils.isEmpty(phone))
            Toast.makeText(this, "Enter Your Phone Number", Toast.LENGTH_SHORT).show();
        else if (TextUtils.isEmpty(password))
            Toast.makeText(this, "Enter Your Password", Toast.LENGTH_SHORT).show();
        else {
            loadingBar.setTitle("Login");
            loadingBar.setMessage("Accessing Account");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            allowAccessToAccount(phone, password);
        }
    }

    private void allowAccessToAccount(final String phone, final String password) {
        if (chkBoxRememberMe.isChecked()) {
            Paper.book().write(Prevalent.userPhoneKey, phone);
            Paper.book().write(Prevalent.userPasswordKey, password);
        }

        final DatabaseReference rootRef;
        rootRef = FirebaseDatabase.getInstance().getReference();

        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(parentDbName).child(phone).exists()) {
                    Users usersData = dataSnapshot.child(parentDbName).child(phone).getValue(Users.class);

                    if (usersData.getPhone().equals(phone)) {
                        if (usersData.getPassword().equals(password)) {
                            if (parentDbName.equals("Admins")) {
                                Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();

                                Intent intent = new Intent(LoginActivity.this, AdminCategoryActivity.class);
                                startActivity(intent);
                            } else if (parentDbName.equals("Users")) {
                                Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();

                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                Prevalent.currentOnlineUser = usersData;
                                startActivity(intent);
                            }
                        }
                    } else {
                        loadingBar.dismiss();
                        Toast.makeText(LoginActivity.this, "Incorrect Password", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "The Number " + phone + " Does Not Belong To An Account", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
