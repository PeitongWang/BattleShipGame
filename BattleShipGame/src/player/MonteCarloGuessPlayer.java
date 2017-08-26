package player;

import java.util.ArrayList;
import java.util.List;
import ship.AircraftCarrier;
import ship.Battleship;
import ship.Cruiser;
import ship.Destroyer;
import ship.Ship;
import ship.Submarine;
import world.World;
import world.World.Coordinate;
import world.World.ShipLocation;

/**
 * Monte Carlo guess player (task C).
 * Please implement this class.
 */
public class MonteCarloGuessPlayer  implements Player{
	
	/**
	 * Strategy 
	 * Under hunting mode:
	 * 1.calculate all coordicates' value.
	 * 2.shoot the coordinate with highest value.
	 * 3.when hit a ship, change to targeting mode.
	 * 
	 * Under targeting mode:
	 * calculate all adjacent coordinates of current hit coordinate.
	 * then choose one to shoot.
	 * 
	 * if not hit, then change another one until hits
	 * 
	 * when hit on an adjacent coordinate , continue shooting on this direction
	 *  until no valid coordinate on this direction before a ship sunk
	 *  or a ship sunk.
	 *  
	 *  when a ship sunk:
	 *  if there is any coordinate in 'HIT' state, 
	 *  continue repeating the steps above
	 *  
	 *  otherwise change to hunting mode.
	 * 
	 * 
	 * 
	 * 
	 */
	private short VALID = 0,SHOT = 1,HIT = 2,SUNK = 3,EMPTY = 4;
	
	private Mode mode = Mode.HUNTING_MODE; 
	private World world = null;
	private short[][] opponentMap = null; 
	private int[][] opponentValueMap = null; 
	private ArrayList<Ship> opponentShips = null;
	private Coordinate nextRoundShootCoordinate = null;
	private Direction lastRoundShotDirection = null;
	private ArrayList<Coordinate> hitCoordinateList = new ArrayList<Coordinate>();
	
    @Override
    public void initialisePlayer(World world) {
        // To be implemented.
    	this.world = world;
    	this.opponentMap = new short[world.numRow][world.numColumn];//the default value is 0,so it doesn't need to be initialised.
    	this.opponentValueMap = new int[world.numRow][world.numColumn];
    	nextRoundShootCoordinate = world.new Coordinate();
    	initOponentShip();
    	
    } // end of initialisePlayer()
    
    /**
     * we need know the information of opponent's ships
     */
    //copy this method from world.java
    private void initOponentShip(){
    	opponentShips = new ArrayList<Ship>();
    	for(ShipLocation sl : world.shipLocations){
    		Ship ship = null;
    		switch (sl.ship.name()) {
            case "AircraftCarrier":
                ship = new AircraftCarrier();
                break;
            case "Battleship":
                ship = new Battleship();
                break;
            case "Submarine":
                ship = new Submarine();
                break;
            case "Cruiser":
                ship = new Cruiser();
                break;
            case "Destroyer":
                ship = new Destroyer();
                break;
            default:
                break;
        }
    		//order ships by length
		int i = 0 ;
		for(; i < opponentShips.size() ; i ++){
			if(ship.len()>opponentShips.get(i).len()){
				break;
			}
		}
		opponentShips.add(ship);
    	
    }
    }

    @Override
    public Answer getAnswer(Guess guess) {
        // To be implemented.
    	Answer answer = new Answer();
    	Coordinate shot = world.new Coordinate();
    	shot.row = guess.row;
    	shot.column = guess.column;
    	for(World.ShipLocation sl :world.shipLocations){
    		if(sl.coordinates.contains(shot)){
    			answer.isHit = true;
    			if(world.shots.containsAll(sl.coordinates)){
    				answer.shipSunk = sl.ship;	
    			}else{
    				answer.shipSunk = null;
    			}
    			break;
    		}
    	}
        // dummy return
        return answer;
    } // end of getAnswer()


