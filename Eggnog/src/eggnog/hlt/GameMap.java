package eggnog.hlt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Collection;

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
        return playerId;
    }

    public Map<Integer, Player> getAllPlayers() {
        return players;
    }

    public Player getMyPlayer() {
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
            	player = new Player( playerId );
            }
            MetadataParser.updateShipList(allShips, player, mapMetadata);
            
//            for (final Ship ship : currentShips) {
//                currentPlayerShips.put(ship.getId(), ship);
//            }
//            players.add(currentPlayer);
        }
        for( Integer id : allShips.keySet()) {
        	Ship ship = allShips.get( id );
        	if( ship.getHealth() == 0 ) {
        		allShips.remove( id );
        	}
        }

        final int numberOfPlanets = Integer.parseInt(mapMetadata.pop());

        for( Planet planet : planets.values()) {
        	planet.setHealth(0);
        }

        for (int i = 0; i < numberOfPlanets; ++i) {
//            final List<Integer> dockedShips = new ArrayList<>();
//            final Planet planet = MetadataParser.newPlanetFromMetadata(dockedShips, mapMetadata);
//            planets.put(planet.getId(), planet);
        	MetadataParser.updatePlanetFromMetadata( planets, mapMetadata );
        }
        for( Integer id : planets.keySet()) {
        	Planet planet = planets.get( id );
        	if( planet.getHealth() == 0 ) {
        		planets.remove( id );
        	}
        }

        if (!mapMetadata.isEmpty()) {
            throw new IllegalStateException("Failed to parse data from Halite game engine. Please contact maintainers.");
        }

        return this;
    }
}
