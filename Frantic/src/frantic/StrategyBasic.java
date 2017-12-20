package frantic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import frantic.hlt.Assignment;
import frantic.hlt.Constants;
import frantic.hlt.DockMove;
import frantic.hlt.Entity;
import frantic.hlt.GameMap;
import frantic.hlt.Log;
import frantic.hlt.Move;
import frantic.hlt.Navigation;
import frantic.hlt.Planet;
import frantic.hlt.Player;
import frantic.hlt.Ship;
import frantic.hlt.ThrustMove;

public class StrategyBasic extends Strategy {

	@Override
	public void getInitialAssignments( GameMap gameMap ) {
    	// Choose three closest planets such that paths don't cross
    	List<Planet> closePlanets = new ArrayList<Planet>();
    	List<Ship> myShips = new ArrayList<Ship>();
//        Log.log("My Player = " + gameMap.getMyPlayer());
//        Log.log("My Player num ships = " + gameMap.getMyPlayer().getShips().size());

    	for( Ship ship : gameMap.getMyPlayer().getShips().values()) {
    		double dClosestPlanetDist = Double.MAX_VALUE;
    		myShips.add(ship);
    		Planet planetClosest = null;
            for (final Planet planet : gameMap.getAllPlanets().values()) {
            	if( closePlanets.contains( planet )) {
            		continue;
            	}
            	double dist = planet.getDistanceTo(ship);
            	if( dist < dClosestPlanetDist ) {
            		dClosestPlanetDist = dist;
            		planetClosest = planet;
            	}
            }
            closePlanets.add(planetClosest);
            Log.log("Adding initial planet " + planetClosest.getId());
    	}
    	// We have the three closest planets.  Now assign our ships to them
    	for( Planet planet : closePlanets ) {
    		double dClosestShipDist = Double.MAX_VALUE;
    		Ship shipClosest = null;
    		for( Ship ship : myShips ) {
            	double dist = planet.getDistanceTo(ship);
            	if( dist < dClosestShipDist ) {
            		dClosestShipDist = dist;
            		shipClosest = ship;
            	}   			
    		}
    		shipClosest.setAssignment(new ClaimPlanet( shipClosest, planet));
    		myShips.remove(shipClosest);
    	}
    }

	@Override
	public void calculateMoves(GameMap gameMap, ArrayList<Move> moveList) {
		// Make sure we give each ship something useful to do
		Set<Ship> usedShips = new HashSet<Ship>();
		Set<Planet> claimedPlanets = new HashSet<Planet>();
		Map<Integer,Integer> shipsPerPlanet = new HashMap<Integer, Integer>();

		cleanUpDefenders( gameMap, usedShips);

		processAssignments(gameMap, moveList, usedShips, claimedPlanets, shipsPerPlanet);

		allocateDefenders( gameMap, usedShips );
		
		mineWherePossible(gameMap, moveList, usedShips);

		claimEmptyPlanets(gameMap, moveList, usedShips, claimedPlanets);
		// We have ships moving to all available planets.
		// If there are any more ships, see if they can dock where they are
		// We are doing max mining.  Use any remaining ships to go after enemy planets.  We want to overwhelm,
		// so use at least 20 ships to attack any planet
		
		attackStrongestEnemyMultiplePlanets(gameMap, moveList, usedShips, claimedPlanets, shipsPerPlanet);
		
		defendPlanets( gameMap, moveList );
	}
}
