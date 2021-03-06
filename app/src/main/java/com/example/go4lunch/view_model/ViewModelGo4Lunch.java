package com.example.go4lunch.view_model;

import android.location.Location;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.go4lunch.model.Restaurant;
import com.example.go4lunch.model.RestaurantPOJO;
import com.example.go4lunch.model.User;
import com.example.go4lunch.view_model.repositories.RestaurantFirebaseRepository;
import com.example.go4lunch.view_model.repositories.RestaurantPlacesRepository;
import com.example.go4lunch.view_model.repositories.UserFirebaseRepository;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import retrofit2.Call;

public class ViewModelGo4Lunch extends ViewModel
{
    private RestaurantFirebaseRepository restaurantFirebaseRepository;
    private UserFirebaseRepository userFirebaseRepository;
    private RestaurantPlacesRepository restaurantPlacesRepository;

    public ViewModelGo4Lunch(RestaurantFirebaseRepository restaurantFirebaseRepository,
                             UserFirebaseRepository userFirebaseRepository,
                             RestaurantPlacesRepository restaurantPlacesRepository) {
        this.restaurantFirebaseRepository = restaurantFirebaseRepository;
        this.userFirebaseRepository = userFirebaseRepository;
        this.restaurantPlacesRepository = restaurantPlacesRepository;
    }

    private MutableLiveData<User> userCurrentMutableLiveData = new MutableLiveData<>();
    private MutableLiveData<List<User>> usersListMutableLiveData = new MutableLiveData<>();

    private MutableLiveData<Restaurant> restaurantFirebaseMutableLiveData = new MutableLiveData<>();
    private MutableLiveData<List<Restaurant>> restaurantsListFirebaseMutableLiveData = new MutableLiveData<>();

    private MutableLiveData<Observable<List<Restaurant>>> restaurantsListPlacesMutableLiveData = new MutableLiveData<>();
    private MutableLiveData<Observable<Restaurant>> restaurantDetailPlacesMutableLiveData = new MutableLiveData<>();
    private MutableLiveData<Observable<List<Restaurant>>> restaurantsListPlacesAutocompleteMutableLiveData = new MutableLiveData<>();

    /////////////////////// USER FIREBASE ///////////////////////

    public MutableLiveData<User> getUserCurrentMutableLiveData(String uid)
    {
        if (this.userCurrentMutableLiveData != null)
        {
            this.setUserCurrentMutableLiveData(uid);
        }

        return this.userCurrentMutableLiveData;
    }

    private void setUserCurrentMutableLiveData(String uid)
    {
        this.userFirebaseRepository.getUser(uid).addOnSuccessListener(documentSnapshot ->
        {
            if(documentSnapshot.exists())
            {
                User user = documentSnapshot.toObject(User.class);
                userCurrentMutableLiveData.setValue(user);
            }
        });
    }

    public MutableLiveData<List<User>> getUsersListMutableLiveData()
    {
        if (this.usersListMutableLiveData != null)
        {
            this.setUsersListMutableLiveData();
        }

        return this.usersListMutableLiveData;
    }

    private void setUsersListMutableLiveData()
    {
        this.userFirebaseRepository.getListUsers().addSnapshotListener((queryDocumentSnapshots, e) ->
        {
            if (queryDocumentSnapshots != null)
            {
                List<DocumentSnapshot> userList = queryDocumentSnapshots.getDocuments();
                List<User> users = new ArrayList<>();
                int size = userList.size();
                for (int i = 0; i < size; i ++)
                {
                    User user = userList.get(i).toObject(User.class);
                    users.add(user);
                }

                usersListMutableLiveData.setValue(users);
            }

        });
    }

    public void createUser (String uid, String email, String username, String urlPicture)
    {
        this.userFirebaseRepository.createUser(uid, email, username, urlPicture);
    }

    public void updateUserIsChooseRestaurant (String uid, Boolean isChooseRestaurant)
    {
        this.userFirebaseRepository.updateUserIsChooseRestaurant(uid, isChooseRestaurant);
    }

    public void updateUserRestaurant(String uid, Restaurant restaurantChoose)
    {
        this.userFirebaseRepository.updateUserRestaurant(uid, restaurantChoose);
    }