    @Override
    public Guess makeGuess() {
        // To be implemented.
    	Guess guess = new Guess();
    	//if its hunting mode :
    	//simply find the most valuable coordinate and shoot.
    	if(mode == Mode.HUNTING_MODE){
        	calculateValues();
        	Coordinate coor = getMostValuableCoordinateFromValueMap();
        	guess.row = coor.row;
        	guess.column = coor.column;
        	opponentMap[guess.row][guess.column] = SHOT;
    	}else{
    		//otherwise, shoot the 'nextRoundShootCoordinate'
    		guess.row = this.nextRoundShootCoordinate.row;
    		guess.column = this.nextRoundShootCoordinate.column;
    	}

        // dummy return
        return guess;
    } // end of makeGuess()


    /**
     * There is 3 situations may happen after a guess request:
     * 
     * firstly, we shot a ship but it isn't sunk
     * 
     * secondly, a ship is sunk by our last shooting.
     * 
     * thirdly, we find a empty coordinate on map
     * 
     * for those 3 situations, we need consider the different modes.
     *
     */
    @Override
    public void update(Guess guess, Answer answer) {
        // To be implemented.
		Coordinate coor = world.new Coordinate();
		coor.row = guess.row;
		coor.column = guess.column;
    	//1.update the map 
    	//case1: shoot and sunk
    	if(answer.shipSunk != null){

    			//update map information
        		opponentMap[guess.row][guess.column] = SUNK;
        		//remove the sunk ship from opponentShip list
        		for(int i = 0 ; i < opponentShips.size() ; i ++){
        			Ship ship = opponentShips.get(i);
        			if(ship.name().equals(answer.shipSunk.name())){
        				opponentShips.remove(i);
        				break;
        			}
        		}
        		ShipLocation sl = world.new ShipLocation();
        		sl.ship = answer.shipSunk;
        		for(int i = 1 ; i < answer.shipSunk.len() ; i ++){
        			Coordinate coordinate = world.new Coordinate();
        			switch(this.lastRoundShotDirection){
        			case NORTH:
        				coordinate.row = guess.row-i;
        				coordinate.column = guess.column;
        				break;
        			case SOUTH:
        				coordinate.row = guess.row+i;
        				coordinate.column = guess.column;
        				break;
        			case EAST:
        				coordinate.row = guess.row;
        				coordinate.column = guess.column-i;
        				break;
        			case WEST:
        				coordinate.row = guess.row;
        				coordinate.column = guess.column+i;
        				break;
        			}
        			this.opponentMap[coordinate.row][coordinate.column] = SUNK;
        			//remove coordinates of the sunk ship from hitsInMode2 list.
        			this.hitCoordinateList.remove(coordinate);
        			sl.coordinates.add(coordinate);
        		}
        		if(this.hitCoordinateList.size() == 0){
        			mode = Mode.HUNTING_MODE;
        		}else{
        			this.nextRoundShootCoordinate = this.getMostValuableCoordinateNextToTHeCoordinate(this.hitCoordinateList.get(0));
        		}
        		//It is the case that ships are adjacent.

    	//case2: shoot but not sunk
    	}else if(answer.isHit){
    		opponentMap[guess.row][guess.column] = HIT;
			hitCoordinateList.add(coor);
    		if(mode == Mode.HUNTING_MODE){//mode 1
    			mode = Mode.TARGETING_MODE;
    			hitCoordinateList.clear();
    			hitCoordinateList.add(coor);
    			nextRoundShootCoordinate = getMostValuableCoordinateNextToTHeCoordinate(coor);
    			
    		}else{//mode 2
    			this.nextRoundShootCoordinate = getNextShotWhenHitAtCurrentDirrection(coor);
    			if(nextRoundShootCoordinate == null){
    				this.nextRoundShootCoordinate = this.getMostValuableCoordinateNextToTHeCoordinate(this.hitCoordinateList.get(0));
    			}
    		}
    	}else{
    		opponentMap[guess.row][guess.column] = EMPTY;
    		if(mode == Mode.TARGETING_MODE){
    			nextRoundShootCoordinate = this.getMostValuableCoordinateNextToTHeCoordinate(this.hitCoordinateList.get(0));
    		}
    	}
    	
    } // end of update()


