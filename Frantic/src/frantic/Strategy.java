package frantic;

import java.util.ArrayList;
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
import frantic.hlt.Position;
import frantic.hlt.Ship;
import frantic.hlt.ThrustMove;

public abstract class Strategy {
    public abstract void getInitialAssignments( GameMap gameMap, AssignmentList assignments );
	public abstract void calculateMoves(final GameMap gameMap, final ArrayList<Move> moveList, AssignmentList assignments);

	public static void attackStrongestEnemyMultiplePlanets(final GameMap gameMap, final ArrayList<Move> moveList,
			AssignmentList assignments, Set<Ship> usedShips, Set<Planet> claimedPlanets, Map<Integer,Integer> shipsPerPlanet) {
		int idStrongestOpponent = 0;
		int nStrength = 0;
		for( Player player : gameMap.getAllPlayers().values()) {
			if( player.getId() != gameMap.getMyPlayerId() ) {
				int nNumShips = player.getShips().size();
				if( nNumShips > nStrength ) {
					nStrength = nNumShips;
					idStrongestOpponent = player.getId();
				}
			}
		}

		int numEnemyPlanets = 0;
		for (final Planet planet : gameMap.getAllPlanets().values()) {
			if( planet.isOwned() && ( planet.getOwner() == idStrongestOpponent)) {
				numEnemyPlanets++;
			}
		}
//		for (final Planet planet : gameMap.getAllPlanets().values()) {
//			if( planet.isOwned() && ( planet.getOwner() != gameMap.getMyPlayerId())) {
//				numEnemyPlanets++;
//			}
//		}
		
		int nMinShipsToAttackPlanet = 10;
		int nAvailableShips = gameMap.getMyPlayer().getShips().size() - usedShips.size();
		int numPlanetsToAttack = nAvailableShips / nMinShipsToAttackPlanet;
		if( numPlanetsToAttack > numEnemyPlanets) {
			numPlanetsToAttack = numEnemyPlanets;
		}
		int numShipsPerPlanet = 0;
//		if( numPlanetsToAttack > 0 ) {
//			numShipsPerPlanet = nAvailableShips / numPlanetsToAttack;
//		}

		// Find out how many ships are already assigned to this opponent's planets
		int nTotalShipsAssigned = 0;
		int nPlanets = 0;
		for (final Planet planet : gameMap.getAllPlanets().values()) {
			if( planet.getOwner() == idStrongestOpponent ) {
				// Found an enemy planet
				if( shipsPerPlanet.containsKey(planet.getId())) {
					nTotalShipsAssigned += shipsPerPlanet.get(planet.getId());
				}
				nPlanets++;
			}
		}
		int nTotalShips = nTotalShipsAssigned + nAvailableShips;
		if( nPlanets > 0 ) {
			numShipsPerPlanet = nTotalShips / nPlanets;
		}else {
			numShipsPerPlanet = nTotalShips;
		}
		
		for (final Planet planet : gameMap.getAllPlanets().values()) {
			if( planet.getOwner() == idStrongestOpponent ) {
				// Found an enemy planet
				int nShips = numShipsPerPlanet;
				if( shipsPerPlanet.containsKey(planet.getId())) {
					nShips = numShipsPerPlanet - shipsPerPlanet.get(planet.getId());
				}

				attackEnemyPlanet( gameMap,  moveList, assignments, usedShips, claimedPlanets, planet, shipsPerPlanet, nShips);
		        //break;
			}
		}
		
		nAvailableShips = gameMap.getMyPlayer().getShips().size() - usedShips.size();
		while( nAvailableShips > 0 ) {
			int nOldAvailableShips = nAvailableShips;
			for (final Planet planet : gameMap.getAllPlanets().values()) {
				if( planet.getOwner() != gameMap.getMyPlayerId() ) {
					// Found an enemy planet
					attackEnemyPlanet( gameMap,  moveList, assignments, usedShips, claimedPlanets, planet, shipsPerPlanet, 1);
			        //break;
				}
			}
			nAvailableShips = gameMap.getMyPlayer().getShips().size() - usedShips.size();	
			if( nAvailableShips == nOldAvailableShips) {
				break;
			}
		}

	}
	public static void attackClosestEnemySinglePlanet(final GameMap gameMap, final ArrayList<Move> moveList,
			AssignmentList assignments, Set<Ship> usedShips, Set<Planet> claimedPlanets, Map<Integer,Integer> shipsPerPlanet) {

		Planet selectedPlanet = null;
		// FindMyAveragePosition
		double x = 0;
		double y = 0;
		for( Ship ship : gameMap.getMyPlayer().getShips().values()) {
			x += ship.getXPos();
			y += ship.getYPos();			
		}
		int numShips = gameMap.getMyPlayer().getShips().size();
		x /= numShips;
		y /= numShips;
		Position pos = new Position( x, y );
		Map<Double, Entity> map = gameMap.nearbyEntitiesByDistance(pos);
		for (Map.Entry<Double, Entity> entry : map.entrySet())
	    {
	    	Entity e = entry.getValue();
	    	if( e.getClass() == Planet.class) {
	    		Planet planet = (Planet)e;
	    		if( planet.getOwner() == gameMap.getMyPlayerId()) {
	    			continue;
	    		}
	    		// Avoid the center: too much traffic
	    		if( planet.getId() < 4 ) {
	    			continue;
	    		}
    			selectedPlanet = planet;
    			break;
	    	}
	    }
		if( selectedPlanet == null) {
			// No enemy planets except possibly in the center, so don't avoid them
			for (Map.Entry<Double, Entity> entry : map.entrySet())
		    {
		    	Entity e = entry.getValue();
		    	if( e.getClass() == Planet.class) {
		    		Planet planet = (Planet)e;
		    		if( planet.getOwner() == gameMap.getMyPlayerId()) {
		    			continue;
		    		}
	    			selectedPlanet = planet;
	    			break;
		    	}
		    }
			
		}
		int nAvailableShips = gameMap.getMyPlayer().getShips().size() - usedShips.size();

		if( selectedPlanet != null ) {
			attackEnemyPlanet( gameMap,  moveList, assignments, usedShips, claimedPlanets, selectedPlanet, shipsPerPlanet, nAvailableShips);
			return;
		}	
	}
	
