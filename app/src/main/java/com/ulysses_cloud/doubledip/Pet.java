package com.ulysses_cloud.doubledip;

/**
 * Created by Tristan on 5/02/2015.
 */
public class Pet {
    public String petID= null;
    public String petName= null;
    public long lastFed= 0;

    public Pet(){

    }

    public Pet(String newPetID, String newPetName, long newLastFed){
        petID= newPetID;
        petName= newPetName;
        lastFed= newLastFed;
    }

}
