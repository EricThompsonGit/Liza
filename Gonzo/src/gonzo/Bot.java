package gonzo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gonzo.hlt.*;

public class Bot {

	String botName;
    public static void main(final String[] args) {
    	Bot bot = new Bot( "Drowsy" );
    	bot.run();
    }
    
    public Bot( String name ) {
    	botName = name;
    	run();
    }
    public void run() {
        final Networking networking = new Networking();
        final GameMap gameMap = networking.initialize(botName);

        // We now have 1 full minute to analyse the initial map.
        final String initialMapIntelligence =
                "width: " + gameMap.getWidth() +
                "; height: " + gameMap.getHeight() +
                "; players: " + gameMap.getAllPlayers().size() +
                "; planets: " + gameMap.getAllPlanets().size();
        Log.log(initialMapIntelligence);

        final ArrayList<Move> moveList = new ArrayList<>();
        
        final int myId = gameMap.getMyPlayerId();
        Log.log( "My Id = " + myId );



        Strategy strategy = null;
        
        if( gameMap.getAllPlayers().size() == 4 ) {
        	strategy = new StrategyShy();
        }else {
        	//strategy = new StrategyBasic();
        	strategy = new StrategyAttack();
        }
        
        Log.log( "Initial ships:");
        for( Ship ship : gameMap.getMyPlayer().getShips().values()) {
        	Log.log(ship.toString());
        }
        strategy.getInitialAssignments( gameMap );
        
        for (;;) {
        	Log.log("Starting a turn");
            moveList.clear();
            networking.updateMap(gameMap);
            
            strategy.calculateMoves(gameMap, moveList);

            Log.log("Sending Moves");
            Networking.sendMoves(moveList);
            Log.log("Moves Sent");

        }
    }


}
