package com.example.renthouse.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.Manifest;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.renthouse.Admin.Activity.Admin_ActivityMain;
import com.example.renthouse.OOP.AccountClass;
import com.example.renthouse.Other.CommonUtils;
import com.example.renthouse.R;
import com.example.renthouse.utilities.Constants;
import com.example.renthouse.utilities.PreferenceManager;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.checkerframework.checker.units.qual.C;

import java.net.IDN;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;

public class ActivityLogIn extends AppCompatActivity {
    private static final int REQUEST_CODE = 1;
    private boolean showOneTapUI = true;
    private TextView forgotPasswordBtn;
    private TextView signUpBtn;
    private Button loginWithGoogleBtn, login_logInBtn;
    private FirebaseStorage storage;
    FirebaseAuth mAuth;
    private TextInputEditText login_email, login_password;
    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    FirebaseDatabase database;
    DatabaseReference reference;
    String imageURL;
    private PreferenceManager preferenceManager;
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        preferenceManager = new PreferenceManager(getApplicationContext());

        progressDialog = new ProgressDialog(ActivityLogIn.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");

        database = FirebaseDatabase.getInstance();
        reference = database.getReference();
        storage = FirebaseStorage.getInstance();
        //

        forgotPasswordBtn = findViewById(R.id.forgotPasswordBtn);
        signUpBtn = findViewById(R.id.login_signUpBtn);
        loginWithGoogleBtn = findViewById(R.id.login_loginWithGoogleBtn);
        login_email = findViewById(R.id.login_email);
        login_password = findViewById(R.id.login_password);
        login_logInBtn = findViewById(R.id.login_logInBtn);
        mAuth = FirebaseAuth.getInstance();


        login_logInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email, password;
                email = String.valueOf(login_email.getText());
                password = String.valueOf(login_password.getText());

                if (email.equals("admin") && password.equals("admin")) {
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                    preferenceManager.putString(Constants.KEY_EMAIL, email);
                    preferenceManager.putString(Constants.KEY_PASSWORD, password);
                    CommonUtils.showNotification(ActivityLogIn.this, "Thông Báo", "Chào mừng Admin trở lại", R.drawable.ic_phobien_1);
                    startActivity(new Intent(ActivityLogIn.this, Admin_ActivityMain.class));
                    finish();
                    return;
                }
                if (TextUtils.isEmpty(email)) {
                    login_email.setError("Vui lòng điền email");
                    return;
                }
                if (!isValidEmail(email)) {
                    login_email.setError("Vui lòng điền đúng định dạng email");
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    login_password.setError("Vui lòng điền mật khẩu");
                    return;
                }


                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            preferenceManager.putString(Constants.KEY_EMAIL, email);
                            preferenceManager.putString(Constants.KEY_PASSWORD, password);
                            preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);

