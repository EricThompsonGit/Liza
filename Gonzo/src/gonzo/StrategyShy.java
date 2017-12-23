package gonzo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import gonzo.hlt.Constants;
import gonzo.hlt.DockMove;
import gonzo.hlt.Entity;
import gonzo.hlt.GameMap;
import gonzo.hlt.Log;
import gonzo.hlt.Move;
import gonzo.hlt.Navigation;
import gonzo.hlt.Planet;
import gonzo.hlt.Player;
import gonzo.hlt.Position;
import gonzo.hlt.Ship;
import gonzo.hlt.ThrustMove;

public class StrategyShy extends Strategy {

	@Override
	public void getInitialAssignments(GameMap gameMap) {
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
	    
	    middleShip.setAssignment(new ClaimPlanet(middleShip, selectedPlanet, pos ));
	    if( pos1.getYPos() > pos2.getYPos()) {
		    topShip.setAssignment(new ClaimPlanet(topShip, selectedPlanet, pos1 ));
		    bottomShip.setAssignment(new ClaimPlanet(bottomShip, selectedPlanet, pos2 ));
	    }else {
		    topShip.setAssignment(new ClaimPlanet(topShip, selectedPlanet, pos2 ));
		    bottomShip.setAssignment(new ClaimPlanet(bottomShip, selectedPlanet, pos1 ));
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
		
		// We are mining where we can, now claim a nearby empty planet
		claimEmptyPlanets(gameMap, moveList, usedShips, claimedPlanets);
		// We have ships moving to all available planets.

		// We are doing max mining.  Use any remaining ships to go after enemy planets.  We want to overwhelm,
		// so use at least 20 ships to attack any planet
			
		//attackClosestEnemySinglePlanet(gameMap, moveList, usedShips, claimedPlanets, shipsPerPlanet);
		//attackStrongestEnemyMultiplePlanets(gameMap, moveList, usedShips, claimedPlanets, shipsPerPlanet);
		attackAnyEnemyMultiplePlanets(gameMap, moveList, usedShips, claimedPlanets, shipsPerPlanet);
		
		defendPlanets( gameMap, moveList );

	}

}
