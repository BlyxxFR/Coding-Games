import java.util.*;
import java.io.*;
import java.math.*;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Player {
    static class Factory {
        private ArrayList<Factory> accessFactory = new ArrayList<>();
        private Map<Factory, Integer> linkMap = new HashMap<>();
        private int idFactory;
        private int idPlayer = 0;
        private int numberCyborgs = 0;
        private int production = 0;
        
        public void setIdPlayer(int idPlayer) { this.idPlayer = idPlayer; }
        public void setNumberCyborgs(int number) { this.numberCyborgs = number; }
        public void setProduction(int prod) { this.production = prod; }
        public ArrayList<Factory> getAccessFactory() { return this.accessFactory; }
        public int getIdFactory() { return this.idFactory; }
        public int getIdPlayer() { return this.idPlayer; }
        public int getNumberCyborgs() { return this.numberCyborgs; }
        public int getProduction() { return this.production; }
        
        public Factory(int idFactory) {
            this.idFactory = idFactory;   
        }
        
        public void addLinkToFactory(Factory factory, int distance) {
            this.linkMap.put(factory, distance);
            this.accessFactory.add(factory);
        }
        
        public int getDistanceTo(Factory factory) {
            return linkMap.get(factory);   
        }
        
        public boolean canGoTo(Factory factory) {
            return linkMap.containsKey(factory);   
        }
        
        public void addTroops(int troop) {
            this.numberCyborgs += troop;   
        }
        
        public void removeTroops(int troop) {
            this.numberCyborgs -= troop;   
        }
        
        public String toString() { return "" + this.idFactory; }
        
    }
    
    static class Bomb {
        private int idBomb;
        private int idPlayer = 0;
        private Factory startFactory;
        private Factory endFactory;
        private int numberTurns = 0;
        
        public Factory getEndFactory() { return this.endFactory; }
        public int getNumberTurns() { return this.numberTurns; }
        public int getIdPlayer() { return this.idPlayer; }
        
        public Bomb(int idBomb, int idPlayer, Factory startFactory, Factory endFactory, int numberTurns) {
            this.idBomb = idBomb;
            this.idPlayer = idPlayer;
            this.startFactory = startFactory;
            this.endFactory = endFactory;
            this.numberTurns = numberTurns;
        }
        
        public static boolean isBombing(Factory factory) {
            for(Bomb bomb : bombList) {
                if(bomb.getIdPlayer() == 1 && bomb.getEndFactory() == factory)
                    return true;
            }
            return false;
        }
    }
    
    static class Troop {
        private int idTroop;
        private int idPlayer = 0;
        private Factory startFactory;
        private Factory endFactory;
        private int numberCyborgs = 0;
        private int numberTurns = 0;
        
        public Factory getEndFactory() { return this.endFactory; }
        public int getNumberCyborgs() { return this.numberCyborgs; }
        public int getNumberTurns() { return this.numberTurns; }
        public int getIdPlayer() { return this.idPlayer; }
        
        public Troop(int idTroop, int idPlayer, Factory startFactory, Factory endFactory, int numberCyborgs, int numberTurns) {
            this.idTroop = idTroop;
            this.idPlayer = idPlayer;
            this.startFactory = startFactory;
            this.endFactory = endFactory;
            this.numberCyborgs = numberCyborgs;
            this.numberTurns = numberTurns;
        }
        
    }
    
    static class Decision {
        public Factory startFactory;
        public Factory endFactory;
        public int cyborgsToSend;
        
        public Decision() {
            this.startFactory = null;
            this.endFactory = null;
            this.cyborgsToSend = 0;
        }
        
        public Decision(Factory start, Factory end, int cyborgsToSend) {
            this.startFactory = start;
            this.endFactory = end;
            this.cyborgsToSend = cyborgsToSend;
        }
        
        private static int computeMin(int nbCyborgs, int distance, boolean closest, boolean weakest) {
            if(closest && !weakest) 
                return distance;
            if(weakest && !closest)
                return -nbCyborgs;
            
            return nbCyborgs * (weakest ? 1 : 0) - distance * (closest ? 1 : 0) * 10; 
        }
        
        public static Decision findBestTargetToExpand(boolean closest, boolean weakest) {
            Factory start = null;
            Factory end = null;
            int nbcyborgs = 0;
            int distance = Integer.MAX_VALUE;
            
            System.err.println(" ################################## ");
            System.err.println("Getting the incomming ennemy troops within 1 turns");
            Map<Factory, Integer> incommingTroops = new HashMap<>();
            for(Troop troop : troopList) {
                if(troop.getIdPlayer() == -1 && troop.getNumberTurns() <= 1) {
                    if (incommingTroops.containsKey(troop.getEndFactory()))
                        incommingTroops.put(troop.getEndFactory(), incommingTroops.get(troop.getEndFactory()) + troop.getNumberCyborgs());
                    else
                        incommingTroops.put(troop.getEndFactory(), troop.getNumberCyborgs());
                        
                    if(incommingTroops.get(troop.getEndFactory()) > (int)(1.5*troop.getEndFactory().getNumberCyborgs()) && !ignoredFactories.contains(troop.getEndFactory()))
                        ignoredFactories.add(troop.getEndFactory());
                }    
            }
            
            for(Map.Entry<Factory, Integer> entry : incommingTroops.entrySet()) {
                System.err.println("Troop of " + entry.getValue() + " cyborgs incomming to factory " + entry.getKey() + " soon");
            }
            
            System.err.println("Ignored factories because of attacks : " + ignoredFactories);
            
            System.err.println(" ################################## ");
            // We find our base with the more cyborgs
            System.err.println("Trying to find the weakest target to attack");
            
            for(Map.Entry<Integer, Factory> entry : myFactories.entrySet()) {
                Factory myFactory = entry.getValue(); 
                if(myFactory.getNumberCyborgs() > nbcyborgs && !ignoredFactories.contains(myFactory)) {
                    System.err.println("New strongest factory found : " + myFactory + " with " + myFactory.getNumberCyborgs() + " cyborgs");
                    start = myFactory;
                    nbcyborgs = myFactory.getNumberCyborgs();
                    
                    // We find the weakest target
                    int mintroop = Integer.MAX_VALUE;
                    System.err.println("Factories we can access : " + myFactory.getAccessFactory());
                    for(Factory factory : myFactory.getAccessFactory()) {
                        if(factory.getIdPlayer() != 1 && computeMin(factory.getNumberCyborgs(), myFactory.getDistanceTo(factory), closest, weakest) < computeMin(nbcyborgs, distance, closest, weakest)) {
                            System.err.println("New weakest factory found : " + factory);
                            end = factory;
                            mintroop = factory.getNumberCyborgs();
                            distance = myFactory.getDistanceTo(factory);
                        }
                    }
                }
            }
            
            // We check if the result is ok
            if(start != null && end != null && start != end) {
                // We compute the number of cyborgs
                nbcyborgs = end.getNumberCyborgs() + (int)(0.1 * start.getNumberCyborgs());
                
                if(0.8 * start.getNumberCyborgs() - nbcyborgs > 0) {
                    if(end.getIdPlayer() == 0)
                        nbcyborgs = (int)(0.1 * start.getNumberCyborgs());
                    else
                        nbcyborgs = (int)(0.3 * start.getNumberCyborgs());
                }
                
                if(nbcyborgs == 0) { nbcyborgs = 1; }
                
                if(start.getNumberCyborgs() > nbcyborgs+1) {
                    attackingFactories.add(start);
                    return new Decision(start, end, nbcyborgs);
                }
            }
            
            // If we get here, we should consider a support mission
            start = null;
            end = null;
            nbcyborgs = 0;
            distance = Integer.MAX_VALUE;
            
            System.err.println("Nothing to attack");
            System.err.println("Trying to find the weakest target to support");            
            for(Map.Entry<Integer, Factory> entry : myFactories.entrySet()) {
                Factory myFactory = entry.getValue();
                if(myFactory.getNumberCyborgs() > nbcyborgs && !ignoredFactories.contains(myFactory)) {
                    System.err.println("New strongest factory found : " + myFactory + " with " + myFactory.getNumberCyborgs() + " cyborgs");
                    start = myFactory;
                    nbcyborgs = myFactory.getNumberCyborgs();
                    int mintroop = Integer.MAX_VALUE;
                    System.err.println("Factories we can access : " + myFactory.getAccessFactory());
                    for(Factory factory : myFactory.getAccessFactory()) {
                        if(factory.getIdPlayer() == 1 && computeMin(factory.getNumberCyborgs(), myFactory.getDistanceTo(factory), closest, weakest) < computeMin(nbcyborgs, distance, closest, weakest)) {
                            end = factory;
                            mintroop = factory.getNumberCyborgs(); 
                            distance = myFactory.getDistanceTo(factory);
                            System.err.println("New weakest factory found : " + factory);
                        }
                    }
                }
            }
            
            // We check if it's ok
            if(start != null && end != null && start != end) {
                nbcyborgs = (int)(0.2 * start.getNumberCyborgs());
                attackingFactories.add(start);
                return new Decision(start, end, nbcyborgs);
            }
            
            start = null;
            end = null;
            nbcyborgs = -1;
            
            System.err.println("Nothing to support");
            if(bombLeft > 0) {
                System.err.println("Trying to find the strongest target to bomb");  
                // If we get here, we might try to bomb someone 
                for(Map.Entry<Integer, Factory> entry : ennemyFactories.entrySet()) {
                    Factory ennemyFactory = entry.getValue();
                    if(ennemyFactory.getNumberCyborgs() > nbcyborgs && !bombingFactories.contains(ennemyFactory) && !Bomb.isBombing(ennemyFactory)) {
                        end = ennemyFactory;
                        nbcyborgs = ennemyFactory.getNumberCyborgs();
                        System.err.println("New strongest factory found : " + ennemyFactory + " with " + ennemyFactory.getNumberCyborgs() + " cyborgs");
                        int mintroop = Integer.MAX_VALUE;
                        for(Factory factory : ennemyFactory.getAccessFactory()) {
                            if(factory.getIdPlayer() == 1 && factory.getNumberCyborgs() < mintroop && !attackingFactories.contains(factory)) {
                                System.err.println("New weakest factory found : " + factory);
                                start = factory;
                                mintroop = factory.getNumberCyborgs(); 
                            }
                        }
                    }
                }
                
                if(start != null && end != null && start != end) {
                    nbcyborgs = -1;
                    ignoredFactories.add(start);
                    bombingFactories.add(end);
                    bombLeft--;
                    return new Decision(start, end, nbcyborgs);
                }
            }
            
            // If we get here, all failed
            System.err.println("Nothing to attack nor support");
            return new Decision();
        }

    }
    
    private static ArrayList<Factory> factoryList = new ArrayList<Factory>();
    private static Map<Integer, Factory> myFactories = new HashMap<>();
    private static Map<Integer, Factory> ennemyFactories = new HashMap<>();
    private static Map<Integer, Factory> emptyFactories = new HashMap<>();
    private static ArrayList<Troop> troopList = new ArrayList<Troop>();
    private static ArrayList<Bomb> bombList = new ArrayList<Bomb>();
    private static ArrayList<Factory> ignoredFactories;
    private static ArrayList<Factory> attackingFactories;
    private static ArrayList<Factory> bombingFactories;
    private static boolean hasAttackedPreviously = false;
    private static int bombLeft = 2;
    
    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        int factoryCount = in.nextInt(); // the number of factories
        for(int i = 0; i<factoryCount; i++) {
            factoryList.add(new Factory(i));   
        }
        
        int linkCount = in.nextInt(); // the number of links between factories
        for (int i = 0; i < linkCount; i++) {
            int idFactory1 = in.nextInt();
            int idFactory2 = in.nextInt();
            int distance = in.nextInt();
            
            Factory factory1 = factoryList.get(idFactory1);
            Factory factory2 = factoryList.get(idFactory2);
            
            factory1.addLinkToFactory(factory2, distance);
            factory2.addLinkToFactory(factory1, distance);
        }
        
        int currentTurn = 0;
        // game loop
        while (true) {
            ignoredFactories = new ArrayList<Factory>();
            attackingFactories = new ArrayList<Factory>();
            bombingFactories = new ArrayList<Factory>();
            troopList = new ArrayList<Troop>();
            bombList = new ArrayList<Bomb>();
            
            int entityCount = in.nextInt(); // the number of entities (e.g. factories and troops)
            
            for (int i = 0; i < entityCount; i++) {
                int entityId = in.nextInt();
                String entityType = in.next();
                int arg1 = in.nextInt();
                int arg2 = in.nextInt();
                int arg3 = in.nextInt();
                int arg4 = in.nextInt();
                int arg5 = in.nextInt();
                
                switch(entityType) {
                    case "TROOP":
                        Troop troop = new Troop(entityId, arg1, factoryList.get(arg2), factoryList.get(arg3), arg4, arg5);
                        troopList.add(troop);
                        break;
                    case "BOMB":
                        Bomb bomb = new Bomb(entityId, arg1, factoryList.get(arg2), (arg3 != -1) ? factoryList.get(arg3) : null, arg4);
                        bombList.add(bomb);
                        break;
                    default:
                        Factory factory = factoryList.get(entityId);
                        factory.setIdPlayer(arg1);
                        factory.setNumberCyborgs(arg2);
                        factory.setProduction(arg3);

                        if(factory.getIdPlayer() == 1)
                            myFactories.put(entityId, factory);
                        else if(factory.getIdPlayer() == -1)
                            ennemyFactories.put(entityId, factory);
                        else
                            emptyFactories.put(entityId, factory);
                        break;
                }
            }
            
            ArrayList<Decision> myDecisions = new ArrayList<Decision>();
            int maxDecisionsAllowedToTake = 6;
            int maxIter = 100;
            int currentIter = 0;
            
            while(myDecisions.size() < maxDecisionsAllowedToTake && currentIter< maxIter) {
                Decision myDecision = Decision.findBestTargetToExpand(true, false);
                if(myDecision.startFactory != null && myDecision.endFactory != null && myDecision.startFactory.getIdPlayer() == 1) {
                    myDecisions.add(myDecision);
                    myDecision.startFactory.removeTroops(myDecision.cyborgsToSend);
                    hasAttackedPreviously = (myDecision.endFactory.getIdPlayer() == -1);
                } else {
                    break;
                }
                currentIter++;
            }
            
            String result = "WAIT;";
            
            if(myDecisions.size() == 0) {
                Decision myDecision = Decision.findBestTargetToExpand(false, true);
                if(myDecision.startFactory != null && myDecision.endFactory != null && myDecision.startFactory.getIdPlayer() == 1) {
                    myDecisions.add(myDecision);
                }
            }
            
            if(myDecisions.size() > 0) {
                for(Decision myDecision : myDecisions) {
                    if(myDecision.cyborgsToSend == -1) {
                        // We are sending a bomb
                        System.err.println(myDecision.startFactory.getIdFactory() + " --> " + myDecision.endFactory.getIdFactory() + " : Bombing");
                        result += "BOMB " + myDecision.startFactory.getIdFactory() + " " + myDecision.endFactory.getIdFactory() + ";";   
                    } else {
                        System.err.println(myDecision.startFactory.getIdFactory() + " --> " + myDecision.endFactory.getIdFactory() + " : Moving " + myDecision.cyborgsToSend + " cyborgs (" + myDecision.startFactory.getNumberCyborgs() + ")");
                        result += "MOVE " + myDecision.startFactory.getIdFactory() + " " + myDecision.endFactory.getIdFactory() + " " + myDecision.cyborgsToSend + ";";
                    }
                }
            }
            
            result = result.substring(0, result.length() - 1);
            
            System.out.println(result);
        }
    }
}
        
