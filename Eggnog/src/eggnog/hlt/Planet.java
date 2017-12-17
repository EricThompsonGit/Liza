package eggnog.hlt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Planet extends Entity {

    private int remainingProduction;
    private int currentProduction;
    private int dockingSpots;
    private List<Integer> dockedShips;
    private List<Integer> assignedShips;

    public Planet(final int owner, final int id, final double xPos, final double yPos, final int health,
                  final double radius, final int dockingSpots, final int currentProduction,
                  final int remainingProduction, final List<Integer> dockedShips) {

        super(owner, id, xPos, yPos, health, radius);

        this.dockingSpots = dockingSpots;
        this.currentProduction = currentProduction;
        this.remainingProduction = remainingProduction;
        this.dockedShips = Collections.unmodifiableList(dockedShips);
    }
    public Planet(final int owner, final int id, final double xPos, final double yPos, final int health,
            final double radius, final int dockingSpots, final int currentProduction,
            final int remainingProduction) {

		  super(owner, id, xPos, yPos, health, radius);
		
		  this.dockingSpots = dockingSpots;
		  this.currentProduction = currentProduction;
		  this.remainingProduction = remainingProduction;
		  dockedShips = new ArrayList<Integer>();
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
}
