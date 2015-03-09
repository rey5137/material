package com.rey.material.app;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Rey on 3/2/2015.
 */
public class Recipient implements Parcelable{
    String name;
    String number;
    String lookupKey;

    public Recipient() {
    }

    private Recipient(Parcel in) {
        this.name = in.readString();
        this.number = in.readString();
        this.lookupKey = in.readString();
    }

    @Override
    public String toString(){
        return Recipient.class.getSimpleName() + "[name = " + name + ", number = " + number + ", key = " + lookupKey + "]";
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.number);
        dest.writeString(this.lookupKey);
    }

    public static final Creator<Recipient> CREATOR = new Creator<Recipient>() {
        public Recipient createFromParcel(Parcel source) {
            return new Recipient(source);
        }

        public Recipient[] newArray(int size) {
            return new Recipient[size];
        }
    };
}
