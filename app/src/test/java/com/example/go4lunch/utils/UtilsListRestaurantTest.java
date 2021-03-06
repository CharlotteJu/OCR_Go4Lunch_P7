package com.example.go4lunch.utils;

import com.example.go4lunch.model.Restaurant;
import com.example.go4lunch.model.User;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class UtilsListRestaurantTest {
    private static List<Restaurant> restaurantList = new ArrayList<>();
    private static final String placeIdTest = "placeId";
    private static final String addressTest = "Address";
    private static final List<User> userList = new ArrayList<>();

    private static final String name1Test = "Name 1";
    private static final String name2Test = "Name 2";
    private static final String name3Test = "Name 3";
    private static final String name4Test = "Name 4";

    private static final int distance1Test = 123;
    private static final int distance2Test = 234;
    private static final int distance3Test = 345;
    private static final int distance4Test = 456;

    private static final double rating1Test = 1.23;
    private static final double rating2Test = 2.34;
    private static final double rating3Test = 3.45;
    private static final double rating4Test = 4.56;

    private static Restaurant restaurant1;
    private static Restaurant restaurant2;
    private static Restaurant restaurant3;
    private static Restaurant restaurant4;


    @BeforeClass
    public static void configTest() {
        restaurant1 = new Restaurant(placeIdTest, userList, name1Test, addressTest);
        restaurant1.setDistanceCurrentUser(distance3Test);
        restaurant1.setRating(rating4Test);
        restaurantList.add(restaurant1);

        restaurant2 = new Restaurant(placeIdTest, userList, name3Test, addressTest);
        restaurant2.setDistanceCurrentUser(distance1Test);
        restaurant2.setRating(rating2Test);
        restaurantList.add(restaurant2);

        restaurant3 = new Restaurant(placeIdTest, userList, name4Test, addressTest);
        restaurant3.setDistanceCurrentUser(distance2Test);
        restaurant3.setRating(rating3Test);
        restaurantList.add(restaurant3);

        restaurant4 = new Restaurant(placeIdTest, userList, name2Test, addressTest);
        restaurant4.setDistanceCurrentUser(distance4Test);
        restaurant4.setRating(rating1Test);
        restaurantList.add(restaurant4);
    }

    @Test
    public void triProximity_Success() {
        UtilsListRestaurant.sortProximity(restaurantList);
        assertEquals(restaurantList.get(0).getName(), restaurant2.getName());
        assertEquals(restaurantList.get(1).getName(), restaurant3.getName());
        assertEquals(restaurantList.get(2).getName(), restaurant1.getName());
        assertEquals(restaurantList.get(3).getName(), restaurant4.getName());
    }

    @Test
    public void triName_Success() {
        UtilsListRestaurant.sortName(restaurantList);
        assertEquals(restaurantList.get(0).getName(), restaurant1.getName());
        assertEquals(restaurantList.get(1).getName(), restaurant4.getName());
        assertEquals(restaurantList.get(2).getName(), restaurant2.getName());
        assertEquals(restaurantList.get(3).getName(), restaurant3.getName());
    }

    @Test
    public void triRatingReverse_Success() {
        UtilsListRestaurant.sortRatingReverse(restaurantList);
        assertEquals(restaurantList.get(0).getName(), restaurant1.getName());
        assertEquals(restaurantList.get(1).getName(), restaurant3.getName());
        assertEquals(restaurantList.get(2).getName(), restaurant2.getName());
        assertEquals(restaurantList.get(3).getName(), restaurant4.getName());
    }

}