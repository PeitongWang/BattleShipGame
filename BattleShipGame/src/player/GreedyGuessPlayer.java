package player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ship.Ship;
import world.World;
import world.World.Coordinate;

/**
 * Greedy guess player (task B).
 * Please implement this class.
 */
public class GreedyGuessPlayer  implements Player{
	
	/**
	 * Strategy:
	 * 
	 * there are 2 modes: hunting mode & targeting mode:
	 * when game is under the hunting mode,
	 * randomly shoot the coordinates on map that don't adjoin with each others.
	 * mode change to targeting mode until we hit a ship.
	 * 
	 * when game is under the targeting mode:
	 * randomly choose one adjacent coordinate form the hit coordinate.
	 * mode change  to hunting mode until a ship sunk by our shooting and 
	 * there is also no any other coordinate under 'hit' state.
	 */

	private Mode mode = Mode.HUNTING_MODE;
	private World world;
	private ArrayList<Coordinate> waitingShootCoordinates = new ArrayList<Coordinate>();
	private ArrayList<Coordinate> shootHistoryList = new ArrayList<>();
	private ArrayList<Coordinate> checkboard = new ArrayList<Coordinate>();
    @Override
    public void initialisePlayer(World world) {
        // To be implemented.
    	this.world = world;
    	initCheckBoard();
    } // end of initialisePlayer()
    
    /**
     * initialize our check board
     * which actually is the opponent's map
     */
    private void initCheckBoard(){
    	for(int row = 0 ; row < world.numRow ; row++){
    		for(int column = 0 ; column < world.numColumn ; column++){
    			if(row%2 == column%2){
    				Coordinate coor = world.new Coordinate();
    				coor.row = row;
    				coor.column = column;
    				checkboard.add(coor);
    			}
    		}
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
    			answer.shipSunk = sl.ship;
    			for(Coordinate coor : sl.coordinates){
    				if(!world.shots.contains(coor)){
    					answer.shipSunk = null;
    					break;
    				}
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
    	Random random  = new Random();
    	//randomly generate a coordinate,which satisfies that both row%2 == column%2   if this coordinate is not contained by shots, which saves all coordinates we shoot before,
    	//then shoot at this coordinate.
    	if(mode == Mode.HUNTING_MODE){
        	loop1: while(true){
        		int row = random.nextInt(world.numRow);
        		int column = random.nextInt(world.numColumn);
        		if(row%2 == column%2){
    	    		for(Coordinate shootCoor : shootHistoryList){
    	    			if(shootCoor.row == row && shootCoor.column == column){
    	    				continue loop1;
    	    			}
    	    		}
            		guess.row = row;
            		guess.column = column;
            		break;
        		}
        	}
    	}else{
        	for(Coordinate shotCoordinate : shootHistoryList){
        		if(waitingShootCoordinates.contains(waitingShootCoordinates)){
        			waitingShootCoordinates.remove(shotCoordinate);
        		}
        	}
    		Coordinate next = null;
    		do{
    			next = waitingShootCoordinates.remove(0);
    		}while(shootHistoryList.contains(next));
    		guess.row = next.row;
    		guess.column = next.column;
        	if(shootHistoryList.contains(next)){
        		System.out.println("("+guess.column+","+guess.row+")");
        	}
    	}

    	
        // dummy return
        return guess;
    } // end of makeGuess()


    @Override
    public void update(Guess guess, Answer answer) {
        // To be implemented.

    	if(guess.row == 8 && guess.column ==2){
    		world.new Coordinate();
    	}
    	Coordinate newShot = world.new Coordinate();
    	newShot.row = guess.row;
    	newShot.column = guess.column;
    	shootHistoryList.add(newShot);
    	if(answer.isHit){
    		if(mode==Mode.HUNTING_MODE){
    			mode = Mode.TARGETING_MODE;
    			//create waiting shot list
    			//and add the four adjacent cells into the waiting list.
    			waitingShootCoordinates.clear();
    		}
			updateForNextShootCoordinateList(newShot);
    	}
    	//when waiting list is empty, change to seeking mode.
		if(mode == Mode.TARGETING_MODE && waitingShootCoordinates.size() == 0){
			mode = Mode.HUNTING_MODE;
		}


    } // end of update()


    @Override
    public boolean noRemainingShips() {
        // To be implemented.
    	//check all ship, if any coordinate hasn't been shoot, then return false; otherwise return true;
    	for(World.ShipLocation sl :world.shipLocations){
    		for(Coordinate coor : sl.coordinates){
    			if(!world.shots.contains(coor)){
    				return false;
    			}
    		}
    	}
        // dummy return
        return true;
    } // end of noRemainingShips()

    
    private boolean isInWorld(Coordinate cdn) {
        if (world.isHex)
            return cdn.row >= 0 && cdn.row < world.numRow && cdn.column >= (cdn.row + 1) / 2 && cdn.column < world.numColumn + (cdn.row + 1) / 2;
        else
            return cdn.row >= 0 && cdn.row < world.numRow && cdn.column >= 0 && cdn.column < world.numColumn;
    }
    //this method is just used under targeting mode
    
    private void updateForNextShootCoordinateList(Coordinate newShot){
		Coordinate north = world.new Coordinate();
		north.row = newShot.row;
		north.column = newShot.column-1;
		if(isInWorld(north) && !shootHistoryList.contains(north)){
			waitingShootCoordinates.add(north);
		}
		Coordinate south = world.new Coordinate();
		south.row = newShot.row;
		south.column = newShot.column+1;
		if(isInWorld(south)&& !shootHistoryList.contains(south)){
			waitingShootCoordinates.add(south);
		}
		Coordinate west = world.new Coordinate();
		west.row = newShot.row-1;
		west.column = newShot.column;
		if(isInWorld(west)&& !shootHistoryList.contains(west)){
			waitingShootCoordinates.add(west);
		}
		Coordinate east = world.new Coordinate();
		east.row = newShot.row+1;
		east.column = newShot.column;
		if(isInWorld(east)&& !shootHistoryList.contains(east)){
			waitingShootCoordinates.add(east);
		}
    }

    

} // end of class GreedyGuessPlayer
