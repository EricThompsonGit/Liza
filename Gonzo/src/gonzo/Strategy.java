package gonzo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import gonzo.hlt.Assignment;
import gonzo.hlt.Constants;
import gonzo.hlt.Defense;
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

public abstract class Strategy {
    public abstract void getInitialAssignments( GameMap gameMap );
	public abstract void calculateMoves(final GameMap gameMap, final ArrayList<Move> moveList);
	public static Random rand = new Random();

	public static void attackStrongestEnemyMultiplePlanets(final GameMap gameMap, final ArrayList<Move> moveList,
			Set<Ship> usedShips, Set<Planet> claimedPlanets, Map<Integer,Integer> shipsPerPlanet) {
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
		
		int nMinShipsToAttackPlanet = 25;
		int nAvailableShips = gameMap.getMyPlayer().getShips().size() - usedShips.size();
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
		int numPlanetsToAttack = nTotalShips / nMinShipsToAttackPlanet;
		if( numPlanetsToAttack > numEnemyPlanets) {
			numPlanetsToAttack = numEnemyPlanets;
		}

		if( numPlanetsToAttack > 0 ) {
			numShipsPerPlanet = nTotalShips / numPlanetsToAttack;
		}else {
			numShipsPerPlanet = nTotalShips;
		}
		
		// We know how many planets we want to attack, and how many ships to use.
		// Choose the planets closest to our center of mass
		int numSelected = 0;
		List<Planet> selectedPlanets = new ArrayList<Planet>();
		
		int nMinShipsPerPlanet = Integer.MAX_VALUE;
		Position pos = getCenterOfMyShips(gameMap);
		Map<Double, Entity> map = gameMap.nearbyEntitiesByDistance(pos);
		for (Map.Entry<Double, Entity> entry : map.entrySet())
	    {
	    	Entity e = entry.getValue();
	    	if( e.getClass() == Planet.class) {
	    		Planet planet = (Planet)e;
	    		if( planet.getOwner() != idStrongestOpponent) {
	    			continue;
	    		}
	    		numSelected++;
    			selectedPlanets.add( planet );
    			int nNumShips = 0;
				if( shipsPerPlanet.containsKey(planet.getId())) {
					nNumShips = shipsPerPlanet.get(planet.getId());
				}else {
					shipsPerPlanet.put(planet.getId(), 0);
				}
				if( nNumShips < nMinShipsPerPlanet ) {
					nMinShipsPerPlanet = nNumShips;
				}

    			if( numSelected == numPlanetsToAttack ) {
    				break;
    			}
	    	}
	    }
		// Get a list of available ships
		List<Ship> availableShips = new ArrayList<Ship>();
		for( Ship ship : gameMap.getMyPlayer().getShips().values()) {
			if( !usedShips.contains( ship )) {
				availableShips.add(ship);
			}
		}
		
		// Now have planets choose the closest available ship
		while( availableShips.size() > 0) {
			int lastAvailableShips = availableShips.size();
			nMinShipsPerPlanet++;
			for( Planet planet : selectedPlanets ) {
				int nNumShips = 0;
				if(shipsPerPlanet.containsKey(planet.getId())) {
					nNumShips = shipsPerPlanet.get(planet.getId());
				}
				if( nNumShips < nMinShipsPerPlanet ) {
					attackEnemyPlanet( gameMap,  moveList, usedShips, availableShips, claimedPlanets, planet, shipsPerPlanet, 1);					
//					if( availableShips.size() == lastAvailableShips ) {
//						Log.log( "NO SHIP ASSIGNED!!!");
//					}else {
//						Log.log("Ship Assigned");
//					}
				}
//				else {
//					Log.log( "nNumShips = " + nNumShips +", nMinShipsPerPlanet = " + nMinShipsPerPlanet);
//				}
				if( availableShips.size() == 0 ) {
					break;
				}
			}
			if( availableShips.size() == lastAvailableShips ) {
				Log.log( "Ship Assignment Not Working!!!");				
				break;				
			}
		}
		

	}
	public static void attackAnyEnemyMultiplePlanets(final GameMap gameMap, final ArrayList<Move> moveList,
			Set<Ship> usedShips, Set<Planet> claimedPlanets, Map<Integer,Integer> shipsPerPlanet) {

		int numEnemyPlanets = 0;
		for (final Planet planet : gameMap.getAllPlanets().values()) {
			if( planet.isOwned() && ( planet.getOwner() != gameMap.getMyPlayerId())) {
				numEnemyPlanets++;
			}
		}
//		for (final Planet planet : gameMap.getAllPlanets().values()) {
//			if( planet.isOwned() && ( planet.getOwner() != gameMap.getMyPlayerId())) {
//				numEnemyPlanets++;
//			}
//		}
		
		int nMinShipsToAttackPlanet = 25;
		int nAvailableShips = gameMap.getMyPlayer().getShips().size() - usedShips.size();
		int numShipsPerPlanet = 0;
//		if( numPlanetsToAttack > 0 ) {
//			numShipsPerPlanet = nAvailableShips / numPlanetsToAttack;
//		}

		// Find out how many ships are already assigned to this opponent's planets
		int nTotalShipsAssigned = 0;
		int nPlanets = 0;
		for (final Planet planet : gameMap.getAllPlanets().values()) {
			if( planet.getOwner() != gameMap.getMyPlayerId() ) {
				// Found an enemy planet
				if( shipsPerPlanet.containsKey(planet.getId())) {
					nTotalShipsAssigned += shipsPerPlanet.get(planet.getId());
				}
				nPlanets++;
			}
		}
		int nTotalShips = nTotalShipsAssigned + nAvailableShips;
		int numPlanetsToAttack = nTotalShips / nMinShipsToAttackPlanet;
		if( numPlanetsToAttack > numEnemyPlanets) {
			numPlanetsToAttack = numEnemyPlanets;
		}

		if( numPlanetsToAttack > 0 ) {
			numShipsPerPlanet = nTotalShips / numPlanetsToAttack;
		}else {
			numShipsPerPlanet = nTotalShips;
		}
		
		// We know how many planets we want to attack, and how many ships to use.
		// Choose the planets closest to our center of mass
		int numSelected = 0;
		List<Planet> selectedPlanets = new ArrayList<Planet>();
		
		int nMinShipsPerPlanet = Integer.MAX_VALUE;
		Position pos = getCenterOfMyShips(gameMap);
		Map<Double, Entity> map = gameMap.nearbyEntitiesByDistance(pos);
		for (Map.Entry<Double, Entity> entry : map.entrySet())
	    {
	    	Entity e = entry.getValue();
	    	if( e.getClass() == Planet.class) {
	    		Planet planet = (Planet)e;
	    		if( planet.getOwner()  == gameMap.getMyPlayerId()) {
	    			continue;
	    		}
	    		numSelected++;
    			selectedPlanets.add( planet );
    			int nNumShips = 0;
				if( shipsPerPlanet.containsKey(planet.getId())) {
					nNumShips = shipsPerPlanet.get(planet.getId());
				}else {
					shipsPerPlanet.put(planet.getId(), 0);
				}
				if( nNumShips < nMinShipsPerPlanet ) {
					nMinShipsPerPlanet = nNumShips;
				}

    			if( numSelected == numPlanetsToAttack ) {
    				break;
    			}
	    	}
	    }
		// Get a list of available ships
		List<Ship> availableShips = new ArrayList<Ship>();
		for( Ship ship : gameMap.getMyPlayer().getShips().values()) {
			if( !usedShips.contains( ship )) {
				availableShips.add(ship);
			}
		}
		
		// Now have planets choose the closest available ship
		while( availableShips.size() > 0) {
			int lastAvailableShips = availableShips.size();
			nMinShipsPerPlanet++;
			for( Planet planet : selectedPlanets ) {
				int nNumShips = 0;
				if(shipsPerPlanet.containsKey(planet.getId())) {
					nNumShips = shipsPerPlanet.get(planet.getId());
				}
				if( nNumShips < nMinShipsPerPlanet ) {
					attackEnemyPlanet( gameMap,  moveList, usedShips, availableShips, claimedPlanets, planet, shipsPerPlanet, 1);					
//					if( availableShips.size() == lastAvailableShips ) {
//						Log.log( "NO SHIP ASSIGNED!!!");
//					}else {
//						Log.log("Ship Assigned");
//					}
				}
//				else {
//					Log.log( "nNumShips = " + nNumShips +", nMinShipsPerPlanet = " + nMinShipsPerPlanet);
//				}
				if( availableShips.size() == 0 ) {
					break;
				}
			}
			if( availableShips.size() == lastAvailableShips ) {
				Log.log( "Ship Assignment Not Working!!!");				
				break;				
			}
		}
		

	}

