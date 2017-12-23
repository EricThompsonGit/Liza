package gonzo.hlt;

import java.util.ArrayList;
import java.util.Set;

import gonzo.AttackMiner;
import gonzo.Mine;
import gonzo.hlt.Ship.DockingStatus;

public class AttackShip extends Assignment {

		private Ship enemy;
		private int speedToUse;
		
		//int nPlanet = 0;
		public AttackShip(Ship ship, Ship enemy, int initialSpeed ) {
			super(ship);
			this.enemy = enemy;
			this.speedToUse = initialSpeed;
		}

		public Assignment isValid(GameMap gameMap) {
			// Check that the ship still exists, and that the planet is still unclaimed
			if( ship == null ) return null;
			
			if( enemy.getDockingStatus() != DockingStatus.Undocked) {				
				return this;
			}
					
			return this;
		}
		
	 	public Move getMove( GameMap gameMap, ArrayList<Move> moveList, Set<Ship> usedShips, Set<Planet> claimedPlanets) {

	 		if( enemy == null)  {
	 			Log.log("Attack Ship: enemy invalid" );
	 		}
	 		if( ship == null)  {
	 			Log.log("Attack Ship: ship invalid" );
	 		}

	 	 	if(( enemy != null) && ( ship != null )) {
	 	 		ThrustMove move = null;
	 		 	move = Navigation.navigateShipTowardsTarget(gameMap, ship, enemy.getNextPosition(), speedToUse, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180.0);
		        if (move != null) {
		        	speedToUse = Constants.MAX_SPEED;
		            usedShips.add(ship);
		            Log.log("Navigating ship " + ship.getId() + " to enemy " + enemy.getId());
		            Log.log("Thrust Move " + move.getThrust());
		            return move;
		
		        }
	        }
	        return null;
	 	}

	    @Override
	    public String toString() {
	        return "AttackShip[" +
	                ", ship=" + ship.getId() +
	                ", minerenemy=" + enemy.getId() +
	                "]";
	    }

}
