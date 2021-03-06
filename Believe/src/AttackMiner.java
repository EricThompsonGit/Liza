import java.util.ArrayList;
import java.util.Set;

import hlt.Constants;
import hlt.DockMove;
import hlt.GameMap;
import hlt.Log;
import hlt.Move;
import hlt.Navigation;
import hlt.Planet;
import hlt.Ship;
import hlt.Ship.DockingStatus;
import hlt.ThrustMove;

public class AttackMiner extends Assignment {

	public AttackMiner(int shipId, int target) {
		super(shipId, target);
		// TODO Auto-generated constructor stub
	}

	boolean IsValid(GameMap gameMap) {
		// Check that the ship still exists, and that the planet is still unclaimed
		Ship ship = FindShip( gameMap );
		if( ship == null ) return false;
		Ship miner = FindOtherShip( gameMap );
		if( miner == null ) return false;
		if( miner.getDockingStatus() != DockingStatus.Undocked) {				
			return true;
		}
		return false;
	}
	
 	Move GetMove( GameMap gameMap, ArrayList<Move> moveList, Set<Ship> usedShips, Set<Planet> claimedPlanets) {
 		Ship ship = FindShip(gameMap );
 		Ship miner = FindOtherShip( gameMap );

 		if( miner == null)  {
 			Log.log("Attack Miner: miner invalid" );
 		}
 		if( ship == null)  {
 			Log.log("Attack Miner: ship invalid" );
 		}

 	 	if(( miner != null) && ( ship != null )) {
	 		ThrustMove newThrustMove = Navigation.navigateShipTowardsTarget(gameMap, ship, miner.getClosestPoint(ship), Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180.0);
	       // final ThrustMove newThrustMove = Navigation.navigateShipToDock(gameMap, ship, planet, Constants.MAX_SPEED);
	        if (newThrustMove != null) {
	            usedShips.add(ship);
	            Log.log("Navigating ship " + ship.getId() + " to miner " + miner.getId());
	            return newThrustMove;
	
	        }
        }
        return null;
 	}

}
