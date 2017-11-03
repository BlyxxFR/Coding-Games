import java.util.*;
import java.io.*;
import java.math.*;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Player {
    
    private static Scanner in;
    private static int myShipCount;
    private static int entityCount;
    private static ArrayList<Ship> shipsList;
    private static ArrayList<Barrel> barrelsList;

    public static void main(String args[]) {
        in = new Scanner(System.in);

        // game loop
        while (true) {
            shipsList = new ArrayList<Ship>();
            barrelsList = new ArrayList<Barrel>();
            getData();
            
            Map<Ship, Action> myActions = getActions();
            System.err.println(myActions);
            
            for (int i = 0; i < myShipCount; i++) {

                if(myActions.containsKey(shipsList.get(i)))
                    System.out.println(myActions.get(shipsList.get(i)));
                else
                    System.out.println("WAIT"); // Any valid action, such as "WAIT" or "MOVE x y"
                    
            }
        }
    }
    
    private static Map<Ship, Action> getActions() { 
        Map<Ship, Action> myActions = new HashMap<>();
        ArrayList<Barrel> markedBarrel = new ArrayList<Barrel>();
        while(myActions.size() < myShipCount) {
            ActionType type = ActionType.WAIT;
            
            // We search for the ships with <= 16 with ennemies
            for (int i = 0; i < myShipCount; i++) {
                Ship currentShip = shipsList.get(i);
                if(!myActions.containsKey(currentShip)) {
                     for(int j = myShipCount; j < shipsList.size(); j++) {
                        Ship currentOtherShip = shipsList.get(j);
                        double currentDist = getDistance2(currentShip.coordX, currentShip.coordY, currentOtherShip.coordX, currentOtherShip.coordY);
                        
                        System.err.println(currentDist);
                        
                        if(currentDist <= 16) {
                            myActions.put(currentShip, new Action(ActionType.FIRE, currentOtherShip.coordX, currentOtherShip.coordY));
                            break;
                        }
                    }
                }
            }
            
            // We search for the closests ships from the barrels
            for (int i = 0; i < myShipCount; i++) {
                Ship currentShip = shipsList.get(i);
                if(!myActions.containsKey(currentShip)) {
                    double dist = Integer.MAX_VALUE;
                    Barrel closestBarrel = null;
            
                    for(int j = 0; j < barrelsList.size(); j++) {
                        Barrel currentBarrel = barrelsList.get(j);
                        if(!markedBarrel.contains(currentBarrel)) {
                            double currentDist = getDistance2(currentShip.coordX, currentShip.coordY, currentBarrel.coordX, currentBarrel.coordY);
                            if(dist > currentDist) {
                                dist = currentDist;
                                closestBarrel = currentBarrel;
                            }
                        }
                    }
                    
                    currentShip.closestBarrel = closestBarrel;
                    if(closestBarrel == null) {
                        myActions.put(currentShip, new Action(ActionType.WAIT, 0, 0));
                    } else {
                        myActions.put(currentShip, new Action(ActionType.MOVE, closestBarrel.coordX, closestBarrel.coordY));
                        markedBarrel.add(closestBarrel);
                    }
                }                    
            }
             
        }
        
        return myActions;
    }
    
    private static enum ActionType { MOVE, SLOWER, WAIT, FIRE, MINE }
    
    private static double getDistance2(int coordX1, int coordY1, int coordX2, int coordY2) {
        return Math.pow(coordX1 - coordX2, 2) + Math.pow(coordY1 - coordY2, 2);
    }
    
    static class Action {
        public ActionType type = ActionType.WAIT;  
        public int coordX;
        public int coordY;
        
        public Action(ActionType type, int coordX, int coordY) {
            this.type = type;
            this.coordX = coordX;
            this.coordY = coordY;
        }
        
        public String toString() {
            switch(this.type) {
                case MOVE:
                    return "MOVE " + this.coordX + " " + this.coordY;
                case SLOWER:
                    return "SLOWER";
                case FIRE:
                    return "FIRE " + this.coordX + " " + this.coordY;
                case MINE:
                    return "MINE";
                default:
                    return "WAIT";
            }
        }
        
    }
    
    private static void getData() {
        myShipCount = in.nextInt(); // the number of remaining ships
        entityCount = in.nextInt(); // the number of entities (e.g. ships, mines or cannonballs)
        for (int i = 0; i < entityCount; i++) {
            int entityId = in.nextInt();
            String entityType = in.next();
            int x = in.nextInt();
            int y = in.nextInt();
            int arg1 = in.nextInt();
            int arg2 = in.nextInt();
            int arg3 = in.nextInt();
            int arg4 = in.nextInt();
            
            switch(entityType) {
                case "SHIP":
                    Ship ship = new Ship(entityId, x, y, arg1, arg2, arg3, arg4);
                    shipsList.add(ship);
                    break;
                case "BARREL":
                    Barrel barrel = new Barrel(entityId, x, y, arg1);
                    barrelsList.add(barrel);
                    break;
                case "MINE":
                    break;
                default:
                    break;
            }
            
        }   
    }
    
    static class Ship {
        
        public int id;
        public int orientation;
        public int vitesse;
        public int stock;
        public int player;
        public int coordX;
        public int coordY;
        public Barrel closestBarrel = null;
        
        public Ship(int id, int coordX, int coordY, int orientation, int vitesse, int stock, int player) {
            this.id = id;
            this.coordX = coordX;
            this.coordY = coordY;
            this.orientation = orientation;
            this.vitesse = vitesse;
            this.stock = stock;
            this.player = player;
        }
        
    }
    
    static class Barrel {
        
        public int id;
        public int coordX;
        public int coordY;
        public int stock;
        
        public Barrel(int id, int coordX, int coordY, int stock) {
            this.id = id;
            this.coordX = coordX;
            this.coordY = coordY;
            this.stock = stock;
        }
        
    }
    
}
