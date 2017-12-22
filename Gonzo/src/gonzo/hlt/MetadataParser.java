package gonzo.hlt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MetadataParser {


    public static void updateShipList(final Map<Integer, Ship> allShips, Player owner, final Metadata shipsMetadata) {
        final long numberOfShips = Long.parseLong(shipsMetadata.pop());

        
        for(int i = 0; i < numberOfShips; ++i) {
        	updateShipFromMetadata( allShips, owner, shipsMetadata );
        }

    }

    
    private static void updateShipFromMetadata(final Map<Integer, Ship> allShips, Player owner, final Metadata metadata) {
        final int id = Integer.parseInt(metadata.pop());
        final double xPos = Double.parseDouble(metadata.pop());
        final double yPos = Double.parseDouble(metadata.pop());
        final int health = Integer.parseInt(metadata.pop());
        // Ignoring velocity(x,y) which is always (0,0) in current version.
        metadata.pop();
        metadata.pop();

        final Ship.DockingStatus dockingStatus = Ship.DockingStatus.values()[Integer.parseInt(metadata.pop())];
        final int dockedPlanet = Integer.parseInt(metadata.pop());
        final int dockingProgress = Integer.parseInt(metadata.pop());
        final int weaponCooldown = Integer.parseInt(metadata.pop());

        if( allShips.containsKey( id )) {
        	Ship ship = allShips.get(id);
        	ship.setHealth( health );
        	ship.setPosition( xPos, yPos );
        	ship.setDockingStatus( dockingStatus );
        	ship.setDockedPlanet( dockedPlanet );
        	ship.setDockingProgress( dockingProgress );
        	ship.setWeaponCooldown( weaponCooldown );
        	owner.addShip( ship );
        }else {
        	Ship ship = new Ship(owner.getId(), id, xPos, yPos, health, dockingStatus, dockedPlanet, dockingProgress, weaponCooldown);
            allShips.put(id, ship);
            owner.addShip( ship );
        }

    }
	public static void updatePlanetFromMetadata( Map<Integer, Planet> planets, Metadata metadata ) {
        final int id = Integer.parseInt(metadata.pop());
        final double xPos = Double.parseDouble(metadata.pop());
        final double yPos = Double.parseDouble(metadata.pop());
        final int health = Integer.parseInt(metadata.pop());

        final double radius = Double.parseDouble(metadata.pop());
        final int dockingSpots = Integer.parseInt(metadata.pop());
        final int currentProduction = Integer.parseInt(metadata.pop());
        final int remainingProduction = Integer.parseInt(metadata.pop());

        final int hasOwner = Integer.parseInt(metadata.pop());
        final int ownerCandidate = Integer.parseInt(metadata.pop());
        final int owner;
        if (hasOwner == 1) {
            owner = ownerCandidate;
        } else {
            owner = -1; // ignore ownerCandidate
        }

        Planet planet = null;
        if( planets.containsKey(id)) {
        	planet = planets.get(id);
        	planet.setHealth(health);
        	planet.setOwner( owner );
        	if( planet.getXPos() != xPos ) {
        		Log.log("ERROR: Planet Changed xPos");        		
        	}
        	if( planet.getYPos() != yPos ) {
        		Log.log("ERROR: Planet Changed yPos");        		
        	}
        	if( planet.getRadius() != radius ) {
        		Log.log("ERROR: Planet Changed Size");
        	}
        	if( planet.getDockingSpots() != dockingSpots ) {
        		Log.log("ERROR: Planet Changed Number of Docking Spots");
        	}
        }else {
        	planet = new Planet(owner, id, xPos, yPos, health, radius, dockingSpots,
                          currentProduction, remainingProduction);
        	planets.put(id,  planet);
        }
        
        planet.clearDockedShipList();
        final int dockedShipCount = Integer.parseInt(metadata.pop());
        for (int i = 0; i < dockedShipCount; ++i) {
        	planet.addShip(Integer.parseInt(metadata.pop()));
        }

	}


    public static int parsePlayerNum(final Metadata metadata) {
        return Integer.parseInt(metadata.pop());
    }

    public static int parsePlayerId(final Metadata metadata) {
        return Integer.parseInt(metadata.pop());
    }
}
