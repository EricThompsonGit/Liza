package gonzo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gonzo.hlt.AttackGroup;
import gonzo.hlt.AttackShip;
import gonzo.hlt.Entity;
import gonzo.hlt.GameMap;
import gonzo.hlt.Move;
import gonzo.hlt.Planet;
import gonzo.hlt.Player;
import gonzo.hlt.Position;
import gonzo.hlt.Ship;
import gonzo.hlt.ThrustMove;
import gonzo.hlt.Util;

public class StrategyAttackVaried extends Strategy {
	AttackGroup group = null;
	int nTurn = 0;
	Player opponent;

	@Override
	public void getInitialAssignments(GameMap gameMap) {
		for( Player player : gameMap.getAllPlayers().values()) {
			if( player != gameMap.getMyPlayer()) {
				opponent = player;
				break;
			}
			
		}
		return;

	}

	@Override
	public void calculateMoves(GameMap gameMap, ArrayList<Move> moveList) {
		nTurn++;
		if( nTurn == 1 ) {
			// First move.  Move toward opponent, if that will not cause collisions
			Position me = getAveragePosition( gameMap.getMyPlayer());
			Position them = getAveragePosition( opponent );
			double dx = them.getXPos() - me.getXPos();
			double dy = them.getYPos() - me.getYPos();
			double dist = Math.sqrt( dx*dx+dy*dy);
			if( dist != 0 ) {
				dx /= dist;
				dy /= dist;
			}
			dx *= 7;
			dy *= 7;
			// Check if safe for each ship
			boolean safe = true;
			for( Ship ship : gameMap.getMyPlayer().getShips().values()) {
				Position nextPos = new Position( ship.getXPos()+dx, ship.getYPos()+dy);
				if( !gameMap.planetsBetween(me, nextPos).isEmpty() ) {
					safe = false;
					break;
				}
			}
			if( safe ) {
				for( Ship ship : gameMap.getMyPlayer().getShips().values()) {
					Position nextPos = new Position( ship.getXPos()+dx, ship.getYPos()+dy);
					double angleRad = ship.orientTowardsInRad(nextPos);
					int angleDeg = Util.angleRadToDegClipped(angleRad);
					Move move = new ThrustMove( ship, angleDeg, 7 );
					moveList.add(move);
				}
				
			}
			return;
			
		}else if( nTurn == 2 ) {
			// See if we can figure out what our opponent is doing based on their opening move
			if( opponent.getShips().size() == 1 ) {
				// Two of their ships collided. Attack the remaining one as a group
				group = new AttackGroup( gameMap.getMyPlayer().getShips().values());
			}else if( opponent.getShips().size() == 2 ) {
				// Lost one ship?!?!?
				group = new AttackGroup( gameMap.getMyPlayer().getShips().values());
			}else {
				Iterator<Ship> iter =  opponent.getShips().values().iterator();
				Ship opponent1 = iter.next();
				Ship opponent2 = iter.next();
				Ship opponent3 = iter.next();
				double dist = opponent1.getDistanceTo(opponent2)+opponent2.getDistanceTo(opponent3)+opponent3.getDistanceTo(opponent1);
				if( dist >= 12.1 ) {
					// They start with a total distance of 12 apart. They have since moved apart, so send one ship to attack each
					Iterator<Ship> iterMe =  gameMap.getMyPlayer().getShips().values().iterator();
					Ship me1 = iterMe.next();
					Ship me2 = iterMe.next();
					Ship me3 = iterMe.next();
					if( me1.getYPos()<me2.getYPos()) { Ship tmp = me2; me2 = me1; me1 = tmp; }
					if( me2.getYPos()<me3.getYPos()) { Ship tmp = me3; me3 = me2; me2 = tmp; }
					if( me1.getYPos()<me2.getYPos()) { Ship tmp = me2; me2 = me1; me1 = tmp; }
					if( opponent1.getYPos()<opponent2.getYPos()) { Ship tmp = opponent2; opponent2 = opponent1; opponent1 = tmp; }
					if( opponent2.getYPos()<opponent3.getYPos()) { Ship tmp = opponent3; opponent3 = opponent2; opponent2 = tmp; }
					if( opponent1.getYPos()<opponent2.getYPos()) { Ship tmp = opponent2; opponent2 = opponent1; opponent1 = tmp; }
					me1.setAssignment( new AttackShip( me1, opponent1, 7 ));
					me2.setAssignment( new AttackShip( me2, opponent2, 5 ));
					me3.setAssignment( new AttackShip( me3, opponent3, 3 ));
				}else {
					// They stayed together, either to mine the same planet, or to attack me.  In either case, attack as a group
					group = new AttackGroup( gameMap.getMyPlayer().getShips().values());
				}
			}
		}
		if( group != null ) {
			group.chooseMove();
		}
		
		Set<Ship> usedShips = new HashSet<Ship>();
		Set<Planet> claimedPlanets = new HashSet<Planet>();
		for( Ship ship : gameMap.getMyPlayer().getShips().values()) {
			Move move = ship.getAssignment().getMove( gameMap, moveList, usedShips, claimedPlanets);
			if( move != null) {
				moveList.add( move );
			}				
		}
		if( usedShips.size() < gameMap.getMyPlayer().getShips().size()) {
			// We have a ship with nothing to do.  Figure out which ships are available
			for( Ship ship : gameMap.getMyPlayer().getShips().values()) {
				if( !usedShips.contains(ship)) {
					// find closest enemy ship
					Map<Double, Entity> map = gameMap.nearbyEntitiesByDistance(ship);
					for (Map.Entry<Double, Entity> entry : map.entrySet())
				    {
				    	Entity e = entry.getValue();
				    	if( e.getClass() == Ship.class) {
				    		Ship enemy = (Ship)e;
				    		if( ship.getOwner()  == gameMap.getMyPlayerId()) {
				    			continue;
				    		}
				    		ship.setAssignment(new AttackShip( ship, enemy, 7 ));
							Move move = ship.getAssignment().getMove( gameMap, moveList, usedShips, claimedPlanets);
							if( move != null) {
								moveList.add( move );
							}		
				    	}
				    }
				}
			}
		}

	}
	
	public Position getAveragePosition(Player player) {
		double x = 0;
		double y = 0;
		for( Ship ship : player.getShips().values()) {
			x += ship.getXPos();
			y += ship.getYPos();
		}
		int numShips = player.getShips().size();
		if( numShips> 0) {
			x /= numShips;
			y /= numShips;
		}
		return new Position(x,y);
	}

}
