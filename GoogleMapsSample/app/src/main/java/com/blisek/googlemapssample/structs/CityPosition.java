package com.blisek.googlemapssample.structs;

public class CityPosition {
    public String city;
    public float latitude;
    public float longtitude;

    public CityPosition() {}

    public CityPosition(String city, float latitude, float longtitude) {
        this.city = city;
        this.latitude = latitude;
        this.longtitude = longtitude;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CityPosition that = (CityPosition) o;

        if (Float.compare(that.latitude, latitude) != 0) return false;
        if (Float.compare(that.longtitude, longtitude) != 0) return false;
        return city != null ? city.equals(that.city) : that.city == null;

    }

    @Override
    public int hashCode() {
        int result = city != null ? city.hashCode() : 0;
        result = 31 * result + (latitude != +0.0f ? Float.floatToIntBits(latitude) : 0);
        result = 31 * result + (longtitude != +0.0f ? Float.floatToIntBits(longtitude) : 0);
        return result;
    }

    @Override
    public String toString() {
        return city;
    }
}
