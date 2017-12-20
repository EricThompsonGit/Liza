package frantic;

import java.util.ArrayList;
import java.util.Set;

import frantic.hlt.Assignment;
import frantic.hlt.Constants;
import frantic.hlt.DockMove;
import frantic.hlt.GameMap;
import frantic.hlt.Log;
import frantic.hlt.Move;
import frantic.hlt.Navigation;
import frantic.hlt.Planet;
import frantic.hlt.Ship;
import frantic.hlt.ThrustMove;
import frantic.hlt.Ship.DockingStatus;

public class AttackMiner extends Assignment {

	private Planet planet;
	private Ship enemy;
	
	//int nPlanet = 0;
	public AttackMiner(Ship ship, Ship miner, Planet planet) {
		super(ship);
		this.planet = planet;
		this.enemy = miner;
		// TODO Auto-generated constructor stub
	}

	public Planet getPlanet() {
		return planet;
	}
	public Assignment isValid(GameMap gameMap) {
		// Check that the ship still exists, and that the planet is still unclaimed
		if( ship == null ) return null;
		if( planet == null  ) {
			return null;
		}
		if( planet.isOwned() && (planet.getOwner() == gameMap.getMyPlayerId())) return null;

		
		if( enemy == null || enemy.getHealth() == 0 ) {
			// The enemy ship we were sent to attack is gone.  See if there is another one
			if(( planet == null )  || ( planet.getHealth() == 0 )) {
				ship.setAssignment(null);
				return null;
			}
			if( planet.isOwned() ) {
				if( planet.getOwner() != gameMap.getMyPlayerId()) {
					// Someone else owns this planet, so attack them
					if( planet.getDockedShips().size() > 0 ) {
						int enemyId = planet.getDockedShips().get(0);
						Ship enemy = gameMap.getAllShips().get(enemyId);
						ship.setAssignment(new AttackMiner( ship, enemy, planet ));
						return ship.getAssignment();
					}else {
						// I own this planet, add another miner if there is room
						if( planet.getDockingSpots() > planet.getDockedShips().size()) {
							ship.setAssignment(new Mine( ship, planet ));
							return ship.getAssignment();
						}
					}
				}
				ship.setAssignment(null);
				return null;
			}
			else {
				ship.setAssignment(new Mine( ship, planet ));
				return ship.getAssignment();
			}
		}
		if( enemy.getDockingStatus() != DockingStatus.Undocked) {				
			return this;
		}
				
		return this;
	}
	
 	public Move getMove( GameMap gameMap, ArrayList<Move> moveList, Set<Ship> usedShips, Set<Planet> claimedPlanets) {

 		if( enemy == null)  {
 			Log.log("Attack Miner: enemy invalid" );
 		}
 		if( ship == null)  {
 			Log.log("Attack Miner: ship invalid" );
 		}
 		if( planet == null)  {
 			Log.log("Attack Miner: planet invalid" );
 			return null;
 		}

 	 	if(( enemy != null) && ( ship != null )) {
 	 		ThrustMove move = null;
 	 		if( ship.canDock(planet)) {
 	 			// We are close
 		 		move = Navigation.navigateShipTowardsTarget(gameMap, ship, ship.getClosestPoint(enemy), Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180.0);
 	 		}else {
 		 		move = Navigation.navigateShipTowardsTarget(gameMap, ship, ship.getClosestPoint(enemy), Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180.0);
 	 			//move = Navigation.navigateShipToDock(gameMap, ship, planet, Constants.MAX_SPEED);
 	 		}
	        if (move != null) {
	            usedShips.add(ship);
	            Log.log("Navigating ship " + ship.getId() + " to miner " + enemy.getId());
	            Log.log("Thrust Move " + move.getThrust());
	            return move;
	
	        }
        }
        return null;
 	}

    @Override
    public String toString() {
        return "AttackMiner[" +
                ", ship=" + ship.getId() +
                ", planet=" + planet.getId() +
                ", miner=" + enemy.getId() +
                "]";
    }

}
