package gonzo.hlt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class Planet extends Entity {

    private int remainingProduction;
    private int currentProduction;
    private int dockingSpots;
    private List<Integer> dockedShips;
    private Set<Ship> defendingShips;

    public Planet(final int owner, final int id, final double xPos, final double yPos, final int health,
                  final double radius, final int dockingSpots, final int currentProduction,
                  final int remainingProduction, final List<Integer> dockedShips) {

        super(owner, id, xPos, yPos, health, radius);

        this.dockingSpots = dockingSpots;
        this.currentProduction = currentProduction;
        this.remainingProduction = remainingProduction;
        this.dockedShips = Collections.unmodifiableList(dockedShips);
        defendingShips = new HashSet<Ship>();
    }
    public Planet(final int owner, final int id, final double xPos, final double yPos, final int health,
            final double radius, final int dockingSpots, final int currentProduction,
            final int remainingProduction) {

		  super(owner, id, xPos, yPos, health, radius);
		
		  this.dockingSpots = dockingSpots;
		  this.currentProduction = currentProduction;
		  this.remainingProduction = remainingProduction;
		  dockedShips = new ArrayList<Integer>();
	      defendingShips = new HashSet<Ship>();
	}

    public void addDefender( Ship ship ) {
    	defendingShips.add(ship);   	
    }
    
    public int getNumDefenders() {
    	return defendingShips.size();
    }
    
    public void cleanUpDefenders(Player me, Set<Ship> usedShips ) {
    	if( getOwner() != me.getId () ) {
    		defendingShips.clear();
    		return;
    	}
    	for (Iterator<Ship> iterator = defendingShips.iterator(); iterator.hasNext();) {
    	    Ship ship = iterator.next();
    	    if (ship.getHealth() == 0 ) {
    	        // Remove the current element from the iterator and the list.
    	        iterator.remove();
    	    }else {
    	    	usedShips.add( ship );
    	    }
    	}
    }
    
    public void removeDefenders( Ship defender) {
    	for (Iterator<Ship> iterator = defendingShips.iterator(); iterator.hasNext();) {
    	    Ship ship = iterator.next();
    	    if (ship == defender ) {
    	        // Remove the current element from the iterator and the list.
    	        iterator.remove();
    	    }
    	}
    }
    
    public int getRemainingProduction() {
        return remainingProduction;
    }

    public int getCurrentProduction() {
        return currentProduction;
    }

    public int getDockingSpots() {
        return dockingSpots;
    }

    public List<Integer> getDockedShips() {
        return dockedShips;
    }

    public boolean isFull() {
        return dockedShips.size() == dockingSpots;
    }

    public boolean isOwned() {
        return getOwner() != -1;
    }

    @Override
    public String toString() {
        return "Planet[" +
                super.toString() +
                ", remainingProduction=" + remainingProduction +
                ", currentProduction=" + currentProduction +
                ", dockingSpots=" + dockingSpots +
                ", dockedShips=" + dockedShips +
                "]";
    }


	public void clearDockedShipList() {
		dockedShips.clear();
	}

	public void addShip(int id) {
		dockedShips.add(id);
	}
	
	public void allocateDefensiveShips(GameMap gameMap, ArrayList<Move> moveList ) {
		if( getNumDefenders() == 0 ) {
			return;
		}
		List<Ship> availableShips = new ArrayList<Ship>();
		for( Ship ship : defendingShips ) {
			availableShips.add( ship );
		}

	    Map<Double, Entity> map = gameMap.nearbyEntitiesByDistance(this);
	    for (Map.Entry<Double, Entity> entry : map.entrySet())
	    {
	    	Entity e = entry.getValue();
	    	double dist = Math.sqrt(Math.pow( (e.getXPos()-getXPos()), 2) + Math.pow( (e.getYPos()-getYPos()), 2) );
	    	dist -= getRadius();
	    	if( dist > 20 ) {
	    		// Ignore far away ships
	    		break;
	    	}
	    	if( e.getClass() == Ship.class) {
	    		Ship enemy = (Ship)e;
	    		if( enemy.getOwner() != gameMap.getMyPlayerId()) {
	    			// found an enemy ship.  Find the closest defender
	    			Ship closest = null;
	    			double dMinDist = Double.MAX_VALUE;
	    			for( Ship defender : availableShips ) {
	    				double distToEnemy = defender.getDistanceTo(enemy);
	    				if( distToEnemy < dMinDist ) {
	    					dMinDist = distToEnemy;
	    					closest = defender;	    					
	    				}
	    			}
	    			if( closest != null ) {
	    				// TODO - aim for the first intersect point, so we don't chase
	    				Position targetPos = enemy.getNextPosition();
	    				Move m = Navigation.navigateShipTowardsTarget(gameMap, closest, targetPos, Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180.0);
	    				if( m != null ) {
	    					moveList.add(m);
	    					availableShips.remove(closest);
	    				}
	    			}
	    		}
	    	}
	    	
	    }
	    // TODO- Move unused ships to good defensive positions
	    
	}

}
