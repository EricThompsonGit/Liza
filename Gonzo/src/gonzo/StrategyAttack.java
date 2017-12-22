package gonzo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import gonzo.hlt.AttackGroup;
import gonzo.hlt.GameMap;
import gonzo.hlt.Move;
import gonzo.hlt.Planet;
import gonzo.hlt.Ship;

public class StrategyAttack extends Strategy {

	AttackGroup group;
	@Override
	public void getInitialAssignments(GameMap gameMap) {
		group = new AttackGroup( gameMap.getMyPlayer().getShips().values());		
	}

	@Override
	public void calculateMoves(GameMap gameMap, ArrayList<Move> moveList) {
		group.chooseMove();
		
		Set<Ship> usedShips = new HashSet<Ship>();
		Set<Planet> claimedPlanets = new HashSet<Planet>();
		for( Ship ship : gameMap.getMyPlayer().getShips().values()) {
			Move move = ship.getAssignment().getMove( gameMap, moveList, usedShips, claimedPlanets);
			if( move != null) {
				moveList.add( move );
			}				
		}
	}

}
