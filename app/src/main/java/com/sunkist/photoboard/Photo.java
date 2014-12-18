package com.sunkist.photoboard;


public class Photo {
    String image_url;
    String created_at;

    public Photo(String image_url, String created_at) {
        this.image_url = image_url;
        this.created_at = created_at;
    }
}
