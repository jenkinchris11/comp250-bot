package bot;

import ai.abstraction.AbstractAction;
import ai.abstraction.AbstractionLayerAI;
import ai.abstraction.Harvest;
import ai.abstraction.WorkerRush;
import ai.abstraction.pathfinding.AStarPathFinding;
import ai.abstraction.pathfinding.PathFinding;
import ai.core.AI;
import ai.core.ParameterSpecification;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import rts.*;
import rts.units.Unit;
import rts.units.UnitType;
import rts.units.UnitTypeTable;

public class CJBot extends AbstractionLayerAI {    
    private UnitTypeTable utt;
    private UnitType workertype;
    private UnitType Basetype;
    
    public CJBot(UnitTypeTable utt) {
        super(new AStarPathFinding());
        this.utt = utt;
        workertype = utt.getUnitType("Worker");
        Basetype = utt.getUnitType("Base");
    }
    
    
   public CJBot(UnitTypeTable a_utt, PathFinding a_pf) {
       super(a_pf);
       reset(a_utt);
   }
   
   public void reset(UnitTypeTable a_utt)  
   {
       utt = a_utt;
       if (utt!=null) {
           workertype = utt.getUnitType("Worker");
           Basetype = utt.getUnitType("Base");
       }
   }   
   
   
   public AI clone() {
       return new WorkerRush(utt, pf);
   }
   
   public PlayerAction getAction(int player, GameState gs) {
       PhysicalGameState pgs = gs.getPhysicalGameState();
       Player p = gs.getPlayer(player);
//       System.out.println("LightRushAI for player " + player + " (cycle " + gs.getTime() + ")");
               
       
       for(Unit u:pgs.getUnits()) {
           if (u.getType()==Basetype && 
               u.getPlayer() == player && 
               gs.getActionAssignment(u)==null) {
               baseBehavior(u,p,pgs);
           }
       }

       
       for(Unit u:pgs.getUnits()) {
           if (u.getType().canAttack && !u.getType().canHarvest && 
               u.getPlayer() == player && 
               gs.getActionAssignment(u)==null) {
               meleeUnitBehavior(u,p,gs);
           }        
       }

    
       List<Unit> workers = new LinkedList<Unit>();
       for(Unit u:pgs.getUnits()) {
           if (u.getType().canHarvest && 
               u.getPlayer() == player) {
               workers.add(u);
           }        
       }
       workersBehavior(workers,p,gs);
       
               
       return translateActions(player,gs);
   }
   
   
   public void baseBehavior(Unit u,Player p, PhysicalGameState pgs) {
       if (p.getResources()>=workertype.cost) train(u, workertype);
   }
   
   public void meleeUnitBehavior(Unit u, Player p, GameState gs) {
       PhysicalGameState pgs = gs.getPhysicalGameState();
       Unit closestEnemy = null;
       int closestDistance = 0;
       for(Unit u2:pgs.getUnits()) {
           if (u2.getPlayer()>=0 && u2.getPlayer()!=p.getID()) { 
               int d = Math.abs(u2.getX() - u.getX()) + Math.abs(u2.getY() - u.getY());
               if (closestEnemy==null || d<closestDistance) {
                   closestEnemy = u2;
                   closestDistance = d;
               }
           }
       }
       if (closestEnemy!=null) {
           attack(u,closestEnemy);
       }
   }
   
   public void workersBehavior(List<Unit> workers,Player p, GameState gs) {
       PhysicalGameState pgs = gs.getPhysicalGameState();
       int nbases = 0;
       int resourcesUsed = 0;
       Unit harvestWorker = null;
       List<Unit> freeWorkers = new LinkedList<Unit>();
       freeWorkers.addAll(workers);
       
       if (workers.isEmpty()) return;
       
       for(Unit u2:pgs.getUnits()) {
           if (u2.getType() == Basetype && 
               u2.getPlayer() == p.getID()) nbases++;
       }
       
       List<Integer> reservedPositions = new LinkedList<Integer>();
       if (nbases==0 && !freeWorkers.isEmpty()) {
           // build a base:
           if (p.getResources()>=Basetype.cost + resourcesUsed) {
               Unit u = freeWorkers.remove(0);
               buildIfNotAlreadyBuilding(u,Basetype,u.getX(),u.getY(),reservedPositions,p,pgs);
               resourcesUsed+=Basetype.cost;
           }
       }
       
       if (freeWorkers.size()>0) harvestWorker = freeWorkers.remove(0);
       
       // harvest with the harvest worker:
       if (harvestWorker!=null) {
           Unit closestBase = null;
           Unit closestResource = null;
           int closestDistance = 0;
           for(Unit u2:pgs.getUnits()) {
               if (u2.getType().isResource) { 
                   int d = Math.abs(u2.getX() - harvestWorker.getX()) + Math.abs(u2.getY() - harvestWorker.getY());
                   if (closestResource==null || d<closestDistance) {
                       closestResource = u2;
                       closestDistance = d;
                   }
               }
           }
           closestDistance = 0;
           for(Unit u2:pgs.getUnits()) {
               if (u2.getType().isStockpile && u2.getPlayer()==p.getID()) { 
                   int d = Math.abs(u2.getX() - harvestWorker.getX()) + Math.abs(u2.getY() - harvestWorker.getY());
                   if (closestBase==null || d<closestDistance) {
                       closestBase = u2;
                       closestDistance = d;
                   }
               }
           }
           if (closestResource!=null && closestBase!=null) {
               AbstractAction aa = getAbstractAction(harvestWorker);
               if (aa instanceof Harvest) {
                   Harvest h_aa = (Harvest)aa;
                   if (h_aa.target != closestResource || h_aa.base!=closestBase) harvest(harvestWorker, closestResource, closestBase);
               } else {
                   harvest(harvestWorker, closestResource, closestBase);
               }
           }
       }
       
       for(Unit u:freeWorkers) meleeUnitBehavior(u, p, gs);
       
   }
   
   
   @Override
   public List<ParameterSpecification> getParameters()
   {
       List<ParameterSpecification> parameters = new ArrayList<>();
       
       parameters.add(new ParameterSpecification("PathFinding", PathFinding.class, new AStarPathFinding()));

       return parameters;
   }

}
