package com.example.renthouse.Activity;

import static com.example.renthouse.utilities.Constants.KEY_FULLNAME;
import static com.example.renthouse.utilities.Constants.KEY_USER_KEY;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.renthouse.Admin.Activity.Admin_ActivityMain;
import com.example.renthouse.OOP.Region;
import com.example.renthouse.OOP.Room;
import com.example.renthouse.R;
import com.example.renthouse.utilities.Constants;
import com.example.renthouse.utilities.PreferenceManager;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ktx.Firebase;

public class ActivitySplash extends AppCompatActivity {
    private final static int REQUEST_CODE = 100;
    private PreferenceManager preferenceManager;
    private FirebaseAnalytics mFirebaseAnalytics;
    private boolean isCompleteAdd = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        getPermission();

    }

    private void runningSplash() {
        ProgressBar progressBar = findViewById(R.id.progressBar);
        int totalProgressTime = 10000; // Total progress time in milliseconds
        int intervalTime = 1; // Interval time for each update in milliseconds
        int increment = 10; // Increment value of the progress bar

        final int maxProgress = totalProgressTime / intervalTime; // Calculate the maximum progress value
        progressBar.setMax(maxProgress);

        new Thread(new Runnable() {
            public void run() {
                int progress = 0;

                while (progress < maxProgress) {
                    try {
                        Thread.sleep(intervalTime);
                        progress += increment;

                        // Update the progress bar on the UI thread
                        int finalProgress = progress;
                        runOnUiThread(new Runnable() {
                            public void run() {
                                progressBar.setProgress(finalProgress);
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                mFirebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "Log_app");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, bundle);
                mFirebaseAnalytics.setAnalyticsCollectionEnabled(true);

                preferenceManager = new PreferenceManager(getApplicationContext());
                if(preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)) {
                    if(preferenceManager.getString(Constants.KEY_EMAIL).equals("admin")) {
                        startActivity(new Intent(ActivitySplash.this, Admin_ActivityMain.class));
                    } else{
                        startActivity(new Intent(ActivitySplash.this, ActivityMain.class));

                    }
                } else {
                    startActivity(new Intent(ActivitySplash.this, ActivityLogIn.class));
                }
                finish();

                /*// After the progress is complete, check the user authentication status
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();

                // Start the appropriate activity based on the user's authentication status
                runOnUiThread(new Runnable() {
                    public void run() {
                        *//*if (currentUser != null) {
                            // User is already logged in, navigate to MainActivity
                            startActivity(new Intent(ActivitySplash.this, ActivityMain.class));
                        } else {
                            // User is not logged in, navigate to LoginActivity
                            startActivity(new Intent(ActivitySplash.this, ActivityLogIn.class));
                        }

                        finish(); // Optional: Close the current activity*//*

                    }
                });*/
            }
        }).start();

//        // Tạo node PopularRegion
//        FirebaseDatabase database = FirebaseDatabase.getInstance();
//        DatabaseReference myRef = database.getReference("Rooms");
//        DatabaseReference databaseReference = database.getReference("PopularRegion");
//        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
//                    Room room = snapshot1.getValue(Room.class);
//                    String path_with_type = snapshot1.getValue(Room.class).getLocation().getDistrict().getPath_with_type();
//                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                            Log.d("Số lượng", String.valueOf(snapshot.getChildrenCount()));
//                            if (snapshot.hasChild(path_with_type)) {
//                                Region region = snapshot.child(path_with_type).getValue(Region.class);
//                                region.setSoLuong(region.getSoLuong() + 1);
//                                databaseReference.child(path_with_type).setValue(region);
//                                Log.d("Trùng thông tin", String.valueOf(region));
//                            } else {
//                                Region region = new Region(
//                                        room.getLocation().getDistrict().getName_with_type(),
//                                        room.getLocation().getCity().getName_with_type(),
//                                        1
//                                );
//                                databaseReference.child(path_with_type).setValue(region);
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {
//
//                        }
//                    });
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
    }


    private void getPermission() {
        if(ContextCompat.checkSelfPermission(ActivitySplash.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            runningSplash();
        }else {
            String[] permissons = {Manifest.permission.ACCESS_FINE_LOCATION};
            requestPermissions(permissons, REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                runningSplash();
            } else {
                finish();
            }
        }
    }

    // Declare the launcher at the top of your Activity/Fragment:
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // FCM SDK (and your app) can post notifications.
                } else {
                    // TODO: Inform user that that your app will not show notifications.
                }
            });

    private void askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }
}