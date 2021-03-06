import java.util.ArrayList;
import java.util.Set;

import hlt.Constants;
import hlt.DockMove;
import hlt.GameMap;
import hlt.Log;
import hlt.Move;
import hlt.Navigation;
import hlt.Planet;
import hlt.Ship;
import hlt.ThrustMove;

public class ClaimPlanet extends Assignment {

	public ClaimPlanet(int shipId, int target) {
		super(shipId, target);
		// TODO Auto-generated constructor stub
	}

	
	boolean IsValid(GameMap gameMap) {
		// Check that the ship still exists, and that the planet is still unclaimed
		Ship ship = FindShip( gameMap );
		if( ship == null ) {
			Log.log("Claim Planet Invalid, can't find ship");
			return false;
		}
		Planet planet = FindPlanet( gameMap );
		if( planet == null ) {
			Log.log("Claim Planet Invalid, can't find planet");
			return false;
		}
		if( planet.isOwned()) {
			Log.log("Claim Planet Invalid, planet is owned");
			return false;
		}
				
		return true;
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
            Move move =new DockMove(ship, planet);
            usedShips.add( ship );
            claimedPlanets.add( planet );
         	Log.log("Docking ship " + ship.getId() + " to planet " + planet.getId());         
            return move;
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
