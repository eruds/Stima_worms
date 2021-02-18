package za.co.entelect.challenge.enums;

public enum Direction {

    N(0, -1),
    NE(1, -1),
    E(1, 0),
    SE(1, 1),
    S(0, 1),
    SW(-1, 1),
    W(-1, 0),
    NW(-1, -1);

    public final int x;
    public final int y;

    Direction(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public static String getDirection(int x, int y) {
        if(x > 1 || x < -1 || y > 1 || y < -1){
            throw new IllegalArgumentException("getDirection argument must be in range of -1 to 1");
        }

        if(x == 0 && y == -1){
            return "N";
        } else if ( x == 1 && y == 0){
            return "E";
        } else if ( x == 0 && y == 1){
            return "S";
        } else if ( x == -1 && y == 0){
            return "W";
        } else {
            return getDirection(0,y) + getDirection(x,0);
        }

    }

    public static String[] getNeighbors(String direction){
        switch (direction) {
            case "N":
            case "S":
                return new String[]{"W", "E"};
            case "E":
            case "W":
                return new String[]{"N", "S"};
        }
        return direction.split("");
    }
}
