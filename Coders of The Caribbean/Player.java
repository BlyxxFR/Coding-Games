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
    private static ArrayList<Ship> shipsList = new ArrayList<Ship>();
    private static ArrayList<Barrel> barrelsList;

    public static void main(String args[]) {
        in = new Scanner(System.in);

        // game loop
        while (true) {
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
            
            // We search for the ships with <= 3 with ennemies
            for (int i = 0; i < myShipCount; i++) {
                Ship currentShip = shipsList.get(i);
                if(!myActions.containsKey(currentShip)) {
                     for(int j = myShipCount; j < shipsList.size(); j++) {
                        Ship currentOtherShip = shipsList.get(j);
                        if((barrelsList.size() > 0 && currentShip.stock > 20) || barrelsList.size() == 0) {
                            double currentDist = getDistance2(currentShip.coordX, currentShip.coordY, currentOtherShip.coordX, currentOtherShip.coordY);
                            
                            System.err.println(currentDist);
                            
                            if(currentDist <= 2) {
                                Barrel closestBarrel = findClosestBarrel(currentOtherShip, new ArrayList<Barrel>());
                                
                                // We try to predict the position of the ship
                                System.err.println(currentShip.id + " " + currentOtherShip.id + " " + "Prev coord : " + currentOtherShip.prevCoordX + " " + currentOtherShip.prevCoordY + " --- " + currentOtherShip.coordX + " " + currentOtherShip.coordY);
                                 
                                int deltaX = currentOtherShip.coordX - currentOtherShip.prevCoordX;
                                int deltaY = currentOtherShip.coordY - currentOtherShip.prevCoordY;
                                
                                int coordX = currentOtherShip.coordX + currentOtherShip.vitesse * deltaX;
                                int coordY = currentOtherShip.coordY + currentOtherShip.vitesse * deltaY;
                               
                                myActions.put(currentShip, new Action(ActionType.FIRE, coordX, coordY));
                                break;
                            }
                        }
                    }
                }
            }
            
            // We search for the closests ships from the barrels
            for (int i = 0; i < myShipCount; i++) {
                Ship currentShip = shipsList.get(i);
                if(!myActions.containsKey(currentShip)) {
                    Barrel closestBarrel = findClosestBarrel(currentShip, markedBarrel);
                    currentShip.closestBarrel = closestBarrel;
                    if(closestBarrel != null) {
                        myActions.put(currentShip, new Action(ActionType.MOVE, closestBarrel.coordX, closestBarrel.coordY));
                        markedBarrel.add(closestBarrel);
                    }
                }                    
            }
            
            // If there is no barrel we move to closest ennemy
            for (int i = 0; i < myShipCount; i++) {
                Ship currentShip = shipsList.get(i);
                if(!myActions.containsKey(currentShip)) {
                    Ship closestShip = null;
                    double dist = Integer.MAX_VALUE;
                    for(int j = myShipCount; j < shipsList.size(); j++) {
                        Ship currentOtherShip = shipsList.get(j);
                        double currentDist = getDistance2(currentShip.coordX, currentShip.coordY, currentOtherShip.coordX, currentOtherShip.coordY);
                        if(dist > currentDist) {
                            dist = currentDist;
                            closestShip = currentOtherShip;
                        }
                    }
                    myActions.put(currentShip, new Action(ActionType.MOVE, closestShip.coordX, closestShip.coordY));
                }
            }
             
        }
        
        return myActions;
    }
    
    private static Barrel findClosestBarrel(Ship currentShip, ArrayList<Barrel> markedBarrel) {
        Barrel closestBarrel = null;
        double dist = Integer.MAX_VALUE;
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
        return closestBarrel;
    }
    
    
    private static enum ActionType { MOVE, SLOWER, WAIT, FIRE, MINE }
    
    private static double getDistance2(int coordX1, int coordY1, int coordX2, int coordY2) {
        return (Math.abs(coordX1 - coordX2) + Math.abs(coordY1 - coordY2)) / 2;
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
        ArrayList<Ship> tempShipsList = new ArrayList<Ship>();
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
                    tempShipsList.add(ship);
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
        
        // We get the previous xoords
        for(int i = 0; i < tempShipsList.size(); i++) {
            for(int j = i; j < shipsList.size(); j++) {
                if(tempShipsList.get(i).id == shipsList.get(j).id) {
                    tempShipsList.get(i).prevCoordX = shipsList.get(j).coordX;
                    tempShipsList.get(i).prevCoordY = shipsList.get(j).coordY;
                }
            }
        }
        
        shipsList = tempShipsList;
    }
    
    static class Ship {
        
        public int id;
        public int orientation;
        public int vitesse;
        public int stock;
        public int player;
        public int prevCoordX;
        public int prevCoordY;
        public int coordX;
        public int coordY;
        public Barrel closestBarrel = null;
        
        public Ship(int id, int coordX, int coordY, int orientation, int vitesse, int stock, int player) {
            this.id = id;
            this.coordX = coordX;
            this.coordY = coordY;
            this.prevCoordX = coordX;
            this.prevCoordY = coordY;
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
