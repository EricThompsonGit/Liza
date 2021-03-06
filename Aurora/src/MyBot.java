import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import hlt.*;

public class MyBot {

    public static void main(final String[] args) {
        final Networking networking = new Networking();
        final GameMap gameMap = networking.initialize("Aurora");

        // We now have 1 full minute to analyse the initial map.
        final String initialMapIntelligence =
                "width: " + gameMap.getWidth() +
                "; height: " + gameMap.getHeight() +
                "; players: " + gameMap.getAllPlayers().size() +
                "; planets: " + gameMap.getAllPlanets().size();
        Log.log(initialMapIntelligence);

        final ArrayList<Move> moveList = new ArrayList<>();
        
        final int myId = gameMap.getMyPlayerId();
        Log.log( "My Id = " + myId );
        
        for (;;) {
            moveList.clear();
            networking.updateMap(gameMap);
            
            // Make sure we give each ship something useful to do
            Set<Ship> usedShips = new HashSet<Ship>();
            Set<Planet> claimedPlanets = new HashSet<Planet>();

            for (final Ship ship : gameMap.getMyPlayer().getShips().values()) {
                if (ship.getDockingStatus() != Ship.DockingStatus.Undocked) {
                	usedShips.add( ship );
                    continue;
                }


                for (final Planet planet : gameMap.getAllPlanets().values()) {
                	if( claimedPlanets.contains(planet)) {
                		continue;
                	}
                    if (planet.isOwned()) {
                    	claimedPlanets.add( planet );
                        continue;
                    }

                    if (ship.canDock(planet)) {
                        moveList.add(new DockMove(ship, planet));
                        usedShips.add( ship );
                        claimedPlanets.add( planet );
                     	Log.log("Docking ship " + ship.getId() + " to planet " + planet.getId());         
                        break;
                    }

                    final ThrustMove newThrustMove = Navigation.navigateShipToDock(gameMap, ship, planet, Constants.MAX_SPEED);
                    if (newThrustMove != null) {
                        moveList.add(newThrustMove);
                        usedShips.add(ship);
                        claimedPlanets.add( planet );
                        Log.log("Navigating ship " + ship.getId() + " to planet " + planet.getId());

                    }

                    break;
                }
            }
            // We have ships moving to all available planets.
            // If there are any more ships, see if they can dock where they are
            for (final Ship ship : gameMap.getMyPlayer().getShips().values()) {
            	if( usedShips.contains(ship)) {
            		continue;
            	}
            	                
                for (final Planet planet : gameMap.getAllPlanets().values()) {

                    if (ship.canDock(planet) && ( planet.getDockingSpots()> planet.getDockedShips().size())) {
                        moveList.add(new DockMove(ship, planet));
                        usedShips.add( ship );
                    	Log.log("Docking extra ship " + ship.getId() + " to planet " + planet.getId());
                        break;
                    }
                }
            }

            // We are doing max mining.  Use any remaining ships to go after enemy planets, one at a time
            for (final Planet planet : gameMap.getAllPlanets().values()) {
            	if( planet.getOwner() != gameMap.getMyPlayerId() ) {
            		// Found an enemy planet
                    for (final Ship ship : gameMap.getMyPlayer().getShips().values()) {
                    	if( usedShips.contains(ship)) {
                    		continue;
                    	}
                    	// This ship is available
                    	final ThrustMove newThrustMove = Navigation.navigateShipTowardsTarget(gameMap, ship, planet.getClosestPoint(ship), Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180.0);
                        if (newThrustMove != null) {
                            moveList.add(newThrustMove);
                        	Log.log("Using ship " + ship.getId() + " to attack planet " + planet.getId());
                        }

                    }
                    break;
            	}
            }

            
            Networking.sendMoves(moveList);
        }
    }
}