    @Override
    public boolean noRemainingShips() {
        // To be implemented.
    	for(World.ShipLocation currentShipLocation :world.shipLocations){
			if(!world.shots.containsAll(currentShipLocation.coordinates)){
				return false;
			}

    	}
        // dummy return
        return true;
    } // end of noRemainingShips()
    
    
    /**
     * calculate value of all valid coordinates on map
     * which is using for hunting mode
     */
    private void calculateValues(){
		Coordinate currentCoordinate = world.new Coordinate();
    	for(int row = 0 ; row < opponentMap.length ; row ++){
    		for(int column = 0 ; column < opponentMap[0].length ; column ++){
    			if(opponentMap[row][column] == VALID){
    				int shipLength = this.opponentShips.get(0).len();
    				int horizenLength = 1;
    				int verticalLength = 1;
    				for(int i = 1; i < shipLength;i++){
    					currentCoordinate.row = row;
    					currentCoordinate.column = column-i;
    					if(this.isInWorld(currentCoordinate) && opponentMap[row][column-i] == VALID){
    						horizenLength++;
    					}else{
    						break;
    					}
    				}
    				for(int i = 1; i < shipLength;i++){
    					currentCoordinate.row = row;
    					currentCoordinate.column = column+i;
    					if(this.isInWorld(currentCoordinate) && opponentMap[row][column+i] == VALID){
    						horizenLength++;
    					}else{
    						break;
    					}
    				}
    				
    				for(int i = 1; i < shipLength;i++){
    					currentCoordinate.row = row-i;
    					currentCoordinate.column = column;
    					if(this.isInWorld(currentCoordinate) && opponentMap[row-i][column] == VALID){
    						verticalLength++;
    					}else{
    						break;
    					}
    				}
    				for(int i = 1; i < shipLength;i++){
    					currentCoordinate.row = row+i;
    					currentCoordinate.column = column;
    					if(this.isInWorld(currentCoordinate) && opponentMap[row+i][column] == VALID){
    						verticalLength++;
    					}else{
    						break;
    					}
    				}
    				int horizon = (horizenLength-shipLength +1) < 0 ? 0 :  (horizenLength-shipLength +1);
    				int vertical = (verticalLength-shipLength +1) < 0 ? 0 :  (verticalLength-shipLength +1);
    				opponentValueMap[row][column] = horizon + vertical;
    				
    			}else{
    				opponentValueMap[row][column] = 0;
    			}
    		}
    	}
    }
    
    private Coordinate getNextShotWhenHitAtCurrentDirrection(Coordinate c){
    	Coordinate result = world.new Coordinate();
		switch(this.lastRoundShotDirection){
		case NORTH:
			result.row = c.row+1;
			result.column = c.column;
			break;
		case SOUTH:
			result.row = c.row-1;
			result.column = c.column;
			break;
		case EAST:
			result.row = c.row;
			result.column = c.column+1;
			break;
		case WEST:
			result.row = c.row;
			result.column = c.column-1;
			break;
		}
		if(!this.isInWorld(result) || opponentMap[result.row][result.column] != 0){
			result = null;
		}
    	return result;
    }
    
