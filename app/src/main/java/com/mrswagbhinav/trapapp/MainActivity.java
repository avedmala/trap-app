package com.mrswagbhinav.trapapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    FirebaseUser user;
    FirebaseFirestore db;
    DocumentSnapshot document;
    FirebaseStorage storage;
    StorageReference storageReference;
    ContentResolver cr;

    FloatingActionButton buttonFilterFeed;
    FloatingActionButton buttonMap;
    BottomNavigationView bottomNav;

    Double longitude;
    Double latitude;

    boolean map = false;

    private static final String TAG = "MainActivity";


    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonFilterFeed = findViewById(R.id.id_buttonFilterFeed);
        buttonMap = findViewById(R.id.id_buttonMap);
        bottomNav = findViewById(R.id.bottom_navigation);

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

//        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
//            @SuppressLint("RestrictedApi")
//            @Override
//            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
//
//                switch (menuItem.getItemId()) {
//                    case R.id.nav_log:
//                        pushFragments("NewtrapFragment", new NewtrapFragment());
//                        buttonFilterFeed.setVisibility(View.GONE);
//                        buttonMap.setVisibility(View.GONE);
//                        break;
//                    case R.id.nav_feed:
//                        pushFragments("FeedFragment", new FeedFragment());
//                        buttonFilterFeed.setVisibility(View.VISIBLE);
//                        buttonMap.setVisibility(View.VISIBLE);
//                        break;
//                    case R.id.nav_profile:
//                        pushFragments("ProfileFragment", new ProfileFragment());
//                        buttonFilterFeed.setVisibility(View.GONE);
//                        buttonMap.setVisibility(View.GONE);
//                        break;
//                }
//
//                return true;
//            }
//        });

        buttonMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(map) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FeedFragment()).commit();
                    buttonMap.setImageResource(R.drawable.ic_format_list_bulleted_black_24dp);
                    map = false;
                }
                else {
                    SupportMapFragment mapFragment = new SupportMapFragment();
                    mapFragment.getMapAsync(MainActivity.this);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, mapFragment).commit();
                    buttonMap.setImageResource(R.drawable.ic_map_black_24dp);
                    map = true;
                }
            }
        });

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FeedFragment()).commit();
        bottomNav.setSelectedItemId(R.id.nav_feed);
        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment selectedFragment = null;

                switch (menuItem.getItemId()) {
                    case R.id.nav_log:
                        selectedFragment = new NewtrapFragment();
                        buttonFilterFeed.setVisibility(View.GONE);
                        buttonMap.setVisibility(View.GONE);
                        break;
                    case R.id.nav_feed:
                        selectedFragment = new FeedFragment();
                        buttonFilterFeed.setVisibility(View.VISIBLE);
                        buttonMap.setVisibility(View.VISIBLE);
                        break;
                    case R.id.nav_profile:
                        selectedFragment = new ProfileFragment();
                        buttonFilterFeed.setVisibility(View.GONE);
                        buttonMap.setVisibility(View.GONE);
                        break;
                }

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
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

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        db.collection("traps")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        ArrayList<Trap> latLngArrayList = new ArrayList<>();
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                if(((Timestamp) document.get("time")).compareTo(Timestamp.now()) > 0) {     //check if the trap has already happened
                                    if(((ArrayList) document.get("invites")).contains(user.getUid()) || document.get("host").equals(user.getUid())) {     //check if user is invited or hosting
                                        latLngArrayList.add(0, new Trap((String) document.get("title"), (String) document.get("host"), (String) document.get("location_name"), (String) document.get("location_address"), (Timestamp) document.get("time"), (GeoPoint) document.get("geopoint"), document.getId()));
                                    }
                                }
                            }
                        }
                        else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }

                        for(Trap trap : latLngArrayList) {
                            LatLng latLng = new LatLng(trap.getLat(), trap.getLng());
                            googleMap.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .title(trap.getTitle())
                                    .snippet(trap.getLocationName())
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                            );
                        }

                    }
                });
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude)));
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(MainActivity.this, R.raw.map_style));

        googleMap.getUiSettings().setMapToolbarEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);
        googleMap.getUiSettings().setTiltGesturesEnabled(true);

        googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                googleMap.setMinZoomPreference(1);
            }
        });

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                googleMap.setMinZoomPreference(16);
                Toast.makeText(MainActivity.this, marker.getSnippet(), Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }

}
