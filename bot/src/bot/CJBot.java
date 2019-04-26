package bot;

import ai.abstraction.AbstractionLayerAI;
import ai.abstraction.pathfinding.AStarPathFinding;
import ai.core.AI;
import ai.core.ParameterSpecification;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import rts.*;
import rts.units.Unit;
import rts.units.UnitType;
import rts.units.UnitTypeTable;

public class MyBot extends AbstractionLayerAI {    
    private UnitTypeTable utt;
    private UnitType worker;
    
    public MyBot(UnitTypeTable utt) {
        super(new AStarPathFinding());
        this.utt = utt;
        worker = utt.getUnitType("Worker");
    }
    

    @Override
    public void reset() {
    }

    
    @Override
    public AI clone() {
        return new MyBot(utt);
    }
   
    
    @Override
    public PlayerAction getAction(int player, GameState gs) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        
        for (Unit unit : pgs.getUnits()) {
            // TODO: issue commands to units
        }
        
        return translateActions(player, gs);
    }
    
    @Override
    public List<ParameterSpecification> getParameters() {
        return new ArrayList<>();
    }
}