    public void updateUserRestaurantListFavorites(String uid, List<Restaurant> restaurantList)
    {
        this.userFirebaseRepository.updateUserRestaurantListFavorites(uid, restaurantList);
    }

    /////////////////////// RESTAURANT FIREBASE ///////////////////////

    public MutableLiveData<Restaurant> getRestaurantFirebaseMutableLiveData(Restaurant restaurant)
    {
        if (this.restaurantFirebaseMutableLiveData != null)
        {
            this.setRestaurantFirebaseMutableLiveData(restaurant);
        }

        return this.restaurantFirebaseMutableLiveData;
    }

    private void setRestaurantFirebaseMutableLiveData(Restaurant restaurant)
    {
        this.restaurantFirebaseRepository.getRestaurant(restaurant.getPlaceId()).addOnSuccessListener(documentSnapshot ->
        {
            if(documentSnapshot.exists())
            {
                Restaurant restaurantToLiveData = documentSnapshot.toObject(Restaurant.class);
                restaurantFirebaseMutableLiveData.setValue(restaurantToLiveData);
            }
        });
    }

    public MutableLiveData<List<Restaurant>> getRestaurantsListFirebaseMutableLiveData()
    {
        if (this.restaurantsListFirebaseMutableLiveData != null)
        {
            this.setRestaurantsListFirebaseMutableLiveData();
        }

        return this.restaurantsListFirebaseMutableLiveData;
    }

    private void setRestaurantsListFirebaseMutableLiveData()
    {
        this.restaurantFirebaseRepository.getListRestaurants().addSnapshotListener((queryDocumentSnapshots, e) ->
        {
            if (queryDocumentSnapshots != null)
            {
                List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();

                List<Restaurant> restaurantList = new ArrayList<>();
                int size = documents.size();
                for (int i = 0; i < size; i ++)
                {
                    Restaurant restaurant = documents.get(i).toObject(Restaurant.class);
                    restaurantList.add(restaurant);
                }

                restaurantsListFirebaseMutableLiveData.setValue(restaurantList);
            }

        });
    }

    public void createRestaurant(String placeId, List<User> userList, String name, String address)
    {
        this.restaurantFirebaseRepository.createRestaurant(placeId, userList, name, address);
    }

    public void updateRestaurantUserList(String placeId, List<User> userList)
    {
        this.restaurantFirebaseRepository.updateRestaurantUserList(placeId, userList);
    }

    /////////////////////// RESTAURANT PLACES ///////////////////////

    public MutableLiveData<Observable<List<Restaurant>>> getRestaurantsListPlacesMutableLiveData(double lat, double lng, int radius, String key)
    {
        if (this.restaurantsListPlacesMutableLiveData != null)
        {
            this.setRestaurantsListPlacesMutableLiveData(lat, lng, radius, key);
        }

        return this.restaurantsListPlacesMutableLiveData;
    }

    private void setRestaurantsListPlacesMutableLiveData(double lat, double lng, int radius, String key)
    {
        this.restaurantsListPlacesMutableLiveData.setValue(this.restaurantPlacesRepository.streamFetchRestaurantInList(lat, lng, radius, key));
    }

    public MutableLiveData<Observable<Restaurant>> getRestaurantDetailPlacesMutableLiveData(String placeId, String key)
    {
        if (this.restaurantDetailPlacesMutableLiveData != null)
        {
            this.setRestaurantDetailPlacesMutableLiveData(placeId, key);
        }

        return this.restaurantDetailPlacesMutableLiveData;
    }

    private void setRestaurantDetailPlacesMutableLiveData(String placeId, String key)
    {
        this.restaurantDetailPlacesMutableLiveData.setValue(this.restaurantPlacesRepository.streamDetailRestaurantToRestaurant(placeId, key));
    }

    public MutableLiveData<Observable<List<Restaurant>>> getRestaurantsListPlacesAutocompleteMutableLiveData(String key, String input, Location location, int radius)
    {
        String locationToString = location.getLatitude() + "," + location.getLongitude();
        this.restaurantsListPlacesAutocompleteMutableLiveData.setValue(this.restaurantPlacesRepository.streamAutocompleteRestaurantsToRestaurantList(key, input, locationToString, radius));
        return this.restaurantsListPlacesAutocompleteMutableLiveData;
    }



}