                            DatabaseReference ref = database.getReference();
                            Query query = ref.child("Accounts").orderByChild("email").equalTo(email);
                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                        preferenceManager.putString(Constants.KEY_USER_KEY, dataSnapshot.getKey());
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                            pushNotification();
                            Toast.makeText(getApplicationContext(), "Login Successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), ActivityMain.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(ActivityLogIn.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        forgotPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ActivityForgotPassword.class);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ActivitySignUp.class);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });
        // login with google

        loginWithGoogleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();
                gsc = GoogleSignIn.getClient(ActivityLogIn.this, gso);
                signInWithGoogle();
            }
        });


        login_password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO) {
                    login_logInBtn.performClick();
                    return true;
                }
                return false;
            }
        });

        login_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Trước khi text thay đổi

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Trong quá trình text thay đổi
                TextInputLayout pw = findViewById(R.id.textInputPassword);
                pw.setPasswordVisibilityToggleEnabled(true);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Sau khi text đã thay đổi
                if (login_password.getText().toString().isEmpty()) {
                    TextInputLayout pw = findViewById(R.id.textInputPassword);
                    pw.setPasswordVisibilityToggleEnabled(false);
                } else {
                    TextInputLayout pw = findViewById(R.id.textInputPassword);
                    pw.setPasswordVisibilityToggleEnabled(true);
                }
            }
        });
    }

    private void signInWithGoogle() {
        Intent signInIntent = gsc.getSignInIntent();
        startActivityForResult(signInIntent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                progressDialog.show();
                task.getResult(ApiException.class);
                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
                firebaseAuthWithGoogle(account.getIdToken());


            } catch (ApiException e) {
                Toast.makeText(ActivityLogIn.this, "Error", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        } else {
            progressDialog.dismiss();
        }

    }

    private void firebaseAuthWithGoogle(String idToken) {
        progressDialog.show();
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        String TAG;
                        if (task.isSuccessful()) {
                            progressDialog.show();
                            // Cập nhật thông tin người dùng lên Firebase Authentication
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUserInfo(user);
                            // them thong tin tai khoan vao realtime
                            if (user != null) {
                                String personalID = user.getUid();
                                String personName = user.getDisplayName();
                                String personEmail = user.getEmail();
                                Uri personPhoto = user.getPhotoUrl();

                                Date now = new Date();
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                String formattedDate = dateFormat.format(now);
                                AccountClass accountClass = new AccountClass(personName, personEmail, "+84", "********", personPhoto.toString(), formattedDate, false, null);
                                String emailToCheck = personEmail;

                                preferenceManager.putString(Constants.KEY_IMAGE, accountClass.getImage());
                                preferenceManager.putString(Constants.KEY_PHONENUMBER, accountClass.getPhoneNumber());
                                preferenceManager.putString(Constants.KEY_EMAIL, accountClass.getEmail());
                                preferenceManager.putString(Constants.KEY_FULLNAME, accountClass.getFullname());
                                preferenceManager.putString(Constants.KEY_DATECREATEDACCOUNT, accountClass.getNgayTaoTaiKhoan());

                                DatabaseReference accountsRef = reference.child("Accounts");
                                Query emailQuery = accountsRef.orderByChild("email").equalTo(emailToCheck);

                                emailQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (!dataSnapshot.exists()) {
                                            DatabaseReference newChildRef = reference.child("Accounts").push();
                                            String generatedKey = newChildRef.getKey();
                                            preferenceManager.putString(Constants.KEY_USER_KEY, generatedKey);

                                            newChildRef.setValue(accountClass)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                // Data successfully saved



                                                                pushSuccessFullNotification();
                                                                progressDialog.dismiss();
                                                                preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                                                                startActivity(new Intent(ActivityLogIn.this, ActivityMain.class));
                                                            } else {
                                                                // Handle the error here
                                                            }
                                                        }
                                                    });
                                        } else {
                                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                AccountClass checkAcc = snapshot.getValue(AccountClass.class);
                                                preferenceManager.putString(Constants.KEY_USER_KEY, snapshot.getKey());
                                                preferenceManager.putString(Constants.KEY_IMAGE, checkAcc.getImage());

                                                if (checkAcc.getBlocked()) {
                                                    progressDialog.dismiss();
                                                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);

                                                    startActivity(new Intent(ActivityLogIn.this, ActivityBlocked.class));
                                                } else {
                                                    pushSuccessFullNotification();
                                                    progressDialog.dismiss();
                                                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                                                    startActivity(new Intent(ActivityLogIn.this, ActivityMain.class));
                                                }
                                            }

                                        }


                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        // Handle the error here
                                        CommonUtils.showNotification(getApplicationContext(), "Trạng thái đăng nhập", "Thất bại", R.drawable.ic_phobien_1);
                                        progressDialog.dismiss();
                                    }
                                });

                            }

                            //pushSuccessFullNotification();
                            CommonUtils.showNotification(getApplicationContext(), "Trạng thái đăng nhập", "Chào mừng bạn trở lại", R.drawable.ic_phobien_1);
                            //Toast.makeText(getApplicationContext(), "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                        } else {
                            // Đăng nhập thất bại
                            //pushFailerNotification();
                            CommonUtils.showNotification(getApplicationContext(), "Trạng thái đăng nhập", "Thất bại", R.drawable.ic_phobien_1);
                            //Toast.makeText(getApplicationContext(), "Thất bại", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
        progressDialog.dismiss();
    }

    private void updateUserInfo(FirebaseUser user) {
        // Cập nhật thông tin người dùng lên Firebase Authentication
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(user.getDisplayName())
                .setPhotoUri(user.getPhotoUrl())
                .build();
        user.updateProfile(profileUpdates);
    }

    public boolean isValidEmail(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void pushNotification() {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

        // Tạo notification channel nếu ứng dụng chạy trên Android 8.0 trở lên
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("my_channel_id", "My Channel", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("My Channel Description");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "my_channel_id")
                .setContentTitle("Đăng nhập thành công")
                .setContentText("Cảm ơn bạn đã sử dụng sản phẩm")
                .setSmallIcon(R.drawable.notification_flat)
                .setLargeIcon(bitmap);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(0, builder.build());
    }

    private void pushSuccessFullNotification() {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

        // Tạo notification channel nếu ứng dụng chạy trên Android 8.0 trở lên
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("my_channel_id", "My Channel", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("My Channel Description");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "my_channel_id")
                .setContentTitle("Đăng nhập bằng tài khoản Google thành công")
                .setContentText("Cảm ơn bạn đã sử dụng sản phẩm")
                .setSmallIcon(R.drawable.notification_flat)
                .setLargeIcon(bitmap);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(0, builder.build());
    }

    private void pushFailerNotification() {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

        // Tạo notification channel nếu ứng dụng chạy trên Android 8.0 trở lên
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("my_channel_id", "My Channel", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("My Channel Description");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "my_channel_id")
                .setContentTitle("Đăng nhập thất bại")
                .setContentText("Hãy kiểm tra lại tài khoản của bạn")
                .setSmallIcon(R.drawable.notification_flat)
                .setLargeIcon(bitmap);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(0, builder.build());
    }
}