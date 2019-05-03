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
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

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


    @SuppressLint({"RestrictedApi", "ResourceAsColor"})
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonFilterFeed = findViewById(R.id.id_buttonFilterFeed);
        buttonMap = findViewById(R.id.id_buttonMap);
        bottomNav = findViewById(R.id.bottom_navigation);

        try {
            String info;
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(openFileInput("info.json")));
            do {
                info = bufferedReader.readLine();
            } while (bufferedReader.readLine() != null);
            JSONObject obj = new JSONObject(info);

            JSONObject object = new JSONObject();
            if(obj.get("theme").equals("DarkTheme")) {
                setTheme(R.style.DarkTheme);
                object.put("theme", "DarkTheme");
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
//                bottomNav.setBackgroundResource(R.color.colorPrimaryDark);
//                bottomNav.setItemTextColor(ColorStateList.valueOf(R.color.colorText));
//                bottomNav.setItemBackgroundResource(R.color.colorAccent);
            } else {
                setTheme(R.style.LightTheme);
                object.put("theme", "LightTheme");
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.setStatusBarColor(getResources().getColor(R.color.lightColorBackground));
//                bottomNav.setBackgroundResource(R.color.common_google_signin_btn_text_dark_default);
//                bottomNav.setItemTextColor(ColorStateList.valueOf(R.color.colorBackground));
//                bottomNav.setItemBackgroundResource(R.color.colorBackground);
            }
            OutputStreamWriter writer = new OutputStreamWriter(openFileOutput("info.json", MODE_PRIVATE));
            writer.write(object.toString());
            writer.close();

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

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

        final CustomViewPager viewPager = findViewById(R.id.fragment_container);
        final ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new FeedFragment(), "Feed");
        adapter.addFragment(new NewtrapFragment(), "Newtrap");
        adapter.addFragment(new ProfileFragment(), "Profile");

        final SupportMapFragment mapFragment = new SupportMapFragment();
        mapFragment.getMapAsync(MainActivity.this);
        adapter.addFragment(mapFragment, "Map");

        viewPager.setPagingEnabled(false);
        viewPager.setAdapter(adapter);

        buttonMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(map) {
                    viewPager.setCurrentItem(0, false);
                    buttonMap.setImageResource(R.drawable.ic_format_list_bulleted_black_24dp);
                    map = false;
                }
                else {
                    viewPager.setCurrentItem(3, false);
                    buttonMap.setImageResource(R.drawable.ic_map_black_24dp);
                    map = true;
                }
            }
        });

        bottomNav.setSelectedItemId(R.id.nav_feed);
        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                switch (menuItem.getItemId()) {
                    case R.id.nav_log:
                        viewPager.setCurrentItem(1, false);
                        buttonFilterFeed.setVisibility(View.GONE);
                        buttonMap.setVisibility(View.GONE);
                        break;
                    case R.id.nav_feed:
                        viewPager.setCurrentItem(0, false);
                        buttonMap.setImageResource(R.drawable.ic_format_list_bulleted_black_24dp);
                        map = false;
                        buttonFilterFeed.setVisibility(View.VISIBLE);
                        buttonMap.setVisibility(View.VISIBLE);
                        break;
                    case R.id.nav_profile:
                        viewPager.setCurrentItem(2, false);
                        buttonFilterFeed.setVisibility(View.GONE);
                        buttonMap.setVisibility(View.GONE);
                        break;
                }
                return true;
            }
        });

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
        try {
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude)));
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "LAT/LNG is NULL");
        }
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(MainActivity.this, R.raw.map_style));

//        googleMap.getUiSettings().setMapToolbarEnabled(true);
//        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
//        googleMap.getUiSettings().setZoomGesturesEnabled(true);
//        googleMap.getUiSettings().setTiltGesturesEnabled(true);

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

    private class ViewPagerAdapter extends FragmentPagerAdapter {
        private final SparseArray<WeakReference<Fragment>> instantiatedFragments = new SparseArray<>();
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            final Fragment fragment = (Fragment) super.instantiateItem(container, position);
            instantiatedFragments.put(position, new WeakReference<>(fragment));
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            instantiatedFragments.remove(position);
            super.destroyItem(container, position, object);
        }

        @Nullable
        Fragment getFragment(final int position) {
            final WeakReference<Fragment> wr = instantiatedFragments.get(position);
            if (wr != null) {
                return wr.get();
            } else {
                return null;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

}
