package com.sunkist.photoboard;


import android.os.Parcel;
import android.os.Parcelable;

public class Photo {
    String image_url;
    String created_at;

    public Photo(String image_url, String created_at) {
        this.image_url = image_url;
        this.created_at = created_at;
    }
}
