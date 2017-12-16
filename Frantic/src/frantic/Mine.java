package frantic;

import java.util.ArrayList;
import java.util.Set;

import frantic.hlt.DockMove;
import frantic.hlt.GameMap;
import frantic.hlt.Log;
import frantic.hlt.Move;
import frantic.hlt.Planet;
import frantic.hlt.Ship;
import frantic.hlt.Ship.DockingStatus;

public class Mine extends Assignment {

	public Mine(int shipId, int target) {
		super(shipId, target);
		// TODO Auto-generated constructor stub
	}
	
	Assignment IsValid(GameMap gameMap) {
		// Check that the ship still exists, and that the planet is still unclaimed
		Ship ship = FindShip( gameMap );
		if( ship == null ) {
			Log.log("Mine Invalid, can't find ship");
			return null;
		}
		Planet planet = FindPlanet( gameMap );
		if( planet == null ) {
			Log.log("Mine Invalid, can't find planet");
			return null;
		}
		if( planet.isOwned() && ( planet.getOwner() != gameMap.getMyPlayerId())) {
			Log.log("Mine Invalid, planet owned by someone else");
			return null;
		}
		if ((ship.getDockingStatus() == DockingStatus.Undocked) &&(planet.getDockingSpots()==planet.getDockedShips().size())) {
			return null;			
		}
		if( !ship.canDock(planet)) {
			return null;
		}
		
		return this;
	}

	Move GetMove( GameMap gameMap, ArrayList<Move> moveList, Set<Ship> usedShips, Set<Planet> claimedPlanets) {
		Ship ship = FindShip( gameMap );
		Planet planet = FindPlanet( gameMap );
        if ((ship.getDockingStatus() == DockingStatus.Undocked) &&ship.canDock(planet)) {
            Move move =new DockMove(ship, planet);
            usedShips.add( ship );
            claimedPlanets.add( planet );
         	Log.log("Docking ship " + ship.getId() + " to planet " + planet.getId());         
            return move;
        }

		// Nothing to do
		return null;
	}
	
	Planet FindPlanet( GameMap gameMap ) {
        for (final Planet planet : gameMap.getAllPlanets().values()) {
            if (planet.getId() == nTargetId ) {
            	return planet;
            }
        }
		 return null;
	}
    @Override
    public String toString() {
        return "Mine[" +
                ", ship=" + nShipId +
                ", planet=" + nTargetId +
                "]";
    }

}
