package frantic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import frantic.hlt.GameMap;
import frantic.hlt.Log;
import frantic.hlt.Move;
import frantic.hlt.Planet;
import frantic.hlt.Ship;

public class AssignmentList {
	List<Assignment> assignments = new ArrayList<Assignment>();
	
	void Add( Assignment a ) {
		assignments.add(a);
	}
	
	void Process( GameMap gameMap, ArrayList<Move> moveList, Set<Ship> usedShips, Set<Planet> claimedPlanets, Map<Integer,Integer> shipsPerPlanet ){
		Log.log( "Processing "+ assignments.size()+" assignments");
		for( int i = 0; i < assignments.size(); i++  ) {
			Assignment a = assignments.get(i);
			Log.log( "Assignment = " + a.toString());
			Assignment aPrime = a.IsValid(gameMap);
			if( aPrime != null ) {
				if( aPrime != a ) {
					Log.log( "Replacing " + a + " with " + aPrime );
					assignments.set( i,  aPrime );
				}

				if( aPrime.getClass() == AttackMiner.class ) {
					int nPlanet = ((AttackMiner)aPrime).nPlanet;
					if(shipsPerPlanet.containsKey(nPlanet)) {
						shipsPerPlanet.put(nPlanet, shipsPerPlanet.get(nPlanet)+1);
					}else {
						shipsPerPlanet.put(nPlanet, 1);
					}
				}
				Move move = aPrime.GetMove( gameMap, moveList, usedShips, claimedPlanets);
				if( move != null ) {
					moveList.add( move );
					//Log.log( "Adding move " + move.toString());

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
				Log.log( "Removing " + a  );

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
