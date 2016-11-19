package com.zxiu.lillyscard.entities;

import android.databinding.BaseObservable;
import android.os.Parcel;
import android.os.Parcelable;

import com.android.databinding.library.baseAdapters.BR;
import com.google.firebase.database.Exclude;

/**
 * Created by Xiu on 10/12/2016.
 */

public class Player extends BaseObservable implements Parcelable {


    public String name;
    public String imageUrl;
    public boolean selected;

    @Exclude
    public int point;


    @Override
    public String toString() {
        return "Player{" +
                ", point=" + point +
                ", selected=" + selected +
                ", name='" + name + '\'' +
                '}';
    }

    @Exclude
    public String getPoint() {
        return Integer.toString(point);
    }

    @Exclude
    public void setPoint(int point) {
        this.point = point;
        notifyPropertyChanged(BR._all);
    }

    @Exclude
    public void addPoint(int p) {
        setPoint(point + p);
    }


    @Exclude
    public void toggleSelected() {
        selected = !selected;
    }

    @Exclude
    @Override
    public int describeContents() {
        return 0;
    }

    @Exclude
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(imageUrl);
    }

    @Exclude
    public static final Parcelable.Creator<Player> CREATOR
            = new Parcelable.Creator<Player>() {
        public Player createFromParcel(Parcel in) {
            return new Player(in);
        }

        public Player[] newArray(int size) {
            return new Player[size];
        }
    };

    public Player(){

    }

    private Player(Parcel in) {
        name = in.readString();
        imageUrl=in.readString();
    }

}
