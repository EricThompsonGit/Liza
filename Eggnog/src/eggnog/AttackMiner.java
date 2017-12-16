package eggnog;

import java.util.ArrayList;
import java.util.Set;

import eggnog.hlt.Constants;
import eggnog.hlt.DockMove;
import eggnog.hlt.GameMap;
import eggnog.hlt.Log;
import eggnog.hlt.Move;
import eggnog.hlt.Navigation;
import eggnog.hlt.Planet;
import eggnog.hlt.Ship;
import eggnog.hlt.ThrustMove;
import eggnog.hlt.Ship.DockingStatus;

public class AttackMiner extends Assignment {

	int nPlanet = 0;
	public AttackMiner(int shipId, int miner, int planet) {
		super(shipId, miner);
		nPlanet = planet;
		// TODO Auto-generated constructor stub
	}

	Assignment IsValid(GameMap gameMap) {
		// Check that the ship still exists, and that the planet is still unclaimed
		Ship ship = FindShip( gameMap );
		if( ship == null ) return null;
		if( ship.getOwner() == gameMap.getMyPlayerId()) return null;
		
		Ship miner = FindOtherShip( gameMap );
		if( miner == null ) {
			// The enemy ship we were sent to attack is gone.  See if there is another one
			Planet planet = FindPlanet( gameMap, nPlanet );
			if( planet == null ) {
				return null;
			}
			if( planet.isOwned() ) {
				if(  planet.getOwner() == gameMap.getMyPlayerId()) {
					Log.log("Claim Planet Invalid, I own this planet");
					return null;
				}
				// Someone else owns this planet, so attack them
				if( planet.getDockedShips().size() > 0 ) {
					int enemy = planet.getDockedShips().get(0);
					return new AttackMiner( nShipId, enemy, nPlanet );
				}
				return null;
			}
			else {
				return new Mine( nShipId, nPlanet );
			}
		}
		if( miner.getDockingStatus() != DockingStatus.Undocked) {				
			return this;
		}
				
		return this;
	}
	
 	Move GetMove( GameMap gameMap, ArrayList<Move> moveList, Set<Ship> usedShips, Set<Planet> claimedPlanets) {
 		Ship ship = FindShip(gameMap );
 		Ship miner = FindOtherShip( gameMap );
 		Planet planet = FindPlanet( gameMap, nPlanet );

 		if( miner == null)  {
 			Log.log("Attack Miner: miner invalid" );
 		}
 		if( ship == null)  {
 			Log.log("Attack Miner: ship invalid" );
 		}
 		if( planet == null)  {
 			Log.log("Attack Miner: planet invalid" );
 			return null;
 		}

 	 	if(( miner != null) && ( ship != null )) {
 	 		ThrustMove move = null;
 	 		if( ship.canDock(planet)) {
 	 			// We are close
 		 		move = Navigation.navigateShipTowardsTarget(gameMap, ship, ship.getClosestPoint(miner), Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180.0);
 	 		}else {
 		 		move = Navigation.navigateShipTowardsTarget(gameMap, ship, ship.getClosestPoint(miner), Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180.0);
 	 			//move = Navigation.navigateShipToDock(gameMap, ship, planet, Constants.MAX_SPEED);
 	 		}
	        if (move != null) {
	            usedShips.add(ship);
	            Log.log("Navigating ship " + ship.getId() + " to miner " + miner.getId());
	            Log.log("Thrust Move " + move.getThrust());
	            return move;
	
	        }
        }
        return null;
 	}
	Planet FindPlanet( GameMap gameMap, int nPlanetId ) {
        for (final Planet planet : gameMap.getAllPlanets().values()) {
            if (planet.getId() == nPlanetId ) {
            	return planet;
            }
        }
		 return null;
	}

}
