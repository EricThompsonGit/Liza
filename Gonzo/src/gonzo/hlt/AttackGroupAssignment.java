package gonzo.hlt;

import java.util.ArrayList;
import java.util.Set;

public class AttackGroupAssignment extends Assignment {
	private AttackGroup group;
	public AttackGroupAssignment(Ship ship, AttackGroup group) {
		super(ship);
		this.group = group;
		// TODO Auto-generated constructor stub
	}

	public Move getMove( GameMap gameMap, ArrayList<Move> moveList, Set<Ship> usedShips, Set<Planet> claimedPlanets) {
		usedShips.add(ship);
		return group.getMove(gameMap, ship);
	}
	
	public Assignment isValid(GameMap gameMap) {
		return this;
	}

}
