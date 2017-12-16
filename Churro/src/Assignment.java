import java.util.ArrayList;
import java.util.Set;

import hlt.GameMap;
import hlt.Move;
import hlt.Planet;
import hlt.Ship;

public class Assignment {

	int nShipId;
	int nTargetId;
	
	public Assignment( int shipId, int target ) {
		nShipId = shipId;
		nTargetId = target;
	}
	
	Move GetMove( GameMap gameMap, ArrayList<Move> moveList, Set<Ship> usedShips, Set<Planet> claimedPlanets) {
		return null;
	}
	
	Assignment IsValid(GameMap gameMap) {
		return null;
	}
	
	Ship FindShip( GameMap gameMap ) {
		 for (final Ship ship : gameMap.getMyPlayer().getShips().values()) {
			 if( ship.getId() == nShipId ) {
				 return ship;
			 }
		 }
		 return null;
	}
	
	Ship FindOtherShip( GameMap gameMap ) {
		 for (final Ship ship : gameMap.getAllShips()) {
			 if( ship.getId() == nTargetId ) {
				 return ship;
			 }
		 }
		 return null;
	}
	
	Planet FindPlanet( GameMap gameMap ) {
		return null;
	}

}
