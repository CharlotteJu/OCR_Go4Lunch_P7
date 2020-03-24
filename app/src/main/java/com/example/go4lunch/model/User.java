package com.example.go4lunch.model;

import java.util.ArrayList;
import java.util.List;

public class User
{
    private String name;
    private String email;
    private String illustration;
    private boolean chooseRestaurant = false;
    private Restaurant restaurantChoose;
    private List<Restaurant> restaurantListFavorites;

    //////// CONSTRUCTOR ////////


    public User(String email, String name,  String illustration) {
        this.name = name;
        this.email = email;
        this.illustration = illustration;

        this.chooseRestaurant = false;
        this.restaurantListFavorites = new ArrayList<>();
    }

    public User()
    {

    }

    //////// GETTERS ////////

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getIllustration() {
        return illustration;
    }

    public boolean isChooseRestaurant() {
        return chooseRestaurant;
    }

    public List<Restaurant> getRestaurantListFavorites() {
        return restaurantListFavorites;
    }

    public Restaurant getRestaurantChoose() {
        return restaurantChoose;
    }

    //////// SETTERS ////////


    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setIllustration(String illustration) {
        this.illustration = illustration;
    }

    public void setRestaurantListFavorites(List<Restaurant> restaurantListFavorites) {
        this.restaurantListFavorites = restaurantListFavorites;
    }

    public void setRestaurantChoose(Restaurant restaurantChoose) {
        this.restaurantChoose = restaurantChoose;
        this.chooseRestaurant = true;
    }

    public void unSetRestaurantChoose()
    {
        this.restaurantChoose = null;
        this.chooseRestaurant = false;
    }
}
