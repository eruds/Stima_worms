
List<Cell> surroundingBlocks = getSurroundingCells(currentWorm.position.x, currentWorm.position.y);

private Worm getFirstWormInRange() {

    Set<String> cells = constructFireDirectionLines(currentWorm.weapon.range)
            .stream()
            .flatMap(Collection::stream)
            .map(cell -> String.format("%d_%d", cell.x, cell.y))
            .collect(Collectors.toSet());

    for (Worm enemyWorm : opponent.worms) {
        if (enemyWorm.health <= 0) {
            continue;
        }
        String enemyPosition = String.format("%d_%d", enemyWorm.position.x, enemyWorm.position.y);
        if (cells.contains(enemyPosition)) {
            return enemyWorm;
        }
    }

    return null;
    

    private List<Cell> getSurroundingCells(int x, int y) {
        ArrayList<Cell> cells = new ArrayList<>();
        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                // Don't include the current position
                if (i != x && j != y && isValidCoordinate(i, j)) {
                    cells.add(gameState.map[j][i]);
                }
            }
        }

        return cells;
    }

       private Cell getCell(List<Cell> surroundingBlocks, Direction moveDirection) {
       int x = moveDirection.x + currentWorm.position.x;
       int y = moveDirection.y + currentWorm.position.y;
       Cell defaultCell = gameState.map[y][x];
       for (Cell cell : surroundingBlocks) {
           int x = moveDirection.x + currentWorm.position.x;
           int y = moveDirection.y + currentWorm.position.y;
           if (cell.x == x && cell.y == y) {
               return cell;
           }
       }
       return defaultCell;
   }
   int cellIdx = random.nextInt(surroundingBlocks.size());

        Cell block = surroundingBlocks.get(cellIdx);
        if (block.type == CellType.AIR) {
            return new MoveCommand(block.x, block.y);
        } else if (block.type == CellType.DIRT) {
            return new DigCommand(block.x, block.y);
        }


private boolean canBananaBomb(int x, int y) {
    // To avoid consecutive bomb throw
    if (!isValidCoordinate(x, y)) {
        return false;
    }

    // Check if the current worm is an Agent
    if (currentWorm.bananaBombs != null) {
        if (currentWorm.bananaBombs.count > 0) {
            // Checking if the opponent worm is close enough
            int bombRange = currentWorm.bananaBombs.range;
            int distance = euclideanDistance(currentWorm.position.x, currentWorm.position.y, x, y);
            if (distance > bombRange) {
                return false;
            }

            //Checking if there's any of our player worm in the target
            int damageRadius = currentWorm.bananaBombs.damageRadius;
            for (Worm worm : myWorms) {
                int wormDistance = euclideanDistance(worm.position.x, worm.position.y, x, y);
                if (wormDistance <= damageRadius) {
                    return false;
                }
            }
            return true;
        }
    }
    return false;
}

private boolean canSnowball(int x, int y) {

    if (!isValidCoordinate(x, y)) {
        return false;
    }

    // Check if the current worm is an Agent
    if (currentWorm.snowballs != null) {
        if (currentWorm.snowballs.count > 0) {
            // Checking if the target is frozen
            Worm Target = new Worm();
            // To Avoid Null
            Target.roundsUntilUnfrozen = 0;

            for( Worm worm : opponent.worms){
                if(worm.position.x == x && worm.position.y == y){
                    Target = worm;
                }
            }
            System.out.println(Target.roundsUntilUnfrozen);
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
            
            // Checking if there's any of our player worm in the target
            int damageRadius = currentWorm.snowballs.freezeRadius;
            for (Worm worm : myWorms) {
                int wormDistance = euclideanDistance(worm.position.x, worm.position.y, x, y);
                if (wormDistance <= damageRadius) {
                    return false;
                }
            }
            return true;
        }
    }
    return false;
}
