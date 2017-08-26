package player;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import ship.Ship;
import world.World;

/**
 * Random guess player (task A).
 * Please implement this class.
 */
public class RandomGuessPlayer implements Player{

    World world=null;

    Random random=new Random(); 
    
    //This array list is used to store the guesses which have been already guessed.
    ArrayList<Guess> guesses=new ArrayList<Guess>();
    
    //This array list is used to store the ships which have already sunk.
    ArrayList<Ship>destroyedShips=new ArrayList<Ship>();
   
    @Override
    public void initialisePlayer(World world) {
       
        //Initiate the player with the world.
        this.world=world;
    	
        }
    

    @Override
    public Answer getAnswer(Guess guess) {
        
    	Answer answer=new Answer();
        
        //If the point's coordinates are in one of the ship coordinates' array lists, it is a hit.
    	
    	for(int i=0; i<world.shipLocations.size(); i++){
    		
        for(int j=0; j<world.shipLocations.get(i).coordinates.size(); j++){
    			
        if(world.shipLocations.get(i).coordinates.get(j).row==guess.row 
      
        && world.shipLocations.get(i).coordinates.get(j).column==guess.column){
    				
        world.shipLocations.get(i).coordinates.remove(j);
    	
        answer.isHit=true;
            
        //After the last hit if the size of same coordinates's array list which the point is in became 0,
        //it means that the ship sunk.
        if(world.shipLocations.get(i).coordinates.size()==0){
    	    	   
    	answer.shipSunk=world.shipLocations.get(i).ship;
    	    	   
    	}
    	
        }
    			
        }
    		
        }
    
    	return answer;
        } 


    @Override
    public Guess makeGuess() {
       
        Guess guess=new Guess();
        //Randomly generate the row coordinate.
        guess.row= random.nextInt(world.numRow);
        
        //Randomly generate the column coordinate.
        guess.column=random.nextInt(world.numColumn);  
        
        for(int i=0;i<guesses.size();i++){ 
        
        //If the guess point has already been guessed, generate the coordinates again.
        if(guesses.get(i).row==guess.row && guesses.get(i).column==guess.column ){
    		 
        guess.row= random.nextInt(world.numRow);
            
        guess.column=random.nextInt(world.numColumn); 
        
        i=-1;
        
        continue;
    		 
        }
    	 
        }
        
        return guess;
   
        } 


    @Override
    public void update(Guess guess, Answer answer) {
    	
        //Add the guessed guess into the array list.
    	guesses.add(guess);
    	  
        if(answer.shipSunk!=null){
    	  
         //Add the sunk ship into the array list.
         destroyedShips.add(answer.shipSunk);
    	  
    	  }
    	 
         } 


    @Override
    public boolean noRemainingShips() {
        
         //If all the coordinates array lists' size are 0, it means no ship left.
         for(int i=0; i<world.shipLocations.size();i++){
    	  
    	  if(world.shipLocations.get(i).coordinates.size()!=0){
    		 
    	  return false;
    		  
    	  }
    	  
    	  }
    	
    	  return true;

          } 

      } 
