package frantic;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import frantic.hlt.Assignment;
import frantic.hlt.DockMove;
import frantic.hlt.Entity;
import frantic.hlt.GameMap;
import frantic.hlt.Log;
import frantic.hlt.Move;
import frantic.hlt.Planet;
import frantic.hlt.Position;
import frantic.hlt.Ship;
import frantic.hlt.Ship.DockingStatus;

public class Mine extends Assignment {

	private Planet planet;
	public Mine(Ship ship, Planet planet) {
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
			int enemyId = planet.getDockedShips().get(0);
			Ship enemy = gameMap.getAllShips().get(enemyId);
			return new AttackMiner( ship, enemy, planet );
		}
		Ship nearEnemy = getNearEnemy( gameMap );
		if( nearEnemy != null ) {
			return new AttackMiner( ship, nearEnemy, planet );
	
		}
		if ((ship.getDockingStatus() == DockingStatus.Undocked) &&(planet.getDockingSpots()==planet.getDockedShips().size())) {
			return null;			
		}
		if( !ship.canDock(planet)) {
			return null;
		}
		
		return this;
	}

	public Move getMove( GameMap gameMap, ArrayList<Move> moveList, Set<Ship> usedShips, Set<Planet> claimedPlanets) {
        if ((ship.getDockingStatus() == DockingStatus.Undocked) &&ship.canDock(planet)) {
        	// See if an enemy is about to dock.  If so, wait, then kill them
        	if( getNearEnemy(gameMap)!= null ) {
        		return null;
        	}
        	
            Move move =new DockMove(ship, planet);
            usedShips.add( ship );
            claimedPlanets.add( planet );
         	Log.log("Docking ship " + ship.getId() + " to planet " + planet.getId());         
            return move;
        }

		// Nothing to do
		return null;
	}
	
	public Ship getNearEnemy( GameMap gameMap ) {
	    Map<Double, Entity> map = gameMap.nearbyEntitiesByDistance(planet);
	    for (Map.Entry<Double, Entity> entry : map.entrySet())
	    {
	    	Entity e = entry.getValue();		    	
	    	if( e.getClass() == Ship.class) {
	    		Ship nearShip = (Ship)e;
	    		if( nearShip.getOwner() != gameMap.getMyPlayerId()) {
	    			// Check where this ship will likely be next turn	    		
	    			Position targetPos = nearShip.getNextPosition();
	    			double dist = Math.sqrt(Math.pow( (targetPos.getXPos()-planet.getXPos()), 2) + Math.pow( (targetPos.getYPos()-planet.getYPos()), 2) );
	    			dist -= planet.getRadius();
			    	if( dist <= 4) {
			    		return nearShip;
			    	}
			    	else
			    	{
			    		return null;
			    	}
	    		}
	    	}
	    }
	    return null;
	}
	
    @Override
    public String toString() {
        return "Mine[" +
                ", ship=" + ship.getId() +
                ", planet=" + planet.getId() +
                "]";
    }

}
