package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.CellType;
import za.co.entelect.challenge.enums.Direction;

import java.util.*;
import java.util.stream.Collectors;

public class Bot {

//    private Random random;
    private GameState gameState;
    private Opponent opponent;
    private MyWorm currentWorm;
    private MyWorm[] myWorms;
//    private String previousCommand;

    public Bot(Random random, GameState gameState) {
//        this.random = random;
        this.gameState = gameState;
        this.opponent = gameState.opponents[0];
        this.currentWorm = getCurrentWorm(gameState);
        // Added
        this.myWorms = gameState.myPlayer.worms;
//        this.previousCommand = gameState.myPlayer.previousCommand.split(" ")[0];
    }

    private MyWorm getCurrentWorm(GameState gameState) {
        return Arrays.stream(gameState.myPlayer.worms)
                .filter(myWorm -> myWorm.id == gameState.currentWormId)
                .findFirst()
                .get();
    }

    public Command run() {
//        if(currentWorm.roundsUntilUnfrozen > 0){
//            int[] ids = {1,2,3};
//            ids = Arrays.stream(ids).filter(id -> id != currentWorm.id).toArray();
//            return new SelectCommand(ids[0], "Move ");
//        }

//
//        }

        // Attack the closest worm with the priority of
        // Distance > IsFrozen > LowHealth
        Worm closestWorm = getClosestEnemyWorm(currentWorm.position.x, currentWorm.position.y);

        // Always Target Only One Worm
        int targetId = 3;
        Position targetWorm = swarmAttack(targetId);

        // Target One Worm, after that attack the closest worm
        // Position targetWorm = opponent.worms[1].health < 0 ? opponent.worms[1] : closestWorm.position;

        if(currentWorm.health <= 50 &&
                euclideanDistance(currentWorm.position.x, currentWorm.position.y,
                        closestWorm.position.x, closestWorm.position.y) > 5) {
            targetWorm = lookForHealthPack() == null ? targetWorm : lookForHealthPack();
        } else {
            // Check if you can bomb the target
            if (currentWorm.id == 2 && canBananaBomb(targetWorm.x, targetWorm.y)) {
                return new ThrowBananaBomb(targetWorm.x, targetWorm.y);
            }

            // Check if you can throw snowball to the target worm
            if (currentWorm.id == 3 && canSnowball(targetWorm.x, targetWorm.y)) {
                return new ThrowSnowball(targetWorm.x, targetWorm.y);
            }
//
            // Check if you can shoot the closest enemy worm
            if(canShootEnemy(targetWorm.x, targetWorm.y)){
                Direction direction = resolveDirection(currentWorm.position, targetWorm);
                if(!checkFriendlyFire(direction)){
                    return new ShootCommand(direction);
                }
            }
        }


//        assert targetWorm != null;
        Direction moveDirection = resolveDirection(
                currentWorm.position,
                targetWorm
        );

        Cell block = getCellToMove(moveDirection);
        moveDirection = !IsCellOccupied(block) ? moveDirection : getBestMove(moveDirection);
        block = getCellToMove(moveDirection);

        if (block.type == CellType.AIR) {
            return new MoveCommand(block.x, block.y);
        } else if (block.type == CellType.DIRT) {
            return new DigCommand(block.x, block.y);
        }
        return new DoNothingCommand();

    }


    // Added
    // Movements
    private Worm getClosestEnemyWorm(int x, int y) {
        // Worms Position
        Worm[] enemyWorms = opponent.worms;
        Position currentWormPosition = currentWorm.position;

        // Approach Strategy
        int shortest = 100000000; // Maybe this need to be fixed
        int lowestHealth = -1;

        // The best worm choice
        Worm CandidateWorm = currentWorm;
        for (Worm enemyWorm : enemyWorms) {
            // Filter worms with 0 health
            if (enemyWorm.health <= 0) {
                continue;
            }
            Position enemyWormPosition = enemyWorm.position;
            int distance = euclideanDistance(
                    currentWormPosition.x, currentWormPosition.y,
                    enemyWormPosition.x, enemyWormPosition.y
            );

            if(enemyWorm.roundsUntilUnfrozen > 0){
                // Hunt the frozen worm first
                int frozenDistance = euclideanDistance(
                        currentWormPosition.x, currentWormPosition.y,
                        enemyWormPosition.x, enemyWormPosition.y);
                if(frozenDistance - distance <= 3){
                    CandidateWorm = enemyWorm;
                }
            } else {
                if(distance == shortest) {
                    // If they have the same distance attack the one with the lowest health
                    if( lowestHealth > enemyWorm.health ){
                        lowestHealth = enemyWorm.health;
                        CandidateWorm = enemyWorm;
                    }
                } else if (distance < shortest) {
                    shortest = distance;
                    CandidateWorm = enemyWorm;
                }
            }
        }
        return CandidateWorm;
    }

    private Cell getCellToMove( Direction moveDirection) {
        int x = moveDirection.x + currentWorm.position.x;
        int y = moveDirection.y + currentWorm.position.y;
        return isValidCoordinate(x,y) ?
                gameState.map[y][x] :
                gameState.map[currentWorm.position.y][currentWorm.position.x];
    }

