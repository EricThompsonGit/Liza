import java.util.ArrayList;
import java.util.Set;

import hlt.GameMap;
import hlt.Log;
import hlt.Move;
import hlt.Planet;
import hlt.Ship;

public class Mine extends Assignment {

	public Mine(int shipId, int target) {
		super(shipId, target);
		// TODO Auto-generated constructor stub
	}
	
	boolean IsValid(GameMap gameMap) {
		// Check that the ship still exists, and that the planet is still unclaimed
		Ship ship = FindShip( gameMap );
		if( ship == null ) {
			Log.log("Mine Invalid, can't find ship");
			return false;
		}
		Planet planet = FindPlanet( gameMap );
		if( planet == null ) {
			Log.log("Mine Invalid, can't find planet");
			return false;
		}
		if( planet.isOwned() && ( planet.getOwner() != gameMap.getMyPlayerId())) {
			Log.log("Mine Invalid, planet owned by someone else");
			return false;
		}
				
		return true;
	}

	Move GetMove( GameMap gameMap, ArrayList<Move> moveList, Set<Ship> usedShips, Set<Planet> claimedPlanets) {
		// Nothing to do
		return null;
	}
	
	Planet FindPlanet( GameMap gameMap ) {
        for (final Planet planet : gameMap.getAllPlanets().values()) {
            if (planet.getId() == nTargetId ) {
            	return planet;
            }
        }
		 return null;
	}

}
