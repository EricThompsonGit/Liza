package drowsy;


import java.util.ArrayList;
import java.util.Set;

import drowsy.hlt.Constants;
import drowsy.hlt.DockMove;
import drowsy.hlt.GameMap;
import drowsy.hlt.Log;
import drowsy.hlt.Move;
import drowsy.hlt.Navigation;
import drowsy.hlt.Planet;
import drowsy.hlt.Ship;
import drowsy.hlt.ThrustMove;

public class ClaimPlanet extends Assignment {

	public ClaimPlanet(int shipId, int target) {
		super(shipId, target);
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
         for (final Planet planet : gameMap.getAllPlanets().values()) {
             if (planet.getId() == nTargetId ) {
             	return planet;
             }
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

        final ThrustMove newThrustMove = Navigation.navigateShipToDock(gameMap, ship, planet, Constants.MAX_SPEED);
        if (newThrustMove != null) {
            usedShips.add(ship);
            claimedPlanets.add( planet );
            Log.log("Navigating ship " + ship.getId() + " to planet " + planet.getId());
            return newThrustMove;
        }else {
        	Log.log("Invalid thrust move");
        }
        
        return null;
 	}

}