	public static void attackClosestEnemySinglePlanet(final GameMap gameMap, final ArrayList<Move> moveList,
			Set<Ship> usedShips, Set<Planet> claimedPlanets, Map<Integer,Integer> shipsPerPlanet) {

		Planet selectedPlanet = null;
		Position pos = getCenterOfMyShips(gameMap);
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
			attackEnemyPlanet( gameMap,  moveList, usedShips, claimedPlanets, selectedPlanet, shipsPerPlanet, nAvailableShips);
			return;
		}	
	}
	public static Position getCenterOfMyShips(final GameMap gameMap) {
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
		return pos;
	}
	
	public static void attackEnemyPlanet(final GameMap gameMap, final ArrayList<Move> moveList,
			Set<Ship> usedShips, Set<Planet> claimedPlanets, Planet planet, Map<Integer,Integer> shipsPerPlanet, int numShipsPerPlanet) {
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
            	Ship enemy = gameMap.getAllShips().get( shipToAttackId );

            	Assignment a = new AttackMiner( ship, enemy, planet);
            	ship.setAssignment(a);
            	//assignments.Add( a );
            	Move m = a.getMove(gameMap, moveList, usedShips, claimedPlanets);
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
	public static void attackEnemyPlanet(final GameMap gameMap, final ArrayList<Move> moveList,
			Set<Ship> usedShips, List<Ship> availableShips, Set<Planet> claimedPlanets, Planet planet, Map<Integer,Integer> shipsPerPlanet, int numShipsPerPlanet) {

		// Find the closest available ship
		double dMinDist = Double.MAX_VALUE;
		Ship shipClosest = null;
        for ( Ship ship : availableShips) {
        	double dist = ship.getDistanceTo(planet);
        	if( dist < dMinDist ) {
        		dMinDist = dist;
        		shipClosest = ship;
        	}
        }
        if( shipClosest == null ) {
        	Log.log("CLOSEST SHIP WAS NULL!!!!!");
        }
        int numAvail = availableShips.size();
        availableShips.remove(shipClosest);
        if( numAvail == availableShips.size()) {
        	Log.log("FAILED TO REMOVE SHIP!!!!");
        }

    	int nDockedCount = planet.getDockedShips().size();
    	if( nDockedCount > 0 ) {
        	int shipToAttackIndex = rand.nextInt(nDockedCount );
        	int shipToAttackId = planet.getDockedShips().get(shipToAttackIndex);
        	Ship enemy = gameMap.getAllShips().get( shipToAttackId );

        	Assignment a = new AttackMiner( shipClosest, enemy, planet);
        	shipClosest.setAssignment(a);
        	//assignments.Add( a );
        	Move m = a.getMove(gameMap, moveList, usedShips, claimedPlanets);
        	if( m != null ) {
        		moveList.add( m );
        	}
        	Log.log("Using ship " + shipClosest.getId() + " to attack planet " + planet.getId());
        	usedShips.add(shipClosest);
        	if(shipsPerPlanet.containsKey(planet.getId())) {
        		shipsPerPlanet.put(planet.getId(), shipsPerPlanet.get(planet.getId())+1);
        	}else {
        		shipsPerPlanet.put(planet.getId(), +1);
        		
        	}
    	}
	}

	public static void mineWherePossible(final GameMap gameMap, final ArrayList<Move> moveList,
			Set<Ship> usedShips) {
		for (final Ship ship : gameMap.getMyPlayer().getShips().values()) {
			if( usedShips.contains(ship)) {
				continue;
			}
			                
		    for (final Planet planet : gameMap.getAllPlanets().values()) {

		        if (ship.canDock(planet) && ( planet.getDockingSpots()> planet.getDockedShips().size())) {
		            moveList.add(new DockMove(ship, planet));
		            //assignments.Add(new Mine(ship.getId(), planet.getId()));
		            ship.setAssignment(new Mine(ship, planet));
		            usedShips.add( ship );
		        	Log.log("Docking extra ship " + ship.getId() + " to planet " + planet.getId());
		            break;
		        }
		    }
		}
	}
	public static void claimEmptyPlanets(final GameMap gameMap, final ArrayList<Move> moveList,
			Set<Ship> usedShips, Set<Planet> claimedPlanets) {
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
			                //assignments.Add( new Mine( ship.getId(), planet.getId()));
			                ship.setAssignment(new Mine( ship, planet));
			                usedShips.add( ship );
			                claimedPlanets.add( planet );
			             	Log.log("Docking ship " + ship.getId() + " to planet " + planet.getId());         
			                break;
			            }
	
			            final ThrustMove newThrustMove = Navigation.navigateShipToDock(gameMap, ship, planet, Constants.MAX_SPEED);
			            if (newThrustMove != null) {
			                moveList.add(newThrustMove);
			                //assignments.Add( new ClaimPlanet( ship.getId(), planet.getId()));
			                ship.setAssignment( new ClaimPlanet( ship, planet));
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

	public void processAssignments( GameMap gameMap, ArrayList<Move> moveList, Set<Ship> usedShips, Set<Planet> claimedPlanets, Map<Integer,Integer> shipsPerPlanet ) {
//		Log.log( "Processing "+ assignments.size()+" assignments");
		for( Ship ship : gameMap.getMyPlayer().getShips().values()) {
		//for( int i = 0; i < assignments.size(); i++  ) {
			Assignment a = ship.getAssignment();
			// change line number
			if( a == null ) {
				continue;
			}
			Log.log( "Assignment = " + a.toString());
			Assignment aPrime = a.isValid(gameMap);
			if( aPrime != null ) {
				if( aPrime != a ) {
					Log.log( "Replacing " + a + " with " + aPrime );
					//assignments.set( i,  aPrime );
					ship.setAssignment(aPrime);
				}

				if( aPrime.getClass() == AttackMiner.class ) {
					int nPlanet = ((AttackMiner)aPrime).getPlanet().getId();
					if(shipsPerPlanet.containsKey(nPlanet)) {
						shipsPerPlanet.put(nPlanet, shipsPerPlanet.get(nPlanet)+1);
					}else {
						shipsPerPlanet.put(nPlanet, 1);
					}
				}
				Move move = aPrime.getMove( gameMap, moveList, usedShips, claimedPlanets);
				if( move != null ) {
					moveList.add( move );
					//Log.log( "Adding move " + move.toString());

				}
				else
				{
					Log.log( "Null Move");
					Log.log("Move = " + move );
				}
//				Ship ship = findShip( gameMap, a.nShipId );
				usedShips.add(ship);
				Planet planet = a.getPlanet();
				if( planet != null ){
					claimedPlanets.add(planet);
				}
			}else {
				Log.log( "Removing " + a  );
				ship.setAssignment(null);
			}
			
		}
		
	}

	public static void defendPlanets(final GameMap gameMap, final ArrayList<Move> moveList) {
		for ( Planet planet : gameMap.getAllPlanets().values()) {
			planet.allocateDefensiveShips(gameMap, moveList );
		}
	}

	public static void cleanUpDefenders(final GameMap gameMap, Set<Ship> usedShips ) {
		for ( Planet planet : gameMap.getAllPlanets().values()) {
			planet.cleanUpDefenders(gameMap.getMyPlayer(), usedShips);
		}
	}

	public static void allocateDefenders(final GameMap gameMap, Set<Ship> usedShips ) {
		for ( Ship ship : gameMap.getMyPlayer().getShips().values()) {
			if( ship.getAssignment() == null ) {
				// Available ship.  See if the closest planet needs a defender
			    Map<Double, Entity> map = gameMap.nearbyEntitiesByDistance(ship);
			    for (Map.Entry<Double, Entity> entry : map.entrySet())
			    {
			    	Entity e = entry.getValue();

			    	if( e.getClass() == Planet.class) {
			    		Planet planet = (Planet)e;
			    		if( planet.getOwner() == gameMap.getMyPlayerId()) {
			    			// Found a planet I might want to defend
			    			if( planet.getNumDefenders() < planet.getDockedShips().size()/4+1 ) {
			    				ship.setAssignment( new Defense( ship, planet ));
			    				planet.addDefender(ship);
			    				usedShips.add( ship );
			    			}
			    		}
			    		break;
			    	}
			    }
			}
		}
	}

}
