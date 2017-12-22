package gonzo;

import java.util.ArrayList;
import java.util.Set;

import gonzo.hlt.Assignment;
import gonzo.hlt.Constants;
import gonzo.hlt.DockMove;
import gonzo.hlt.GameMap;
import gonzo.hlt.Log;
import gonzo.hlt.Move;
import gonzo.hlt.Navigation;
import gonzo.hlt.Planet;
import gonzo.hlt.Position;
import gonzo.hlt.Ship;
import gonzo.hlt.ThrustMove;

public class ClaimPlanet extends Assignment {

	private Planet planet;
	private boolean posSet = false;
	private Position targetPos;
	
	public ClaimPlanet(Ship ship, Planet planet) {
		super(ship);
		this.planet = planet;
	}

	public ClaimPlanet(Ship ship, Planet planet, Position targetPos ) {
		super(ship);
		this.planet = planet;
		this.targetPos = targetPos;
		posSet = true;
		// TODO Auto-generated constructor stub
	}
	public Planet getPlanet() {
		return planet;
	}

	
	public Assignment isValid(GameMap gameMap) {
		// Check that the ship still exists, and that the planet is still unclaimed
		if( ship == null ) {
			Log.log("Claim Planet Invalid, can't find ship");
			return null;
		}
		if( planet == null ) {
			Log.log("Claim Planet Invalid, can't find planet");
			return null;
		}
		if( planet.isOwned() ) {
			if(  planet.getOwner() == gameMap.getMyPlayerId()) {
				Log.log("Claim Planet Invalid, I own this planet");
				return null;
			}
			// Someone else owns this planet, so attack them
			if( planet.getDockedShips().size() > 0 ) {
				int enemyId = planet.getDockedShips().get(0);
				Ship enemy = gameMap.getAllShips().get( enemyId );
				ship.setAssignment(new AttackMiner( ship, enemy, planet ));
				return ship.getAssignment();
			}
			return this;
		}
				
		return this;
	}

     	
 	public Move getMove( GameMap gameMap, ArrayList<Move> moveList, Set<Ship> usedShips, Set<Planet> claimedPlanets) {
  		if( ship == null ) {
 			Log.log("Claim Planet- Null Ship");
 		}
 		if( planet == null ) {
 			Log.log("Claim Planet- Null Planet");
 		}

        if (ship.canDock(planet)) {
    		if( planet.isOwned() ) {
    			if(  planet.getOwner() == gameMap.getMyPlayerId()) {
		            Move move =new DockMove(ship, planet);
		            usedShips.add( ship );
		            claimedPlanets.add( planet );
		         	Log.log("Docking ship " + ship.getId() + " to planet " + planet.getId()); 
		         	ship.setAssignment(new Mine(ship, planet));
		            return move;
    			}else {
    				int enemyId = planet.getDockedShips().get(0);
    				Ship enemy = gameMap.getAllShips().get(enemyId);
    				ship.setAssignment( new AttackMiner(ship, enemy, planet));
    				return ship.getAssignment().getMove( gameMap, moveList, usedShips, claimedPlanets);
    				//return null;
    			}
    		}else {
	            Move move =new DockMove(ship, planet);
	            usedShips.add( ship );
	            claimedPlanets.add( planet );
	         	Log.log("Docking ship " + ship.getId() + " to planet " + planet.getId());  
	         	ship.setAssignment(new Mine(ship, planet ));
	            return move;
    			
    		}
        }

        Move move = null;
        if( posSet ) {
        	Log.log( "Navigate toward planet position");    
        	Log.log("Ship = " + ship );
        	Log.log("TargetPosition = " + targetPos );
        	move = Navigation.navigateShipTowardsTarget(gameMap, ship, targetPos, Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180.0);
        	if( move != null ) {
        		Log.log("Move = " + move + ", thrust = " + ((ThrustMove)move).getThrust());
        	}
        }else {
        	move = Navigation.navigateShipToDock(gameMap, ship, planet, Constants.MAX_SPEED);
        }
        if (move != null) {
            usedShips.add(ship);
            claimedPlanets.add( planet );
            Log.log("Navigating ship " + ship.getId() + " to planet " + planet.getId());
            return move;
        }else {
        	Log.log("Invalid thrust move");
        }
        
        return null;
 	}
    @Override
    public String toString() {
        return "ClaimPlanet[" +
                ", ship=" + ship.getId() +
                ", planet=" + planet.getId() +
                "]";
    }

}
