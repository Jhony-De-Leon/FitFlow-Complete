package com.example.fitflow;

import android.os.Parcel;
import android.os.Parcelable;

public class Exercise implements Parcelable {
    private String name;
    private boolean isCompleted;

    public Exercise(String name) {
        this.name = name;
        this.isCompleted = false; // Por defecto, no está completado
    }

    public Exercise(String name, boolean isCompleted) {
        this.name = name;
        this.isCompleted = isCompleted;
    }

    protected Exercise(Parcel in) {
        name = in.readString();
        isCompleted = in.readByte() != 0;
    }

    public static final Creator<Exercise> CREATOR = new Creator<Exercise>() {
        @Override
        public Exercise createFromParcel(Parcel in) {
            return new Exercise(in);
        }

        @Override
        public Exercise[] newArray(int size) {
            return new Exercise[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeByte((byte) (isCompleted ? 1 : 0));
    }
}
