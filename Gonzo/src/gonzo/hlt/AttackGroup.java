package gonzo.hlt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AttackGroup {

	List<Ship> ships;
	Position position;
	int nInitialMoves = 1;
	public AttackGroup( Collection<Ship> ships) {
		this.ships = new ArrayList<Ship>( ships);
		position = calcPosition(); 
		for(Ship ship:ships) {
			ship.setAssignment(new AttackGroupAssignment( ship, this ));
		}
	}
	public Position calcPosition() {
		double xTotal = 0;
		double yTotal = 0;
		for( Ship ship : ships ) {
			xTotal += ship.getXPos();
			yTotal += ship.getYPos();
		}
		if( ships.size() > 0) {
			xTotal/= ships.size();
			yTotal/= ships.size();
		}
		return new Position(xTotal, yTotal );
	
	}
	
	public void removeDeadShips() {
        for (Iterator<Ship> iter = ships.iterator(); iter.hasNext(); ) { 
        	Ship ship = iter.next(); 
        	if( ship.getHealth() == 0 ) {
        		iter.remove();
        	}  
        }
	}
	
	public void chooseMove() {
		Log.log( "choose move");
		nInitialMoves--;
		removeDeadShips();
		position = calcPosition();
	}
	
	public Move getMove( GameMap gameMap, Ship ship ) {
		if( nInitialMoves >= 0 ) {
			Log.log( "Get Move Initial");
			if( ship.getYPos() > position.getYPos()) {
				return new ThrustMove( ship, 325, 4 );
			}else if( ship.getYPos() < position.getYPos()) {
				return new ThrustMove( ship, 35, 4 );				
			}else {
				return new ThrustMove( ship, 0, 4 );				
			}			
		}else {
			//Log.log( "Get Move Continue");
			Log.log("Group position = " + position );
			// Pick a target
			Map<Double, Entity> map = gameMap.nearbyEntitiesByDistance(position);
			for (Map.Entry<Double, Entity> entry : map.entrySet())
		    {
		    	Entity e = entry.getValue();
		    	if( e.getClass() == Ship.class) {
		    		Ship enemy = (Ship)e;
		    		if( enemy.getOwner() == gameMap.getMyPlayerId()) {
		    			continue;
		    		}
					Log.log("Enemy position = " + enemy.getXPos() + ", " + enemy.getYPos() );

		    		Move move = Navigation.navigateShipTowardsTarget(gameMap, ship, position, position.getClosestPoint(enemy), Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180.0);
		    		if( move != null ) {
		    			Log.log( "Move = " + move);
		    			return move;
		    		}
		    	}
		    }
		}
		return null;
	}
}
