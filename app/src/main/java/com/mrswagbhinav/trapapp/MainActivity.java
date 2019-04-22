package com.mrswagbhinav.trapapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    FirebaseUser user;
    FirebaseFirestore db;
    DocumentSnapshot document;
    FirebaseStorage storage;
    StorageReference storageReference;
    ContentResolver cr;

    FloatingActionButton buttonFilterFeed;

    Double longitude;
    Double latitude;

    private static final String TAG = "MainActivity";


    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonFilterFeed = findViewById(R.id.id_buttonFilterFeed);
        buttonFilterFeed.setVisibility(View.GONE);

        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 101);

        LocationListener ls = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                longitude = location.getLongitude();
                latitude = location.getLatitude();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        cr = getContentResolver();

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, ls);
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, ls);
        }

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        final Bundle bundle = this.getIntent().getExtras();
        if (bundle != null)
            user = bundle.getParcelable("KEY");

        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        DocumentReference docRef = db.collection("users").document(user.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        //pushFragments("FeedFragment", new FeedFragment());
        bottomNav.setSelectedItemId(R.id.nav_feed);

        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                switch (menuItem.getItemId()) {
                    case R.id.nav_log:
                        pushFragments("NewtrapFragment", new NewtrapFragment());
                        buttonFilterFeed.setVisibility(View.GONE);
                        break;
                    case R.id.nav_feed:
                        pushFragments("FeedFragment", new FeedFragment());
                        buttonFilterFeed.setVisibility(View.VISIBLE);
                        break;
                    case R.id.nav_profile:
                        pushFragments("ProfileFragment", new ProfileFragment());
                        buttonFilterFeed.setVisibility(View.GONE);
                        break;
                }

                return true;
            }
        });

    }

    public void pushFragments(String tag, Fragment fragment) {

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();


        if (manager.findFragmentByTag(tag) == null) {
            ft.add(R.id.fragment_container, fragment, tag);
        }

        Fragment FeedFragment = manager.findFragmentByTag("FeedFragment");
        Fragment NewTrapFragment = manager.findFragmentByTag("NewTrapFragment");
        Fragment ProfileFragment = manager.findFragmentByTag("ProfileFragment");

        // Hide all Fragment
        if (FeedFragment != null) {
            ft.hide(FeedFragment);
        }
        if (NewTrapFragment != null) {
            ft.hide(NewTrapFragment);
        }
        if (ProfileFragment != null) {
            ft.hide(ProfileFragment);
        }

        // Show  current Fragment
        if (tag.equals("FeedFragment")) {
            if (FeedFragment != null) {
                ft.show(FeedFragment);
            }
        }
        if (tag.equals("NewTrapFragment")) {
            if (NewTrapFragment != null) {
                ft.show(NewTrapFragment);
            }
        }
        if (tag.equals("ProfileFragment")) {
            if (ProfileFragment != null) {
                ft.show(ProfileFragment);
            }
        }
        ft.commit();
    }


}
