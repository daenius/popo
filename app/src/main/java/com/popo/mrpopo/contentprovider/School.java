package com.popo.mrpopo.contentprovider;

/**
 * Created by jpowang on 6/25/14.
 */
public class School {
    private int id;
    private String name;
    private double centerLatitude;
    private double centerLongitude;



    public School(int id, String name, double centerLatitude, double centerLongitude){
        this.id = id;
        this.name = name;
        this.centerLatitude = centerLatitude;
        this.centerLongitude = centerLongitude;
    }

    public int getId(){
        return this.id;
    }

    public String getName(){
        return this.name;
    }

    public double getCenterLatitude(){
        return this.centerLatitude;
    }

    public double getCenterLongitude(){
        return this.centerLongitude;
    }

    @Override
    public String toString(){
        return this.getName();
    }
}
