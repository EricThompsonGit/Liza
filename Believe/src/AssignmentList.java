import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import hlt.GameMap;
import hlt.Log;
import hlt.Move;
import hlt.Planet;
import hlt.Ship;

public class AssignmentList {
	List<Assignment> assignments = new ArrayList<Assignment>();
	
	void Add( Assignment a ) {
		assignments.add(a);
	}
	
	void Process( GameMap gameMap, ArrayList<Move> moveList, Set<Ship> usedShips, Set<Planet> claimedPlanets ){
		Log.log( "Processing "+ assignments.size()+" assignments");
		for( int i = 0; i < assignments.size(); i++  ) {
			Assignment a = assignments.get(i);
			Log.log( "Assignment = " + a.toString());
			if( a.IsValid(gameMap) ) {
				Move move = a.GetMove( gameMap, moveList, usedShips, claimedPlanets);
				if( move != null ) {
					moveList.add( move );
					Log.log( "Adding move " + move.toString());

				}
				else
				{
					Log.log( "Null Move");
				}
				Ship ship = findShip( gameMap, a.nShipId );
				usedShips.add(ship);
				Planet planet = a.FindPlanet( gameMap );
				if( planet != null ){
					claimedPlanets.add(planet);
				}
			}else {
				assignments.remove( i );
				i--;
			}
			
		}
	}
	Ship findShip( GameMap gameMap, int id ) {
		 for (final Ship ship : gameMap.getMyPlayer().getShips().values()) {
			 if( ship.getId() == id ) {
				 return ship;
			 }
		 }
		 return null;
	}
	
}