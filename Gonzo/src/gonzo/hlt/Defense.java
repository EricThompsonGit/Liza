package gonzo.hlt;

import java.util.ArrayList;
import java.util.Set;

import gonzo.hlt.Ship.DockingStatus;

public class Defense extends Assignment {
	private Planet planet;
	public Defense(Ship ship, Planet planet) {
		super(ship);
		this.planet = planet;
		// TODO Auto-generated constructor stub
	}
	public Planet getPlanet() {
		return planet;
	}

	public Assignment isValid(GameMap gameMap) {
		// Check that the ship still exists, and that the planet is still unclaimed
		if( ship == null ) {
			Log.log("Mine Invalid, can't find ship");
			return null;
		}
		if( planet == null ) {
			Log.log("Mine Invalid, can't find planet");
			return null;
		}
		if( planet.isOwned() && ( planet.getOwner() != gameMap.getMyPlayerId())) {
			Log.log("Mine Invalid, planet owned by someone else");
			return null;
		}
		if ((ship.getDockingStatus() == DockingStatus.Docked) ) {
			return null;			
		}
		
		return this;
	}

	public Move getMove( GameMap gameMap, ArrayList<Move> moveList, Set<Ship> usedShips, Set<Planet> claimedPlanets) {

		// The planet we are defending will allocate our moves
		return null;
	}
	

    @Override
    public String toString() {
        return "Defense[" +
                ", ship=" + ship.getId() +
                ", planet=" + planet.getId() +
                "]";
    }

}
