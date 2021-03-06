package com.example.go4lunch.view.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.example.go4lunch.R;
import com.example.go4lunch.model.User;
import com.example.go4lunch.notifications.WorkerNotificationController;
import com.example.go4lunch.utils.UtilsCalcul;
import com.example.go4lunch.view.fragments.ListRestaurantsFragment;
import com.example.go4lunch.view.fragments.ListWorkmatesFragment;
import com.example.go4lunch.view.fragments.MapViewFragment;
import com.example.go4lunch.view_model.ViewModelGo4Lunch;
import com.example.go4lunch.view_model.factory.ViewModelFactoryGo4Lunch;
import com.example.go4lunch.view_model.injection.Injection;
import com.example.go4lunch.view_model.repositories.UserFirebaseRepository;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    //FOR DESIGN
    @BindView(R.id.bottom_navigation)
    BottomNavigationView bottomNavigationView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.navigation_drawer)
    DrawerLayout drawerLayout;
    @BindView(R.id.navigation_drawer_nav_view)
    NavigationView navigationView;
    @BindView(R.id.toolbar_edit_txt)
    EditText editTextToolbar;

    //FOR DATA
    private MapViewFragment mapViewFragment;
    private ListRestaurantsFragment listRestaurantsFragment;
    private ListWorkmatesFragment listWorkmatesFragment;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location currentLocation;
    private ViewModelGo4Lunch viewModelGo4Lunch;
    private User currentUser;

    private static final int LOCATION_REQUEST_CODE = 101;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 15;
    private static final String NOTIFICATIONS_SHARED_PREFERENCES = "PREF_NOTIF";
    private static final String NOTIFICATIONS_BOOLEAN = "NOTIFICATIONS_BOOLEAN";

    Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        this.fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        this.fetchLocation();
        this.configureBottomView();
        this.configureToolbar();
        this.configureDrawerLayout();
        this.configureNavigationView();
        this.getSharedPreferences();

        this.editTextToolbar.setVisibility(View.VISIBLE);

        //----------------- V2 WITHOUT WIDGET NEW REQUEST TODO
        this.editTextToolbar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE)
            {
                if (currentFragment == listRestaurantsFragment)
                {
                    String input = editTextToolbar.getText().toString();
                    listRestaurantsFragment.autocompleteSearch(input);
                }
                else if (currentFragment == mapViewFragment)
                {
                    mapViewFragment.displayToast();
                }
                return true;
            }
            else
            {
                return false;
            }
        });

        //----------------- V1 WITH WIDGET IN LIST TODO
        this.editTextToolbar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s)
            {
                if (currentFragment == mapViewFragment)
                {
                    String input = s.toString();
                    mapViewFragment.autocompleteSearch(input);
                }
            }
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        this.configViewModel();
    }

    ///////////////////////////////////VIEW MODEL///////////////////////////////////

    private void configViewModel()
    {
        ViewModelFactoryGo4Lunch viewModelFactoryGo4Lunch = Injection.viewModelFactoryGo4Lunch();
        viewModelGo4Lunch= ViewModelProviders.of(this, viewModelFactoryGo4Lunch).get(ViewModelGo4Lunch.class);
        this.getCurrentUser();
    }

    private void getCurrentUser()
    {
        String uidUser = FirebaseAuth.getInstance().getUid();
        this.viewModelGo4Lunch.getUserCurrentMutableLiveData(uidUser).observe(this, user -> {
            updateNavigationHeader(user);
            currentUser = user;
        });
    }

    ///////////////////////////////////CONFIGURE METHODS///////////////////////////////////

    /**
     * Configure the Toolbar {@link Toolbar}
     */
    private void configureToolbar()
    {
        setSupportActionBar(toolbar);
    }

    /**
     * Configure the DrawerLayout {@link DrawerLayout}
     */
    private void configureDrawerLayout()
    {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                                        R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    /**
     * Configure the NavigationView {@link NavigationView}
     */
    private void configureNavigationView()
    {
        navigationView.setNavigationItemSelectedListener(this);
    }

    /**
     * Configure the BottomView {@link BottomNavigationView}
     */
    private void configureBottomView()
    {
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId())
            {
                case R.id.action_mapview :
                    displayFragment(displayMapViewFragment());
                    this.toolbar.getMenu().findItem(R.id.toolbar_menu_search).setVisible(true);
                    this.editTextToolbar.setVisibility(View.VISIBLE);
                    this.editTextToolbar.setText("");
                    return true;
                case R.id.action_listview :
                    displayFragment(displayListRestaurantsFragment());
                    this.toolbar.getMenu().findItem(R.id.toolbar_menu_search).setVisible(true);
                    this.editTextToolbar.setVisibility(View.VISIBLE);
                    this.editTextToolbar.setText("");
                    return true;
                case R.id.action_workmates :
                    displayFragment(displayListWorkmatesFragment());
                    this.toolbar.getMenu().findItem(R.id.toolbar_menu_search).setVisible(false);
                    this.editTextToolbar.setVisibility(View.INVISIBLE);
                    return true;
                default:
                    return false;
            }
        });
    }

    /**
     * Update the NavigationView's info {@link NavigationView}
     */
    private void updateNavigationHeader(User currentUser)
    {
        final View headerView = navigationView.getHeaderView(0);
        TextView nameUser = headerView.findViewById(R.id.nav_header_name_txt);
        TextView emailUser = headerView.findViewById(R.id.nav_header_email_txt);
        ImageView illustrationUser = headerView.findViewById(R.id.nav_header_image_view);
        nameUser.setText(currentUser.getName());
        emailUser.setText(currentUser.getEmail());
        if (currentUser.getIllustration() != null)
        {
            Glide.with(this).load(currentUser.getIllustration()).circleCrop().into(illustrationUser);
        }
    }

    /**
     * Display a fragment
     */
    private void displayFragment(Fragment fragment)
    {
        getSupportFragmentManager().beginTransaction().replace(R.id.navigation_drawer_frame_layout, fragment).addToBackStack("backstack").commit();
        currentFragment = fragment;
    }

    /**
     * Display the MapViewFragment {@link MapViewFragment}
     */
    private MapViewFragment displayMapViewFragment()
    {
        if (this.mapViewFragment == null)
        {
            this.mapViewFragment = MapViewFragment.newInstance(currentLocation);
        }
        return this.mapViewFragment;
    }

    /**
     * Display the ListRestaurantsFragment {@link ListRestaurantsFragment}
     */
    private ListRestaurantsFragment displayListRestaurantsFragment()
    {
        if (this.listRestaurantsFragment == null)
        {
            this.listRestaurantsFragment = ListRestaurantsFragment.newInstance(currentLocation);
        }
        return this.listRestaurantsFragment;
    }

    /**
     * Display the ListWorkmatesFragment {@link ListWorkmatesFragment}
     */
    private ListWorkmatesFragment displayListWorkmatesFragment()
    {
        if (this.listWorkmatesFragment == null)
        {
            this.listWorkmatesFragment = ListWorkmatesFragment.newInstance();
        }
        return listWorkmatesFragment;
    }


    /**
     * Configure the toolbar search with {@link Autocomplete}
     */
    private void configureAutocompleteSearchToolbar(double radius)
    {
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME,
                Place.Field.LAT_LNG, Place.Field.RATING, Place.Field.ADDRESS,
                Place.Field.OPENING_HOURS, Place.Field.PHOTO_METADATAS);
        LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        List<LatLng> latLngForRectangularBounds = UtilsCalcul.
                calculateRectangularBoundsAccordingToCurrentLocation(radius, currentLatLng);
        RectangularBounds rectangularBounds = RectangularBounds.newInstance
                (latLngForRectangularBounds.get(0), latLngForRectangularBounds.get(1));
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .setLocationRestriction(rectangularBounds)
                .setTypeFilter(TypeFilter.ESTABLISHMENT)
                .build(getApplicationContext());
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    /**
     * Fetch the current location {@link ActivityCompat} {@link Location}
     * If getLastLocation is available, use it
     * Else use GPS
     * We can display 1st fragment when we have the location
     */
    private void fetchLocation() {

        if (ActivityCompat.checkSelfPermission(
                getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        else
        {
            Task<Location> task = fusedLocationProviderClient.getLastLocation();
            task.addOnSuccessListener(location -> {
                if (location != null)
                {
                    currentLocation = location;
                    displayFragment(displayMapViewFragment());
                }
                else
                {
                    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    if (!Objects.requireNonNull(locationManager).isProviderEnabled(LocationManager.GPS_PROVIDER))
                    {
                        //If location is not activate in Settings, open Settings
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        this.fetchLocation();
                    }
                    else
                    {
                        Objects.requireNonNull(locationManager).requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                            @Override
                            public void onLocationChanged(Location location)
                            {
                                currentLocation = location;
                                if (mapViewFragment == null)
                                {
                                    displayFragment(displayMapViewFragment());
                                }
                            }
                            @Override
                            public void onStatusChanged(String provider, int status, Bundle extras) {}
                            @Override
                            public void onProviderEnabled(String provider) {}
                            @Override
                            public void onProviderDisabled(String provider) {}
                        });
                    }
                }
            });
        }

    }


    /////////////////////////////////// METHODS FOR MENU'S NAVIGATION VIEW ONCLICK ///////////////////////////////////

    /**
     * Find current User {@link UserFirebaseRepository}
     * Check Boolean isChooseRestaurant {@link User}
     * Display a Toast or launch Details Activity
     */
    private void showLunch()
    {
        if (currentUser.isChooseRestaurant())
        {
            Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
            intent.putExtra("placeId", currentUser.getRestaurantChoose().getPlaceId());
            startActivity(intent);
        }
        else
        {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.main_activity_no_choose_restaurant), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Create and show an AlertDialog to logOut() {@link AlertDialog}
     */
    private void createAndShowPopUpLogOut()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.main_activity_pop_up_log_out_title));
        builder.setMessage(getResources().getString(R.string.main_activity_pop_up_log_out_message));
        builder.setPositiveButton(getResources().getString(R.string.main_activity_pop_up_yes), (dialogInterface, i) -> logOut());
        builder.setNegativeButton(getResources().getString(R.string.main_activity_pop_up_no), null);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Log Out from Firebase {@link AuthUI}
     */
    private void logOut()
    {
        AuthUI.getInstance().signOut(this).addOnSuccessListener(this, aVoid ->
        {
            if (FirebaseAuth.getInstance().getCurrentUser() == null)
            {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.main_activity_success_log_out), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, AuthActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Create and show an AlertDialog to updateSharedPreferences() {@link AlertDialog}
     */
    private void createAndShowPopUpSettings()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.main_activity_pop_up_notifications_title));
        builder.setMessage(getResources().getString(R.string.main_activity_pop_up_notifications_message));
        builder.setPositiveButton(getResources().getString(R.string.main_activity_pop_up_yes),
                (dialogInterface, i) -> updateSharedPreferences(true));
        builder.setNegativeButton(getResources().getString(R.string.main_activity_pop_up_no),
                (dialog, which) -> updateSharedPreferences(false));
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Update SharedPreferences for notifications {@link SharedPreferences}
     */
    private void updateSharedPreferences(boolean notificationsAuthorized)
    {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(NOTIFICATIONS_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(NOTIFICATIONS_BOOLEAN, notificationsAuthorized);
        editor.apply();
        this.getSharedPreferences();
    }

    /**
     * Get SharedPreferences for notifications {@link SharedPreferences} {@link WorkerNotificationController}
     */
    private void getSharedPreferences()
    {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(NOTIFICATIONS_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        boolean isAuthorized = sharedPreferences.getBoolean(NOTIFICATIONS_BOOLEAN, true);
        if (isAuthorized)
        {
            WorkerNotificationController.startWorkRequest(getApplicationContext());
        }
        else
        {
            WorkerNotificationController.stopWorkRequest(getApplicationContext());
        }
    }

    ///////////////////////////////////OVERRIDE METHODS///////////////////////////////////

    @Override
    public void onBackPressed()
    {
        if(this.drawerLayout.isDrawerOpen(GravityCompat.START))
        {
            this.drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        int id = item.getItemId();
        switch (id)
        {
            case R.id.menu_drawer_lunch :
                this.showLunch();
                break;
            case R.id.menu_drawer_settings :
                this.createAndShowPopUpSettings();
                break;
            case R.id.menu_drawer_logout :
                this.createAndShowPopUpLogOut();
                break;
        }
        this.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if (item.getItemId() == R.id.toolbar_menu_search)
        {
            //----------------- V3 WITH WIDGET TODO
            int radius = mapViewFragment.getRadius()/100;
            configureAutocompleteSearchToolbar(radius);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE)
        {
            if (resultCode == RESULT_OK)
            {
                if (data != null)
                {
                    currentFragment.onActivityResult(requestCode, resultCode, data);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        this.fetchLocation();
    }
}