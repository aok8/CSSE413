import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
	Represents an intelligent agent moving through a particular room.	
	The robot only has one sensor - the ability to get the status of any  
	tile in the environment through the command env.getTileStatus(row, col).
	@author Adam Gaweda, Michael Wollowski
*/

public class Robot {
	private Environment env;
	private int posRow;
	private int posCol;
	private boolean toCleanOrNotToClean;
	ArrayList<Action> currentPlan = new ArrayList<>();
	static boolean pathFound = false;
	ArrayList<Robot> otherRobots;
	Position target;

	
	/**
	    Initializes a Robot on a specific tile in the environment. 
	*/

	
	public Robot (Environment env, int posRow, int posCol) {
		this.env = env;
		this.posRow = posRow;
		this.posCol = posCol;
		this.toCleanOrNotToClean = false;
	}
	
	public int getPosRow() { return posRow; }
	public int getPosCol() { return posCol; }
	public void incPosRow() { posRow++; }
	public void decPosRow() { posRow--; }
	public void incPosCol() { posCol++; }
    public void decPosCol() { posCol--; }
	
	/**
	   Returns the next action to be taken by the robot. A support function 
	   that processes the path LinkedList that has been populates by the
	   search functions.
	*/
	public Action getAction () {
		//need to add the ability to look for the next nearest position if already taken, right now all robots can find the same target position
		otherRobots = env.getRobots();
//		if (toCleanOrNotToClean) {
//			toCleanOrNotToClean = false;
//			return Action.CLEAN;
//		}
		toCleanOrNotToClean = true;
		if(target==null){ // there is no target node
			Position pos = getClosestAvailablePosition();
			if(pos.col!=-1&& pos.row!=-1){
				System.out.println("BFS");
				bfs(pos.row, pos.col);
				target = pos;
				System.out.println("ROBOT "+posRow+","+posCol+" TARGETING: "+target.row+", "+ target.col);
			}else{
				return Action.DO_NOTHING;
			}
		}
		else{ // there is a target node
			if(!currentPlan.isEmpty()) {
				Action answer = currentPlan.get(0);
				currentPlan.remove(0);
				switch (answer){
					case MOVE_UP:{
						if(robotInPosition(posRow-1,posCol)){
							bfs(target.row, target.col);
							if(!currentPlan.isEmpty()){
								answer = currentPlan.get(0);
								currentPlan.remove(0);
								return answer;
							}else{
								return Action.DO_NOTHING;
							}
						}
						break;
					}
					case MOVE_DOWN:{
						if(robotInPosition(posRow+1,posCol)){
							bfs(target.row, target.col);
							if(!currentPlan.isEmpty()){
								answer = currentPlan.get(0);
								currentPlan.remove(0);
								return answer;
							}else{
								return Action.DO_NOTHING;
							}
						}
						break;
					}
					case MOVE_LEFT:{
						if(robotInPosition(posRow,posCol+1)){
							bfs(target.row, target.col);
							if(!currentPlan.isEmpty()){
								answer = currentPlan.get(0);
								currentPlan.remove(0);
								return answer;
							}else{
								return Action.DO_NOTHING;
							}
						}
						break;
					}
					case MOVE_RIGHT:{
						if(robotInPosition(posRow,posCol-1)){
							bfs(target.row, target.col);
							if(!currentPlan.isEmpty()){
								answer = currentPlan.get(0);
								currentPlan.remove(0);
								return answer;
							}else{
								return Action.DO_NOTHING;
							}
						}
						break;
					}
					default: {
						return answer;
					}
				}
				return answer;
			}else{
				target = null;
				return Action.CLEAN;
			}
		}
		return Action.DO_NOTHING;
	}
	public Position getClosestPosition(){
		Tile[][] tiles  = env.getTiles();
		Position pos = new Position(-1,-1);
		double minDistance = Double.MAX_VALUE;
		for(int i=0; i< tiles.length; i++){
			for(int j =0; j<tiles[0].length; j++){
				if(env.getTileStatus(i,j) == TileStatus.DIRTY){
					double distance = distance(posRow, i, posCol, j);
					if(distance<minDistance){
						pos.row = i;
						pos.col = j;
						minDistance = distance;
					}
				}
			}
		}
		return pos;
	}
	public boolean targetCheck(int row, int col){
		this.otherRobots = env.getRobots();
		if(otherRobots!=null){
			for(Robot r: otherRobots){
				if(r!= this && r.target!= null){
					if(r.target.col == col && r.target.row == row){
						return true;
					}
				}
			}
		}
		return false;
	}
	public Position getClosestAvailablePosition(){
		Tile[][] tiles  = env.getTiles();
		Position pos = new Position(-1,-1);
		double minDistance = Double.MAX_VALUE;
		for(int i=0; i< tiles.length; i++){
			for(int j =0; j<tiles[0].length; j++){
				if(env.getTileStatus(i,j) == TileStatus.DIRTY){
					if(!targetCheck(i,j)){
						double distance = distance(posRow, i, posCol, j);
						if(distance<minDistance){
							pos.row = i;
							pos.col = j;
							minDistance = distance;
						}
					}
				}
			}
		}
		return pos;
	}
//	public static double distance(int x1, int x2, int y1, int y2){
//		return Math.sqrt(Math.pow((x2-x1),2) + Math.pow((y2-y1), 2));
//	}
	public static double distance(int x1, int x2, int y1, int y2){
		return Math.abs(x1-x2)+Math.abs(y1-y2);
	}
	private class State {
		private Position pos;
		private double heuristicValue;
		private ArrayList<Action> actions;

