package za.co.entelect.challenge.entities;

import com.google.gson.annotations.SerializedName;

public class BananaBomb extends Weapon{
    @SerializedName("count")
    public int count;

    @SerializedName("damageRadius")
    public int damageRadius;
}
