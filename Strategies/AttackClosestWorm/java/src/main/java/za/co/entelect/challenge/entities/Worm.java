package za.co.entelect.challenge.entities;

import com.google.gson.annotations.SerializedName;

public class Worm {
    @SerializedName("id")
    public int id;

    @SerializedName("health")
    public int health;

    @SerializedName("position")
    public Position position;

//    @SerializedName("profession")
//    public String profession;

    @SerializedName("diggingRange")
    public int diggingRange;

    @SerializedName("movementRange")
    public int movementRange;

    @SerializedName("roundsUntilUnfrozen")
    public int roundsUntilUnfrozen;
}
