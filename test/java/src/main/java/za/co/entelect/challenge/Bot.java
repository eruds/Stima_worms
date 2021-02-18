package za.co.entelect.challenge;

import jdk.internal.org.jline.reader.Candidate;
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
        Worm closestWorm = getClosestEnemyWorm(currentWorm.position.x, currentWorm.position.y);
        Position targetWorm = closestWorm.position;
//        Position targetWorm = opponent.worms[1].health < 0 ? opponent.worms[1] : closestWorm.position;
        Direction moveDirection = getBestMove(resolveDirection(
                currentWorm.position,
                targetWorm
        ));


        if (currentWorm.id == 2 && canBananaBomb(targetWorm.x, targetWorm.y)) {
            return new ThrowBananaBomb(targetWorm.x, targetWorm.y);
//
//            if( !previousCommand.equals("move") && !previousCommand.equals("dig")){
//            }
        }

        if (currentWorm.id == 3 && canSnowball(targetWorm.x, targetWorm.y)) {
            return new ThrowSnowball(targetWorm.x, targetWorm.y);
        }

        if(canShootEnemy(targetWorm.x, targetWorm.y)){
            Direction direction = resolveDirection(currentWorm.position, targetWorm);
            return new ShootCommand(direction);

        }


        // powerup start

        
        int powerupCount = 0;
        for (i = 0; i<=5; i++){
            if (powerupCount == 0){
                powerUpStrat();
                Direction powerupDirection = resolveDirection(
                    currentWorm.position,
                    powerup.position
            );
                digMoveto(powerupDirection);
            }
        }


        Cell block = getCellToMove(moveDirection);
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


    private Direction getBestMove(Direction Move){
        if(!IsCellOccupied(getCellToMove(Move))){
            return Move;
        }
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

            if(!IsCellOccupied(getCellToMove(CandidateDirection))){
                return CandidateDirection;
            }
        }
        return Move;

    }

    // Passive Strategy
    private Command Follow(int leaderId) {
        // Look for the leader, default commando
        Worm leader = myWorms[0];
        for(Worm worm : myWorms){
            if(currentWorm.id == leaderId) {
                leader = worm;
                break;
            }
        }

        int a = currentWorm.position.x;
        int b = currentWorm.position.y;
        int c = leader.position.x;
        int d = leader.position.y;

        // Go to leader if far away
        if (euclideanDistance(a,b,c,d) > 3) {
            Direction moveDirection = resolveDirection(
                    currentWorm.position,
                    leader.position
            );
            Cell block = getCellToMove(moveDirection);
            if (block.type == CellType.AIR && !IsCellOccupied(block)) {
                return new MoveCommand(block.x, block.y);
            } else if (block.type == CellType.DIRT) {
                return new DigCommand(block.x, block.y);
            }
        }

        // Approach enemy if leader is close
        Worm closestWorm = getClosestEnemyWorm(currentWorm.position.x, currentWorm.position.y);
        Position targetWorm = closestWorm.position;
        Direction moveDirection = resolveDirection(
                currentWorm.position,
                targetWorm
        );

        Cell block = getCellToMove(moveDirection);
        if (block.type == CellType.AIR && !IsCellOccupied(block)) {
            return new MoveCommand(block.x, block.y);
        } else if (block.type == CellType.DIRT) {
            return new DigCommand(block.x, block.y);
        }
        return new DoNothingCommand();
    }

    private Command CampMiddle() {
        int a = Math.floorDiv(gameState.mapSize, 2);
        int b = Math.floorDiv(gameState.mapSize, 2);
        Position mid = new Position();
        mid.x = a;
        mid.y = b;
        Direction dir = resolveDirection(
                currentWorm.position,
                mid
        );

        if (currentWorm.position.x != mid.x && currentWorm.position.y != mid.y) {
            Cell block = getCellToMove(dir);
            if (block.type == CellType.AIR && !IsCellOccupied(block)) {
                return new MoveCommand(block.x, block.y);
            } else if (block.type == CellType.DIRT) {
                return new DigCommand(block.x, block.y);
            }
        }
        return new DoNothingCommand();

    }

  // Gerak ke Power Up

  private command powerUpStrat(){
    int a = currentWorm.position.x;
    int b = currentWorm.position.y;
    int c = powerup.position.x;
    int d = powerup.position.y;

    if (euclideanDistance(a,b,c,d) > 3) {
        Direction moveDirection = resolveDirection(
                currentWorm.position,
                powerup.position
        );
        Cell block = getCellToMove(moveDirection);
        if (block.type == CellType.AIR && !IsCellOccupied(block)) {
            return new MoveCommand(block.x, block.y);
        } else if (block.type == CellType.DIRT) {
            return new DigCommand(block.x, block.y);
        }
    }
    return new DoNothingCommand();
}

private digMoveto(Direction moveDirection){
    List<Cell> directionCell = getCellToMove(moveDirection);

    for(int i = 0; i < directionCell.size(); i++){
        cell targetCell = directionCell.get(i);
        if (targetCell.x == x && targetCell.y == y) {
            if (targetCell.type == CellType.DIRT) {
                return new DigCommand(x,y);
            } else if(targetCell.type == CellType.AIR && !IsCellOccupied(block)) {
                return new MoveCommand(x,y);
            }
        }
    }

    return digMoveto(moveDirection);

}

    // Attack Helpers
    private HashMap<String, ArrayList<Worm>> wormInAttackRadius(int x, int y, int damageRadius){
        HashMap<String, ArrayList<Worm>> wormsInArea = new HashMap<>() ;
        wormsInArea.put("Player", new ArrayList<Worm>());
        wormsInArea.put("Opponent", new ArrayList<Worm>());

        for(int i = 0; i < 3; i++){
            MyWorm myWorm = this.myWorms[i];
            Worm opponent = this.opponent.worms[i];
            int distancePlayer = euclideanDistance(myWorm.position.x, myWorm.position.y, x, y);
            int distanceOpponent = euclideanDistance(opponent.position.x, opponent.position.y, x, y);
            if(distancePlayer <= damageRadius){
                wormsInArea.get("Player").add(myWorm);
            }

            if(distanceOpponent <= damageRadius){
                wormsInArea.get("Player").add(opponent);
            }
        }
        return wormsInArea;
    }

    private Position swarmAttack(int wormId){
        Worm OpponentWorm = opponent.worms[wormId-1];
        int i = 0;
        while(OpponentWorm.health < 0){
            OpponentWorm = opponent.worms[i];
            i+= 1;
        }
//
//        int count = 0;
//        for(Worm worm : myWorms){
//            int distance = euclideanDistance()
//        }

//        if(OpponentWorm.health > 0){
//            for(Worm worm : opponent.worms){
//                if(worm.id != wormId){
//                    return worm.position;
//                }
//            }
//        }
        return OpponentWorm.position;
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
                HashMap<String, ArrayList<Worm>> wormsInArea = wormInAttackRadius(x,y,damageRadius);
                return wormsInArea.get("Player").size() <= 0 && wormsInArea.get("Opponent").size() >= 1;
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
                if(worm.roundsUntilUnfrozen > 1){
                    return false;
                }
            }

            return wormsInArea.get("Player").size() <= 0 && wormsInArea.get("Opponent").size() >= 2;
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
