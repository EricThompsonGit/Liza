package eggnog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eggnog.hlt.Constants;
import eggnog.hlt.DockMove;
import eggnog.hlt.Entity;
import eggnog.hlt.GameMap;
import eggnog.hlt.Log;
import eggnog.hlt.Move;
import eggnog.hlt.Navigation;
import eggnog.hlt.Planet;
import eggnog.hlt.Player;
import eggnog.hlt.Ship;
import eggnog.hlt.ThrustMove;

public class StrategyBasic extends Strategy {

	@Override
	public void getInitialAssignments( GameMap gameMap, AssignmentList assignments ) {
    	// Choose three closest planets such that paths don't cross
    	List<Planet> closePlanets = new ArrayList<Planet>();
    	List<Ship> myShips = new ArrayList<Ship>();
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
		
		
		attackEnemyPlanets(gameMap, moveList, assignments, usedShips, claimedPlanets, shipsPerPlanet);
	}
	private static void attackEnemyPlanets(final GameMap gameMap, final ArrayList<Move> moveList,
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
	
	private static void attackEnemyPlanet(final GameMap gameMap, final ArrayList<Move> moveList,
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

	private static void mineWherePossible(final GameMap gameMap, final ArrayList<Move> moveList,
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
	private static void claimEmptyPlanets(final GameMap gameMap, final ArrayList<Move> moveList,
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
