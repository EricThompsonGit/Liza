package gonzo.hlt;

import java.util.ArrayList;
import java.util.Set;

public class Assignment {

	protected Ship ship;
	//public int nTargetId;
	
	public Assignment( Ship ship ) {
		this.ship = ship;
	}
	
	public Ship getShip() {
		return ship;
	}
	public Move getMove( GameMap gameMap, ArrayList<Move> moveList, Set<Ship> usedShips, Set<Planet> claimedPlanets) {
		return null;
	}
	
	public Assignment isValid(GameMap gameMap) {
		return null;
	}
	public Planet getPlanet() {
		return null;
	}
}
