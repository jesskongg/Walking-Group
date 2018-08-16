package ca.cmpt276.walkinggroup.app.model;

import android.net.Uri;

import com.google.android.gms.maps.model.LatLng;

/*
 * PlaceInfo class hold data returned by google place api
 */

public class PlaceInfo {

    private String name;
    private String address;
    private String phoneNumber;
    private LatLng latLng;

    public PlaceInfo(){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

}