    private Boolean IsCellOccupied(Cell cell){
        for(Worm worm : myWorms){
            if(cell.x == worm.position.x && cell.y == worm.position.y && worm.health > 0){
                return true;
            }
        }

        for(Worm worm : opponent.worms){
            if(cell.x == worm.position.x && cell.y == worm.position.y && worm.health > 0){
                return true;
            }
        }

        return false;
    }


    private Direction getBestMove(Direction Move){
        // If the current move candidate is occupied, look for other candidate
        String direction = Direction.getDirection(Move.x, Move.y);
        String[] Neighbors = Direction.getNeighbors(direction);
        for(String Neighbor : Neighbors){
            Direction NeighborDirection = Direction.valueOf(Neighbor);
            Direction CandidateDirection = Direction.valueOf(Direction.getDirection(Move.x, Move.y));
            if(Move.x == 0 || Move.y == 0){
                CandidateDirection =
                        Direction.valueOf(
                                Direction.getDirection(
                                        NeighborDirection.x+Move.x,
                                        NeighborDirection.y+Move.y));
            }
            // Candidate direction next to the current direction
            if(!IsCellOccupied(getCellToMove(CandidateDirection))){
                return CandidateDirection;
            }
            // Second Best candidate direction.
            if(!IsCellOccupied(getCellToMove(NeighborDirection))){
                return NeighborDirection;
            }
        }
        return Move;

    }

    // Passive Strategy
    private Position Follow(int leaderId) {
        // Look for the leader, default commando
        Worm leaderWorm = myWorms[leaderId-1];
        while(leaderWorm.health <= 0){
            leaderId  = leaderId < 3 ? leaderId + 1 : 1;
            leaderWorm  = myWorms[leaderId-1];
        }

        int a = currentWorm.position.x;
        int b = currentWorm.position.y;
        int c = leaderWorm.position.x;
        int d = leaderWorm.position.y;

        // Go to leader if far away
        if (euclideanDistance(a,b,c,d) > 3) {
            return new Position(c,d);
        }
        return null;
    }

    private Boolean ShouldCampMiddle() {
        int a = Math.floorDiv(gameState.mapSize, 2);
        int b = Math.floorDiv(gameState.mapSize, 2);
        int countWormInMiddle = 0;
        for(Worm worm : myWorms){
            int distance = euclideanDistance(worm.position.x, worm.position.y, a, b);
            if(distance < 5){
                countWormInMiddle += 1;
            }
        }
        return countWormInMiddle <= 3;
    }

    private Position lookForHealthPack(){
        Cell [][] Cells = gameState.map;
        Position healthPackPosition = new Position(13,13);
        boolean check = false;
        int shortest = 10000;
        for(Cell[] innerCells : Cells){
            for(Cell cell : innerCells){
                int mid = Math.floorDiv(gameState.mapSize, 2);
                if(Math.abs(cell.y - mid) > 8 || Math.abs(cell.x - mid) > 8){
                    continue;
                }
                if(cell.powerUp != null){
                    check = true;
                    int distance = euclideanDistance(currentWorm.position.x, currentWorm.position.y, cell.x, cell.y);
                    if(distance < shortest){
                        shortest = distance;
                        healthPackPosition.x = cell.x;
                        healthPackPosition.y = cell.y;
                    }
                }
            }
        }
        return check ? healthPackPosition : null;
    }

    // Attack Helpers
    private HashMap<String, ArrayList<Worm>> wormInAttackRadius(int x, int y, int damageRadius){
        HashMap<String, ArrayList<Worm>> wormsInArea = new HashMap<>() ;
        wormsInArea.put("Player", new ArrayList<>());
        wormsInArea.put("Opponent", new ArrayList<>());

        for(int i = 0; i < this.myWorms.length; i++){
            MyWorm myWorm = this.myWorms[i];
            Worm opponent = this.opponent.worms[i];
            int distancePlayer = euclideanDistance(myWorm.position.x, myWorm.position.y, x, y);
            int distanceOpponent = euclideanDistance(opponent.position.x, opponent.position.y, x, y);
            if(distancePlayer <= damageRadius){
                wormsInArea.get("Player").add(myWorm);
            }

            if(distanceOpponent <= damageRadius){
                wormsInArea.get("Opponent").add(opponent);
            }
        }

        return wormsInArea;
    }

    private Position swarmAttack(int wormId){
        Worm targetWorm = opponent.worms[wormId-1];
        while(targetWorm.health <= 0){
            wormId = wormId < 3 ? wormId + 1 : 1;
            targetWorm = opponent.worms[wormId-1];
        }
        return targetWorm.position;
    }

    private Boolean checkFriendlyFire(Direction direction){
        boolean checkFriendlyFire = false;
        for(Worm worm : myWorms){
            if(worm.id == currentWorm.id){
                continue;
            }
            for(int i = 1; i < currentWorm.weapon.range; i++){
                int x = i*direction.x;
                int y = i*direction.y;
                int posX = worm.position.x;
                int posY = worm.position.y;
                if(currentWorm.position.x + x == posX && currentWorm.position.y + y == posY && worm.health > 0){
                    checkFriendlyFire = true;
                    break;
                }
            }
        }
        return checkFriendlyFire;
    }

