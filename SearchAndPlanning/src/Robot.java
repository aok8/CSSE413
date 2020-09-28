import java.lang.reflect.Array;
import java.nio.file.Path;
import java.util.*;

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
	private LinkedList<Action> path;
	private boolean pathFound;
	private long openCount;
	private int pathLength;

	/**
	    Initializes a Robot on a specific tile in the environment.
	*/
//	public Robot (Environment env) { this(env, 0, 0); }

	public Robot (Environment env, int posRow, int posCol) {
		this.env = env;
		this.posRow = posRow;
		this.posCol = posCol;
		this.path = new LinkedList<>();
		this.pathFound = false;
		this.openCount = 0;
		this.pathLength = 0;
	}

	public boolean getPathFound(){
		return this.pathFound;
	}

	public long getOpenCount(){
		return this.openCount;
	}

	public int getPathLength(){
		return this.pathLength;
	}

	public void resetOpenCount() {
		this.openCount = 0;
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
		if(path.isEmpty()) {
			return Action.DO_NOTHING;
		}else{
			return path.removeFirst();
		}
	}

	/**
	 * This method implements breadth-first search. It populates the path LinkedList
	 * and sets pathFound to true, if a path has been found. IMPORTANT: This method
	 * increases the openCount field every time your code adds a node to the open
	 * data structure, i.e. the queue or priorityQueue
	 *
	 */
	public void bfs() {
		LinkedList<Position> targets = (LinkedList<Position>) env.getTargets().clone();
		LinkedList<State> queue = new LinkedList<>();
		LinkedList<State> visited = new LinkedList<>();

		int startRow = posRow; int startCol = posCol;
		queue.add(new State(startRow,startCol, 0, new LinkedList<Action>()));
		this.openCount++;
		State current;
		while(!queue.isEmpty()){
			current = queue.poll();
			visited.add(current);

			TileStatus status = this.env.getTileStatus(current.row, current.col);
			if(status == TileStatus.TARGET && containsPosition(targets, current.row, current.col)){
				removePosition(targets, current.row, current.col);
				if(targets.size() == 0 || env.getTargets().size() == 1)
				{
					this.pathFound = true;
					this.path = current.actions;
					this.pathLength = this.path.size();
					break;
				}
				else
				{
					current.numTargets++;
					queue.clear();
					visited.clear();
					LinkedList<Action> newList= (LinkedList<Action>) current.actions.clone();
					current = new State(current.row, current.col, current.numTargets, newList);
					visited.add(current);
					this.openCount++;
					// tpos = allTargets.poll();
				}
			}

			//Go check neighboring nodes
			//Left Node
			if(env.validPos(current.row, current.col-1) && !containsState((LinkedList<State>) queue.clone(), current.row, current.col-1) && !containsState((LinkedList<State>) visited.clone(), current.row, current.col-1))//if it doesnt exist on open or closed then add it to the list. Contains does not work
			{
				LinkedList<Action> newList= (LinkedList<Action>) current.actions.clone();
				newList.add(Action.MOVE_LEFT);
				queue.add(new State(current.row, current.col-1, current.numTargets, newList));//left
				this.openCount++;
			}
			//Right Node
			if(env.validPos(current.row, current.col+1) && !containsState((LinkedList<State>) queue.clone(), current.row, current.col+1) && !containsState((LinkedList<State>) visited.clone(), current.row, current.col+1))
			{
				LinkedList<Action> newList= (LinkedList<Action>) current.actions.clone();
				newList.add(Action.MOVE_RIGHT);
				queue.add(new State(current.row, current.col+1, current.numTargets, newList));//Right
				this.openCount++;
			}
			//Up Node
			if(env.validPos(current.row-1, current.col) && !containsState((LinkedList<State>) queue.clone(), current.row-1, current.col) && !containsState((LinkedList<State>) visited.clone(), current.row-1, current.col))
			{
				LinkedList<Action> newList= (LinkedList<Action>) current.actions.clone();
				newList.add(Action.MOVE_UP);
				queue.add(new State(current.row-1, current.col, current.numTargets, newList));//up
				this.openCount++;
			}
			//Down Node
			if(env.validPos(current.row+1, current.col) && !containsState((LinkedList<State>) queue.clone(), current.row+1, current.col) && !containsState((LinkedList<State>) visited.clone(), current.row+1, current.col))
			{
				LinkedList<Action> newList= (LinkedList<Action>) current.actions.clone();
				newList.add(Action.MOVE_DOWN);
				queue.add(new State(current.row+1, current.col, current.numTargets, newList));//up
				this.openCount++;
			}
		}
	}

	/**
	 * This method implements A* search for maps 0-5. It populates the path LinkedList
	 * and sets pathFound to true, if a path has been found. IMPORTANT: This method
	 * increases the openCount field every time your code adds a node to the open
	 * data structure, i.e. the queue or priorityQueue
	 *
	 */
	public void astar() {
		AState current;
		PriorityQueue<AState> queue = new PriorityQueue<>();
		LinkedList<AState> visited = new LinkedList<>();
		queue.add(new AState(
				this.posRow,
				this.posCol,
				0,
				0,
				new LinkedList<Action>(),
				(LinkedList<Position>) env.getTargets().clone()
				)
		);
		this.openCount++;

		while(!queue.isEmpty()){
			current = queue.poll();
			visited.add(current);
			if(current.targets.size() == 0){
				this.pathFound = true;
				this.path = current.actions;
				this.pathLength = current.actions.size();
				return;
			}

			//left
			if(env.validPos(current.row, current.col-1))
			{
				LinkedList<Action> newPath = (LinkedList<Action>) current.actions.clone();
				newPath.add(Action.MOVE_LEFT);
				LinkedList<Position> newTargets = (LinkedList<Position>) current.targets.clone();
				removePosition(newTargets, current.row, current.col-1);

				AState newState = new AState(current.row, current.col-1, 0, current.cost+1, newPath, newTargets);

				PriorityQueue<AState> openCopy = new PriorityQueue<AState>(queue);

				if(!containsAState(openCopy, current.row, current.col-1) && !containsAState(visited, current.row, current.col-1))
				{
					newState.finalCost = current.cost + dHeuristic(newState);
					queue.add(newState);
					this.openCount++;
				}
			}

			//right
			if(env.validPos(current.row, current.col+1))
			{
				LinkedList<Action> newPath = (LinkedList<Action>) current.actions.clone();
				newPath.add(Action.MOVE_RIGHT);
				LinkedList<Position> newTargets = (LinkedList<Position>) current.targets.clone();
				removePosition(newTargets, current.row, current.col+1);

				AState newState = new AState(current.row, current.col+1, 0, current.cost+1, newPath, newTargets);

				PriorityQueue<AState> openCopy = new PriorityQueue<AState>(queue);

				if(!containsAState(openCopy, current.row, current.col+1) && !containsAState(visited, current.row, current.col+1))
				{
					newState.finalCost = current.cost + dHeuristic(newState);
					queue.add(newState);
					this.openCount++;
				}
			}

			//up
			if(env.validPos(current.row-1, current.col))
			{
				LinkedList<Action> newPath = (LinkedList<Action>) current.actions.clone();
				newPath.add(Action.MOVE_UP);
				LinkedList<Position> newTargets = (LinkedList<Position>) current.targets.clone();
				removePosition(newTargets, current.row-1, current.col);

				AState newState = new AState(current.row-1, current.col, 0, current.cost+1, newPath, newTargets);

				PriorityQueue<AState> openCopy = new PriorityQueue<AState>(queue);

				if(!containsAState(openCopy, current.row-1, current.col) && !containsAState(visited, current.row-1, current.col))
				{
					newState.finalCost = current.cost + dHeuristic(newState);
					queue.add(newState);
					this.openCount++;
				}
			}

			//down
			if(env.validPos(current.row+1, current.col))
			{
				LinkedList<Action> newPath = (LinkedList<Action>) current.actions.clone();
				newPath.add(Action.MOVE_DOWN);
				LinkedList<Position> newTargets = (LinkedList<Position>) current.targets.clone();
				removePosition(newTargets, current.row+1, current.col);

				AState newState = new AState(current.row+1, current.col, 0, current.cost+1, newPath, newTargets);

				PriorityQueue<AState> openCopy = new PriorityQueue<AState>(queue);

				if(!containsAState(openCopy, current.row+1, current.col) && !containsAState(visited, current.row+1, current.col))
				{
					newState.finalCost = current.cost + dHeuristic(newState);
					queue.add(newState);
					this.openCount++;
				}
			}
		}
	}

	/**
	 * This method implements A* search for maps 10, 11 and 12. It populates the path LinkedList
	 * and sets pathFound to true, if a path has been found. IMPORTANT: This method
	 * increases the openCount field every time your code adds a node to the open
	 * data structure, i.e. the queue or priorityQueue
	 *
	 */
	public void astar101112() {
		AState current;
		PriorityQueue<AState> open = new PriorityQueue<AState>();
		LinkedList<AState> closed = new LinkedList<AState>();

		LinkedList<Position> targetList = (LinkedList<Position>) env.getTargets().clone();
		LinkedList<Position> newTargetList = new LinkedList<Position>();
		newTargetList.add(findClosestTarget(targetList, this.posRow, this.posCol));
		int foundTargets = 0;

		open.add(new AState(this.posRow, this.posCol, 0, 0, new LinkedList<Action>(), (LinkedList<Position>) newTargetList.clone()));
		this.openCount++;

		while(!open.isEmpty())
		{
			current = open.poll();
			closed.add(current);


			if(current.targets.size() == 0){
				if(foundTargets == env.getTargets().size()-1)
				{
					this.pathFound = true;
					this.path = current.actions;
					this.pathLength = current.actions.size();
					return;
				}
				else
				{
					removePosition(targetList, current.row, current.col);
					newTargetList.clear();
					Position pos1 = findClosestTarget(targetList, current.row, current.col);
					newTargetList.add(pos1);
					open.clear();
					closed.clear();
					open.add(new AState(current.row, current.col, current.finalCost, current.cost+1, (LinkedList<Action>) current.actions.clone(), (LinkedList<Position>) newTargetList.clone()));
					this.openCount++;
					foundTargets++;
					continue;
				}
			}

			newTargetList.clear();
			Position pos2 = findClosestTarget(targetList, current.row, current.col);
			newTargetList.add(pos2);
			current.targets = (LinkedList<Position>) newTargetList.clone();

			//Go check neighboring nodes
			//Left Node
			if(env.validPos(current.row, current.col-1))
			{
				LinkedList<Action> newPath = (LinkedList<Action>) current.actions.clone();
				newPath.add(Action.MOVE_LEFT);
				LinkedList<Position> newTargets = (LinkedList<Position>) current.targets.clone();
				removePosition(newTargets, current.row, current.col-1);

				AState newState = new AState(current.row, current.col-1, 0, current.cost+1, newPath, newTargets);

				PriorityQueue<AState> openCopy = new PriorityQueue<AState>(open);

				if(!containsAState(openCopy, current.row, current.col-1) && !containsAState(closed, current.row, current.col-1))
				{
					newState.finalCost = current.cost + dHeuristic(newState);
					open.add(newState);
					this.openCount++;
				}
			}

			//Right Node
			if(env.validPos(current.row, current.col+1))
			{
				LinkedList<Action> newPath = (LinkedList<Action>) current.actions.clone();
				newPath.add(Action.MOVE_RIGHT);
				LinkedList<Position> newTargets = (LinkedList<Position>) current.targets.clone();
				removePosition(newTargets, current.row, current.col+1);

				AState newState = new AState(current.row, current.col+1, 0, current.cost+1, newPath, newTargets);

				PriorityQueue<AState> openCopy = new PriorityQueue<AState>(open);

				if(!containsAState(openCopy, current.row, current.col+1) && !containsAState(closed, current.row, current.col+1))
				{
					newState.finalCost = current.cost + dHeuristic(newState);
					open.add(newState);
					this.openCount++;
				}
			}

			//Up Node
			if(env.validPos(current.row-1, current.col))
			{
				LinkedList<Action> newPath = (LinkedList<Action>) current.actions.clone();
				newPath.add(Action.MOVE_UP);
				LinkedList<Position> newTargets = (LinkedList<Position>) current.targets.clone();
				removePosition(newTargets, current.row-1, current.col);

				AState newState = new AState(current.row-1, current.col, 0, current.cost+1, newPath, newTargets);

				PriorityQueue<AState> openCopy = new PriorityQueue<AState>(open);

				if(!containsAState(openCopy, current.row-1, current.col) && !containsAState(closed, current.row-1, current.col))
				{
					newState.finalCost = current.cost + dHeuristic(newState);
					open.add(newState);
					this.openCount++;
				}
			}

			//Down Node
			if(env.validPos(current.row+1, current.col))
			{
				LinkedList<Action> newPath = (LinkedList<Action>) current.actions.clone();
				newPath.add(Action.MOVE_DOWN);
				LinkedList<Position> newTargets = (LinkedList<Position>) current.targets.clone();
				removePosition(newTargets, current.row+1, current.col);

				AState newState = new AState(current.row+1, current.col, 0, current.cost+1, newPath, newTargets);

				PriorityQueue<AState> openCopy = new PriorityQueue<AState>(open);

				if(!containsAState(openCopy, current.row+1, current.col) && !containsAState(closed, current.row+1, current.col))
				{
					newState.finalCost = current.cost + dHeuristic(newState);
					open.add(newState);
					this.openCount++;
				}
			}
		}
	}

	/**
	 * This method implements A* search for maps 14, 15 and 16. It populates the path LinkedList
	 * and sets pathFound to true, if a path has been found. IMPORTANT: This method
	 * increases the openCount field every time your code adds a node to the open
	 * data structure, i.e. the queue or priorityQueue
	 *
	 */
	public void astar141516() {
	}

	public boolean containsPosition(LinkedList<Position> lst, int row, int col){
		for(Position p: lst){
			if(p.row==row&&p.col==col){
				return true;
			}
		}
		return false;
	}

	public boolean containsState(LinkedList<State> lst, int row, int col){
		for(State s: lst){
			if(s.row==row&&s.col==col){
				return true;
			}
		}
		return false;
	}
	public boolean containsAState(LinkedList<AState> lst, int row, int col){
		for(AState s: lst){
			if(s.row==row&&s.col==col){
				return true;
			}
		}
		return false;
	}
	public boolean containsAState(PriorityQueue<AState> queue, int row, int col){
		for(AState s: queue){
			if(s.row==row&& s.col==col){
				return true;
			}
		}
		return false;
	}

	public void removePosition(LinkedList<Position> lst, int row, int col){
		for(int i = 0; i< lst.size(); i++){
			Position p = lst.get(i);
			if(p.row==row&&p.col==col){
				lst.remove(i);
				break;
			}
		}
	}
	public int dHeuristic (AState s){
		LinkedList<Position> temp = (LinkedList<Position>) s.targets.clone();
		Position p = temp.poll();
		if(p==null) return 0;
		else return Math.abs(s.row-p.row)+Math.abs(s.col-p.col);
	}


	public class State{
		private int row;
		private int col;
		int numTargets;
		private LinkedList<Action> actions;
		public State(int row, int col, int numTargets, LinkedList<Action> actions){
			this.row = row;
			this.col = col;
			this.numTargets = numTargets;
			this.actions = actions;
		}

		public LinkedList<Action> getActions() {
			return actions;
		}
		public void setActions(LinkedList<Action> a){
			this.actions =a;
		}
	}

	public Action getMove(State a, State b){
		if(a.row> b.row){
			return Action.MOVE_UP;
		}
		else if (a.row< b.row){
			return Action.MOVE_DOWN;
		}
		else if (a.col> b.col){
			return Action.MOVE_LEFT;
		}
		else if (a.col< b.col){
			return Action.MOVE_RIGHT;
		}
		return Action.DO_NOTHING;
	}
	public List<State> getNeighbors(State s){
		ArrayList<State> list = new ArrayList<>();
		int startPosX = s.row-1;
		int startPosY = s.col-1;
		int endPosX = s.row+1;
		int endPosY = s.col+1;

		for(int rowNum = startPosX; rowNum<= endPosX; rowNum++){
			for(int colNum = startPosY; colNum<= endPosY; colNum++){
				if(!(rowNum!=s.row && colNum != s.col)&& (rowNum!= s.row || colNum!= s.col)) {
					if (env.validPos(rowNum, colNum) && validSpace(rowNum, colNum)) {
						State temp = new State(rowNum, colNum, s.numTargets, s.actions);
						list.add(temp);
					}
				}
			}
		}
		return list;
	}


	public boolean validSpace (int rowNum, int colNum){
		return (env.getTileStatus(rowNum, colNum)!=TileStatus.DIRTY&&
				env.getTileStatus(rowNum, colNum)!=TileStatus.IMPASSABLE
		);
	}
	public class AState implements Comparable<AState>{
		int row;
		int col;
		int finalCost = 0;
		int cost;
		private LinkedList<Action> actions;
		private LinkedList<Position> targets;
		public AState(int row, int col, int finalCost, int cost, LinkedList<Action> actions, LinkedList<Position> targets){
			this.row = row;
			this.col = col;
			this.finalCost = finalCost;
			this.cost = cost;
			this.actions = actions;
			this.targets =targets;
		}

		public int compareTo(AState s){
			if(this.finalCost < s.finalCost) return -1;
			else if (this.finalCost > s.finalCost) return 1;
			else return 0;
		}
	}
	public Position findClosestTarget(LinkedList<Position> item, int row, int col)
	{
		LinkedList<Position> newList = (LinkedList<Position>) item.clone();
		Position currentSmallestPos = new Position(0, 0);
		int smallestDist = 10000;
		while(newList.size() != 0)
		{
			Position current = newList.poll();
			int distance = Math.abs(row - current.row) + Math.abs(col - current.col);
			if(distance < smallestDist)
			{
				currentSmallestPos.col = current.col;
				currentSmallestPos.row = current.row;
				smallestDist = distance;

			}
		}
		return currentSmallestPos;
	}

}