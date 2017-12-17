package eggnog;

import java.util.ArrayList;
import java.util.List;

import eggnog.hlt.GameMap;
import eggnog.hlt.Log;
import eggnog.hlt.Move;
import eggnog.hlt.Planet;
import eggnog.hlt.Ship;

public abstract class Strategy {
    public abstract void getInitialAssignments( GameMap gameMap, AssignmentList assignments );
	public abstract void calculateMoves(final GameMap gameMap, final ArrayList<Move> moveList, AssignmentList assignments);

}