		public State(Position p, ArrayList<Action> a) {
			this.pos = p;
			this.actions = a;
		}

		public Position getPos() {
			return this.pos;
		}

		public ArrayList<Action> getActions() {
			ArrayList<Action> temp = new ArrayList<Action>(this.actions);
			return temp;
		}
	}

	public void bfs(int row, int col) {
		Tile[][] tiles = this.env.getTiles();
		Queue<State> open = new LinkedList<>();
		LinkedList<Tile> openTiles = new LinkedList<Tile>();
		LinkedList<Tile> closed = new LinkedList<Tile>();
		//get the locations of the targets
		//LinkedList<Position> targets = env.getTargets();
		//add the root to the open queue
		open.offer(new State(new Position(this.posRow, this.posCol), new ArrayList<>()));
		openTiles.add(tiles[this.posRow][this.posCol]);

		while (true) {
			//failure criteria
			if (open.size() == 0) {
				System.out.println("hit failure criteria");
				return;
			}
			State current = open.poll();
			int cRow = current.getPos().row;
			int cCol = current.getPos().col;
			closed.add(tiles[cRow][cCol]);
			openTiles.remove(tiles[cRow][cCol]);

			boolean locationReached = false;
			if (cRow == row && cCol == col) {
				locationReached = true;
				this.currentPlan = current.getActions();
				this.pathFound = true;
				return;
			}

			if (!locationReached) {
				//create states for the children
				//checking tile below current
				if (env.validPos(cRow + 1, cCol)&&!robotInPosition(cRow+1, cCol)) {
					if (openTiles.contains(tiles[cRow + 1][cCol]) || closed.contains(tiles[cRow + 1][cCol])) {
						//do nothing
					} else {
						ArrayList<Action> newActions = current.getActions();
						newActions.add(Action.MOVE_DOWN);
						open.offer(new State(new Position(cRow + 1, cCol), newActions));
						openTiles.add(tiles[cRow + 1][cCol]);
					}
				}
				//checking tile above current
				if (env.validPos(cRow - 1, cCol)&&!robotInPosition(cRow-1, cCol)) {
					if (openTiles.contains(tiles[cRow - 1][cCol]) || closed.contains(tiles[cRow - 1][cCol])) {
						//do nothing
					} else {
						ArrayList<Action> newActions = current.getActions();
						newActions.add(Action.MOVE_UP);
						open.offer(new State(new Position(cRow - 1, cCol), newActions));
						openTiles.add(tiles[cRow - 1][cCol]);
					}
				}
				//checking tile to the right of current
				if (env.validPos(cRow, cCol + 1)&&!robotInPosition(cRow, cCol+1)) {
					if (openTiles.contains(tiles[cRow][cCol + 1]) || closed.contains(tiles[cRow][cCol + 1])) {
						//do nothing
					} else {
						ArrayList<Action> newActions = current.getActions();
						newActions.add(Action.MOVE_RIGHT);
						open.offer(new State(new Position(cRow, cCol + 1), newActions));
						openTiles.add(tiles[cRow][cCol + 1]);
					}
				}
				//checking tile to the left of current
				if (env.validPos(cRow, cCol - 1)&&!robotInPosition(cRow, cCol-1)) {
					if (openTiles.contains(tiles[cRow][cCol - 1]) || closed.contains(tiles[cRow][cCol - 1])) {
						//do nothing
					} else {
						ArrayList<Action> newActions = current.getActions();
						newActions.add(Action.MOVE_LEFT);
						open.offer(new State(new Position(cRow, cCol - 1), newActions));
						openTiles.add(tiles[cRow][cCol - 1]);
					}
				}
			}
		}
	}
	public boolean robotInPosition(int row, int col){
		for(Robot r: env.getRobots()){
			if(r!= this){
				if(r.posRow == row && r.posCol == col){
					return true;
				}
			}
		}
		return false;
	}



}