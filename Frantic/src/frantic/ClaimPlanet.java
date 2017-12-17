package frantic;

import java.util.ArrayList;
import java.util.Set;

import frantic.hlt.Constants;
import frantic.hlt.DockMove;
import frantic.hlt.GameMap;
import frantic.hlt.Log;
import frantic.hlt.Move;
import frantic.hlt.Navigation;
import frantic.hlt.Planet;
import frantic.hlt.Position;
import frantic.hlt.Ship;
import frantic.hlt.ThrustMove;

public class ClaimPlanet extends Assignment {

	private boolean posSet = false;
	private Position targetPos;
	
	public ClaimPlanet(int shipId, int target) {
		super(shipId, target);
		// TODO Auto-generated constructor stub
	}

	public ClaimPlanet(int shipId, int target, Position targetPos ) {
		super(shipId, target);
		this.targetPos = targetPos;
		posSet = true;
		// TODO Auto-generated constructor stub
	}

	
	Assignment IsValid(GameMap gameMap) {
		// Check that the ship still exists, and that the planet is still unclaimed
		Ship ship = FindShip( gameMap );
		if( ship == null ) {
			Log.log("Claim Planet Invalid, can't find ship");
			return null;
		}
		Planet planet = FindPlanet( gameMap );
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
				int enemy = planet.getDockedShips().get(0);
				return new AttackMiner( nShipId, enemy, planet.getId() );
			}
			return this;
		}
				
		return this;
	}

	
	Planet FindPlanet( GameMap gameMap ) {
		if(gameMap.getAllPlanets().containsKey( nTargetId )) {
			return gameMap.getAllPlanets().get( nTargetId );
		}
		return null;
	}
     	
 	Move GetMove( GameMap gameMap, ArrayList<Move> moveList, Set<Ship> usedShips, Set<Planet> claimedPlanets) {
 		Ship ship = FindShip(gameMap );
 		if( ship == null ) {
 			Log.log("Claim Planet- Null Ship");
 		}
 		Planet planet = FindPlanet( gameMap );
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
		            return move;
    			}else {
    				return null;
    			}
    		}else {
	            Move move =new DockMove(ship, planet);
	            usedShips.add( ship );
	            claimedPlanets.add( planet );
	         	Log.log("Docking ship " + ship.getId() + " to planet " + planet.getId());         
	            return move;
    			
    		}
        }

        Move move = null;
        if( posSet ) {
        	Log.log( "Navigate toward planet position");    
        	Log.log("Ship = " + ship );
        	Log.log("TargetPosition = " + targetPos );
        	move = Navigation.navigateShipTowardsTarget(gameMap, ship, targetPos, Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180.0);
        	Log.log("Move = " + move + ", thrust = " + ((ThrustMove)move).getThrust());
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
                ", ship=" + nShipId +
                ", planet=" + nTargetId +
                "]";
    }

}