    /**
     * to find the most valuable adjacent coordinate of the hitCoordinate.
     * if an adjacent coordinate is valid,
     * simply calculate the number of valid coordinates on each direction.
     * 
     * if an adjacent coordinate is hit and the another adjacent coordinate that is on its opposite direction is valid,
     * we should give the opposite one priority to shoot.
     * 
     * @param hitCoordinate
     * @return
     */
    private Coordinate getMostValuableCoordinateNextToTHeCoordinate(Coordinate hitCoordinate){
    	
		int row = hitCoordinate.row;
		int column = hitCoordinate.column;
		Coordinate coordinate = world.new Coordinate();
		int longestShipLength = this.opponentShips.get(0).len();
		int mostValue = 0;
		Coordinate best= world.new Coordinate();

		//west:
		Coordinate temp = this.world.new Coordinate();
		temp.row = row;
		temp.column = column-1;
		if(this.isInWorld(temp) && opponentMap[temp.row][temp.column] == HIT){
			temp.row = row;
			temp.column = column+1;
			if(this.isInWorld(temp) && opponentMap[temp.row][temp.column] == VALID){
				lastRoundShotDirection = Direction.EAST;
				return temp;
			}
		}
		int tempValue = 0;
			for(int i = 1; i < longestShipLength;i++){
				coordinate.row = row;
				coordinate.column = column-i;
				if(this.isInWorld(coordinate) && (opponentMap[row][column-i] == VALID)){
					tempValue++;
				}else{
					break;
				}
			}
			if(mostValue < tempValue){
				best.row = row;
				best.column = column-1;
				this.lastRoundShotDirection =Direction.WEST;
				mostValue = tempValue;
			}

			// east:
			temp = this.world.new Coordinate();
			temp.row = row;
			temp.column = column+1;
			if(this.isInWorld(temp) && opponentMap[temp.row][temp.column] == HIT){
				temp.row = row;
				temp.column = column-1;
				if(this.isInWorld(temp) && opponentMap[temp.row][temp.column] == VALID){
					lastRoundShotDirection = Direction.WEST;
					return temp;
				}
			}
			tempValue = 0;
			for(int i = 1; i < longestShipLength;i++){
				coordinate.row = row;
				coordinate.column = column+i;
				if(this.isInWorld(coordinate) && (opponentMap[row][column+i] == VALID)){
					tempValue++;
				}else{
					break;
				}
			}
			if(mostValue < tempValue){
				best.row = row;
				best.column = column+1;
				this.lastRoundShotDirection =Direction.EAST;
				mostValue = tempValue;
			}
		//north:
			temp = this.world.new Coordinate();
			temp.row = row+1;
			temp.column = column;
			if(this.isInWorld(temp) && opponentMap[temp.row][temp.column] == HIT){
				temp.row = row-1;
				temp.column = column;
				if(this.isInWorld(temp) && opponentMap[temp.row][temp.column] == VALID){
					lastRoundShotDirection = Direction.SOUTH;
					return temp;
				}
			}
			tempValue = 0;
			for(int i = 1; i < longestShipLength;i++){
				coordinate.row = row+i;
				coordinate.column = column;
				if(this.isInWorld(coordinate) && (opponentMap[row+i][column] == VALID)){
					tempValue++;
				}else{
					break;
				}
			}
			if(mostValue < tempValue){
				best.row = row+1;
				best.column = column;
				this.lastRoundShotDirection =Direction.NORTH;
				mostValue = tempValue;
			}

			//south:
			temp = this.world.new Coordinate();
			temp.row = row-1;
			temp.column = column;
			if(this.isInWorld(temp) && opponentMap[temp.row][temp.column] == HIT){
				temp.row = row+1;
				temp.column = column;
				if(this.isInWorld(temp) && opponentMap[temp.row][temp.column] == VALID){
					lastRoundShotDirection = Direction.NORTH;
					return temp;
				}
			}
			tempValue = 0;
			for(int i = 1; i < longestShipLength;i++){
				coordinate.row = row-i;
				coordinate.column = column;
				if(this.isInWorld(coordinate) && (opponentMap[row+-i][column] == VALID)){
					tempValue++;
				}else{
					break;
				}
			}
			if(mostValue < tempValue){
				best.row = row-1;
				best.column = column;
				this.lastRoundShotDirection =Direction.SOUTH;
				mostValue = tempValue;
			}

    	return best;
    	
    }
    /**
     *  copy from the world.java
     * @param cdn
     * @return
     */
    private boolean isInWorld(Coordinate cdn) {
            return cdn.row >= 0 && cdn.row < world.numRow && cdn.column >= 0 && cdn.column < world.numColumn;
    }
    private Coordinate getMostValuableCoordinateFromValueMap(){
    	
    	Coordinate coor = world.new Coordinate();
    	coor.row = 0;
    	coor.column = 0;
    	for(int row = 0 ; row < opponentMap.length ; row ++){
    		for(int column = 0 ; column < opponentMap[0].length ; column ++){
    			if(opponentMap[row][column] == VALID){
    				if(opponentValueMap[row][column] > opponentValueMap[coor.row][coor.column]){
    				   	coor.row = row;
    			    	coor.column = column;
    				}
    			}
    		}
    	}
    	return coor;
    }


} // end of class MonteCarloGuessPlayer