	public static void attackEnemyPlanet(final GameMap gameMap, final ArrayList<Move> moveList,
			AssignmentList assignments, Set<Ship> usedShips, Set<Planet> claimedPlanets, Planet planet, Map<Integer,Integer> shipsPerPlanet, int numShipsPerPlanet) {
		// Found an enemy planet
		int nAttackingShips = 0;


        for (final Ship ship : gameMap.getMyPlayer().getShips().values()) {
        	if( usedShips.contains(ship)) {
        		continue;
        	}
        	// This ship is available. choose one of the docked ships to goes after

        	int nDockedCount = planet.getDockedShips().size();
        	if( nDockedCount > 0 ) {
            	int shipToAttackIndex = nAttackingShips%nDockedCount;
            	int shipToAttackId = planet.getDockedShips().get(shipToAttackIndex);

            	Assignment a = new AttackMiner( ship.getId(), shipToAttackId, planet.getId());
            	assignments.Add( a );
            	Move m = a.GetMove(gameMap, moveList, usedShips, claimedPlanets);
            	if( m != null ) {
            		moveList.add( m );
            	}
            	Log.log("Using ship " + ship.getId() + " to attack planet " + planet.getId());
            	nAttackingShips++;
            	usedShips.add(ship);
            	if( nAttackingShips >= numShipsPerPlanet ) {
            		break;
            	}
        	}
		}
	}

	public static void mineWherePossible(final GameMap gameMap, final ArrayList<Move> moveList,
			AssignmentList assignments, Set<Ship> usedShips) {
		for (final Ship ship : gameMap.getMyPlayer().getShips().values()) {
			if( usedShips.contains(ship)) {
				continue;
			}
			                
		    for (final Planet planet : gameMap.getAllPlanets().values()) {

		        if (ship.canDock(planet) && ( planet.getDockingSpots()> planet.getDockedShips().size())) {
		            moveList.add(new DockMove(ship, planet));
		            assignments.Add(new Mine(ship.getId(), planet.getId()));
		            usedShips.add( ship );
		        	Log.log("Docking extra ship " + ship.getId() + " to planet " + planet.getId());
		            break;
		        }
		    }
		}
	}
	public static void claimEmptyPlanets(final GameMap gameMap, final ArrayList<Move> moveList,
			AssignmentList assignments, Set<Ship> usedShips, Set<Planet> claimedPlanets) {
		for (final Ship ship : gameMap.getMyPlayer().getShips().values()) {
			if( usedShips.contains( ship )) {
				continue;
			}
		    if (ship.getDockingStatus() != Ship.DockingStatus.Undocked) {
		    	usedShips.add( ship );
		        continue;
		    }


		    Map<Double, Entity> map = gameMap.nearbyEntitiesByDistance(ship);
		    for (Map.Entry<Double, Entity> entry : map.entrySet())
		    {
		    	Entity e = entry.getValue();
		    	if( e.getClass() == Planet.class) {
		    		Planet planet = (Planet)e;
		        	if( claimedPlanets.contains(planet)) {
		        		continue;
		        	}
		            if (planet.isOwned()) {
		            	claimedPlanets.add( planet );
		                continue;
		            }

		            if(planet.getDockingSpots()>planet.getDockedShips().size() ) {
			            if (ship.canDock(planet) ) {
			                moveList.add(new DockMove(ship, planet));
			                assignments.Add( new Mine( ship.getId(), planet.getId()));
			                usedShips.add( ship );
			                claimedPlanets.add( planet );
			             	Log.log("Docking ship " + ship.getId() + " to planet " + planet.getId());         
			                break;
			            }
	
			            final ThrustMove newThrustMove = Navigation.navigateShipToDock(gameMap, ship, planet, Constants.MAX_SPEED);
			            if (newThrustMove != null) {
			                moveList.add(newThrustMove);
			                assignments.Add( new ClaimPlanet( ship.getId(), planet.getId()));
			                usedShips.add(ship);
			                claimedPlanets.add( planet );
			                Log.log("Navigating ship " + ship.getId() + " to planet " + planet.getId());
	
			            }
		            }
		            break;
		    	}
		    }
		    
		}
	}

}
