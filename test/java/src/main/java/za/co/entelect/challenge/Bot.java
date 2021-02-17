package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.CellType;
import za.co.entelect.challenge.enums.Direction;

import java.util.*;
import java.util.stream.Collectors;

public class Bot {

    private Random random;
    private GameState gameState;
    private Opponent opponent;
    private MyWorm currentWorm;
    private MyWorm[] myWorms;
    private String previousCommand;

    public Bot(Random random, GameState gameState) {
        this.random = random;
        this.gameState = gameState;
        this.opponent = gameState.opponents[0];
        this.currentWorm = getCurrentWorm(gameState);
        // Added
        this.myWorms = gameState.myPlayer.worms;
        this.previousCommand = gameState.myPlayer.previousCommand.split(" ")[0];
    }

    private MyWorm getCurrentWorm(GameState gameState) {
        return Arrays.stream(gameState.myPlayer.worms)
                .filter(myWorm -> myWorm.id == gameState.currentWormId)
                .findFirst()
                .get();
    }

    public Command run() {
        Position closestWormPosition = getClosestEnemyWormPosition(currentWorm.position.x, currentWorm.position.y);
        Direction moveDirection = resolveDirection(
                currentWorm.position,
                closestWormPosition
        );

        if (currentWorm.id == 2 && canBananaBomb(closestWormPosition.x, closestWormPosition.y)) {
            if( !previousCommand.equals("move") && !previousCommand.equals("dig")){
                return new ThrowBananaBomb(closestWormPosition.x, closestWormPosition.y);
            }
        }

        if(canShootEnemy(closestWormPosition.x, closestWormPosition.y)){
            if (currentWorm.id == 3 && canSnowball(closestWormPosition.x, closestWormPosition.y)) {
                return new ThrowSnowball(closestWormPosition.x, closestWormPosition.y);
            } else {
                Direction direction = resolveDirection(currentWorm.position, closestWormPosition);
                return new ShootCommand(direction);
            }

        }

        Cell block = getCellToMove(moveDirection);
        if (block.type == CellType.AIR && !IsCellOccupied(block)) {
            return new MoveCommand(block.x, block.y);
        } else if (block.type == CellType.DIRT) {
            return new DigCommand(block.x, block.y);
        }
        return new DoNothingCommand();
    }


    // Added
    // Movements
    private Position getClosestEnemyWormPosition(int x, int y) {
        // Worms Position
        Worm[] enemyWorms = opponent.worms;
        Position currentWormPosition = currentWorm.position;

        // Approach Strategy
        int shortest = 100000000; // Maybe this need to be fixed
        int lowestHealth = -1;

        // The best worm choice
        Position candidateWormPosition = currentWorm.position;
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
                    candidateWormPosition = enemyWormPosition;
                }
            } else {
                if(distance == shortest) {
                    // If they have the same distance attack the one with the lowest health
                    if( lowestHealth > enemyWorm.health ){
                        lowestHealth = enemyWorm.health;
                        candidateWormPosition = enemyWormPosition;
                    }
                } else if (distance < shortest) {
                    shortest = distance;
                    candidateWormPosition = enemyWormPosition;
                }
            }
        }
        return candidateWormPosition;
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
            if(cell.x == worm.position.x && cell.y == worm.position.y){
                return true;
            }
        }

        for(Worm worm : opponent.worms){
            if(cell.x == worm.position.x && cell.y == worm.position.y){
                return true;
            }
        }

        return false;
    }

    // Attack Helpers
    private HashMap<String, Worm[]> wormInAttackRadius(int x, int y, int damageRadius){
        HashMap<String, Worm[]> wormsInArea = new HashMap<String, Worm[]>() ;
        wormsInArea.put("Player", new ArrayList<>());
        wormsInArea.put("Opponent", new ArrayList<>());

        for(int i = 0; i < 3; i++){
            MyWorm myWorm = this.myWorms[i];
            Worm opponent = this.opponent.worms[i];
            int distancePlayer = euclideanDistance(myWorm.position.x, myWorm.position.y, x, y);
            int distanceOpponent = euclideanDistance(opponent.position.x, opponent.position.y, x, y);
        }
        return wormCount;
    }

    // Attacks
    private boolean canShootEnemy(int x, int y){
        if(isValidCoordinate(x,y)){
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
                int[] wormCount = wormInAttackRadius(x,y,damageRadius);
                return wormCount[0] <= 0 && wormCount[1] >= 2;
            }
        return false;
    }

    private boolean canSnowball(int x, int y) {
        if (isValidCoordinate(x, y) && currentWorm.snowballs.count > 0) {
            // Checking if the target is frozen
            Worm Target = new Worm();
            // To Avoid Null
            Target.roundsUntilUnfrozen = 0;

            for( Worm worm : opponent.worms){
                if(worm.position.x == x && worm.position.y == y){
                    Target = worm;
                }
            }

            // if the target is still frozen
            if(Target.roundsUntilUnfrozen > 0 ){
                return false;
            }

            // Checking if the opponent worm is close enough
            int snowballRange = currentWorm.snowballs.range;
            int distance = euclideanDistance(currentWorm.position.x, currentWorm.position.y, x, y);
            if (distance > snowballRange) {
                return false;
            }

            //Checking if there's any of our player worm in the target
            int damageRadius = currentWorm.snowballs.freezeRadius;
            int[] wormCount = wormInAttackRadius(x,y,damageRadius);
            return wormCount[0] <= 0 && wormCount[1] >= 2;
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