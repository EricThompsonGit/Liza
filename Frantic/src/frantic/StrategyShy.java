package frantic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import frantic.hlt.Position;
import frantic.hlt.Ship;
import frantic.hlt.ThrustMove;

public class StrategyShy extends Strategy {

	@Override
	public void getInitialAssignments(GameMap gameMap, AssignmentList assignments) {
		// Pick the closest planet that is away from my opponents
		int centerX = gameMap.getWidth()/2;
		int centerY = gameMap.getHeight()/2;
		
		// find the order of the ships
		Ship topShip = null;
		Ship middleShip = null;
		Ship bottomShip = null;
		int totalY = 0;
		for( Ship ship : gameMap.getMyPlayer().getShips().values()) {
			totalY += ship.getYPos();
		}
		for( Ship ship : gameMap.getMyPlayer().getShips().values()) {
			if(totalY/3 == ship.getYPos()) {
				middleShip = ship;
			}else if(totalY/3 > ship.getYPos()) {
				bottomShip = ship;
			}else {
				topShip = ship;
			}
		}
		
		Planet selectedPlanet = null;
		double myDistFromCenterSqr = Math.pow((middleShip.getXPos()-centerX), 2)+Math.pow((middleShip.getYPos()-centerY), 2);
	    Map<Double, Entity> map = gameMap.nearbyEntitiesByDistance(middleShip);
	    for (Map.Entry<Double, Entity> entry : map.entrySet())
	    {
	    	Entity e = entry.getValue();
	    	if( e.getClass() == Planet.class) {
	    		Planet planet = (Planet)e;
	    		double distFromCenterSqr = Math.pow((planet.getXPos()-centerX), 2)+Math.pow((planet.getYPos()-centerY), 2);
	    		if( distFromCenterSqr > myDistFromCenterSqr ) {
	    			selectedPlanet = planet;
	    			break;
	    		}
	    	}
	    }
	    if( selectedPlanet == null ) {
	    	// no planet found in that direction, so just choose closest planet
		    for (Map.Entry<Double, Entity> entry : map.entrySet())
		    {
		    	Entity e = entry.getValue();
		    	if( e.getClass() == Planet.class) {
		    		selectedPlanet = (Planet)e;
		    		break;
		    	}
		    }
	    }
	    
	    // We have our desired planet.  Now choose were to dock each ship so they don't run into each other
	    Position pos = middleShip.getClosestPoint(selectedPlanet);
	    double dx = pos.getXPos() - middleShip.getXPos();
	    double dy = pos.getYPos() - middleShip.getYPos();
	    double dLen = Math.sqrt( dx*dx + dy*dy);
	    dx /= dLen;
	    dy /= dLen;
	    Position pos1 = new Position( pos.getXPos() + dy*2, pos.getYPos() -dx*2 );
	    Position pos2 = new Position( pos.getXPos() - dy*2, pos.getYPos() + dx*2 );
	    
	    assignments.Add( new ClaimPlanet(middleShip.getId(), selectedPlanet.getId(), pos ));
	    if( pos1.getYPos() > pos2.getYPos()) {
		    assignments.Add( new ClaimPlanet(topShip.getId(), selectedPlanet.getId(), pos1 ));
		    assignments.Add( new ClaimPlanet(bottomShip.getId(), selectedPlanet.getId(), pos2 ));	    	
	    }else {
		    assignments.Add( new ClaimPlanet(topShip.getId(), selectedPlanet.getId(), pos2 ));
		    assignments.Add( new ClaimPlanet(bottomShip.getId(), selectedPlanet.getId(), pos1 ));	    		    	
	    }
	}

	@Override
	public void calculateMoves(GameMap gameMap, ArrayList<Move> moveList, AssignmentList assignments) {
		// Make sure we give each ship something useful to do
		Set<Ship> usedShips = new HashSet<Ship>();
		Set<Planet> claimedPlanets = new HashSet<Planet>();
		Map<Integer,Integer> shipsPerPlanet = new HashMap<Integer, Integer>();

		assignments.Process(gameMap, moveList, usedShips, claimedPlanets, shipsPerPlanet);

		mineWherePossible(gameMap, moveList, assignments, usedShips);
		
		// We are mining where we can, now claim a nearby empty planet
		claimEmptyPlanets(gameMap, moveList, assignments, usedShips, claimedPlanets);
		// We have ships moving to all available planets.

		// We are doing max mining.  Use any remaining ships to go after enemy planets.  We want to overwhelm,
		// so use at least 20 ships to attack any planet
			
		attackClosestEnemySinglePlanet(gameMap, moveList, assignments, usedShips, claimedPlanets, shipsPerPlanet);
	}

}
