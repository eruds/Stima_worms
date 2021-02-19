package za.co.entelect.challenge.command;

import za.co.entelect.challenge.enums.Direction;

public class ThrowSnowball implements Command {

    private int x;
    private int y;

    public ThrowSnowball (int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String render() {
        return "snowball " + x  + " " + y;
    }
}
