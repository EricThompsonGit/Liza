package eggnog.hlt;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class Player {

    private final Map<Integer, Ship> ships;
    private final int id;

    public Player(final int id, Map<Integer, Ship> ships) {
        this.id = id;
        this.ships = Collections.unmodifiableMap(ships);
    }
    public Player(final int id) {
        this.id = id;
        this.ships = new TreeMap<Integer, Ship>();
    }

    public Map<Integer, Ship> getShips() {
        return ships;
    }

    public Ship getShip(final int entityId) {
        return ships.get(entityId);
    }

    public int getId() {
        return id;
    }
    public void clearShips() {
    	ships.clear();
    }
    public void addShip( Ship ship ) {
    	ships.put( ship.getId(), ship);
    }
}