    // Attacks
    private boolean canShootEnemy(int x, int y){
        if(isValidCoordinate(x,y)){
            int distance = euclideanDistance(currentWorm.position.x, currentWorm.position.y, x,y);
            if(distance > currentWorm.weapon.range){
                return false;
            }
            // Check if the closest enemy is available to shot.
            Set<String> cells = constructFireDirectionLines(currentWorm.weapon.range)
                    .stream()
                    .flatMap(Collection::stream)
                    .map(cell -> String.format("%d_%d", cell.x, cell.y))
                    .collect(Collectors.toSet());
            String enemyPosition = String.format("%d_%d", x, y);
            // Friendly Fire
//            for (Worm worm : myWorms){
//                String wormPosition = String.format("%d_%d", worm.position.x, worm.position.y);
//                if(cells.contains(wormPosition)){
//                    return false;
//                }
//            }
            return cells.contains(enemyPosition);
        }


        return false;

    }

    private boolean canBananaBomb(int x, int y) {
        // Check if the current worm is an Agent
        if ( isValidCoordinate(x, y) && currentWorm.bananaBombs.count > 0) {
            // Checking if the opponent worm is close enough
            int bombRange = currentWorm.bananaBombs.range;
            int distance = euclideanDistance(currentWorm.position.x, currentWorm.position.y, x, y);
            if (distance > bombRange) {
                return false;
            }

            //Checking if there's any of our player worm in the target
            int damageRadius = currentWorm.bananaBombs.damageRadius;
            HashMap<String, ArrayList<Worm>> wormsInArea = wormInAttackRadius(x,y,damageRadius);
            return (currentWorm.health < 40) ||
                    (wormsInArea.get("Player").size() == 0 && wormsInArea.get("Opponent").size() >= 2) ||
                    (wormsInArea.get("Player").size() == 0 && currentWorm.bananaBombs.count > 1);

        }
        return false;
    }


    private boolean canSnowball(int x, int y) {
        if (isValidCoordinate(x, y) && currentWorm.snowballs.count > 0) {
            // Checking if the opponent worm is close enough
            int snowballRange = currentWorm.snowballs.range;
            int distance = euclideanDistance(currentWorm.position.x, currentWorm.position.y, x, y);
            if (distance > snowballRange) {
                return false;
            }

            //Checking if there's any of our player worm in the target
            int damageRadius = currentWorm.snowballs.freezeRadius;
            HashMap<String, ArrayList<Worm>> wormsInArea = wormInAttackRadius(x,y,damageRadius);
            ArrayList<Worm> OpponentWorms = wormsInArea.get("Opponent");
            for(Worm worm : OpponentWorms){
//                System.out.println(worm.roundsUntilUnfrozen);
//                System.out.println(worm.position.x + " " + worm.position.y);
                if(worm.roundsUntilUnfrozen > 1){
                    return false;
                }
            }

            return (wormsInArea.get("Player").size() == 0 && currentWorm.snowballs.count > 1) || (currentWorm.health < 30 && distance > 1);
        }
        return false;
    }

    // Default Functions
    private List<List<Cell>> constructFireDirectionLines(int range) {
        List<List<Cell>> directionLines = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            List<Cell> directionLine = new ArrayList<>();
            for (int directionMultiplier = 1; directionMultiplier <= range; directionMultiplier++) {

                int coordinateX = currentWorm.position.x + (directionMultiplier * direction.x);
                int coordinateY = currentWorm.position.y + (directionMultiplier * direction.y);

                if (!isValidCoordinate(coordinateX, coordinateY)) {
                    break;
                }

                if (euclideanDistance(currentWorm.position.x, currentWorm.position.y, coordinateX, coordinateY) > range) {
                    break;
                }

                Cell cell = gameState.map[coordinateY][coordinateX];
                if (cell.type != CellType.AIR) {
                    break;
                }

                directionLine.add(cell);
            }
            directionLines.add(directionLine);
        }

        return directionLines;
    }

    private int euclideanDistance(int aX, int aY, int bX, int bY) {
        return (int) (Math.sqrt(Math.pow(aX - bX, 2) + Math.pow(aY - bY, 2)));
    }

    private boolean isValidCoordinate(int x, int y) {
        return x >= 0 && x < gameState.mapSize
                && y >= 0 && y < gameState.mapSize;
    }

    private Direction resolveDirection(Position a, Position b) {
        StringBuilder builder = new StringBuilder();

        int verticalComponent = b.y - a.y;
        int horizontalComponent = b.x - a.x;

        if (verticalComponent < 0) {
            builder.append('N');
        } else if (verticalComponent > 0) {
            builder.append('S');
        }

        if (horizontalComponent < 0) {
            builder.append('W');
        } else if (horizontalComponent > 0) {
            builder.append('E');
        }

        return Direction.valueOf(builder.toString());
    }
}
