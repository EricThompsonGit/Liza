package frantic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	public void getInitialAssignments( GameMap gameMap, AssignmentList assignments ) {
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
    		Assignment a = new ClaimPlanet( shipClosest.getId(), planet.getId());
    		assignments.Add( a );
    		myShips.remove(shipClosest);
    	}
    }

	@Override
	public void calculateMoves(GameMap gameMap, ArrayList<Move> moveList, AssignmentList assignments) {
		// Make sure we give each ship something useful to do
		Set<Ship> usedShips = new HashSet<Ship>();
		Set<Planet> claimedPlanets = new HashSet<Planet>();
		Map<Integer,Integer> shipsPerPlanet = new HashMap<Integer, Integer>();

		assignments.Process(gameMap, moveList, usedShips, claimedPlanets, shipsPerPlanet);

		
		claimEmptyPlanets(gameMap, moveList, assignments, usedShips, claimedPlanets);
		// We have ships moving to all available planets.
		// If there are any more ships, see if they can dock where they are
		mineWherePossible(gameMap, moveList, assignments, usedShips);

		// We are doing max mining.  Use any remaining ships to go after enemy planets.  We want to overwhelm,
		// so use at least 20 ships to attack any planet
		
		
		attackStrongestEnemyMultiplePlanets(gameMap, moveList, assignments, usedShips, claimedPlanets, shipsPerPlanet);
	}
}
