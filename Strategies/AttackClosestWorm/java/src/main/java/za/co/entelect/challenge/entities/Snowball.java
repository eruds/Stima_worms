package za.co.entelect.challenge.entities;

import com.google.gson.annotations.SerializedName;

public class Snowball extends Weapon{
    @SerializedName("count")
    public int count;

    @SerializedName("freezeRadius")
    public int freezeRadius;
}
