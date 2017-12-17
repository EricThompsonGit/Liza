package frantic.hlt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Collection;
import java.util.Iterator;

public class GameMap {
    private final int width, height;
    private final int playerId;
    private final Map<Integer, Player> players;
    private final Map<Integer, Planet> planets;
    private final Map<Integer, Ship> allShips;

    // used only during parsing to reduce memory allocations
    //private final List<Ship> currentShips = new ArrayList<>();

    public GameMap(final int width, final int height, final int playerId) {
        this.width = width;
        this.height = height;
        this.playerId = playerId;
        players = new TreeMap<>();
        planets = new TreeMap<>();
        allShips = new TreeMap<>();
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int getMyPlayerId() {
//    	Log.log( "MyPlayerId = " + playerId);
        return playerId;
    }

    public Map<Integer, Player> getAllPlayers() {
        return players;
    }

    public Player getMyPlayer() {
//    	Log.log("  PlayerIds = " + players.keySet());
        return getAllPlayers().get(getMyPlayerId());
    }

    public Ship getShip(final int playerId, final int entityId) throws IndexOutOfBoundsException {
        return players.get(playerId).getShip(entityId);
    }

    public Planet getPlanet(final int entityId) {
        return planets.get(entityId);
    }

    public Map<Integer, Planet> getAllPlanets() {
        return planets;
    }

    public Map<Integer,Ship> getAllShips() {
        return allShips;
    }

    public ArrayList<Entity> objectsBetween(Position start, Position target) {
        final ArrayList<Entity> entitiesFound = new ArrayList<>();

        addEntitiesBetween(entitiesFound, start, target, planets.values());
        addEntitiesBetween(entitiesFound, start, target, allShips.values());

        return entitiesFound;
    }

    private static void addEntitiesBetween(final List<Entity> entitiesFound,
                                           final Position start, final Position target,
                                           final Collection<? extends Entity> entitiesToCheck) {

        for (final Entity entity : entitiesToCheck) {
            if (entity.equals(start) || entity.equals(target)) {
                continue;
            }
            if (Collision.segmentCircleIntersect(start, target, entity, Constants.FORECAST_FUDGE_FACTOR)) {
                entitiesFound.add(entity);
            }
        }
    }

    public Map<Double, Entity> nearbyEntitiesByDistance(final Entity entity) {
        final Map<Double, Entity> entityByDistance = new TreeMap<>();

        for (final Planet planet : planets.values()) {
            if (planet.equals(entity)) {
                continue;
            }
            entityByDistance.put(entity.getDistanceTo(planet), planet);
        }

        for (final Ship ship : allShips.values()) {
            if (ship.equals(entity)) {
                continue;
            }
            entityByDistance.put(entity.getDistanceTo(ship), ship);
        }

        return entityByDistance;
    }

    public Map<Double, Entity> nearbyEntitiesByDistance(Position pos) {
        final Map<Double, Entity> entityByDistance = new TreeMap<>();

        for (final Planet planet : planets.values()) {
            entityByDistance.put(pos.getDistanceTo(planet), planet);
        }

        for (final Ship ship : allShips.values()) {
            entityByDistance.put(pos.getDistanceTo(ship), ship);
        }

        return entityByDistance;
    }

    public GameMap updateMap(final Metadata mapMetadata) {
        final int numberOfPlayers = MetadataParser.parsePlayerNum(mapMetadata);

//        players.clear();
//        planets.clear();
//        allShips.clear();
        for( Ship ship : allShips.values()) {
        	ship.setHealth(0);
        }

        // update players info
        for (int i = 0; i < numberOfPlayers; ++i) {
            //currentShips.clear();
           // final Map<Integer, Ship> currentPlayerShips = new TreeMap<>();
            final int playerId = MetadataParser.parsePlayerId(mapMetadata);

            Player player = null;
            if( players.containsKey( playerId )) {
            	player = players.get( playerId );
            	player.clearShips();
            }else {
            	Log.log("Adding player " + playerId );
            	player = new Player( playerId );
            	players.put( playerId, player);
            }
            MetadataParser.updateShipList(allShips, player, mapMetadata);
            
        }

        for (Iterator<Map.Entry<Integer, Ship>> entries = allShips.entrySet().iterator(); entries.hasNext(); ) { 
        	Map.Entry<Integer, Ship> entry = entries.next(); 
        	if( entry.getValue().getHealth() == 0 ) {
        		entries.remove();
        	}  
        }

        final int numberOfPlanets = Integer.parseInt(mapMetadata.pop());

        for( Planet planet : planets.values()) {
        	planet.setHealth(0);
        }

        for (int i = 0; i < numberOfPlanets; ++i) {
        	MetadataParser.updatePlanetFromMetadata( planets, mapMetadata );
        }

        for (Iterator<Map.Entry<Integer, Planet>> entries = planets.entrySet().iterator(); entries.hasNext(); ) { 
        	Map.Entry<Integer, Planet> entry = entries.next(); 
        	if( entry.getValue().getHealth() == 0 ) {
        		entries.remove();
        	}  
        }

        if (!mapMetadata.isEmpty()) {
            throw new IllegalStateException("Failed to parse data from Halite game engine. Please contact maintainers.");
        }

        return this;
    }
}
