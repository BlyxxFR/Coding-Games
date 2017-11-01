import java.util.*;
import java.io.*;
import java.math.*;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Player {


    private static Scanner in = new Scanner(System.in);
    private static ArrayList<Factory> factoryList = new ArrayList<Factory>();
	private static ArrayList<Factory> prevMyFactories = new ArrayList<Factory>();
    private static Map<Integer, Factory> myFactories = new HashMap<>();
    private static Map<Integer, Factory> ennemyFactories = new HashMap<>();
    private static Map<Integer, Factory> emptyFactories = new HashMap<>();
    private static ArrayList<Troop> troopList = new ArrayList<Troop>();
    private static ArrayList<Bomb> bombList = new ArrayList<Bomb>();
	private static ArrayList<Factory> ignoredFactories = new ArrayList<>();
	private static ArrayList<Factory> bombingFactories;
    private static int bombsLeft = 2;
    private static int currentTurn = 0;
	
	public static void main(String args[]) {
		initGame();
		while (true) {
		    myFactories = new HashMap<>();
            ennemyFactories = new HashMap<>();
            emptyFactories = new HashMap<>();
			ignoredFactories = new ArrayList<Factory>();
            troopList = new ArrayList<Troop>();
            bombList = new ArrayList<Bomb>();
			bombingFactories = new ArrayList<Factory>();
			
            getData();
            assignFactories();
            
            // We get the incomming troops to ignore the facilities under imminent attacks
			Decision.getIncommingTroops();
			
			ArrayList<Decision> myDecisions = new ArrayList<>();
            if(emptyFactories.size() == 0) {
                myDecisions = Decision.makeDecisions();
            } else {
                myDecisions = Decision.sendCyborgsToEveryOne();
            }
            
            // We prepare the result
            System.err.println(myDecisions);
            
            String result = "WAIT;";
            for(Decision decision : myDecisions) {
                result += decision + ";" ;
            }
            result = result.substring(0, result.length() - 1);
            System.out.println(result);
            currentTurn++;
		}
	}
	
	public static void initGame() {
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
	}
	
	public static void getData() {
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
                    break;
            }
		}
	}
	
	public static void assignFactories() {
		for(Factory factory : factoryList) {
			switch(factory.getIdPlayer()) {
				case 1:
				    myFactories.put(factory.getIdFactory(), factory);
					break;
				case -1:
				    ennemyFactories.put(factory.getIdFactory(), factory);
				    if(prevMyFactories.contains(factory)) { prevMyFactories.remove(factory); }
					break;
				default:
				    emptyFactories.put(factory.getIdFactory(), factory);
					break;
			}
		}
	}
	
	static class Decision {
        public Factory startFactory;
        public Factory endFactory;
        public int cyborgsToSend;
		public Action type = Action.WAIT;
        
        public Decision() {
            this.startFactory = null;
            this.endFactory = null;
            this.cyborgsToSend = 0;
        }
        
        public Decision(Factory start, Factory end, int cyborgsToSend, Action type) {
            this.startFactory = start;
            this.endFactory = end;
            this.cyborgsToSend = cyborgsToSend;
            this.type = type;
        }
		
		private boolean isValid() {
			return (this.startFactory != null && this.endFactory != null && this.startFactory != this.endFactory && this.startFactory.getIdPlayer() == 1);
		}
		
		public String toString() {
			switch(type) {
				case MOVE:
					return "MOVE " + startFactory + " " + endFactory + " " + cyborgsToSend;
				case BOMB:
					return "BOMB " + startFactory + " " + endFactory;
				case INC:
					return "INC " + startFactory;
				default:
					return "WAIT";
			}
		}
		
		public static ArrayList<Decision> makeDecisions() {
			ArrayList<Decision> myDecisions = new ArrayList<>();
			
			// We find our factory which is the closest to an ennemy one
			int minDistance = Integer.MAX_VALUE;
			Factory myFactory = null;
			Factory ennemyFactory = null;
			for(Map.Entry<Integer, Factory> entry : ennemyFactories.entrySet()) {
                Factory _ennemy = entry.getValue();
				for(Factory _factory : _ennemy.getClosestFactories()) {
					if(_factory.getIdPlayer() == 1 && !ignoredFactories.contains(_factory) && minDistance > _factory.getDistanceTo(_ennemy)) {
						//System.err.println("Closest found " + _factory + " " + _ennemy);
						myFactory = _factory;
						ennemyFactory = _ennemy;
						minDistance = _factory.getDistanceTo(_ennemy);
					}
				}
			}
			if(myFactory != null && ennemyFactory != null) {
				int nbCyborgs = (int)(myFactory.getNumberCyborgs() * 0.5)+ennemyFactory.getProduction()*myFactory.getDistanceTo(ennemyFactory)+1;
				myFactory.removeTroops(nbCyborgs);
				ignoredFactories.add(myFactory);
				myDecisions.add(new Decision(myFactory, ennemyFactory, nbCyborgs, Action.MOVE));
			}
			
			// We find the best target to bomb
			if(bombsLeft > 0) {
				Factory start = null;
				Factory end = null;
				int nbcyborgs = 0;
				
				for(Map.Entry<Integer, Factory> entry : ennemyFactories.entrySet()) {
                    Factory _ennemyFactory = entry.getValue();
                    if(_ennemyFactory.getNumberCyborgs() > nbcyborgs && !bombingFactories.contains(_ennemyFactory) && !Bomb.isBombing(_ennemyFactory)) {
                        end = _ennemyFactory;
                        nbcyborgs = _ennemyFactory.getNumberCyborgs();
                        //System.err.println("New strongest factory found : " + _ennemyFactory + " with " + _ennemyFactory.getNumberCyborgs() + " cyborgs");
                        int mintroop = Integer.MAX_VALUE;
                        for(Factory factory : _ennemyFactory.getClosestFactories()) {
                            if(factory.getIdPlayer() == 1 && factory.getNumberCyborgs() < mintroop) {
                                //System.err.println("New weakest factory found : " + factory);
                                start = factory;
                                mintroop = factory.getNumberCyborgs(); 
                            }
                        }
                    }
                }
				
				if(start != null && end != null && start != end) {
                    nbcyborgs = -1;
                    bombingFactories.add(end);
                    bombsLeft--;
                    myDecisions.add(new Decision(start, end, nbcyborgs, Action.BOMB));
                }
			}
			
			// Now for each other factory
			for(Map.Entry<Integer, Factory> entry : myFactories.entrySet()) {
                Factory _myFactory = entry.getValue();
				Factory _closest = null;
				minDistance = Integer.MAX_VALUE;
				System.err.println(prevMyFactories);
				if(!prevMyFactories.contains(_myFactory)) {
				    prevMyFactories.add(_myFactory);
					// It's a new factory
					System.err.println("New factory claimed : " + _myFactory);
					Factory start = _myFactory;
    				Factory end = null;
    				int nbcyborgs = 0;
    				for(Factory otherFactory : _myFactory.getClosestFactories()) {
    					end = otherFactory;
						if(nbcyborgs < start.getNumberCyborgs()) {
    						int nb = (int)(0.5*start.getNumberCyborgs())+1;
    						nbcyborgs += nb;
    						myDecisions.add(new Decision(start, end, nb, Action.MOVE));
    					}
    				}
				} else {
					// We send troop to the closest
					if(!ignoredFactories.contains(_myFactory)) {
						/*for(Factory _factory : _myFactory.getClosestFactories()) {
							if(!ignoredFactories.contains(_myFactory) && _factory.getIdPlayer() == 1 && minDistance > _myFactory.getDistanceTo(_factory)) {
								_closest = _factory;
								minDistance = _myFactory.getDistanceTo(_factory);
							}
						}
						if(_closest != null) {
							System.err.println("Sending troops from " + _myFactory + " to " + _closest);
								
							int nbCyborgs = (int)(_myFactory.getNumberCyborgs() * 0.5)+1;
							_myFactory.removeTroops(nbCyborgs);
							ignoredFactories.add(_closest);
							Decision a = new Decision(_myFactory, _closest, nbCyborgs, Action.MOVE);
							System.err.println(a);
							myDecisions.add(a);
						}*/
						
						_closest = Decision.closestFactoryToGetTo(_myFactory, myFactory);
						if(_closest != null) {
							System.err.println("Sending troops from " + _myFactory + " to " + _closest);
								
							int nbCyborgs = (int)(_myFactory.getNumberCyborgs() * 0.5)+1;
							_myFactory.removeTroops(nbCyborgs);
							myDecisions.add(new Decision(_myFactory, _closest, nbCyborgs, Action.MOVE));
						}
					}
				}
			}
			
			// We find the factory we can update
			for(Map.Entry<Integer, Factory> entry : myFactories.entrySet()) {
                Factory _myFactory = entry.getValue();
				if(_myFactory.getProduction() < 3 && _myFactory.getNumberCyborgs() > 15) {
					myDecisions.add(new Decision(_myFactory, null, 0, Action.INC));
				}
			}
			
			return myDecisions;
		}
		
		private static Factory closestFactoryToGetTo(Factory start, Factory end) {
			ArrayList<Factory> settled = new ArrayList<>();
			Factory _prev = null;
			Factory _temp = end;
			while(_temp != start) {
				if(_temp.getDistanceTo(start) == 1) {
					_prev = _temp;
					break;
				} else {
					for(Factory _factory : _temp.getClosestFactories()) {
						if(_factory.getIdPlayer() == 1 && !settled.contains(_factory)) {
							_prev = _temp;
							_temp = _factory;
							settled.add(_factory);
							break;
						}
					}
				}
			}
			
			return _prev;
		}
		
		private static void getIncommingTroops() {
			//System.err.println(" ################################## ");
            //System.err.println("Getting the incomming ennemy troops within 1 turns");
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
            /*for(Map.Entry<Factory, Integer> entry : incommingTroops.entrySet()) {
                System.err.println("Troop of " + entry.getValue() + " cyborgs incomming to factory " + entry.getKey() + " soon");
            }
            System.err.println("Ignored factories because of attacks : " + ignoredFactories);
			*/
		}
		
		public static ArrayList<Decision> sendCyborgsToEveryOne() {       
            // We find our base with the more cyborgs
            System.err.println("Sending cyborgs to every factory");
            
			ArrayList<Decision> myDecisions = new ArrayList<>();
			
			if(myFactories.size() > 0) {			
				for(Map.Entry<Integer, Factory> entry : myFactories.entrySet()) {
				    Factory myFactory = entry.getValue(); 
				    if(!ignoredFactories.contains(myFactory)) {
    					Factory start = myFactory;
    					Factory end = null;
    					int nbcyborgs = 0;
    					
    					for(Factory otherFactory : myFactory.getClosestFactories()) {
    						end = otherFactory;
    						if(nbcyborgs < start.getNumberCyborgs() && otherFactory.getIdPlayer() != 1) {
    							int nb = end.getNumberCyborgs()+1;
    							if(nb <= 0) { nb = 1; }
    							nbcyborgs += nb;
    							myDecisions.add(new Decision(start, end, nb, Action.MOVE));
    							if(!prevMyFactories.contains(start)) { prevMyFactories.add(start); }
    						}
    					}
				    }
				}
				
			}
			return myDecisions;
		}
		
	}
	
	static class Factory {
        private ArrayList<Factory> closestFactories = new ArrayList<>();
		private boolean isFactoriesSorted = false;
        private Map<Factory, Integer> linkMap = new HashMap<>();
        private int idFactory;
        private int idPlayer = 0;
        private int numberCyborgs = 0;
        private int production = 0;
        
        public void setIdPlayer(int idPlayer) { this.idPlayer = idPlayer; }
        public void setNumberCyborgs(int number) { this.numberCyborgs = number; }
        public void setProduction(int prod) { this.production = prod; }
        public int getIdFactory() { return this.idFactory; }
        public int getIdPlayer() { return this.idPlayer; }
        public int getNumberCyborgs() { return this.numberCyborgs; }
        public int getProduction() { return this.production; }
        
        public Factory(int idFactory) {
            this.idFactory = idFactory;   
        }
        
        public void addLinkToFactory(Factory factory, int distance) {
            this.linkMap.put(factory, distance);
            this.closestFactories.add(factory);
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
		
		public ArrayList<Factory> getClosestFactories() {
			if(!isFactoriesSorted) {
				ArrayList<Factory> _closestFactories = new ArrayList<>();
				
				while(_closestFactories.size() < closestFactories.size()) {
					int min = Integer.MAX_VALUE;
					Factory minfactory = null;
					
					for(Factory factory : closestFactories) {
						if(min > factory.getDistanceTo(this) && !_closestFactories.contains(factory)) {
							min = factory.getDistanceTo(this);
							minfactory = factory;
						}
					}
					
					_closestFactories.add(minfactory);
				}
				
				this.closestFactories = _closestFactories;
				this.isFactoriesSorted = true;
			}
			return this.closestFactories;
		}
        
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
	
	public enum Action { BOMB, MOVE, WAIT, INC }
}
