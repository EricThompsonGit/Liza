package gonzo.hlt;

public class Ship extends Entity {

    public enum DockingStatus { Undocked, Docking, Docked, Undocking }

    private DockingStatus dockingStatus;
    private int dockedPlanet;
    private int dockingProgress;
    private int weaponCooldown;
    private Assignment assignment;
    private double vx;
    private double vy;

    public Ship(final int owner, final int id, final double xPos, final double yPos,
                final int health, final DockingStatus dockingStatus, final int dockedPlanet,
                final int dockingProgress, final int weaponCooldown) {

        super(owner, id, xPos, yPos, health, Constants.SHIP_RADIUS);

        this.dockingStatus = dockingStatus;
        this.dockedPlanet = dockedPlanet;
        this.dockingProgress = dockingProgress;
        this.weaponCooldown = weaponCooldown;
        vx = 0;
        vy = 0;
    }

    public int getWeaponCooldown() {
        return weaponCooldown;
    }

    public DockingStatus getDockingStatus() {
        return dockingStatus;
    }

    public int getDockingProgress() {
        return dockingProgress;
    }

    public int getDockedPlanet() {
        return dockedPlanet;
    }

    public boolean canDock(final Planet planet) {
        return getDistanceTo(planet) <= Constants.SHIP_RADIUS + Constants.DOCK_RADIUS + planet.getRadius();
    }

    @Override
    public String toString() {
        return "Ship[" +
                super.toString() +
                ", dockingStatus=" + dockingStatus +
                ", dockedPlanet=" + dockedPlanet +
                ", dockingProgress=" + dockingProgress +
                ", weaponCooldown=" + weaponCooldown +
                "]";
    }
	public void setDockingStatus( DockingStatus dockingStatusIn ) {
		dockingStatus = dockingStatusIn;
	}
    
	public void setDockedPlanet( int dockedPlanetIn ) {
		dockedPlanet = dockedPlanetIn;
	}
	
	public void setDockingProgress( int dockingProgressIn ) {
		dockingProgress = dockingProgressIn;		
	}
	
	public void setWeaponCooldown( int weaponCooldownIn ) {
		weaponCooldown = weaponCooldownIn;
	}
	
	public Assignment getAssignment() {
		return assignment;
	}
	
	public void setAssignment( Assignment a ) {
		assignment = a;
	}
	public void setPosition( double xPos, double yPos ) {
		vx = xPos - getXPos();
		vy = yPos - getYPos();
		super.setPosition(xPos, yPos);
	}
	public Position getNextPosition() {
		Position pos = new Position( getXPos()+vx, getYPos()+vy );
		return pos;
	}
}
