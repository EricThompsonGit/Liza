package eggnog.hlt;

import eggnog.hlt.Ship.DockingStatus;

public class Entity extends Position {

    private int owner;
    private final int id;
    private int health;
    private final double radius;

    public Entity(final int owner, final int id, final double xPos, final double yPos, final int health, final double radius) {
        super(xPos, yPos);
        this.owner = owner;
        this.id = id;
        this.health = health;
        this.radius = radius;
    }

    public int getOwner() {
        return owner;
    }

    public int getId() {
        return id;
    }

    public int getHealth() {
        return health;
    }

    public double getRadius() {
        return radius;
    }

    @Override
    public String toString() {
        return "Entity[" +
                super.toString() +
                ", owner=" + owner +
                ", id=" + id +
                ", health=" + health +
                ", radius=" + radius +
                "]";
    }
	public void setHealth( int healthIn ) {
		health = healthIn;
	}
	public void setOwner( int ownerIn ) {
		owner = ownerIn;
	}
}
