import java.util.*;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Pair;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

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
	private static String myName = "Avis";

	private Properties props;
	private StanfordCoreNLP pipeline;
	private Action lastAction = Action.DO_NOTHING;
	static Random random = new Random();
	static int numMoves = 0;
	static String userName = "";
	static boolean learnedUserName = false;
	static boolean recording = false;
	ArrayList<Action> currentPlan = new ArrayList<>();
	HashMap<String, ArrayList<Action>> plans = new HashMap<>();
	static boolean executingPlan = false;
	static boolean pathFound = false;
	private static ArrayList<String> responses = new ArrayList<String>(){
		{
			add("Got it.");
			add("Roger that.");
			add("10-4.");
			add("Will do.");
			add("OK.");
		}
	};
	private static ArrayList<String> keywordResponses = new ArrayList<String>(){
		{
			add("I think you wanted me to ");
			add("I think you meant for me to ");
			add("I think you want me to ");
			add("I'm pretty sure you meant for me to ");
			add("I'm guessing you wanted me to ");
		}
	};

	private static ArrayList<String> unableResponses = new ArrayList<String>(){
		{
			add("I don't think I understood that. Could you please try again?");
			add("I'm not sure what you meant by that. Could you try a different command please?");
			add("I don't understand that command. Would you mind trying again?");
			add("I didn't get that. Could you try again please?");
			add("I can't understand that command. Would you try again please?");
			add("Could you try again please?");
			add("I didn't catch that, could you try again please?");
			add("I'm not sure what that meant, would you mind trying again?");
			add("I didn't get that. Please try again.");
			add("Hmm, I'm not sure what that meant. Could you please try again?");
		}
	};

	private static ArrayList<String> praiseResponses =  new ArrayList<String>(){
		{
			add("Thank you! I do try my best.");
			add("Glad I could be of service!");
		}
	};

	private static ArrayList<String> exitResponses = new ArrayList<String>(){
		{
			add("Have a nice day!");
			add("I'll see you later!");
			add("Goodbye!");
			add("See you later!");
		}
	};

	private Scanner sc;

	/**
	    Initializes a Robot on a specific tile in the environment.
	*/


	public Robot (Environment env, int posRow, int posCol) {
		this.env = env;
		this.posRow = posRow;
		this.posCol = posCol;

	    props = new Properties();
	    props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
	    pipeline = new StanfordCoreNLP(props);


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
		if(executingPlan){
			if(!currentPlan.isEmpty()){
				Action temp = currentPlan.get(0);
				currentPlan.remove(0);
				return temp;
			}
			speakAndPrint("Plan finished.");
			stopExecution();
			return Action.DO_NOTHING;
		}
	    Annotation annotation;
	    System.out.print("> ");
	    sc = new Scanner(System.in);
        String name = sc.nextLine();
//	    System.out.println("got: " + name);
        annotation = new Annotation(name);
	    pipeline.annotate(annotation);
	    List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);

	    if (sentences != null && ! sentences.isEmpty()) {
	    	CoreMap sentence = sentences.get(0);
	    	SemanticGraph graph = sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
	    	name = name.toLowerCase();
			if(name.contains("not")) {
				this.lastAction = Action.DO_NOTHING;
				return Action.DO_NOTHING;
			}
			else if(name.equalsIgnoreCase("begin record")){
				speakAndPrint("Recording.");
				startRecord();
				return Action.DO_NOTHING;
			}
			else if(name.equalsIgnoreCase("end record")){
				endRecord();
				speakAndPrint("Recording stopped.");
				speakAndPrint("You should store this plan using the command \"name the plan ____\".");
				return Action.DO_NOTHING;
			}
			else if(name.contains("name the plan")){
				if(name.length()<=14){
					String error = String.format("My apologies, I can only accept this command given in the form: " +
							"name the plan (plan1 name).");
					speakAndPrint(error);
				}
				String planName = name.substring(14);
				planName = planName.toLowerCase();
				storePlan(planName);
				return Action.DO_NOTHING;
			}
			else if(name.contains("name plan")){
				if(name.length()<=10){
					String error = String.format("My apologies, I can only accept this command given in the form: " +
							"name plan (plan1 name).");
					speakAndPrint(error);
				}
				String planName = name.substring(10);
				planName = planName.toLowerCase();
				storePlan(planName);
				return Action.DO_NOTHING;
			}
			else if(name.contains("execute plan")){
				if(name.length()<=13){
					String error = String.format("My apologies, I can only accept this command given in the form: " +
							"execute plan (plan1 name).");
					speakAndPrint(error);
				}
				String planName = name.substring(13);
				planName = planName.toLowerCase();
				setupPlanToExecute(planName);
				return Action.DO_NOTHING;
			}
			else if(name.contains("execute the plan")){
				if(name.length()<=17){
					String error = String.format("My apologies, I can only accept this command given in the form: " +
							"execute the plan (plan name).");
					speakAndPrint(error);
				}
				String planName = name.substring(17);
				planName = planName.toLowerCase();
				setupPlanToExecute(planName);
				return Action.DO_NOTHING;
			}
			else if(name.contains("execute symmetric plan")){
				String planName = name.substring(23);
				planName.toLowerCase();
				executeSymmetricPlan(planName);
				return Action.DO_NOTHING;
			}
			else if(name.contains("combine plan")){
				name.toLowerCase();
				combinePlans(name);
				return Action.DO_NOTHING;
			}
			else if(name.contains("find a path")) {
				Scanner in = new Scanner(name).useDelimiter("[^0-9]+");
				int row = in.nextInt();
				int col = in.nextInt();
				System.out.println("Target Position: " + "[" + row + "," + col + "]");
				bfs(row, col);
				if(pathFound) {
					speakAndPrint("You should store this plan using the command 'name the plan ____'.");
				} else {
					speakAndPrint("A path couldn't be found to this position.");
				}
				return Action.DO_NOTHING;
			}
			IndexedWord root = graph.getFirstRoot();
			List<Pair<GrammaticalRelation, IndexedWord>> pair = graph.childPairs(root);
			String type = root.tag();
			String rootW = root.toString().split("(\\/)(?!.*\\/)")[0];

			if(pair.size()==0){
				String command = root.toString().toLowerCase();
				command = command.split("(\\/)(?!.*\\/)")[0];
				switch(command){
					case "right": {
						respond();
						return right();
					}
					case "left":{
						respond();
						return left();
					}
					case "up": {
						respond();
						return up();
					}
					case "down":{
						respond();
						return down();
					}
					case "clean":{
						respond();
						return clean();
					}
					case "name":{
						name();
						return Action.DO_NOTHING;
					}
					case "hi":
					case "hello":{
						speakAndPrint("Hello, how may I be of service?");
						return Action.DO_NOTHING;
					}
					case "more":
					case "further":
					case "again":{
						respond();
						return again();
					}
					case "thanks": {
						youreWelcome();
						return Action.DO_NOTHING;
					}
					case "quit":
					case "goodbye":
					case "exit":
					case "bye":{
						exit();
					}
					case "moves":{
						moveCount();
						return Action.DO_NOTHING;
					}
					default:{
						unableResponse();
						this.lastAction = Action.DO_NOTHING;
						return Action.DO_NOTHING;
					}
				}
			}
			if(pair.size()> 0){
				for(int i =0; i<pair.size(); i++){
					Pair<GrammaticalRelation, IndexedWord> prt = pair.get(i);
					String command = prt.second.toString().toLowerCase();
					String type1 = command.split("(\\/)(?!.*\\/)")[1];
					String word = command.split("(\\/)(?!.*\\/)")[0];
					if(
							type1.equalsIgnoreCase("rb") ||
							type1.equalsIgnoreCase("rp")||
							type1.equalsIgnoreCase("vb")||
							type1.equalsIgnoreCase("nn")||
							type1.equalsIgnoreCase("jj")||
							type1.equalsIgnoreCase("nns")
					){
						switch(word){
							case "right": {
								respond();
								return right();
							}
							case "left":{
								respond();
								return left();
							}
							case "up": {
								respond();
								return up();
							}
							case "down":{
								respond();
								return down();
							}
							case "clean":{
								respond();
								return clean();
							}
							case "again":{
								respond();
								return again();
							}
							case "name":{
								name();
								return Action.DO_NOTHING;
							}
							case "good":
							case "nice":{
								praiseResponse();
								return Action.DO_NOTHING;
							}
							case "thank":{
								youreWelcome();
								return Action.DO_NOTHING;
							}
							case "moves":{
								moveCount();
								return Action.DO_NOTHING;
							}
							default:{
								break;
							}
						}
					}
				}
			}

			if(type.equalsIgnoreCase("VB")){
				switch(rootW){
					case "right": {
						respond();
						return right();
					}
					case "left":{
						respond();
						return left();
					}
					case "up": {
						respond();
						return up();
					}
					case "down":{
						respond();
						return down();
					}
					case "clean":{
						respond();
						return clean();
					}
					default:{
					}
				}

			}

			if(type.equalsIgnoreCase("jj")){
				switch(rootW){
					case "right": {
						respond();
						return right();
					}
					case "left":{
						respond();
						return left();
					}
					case "up": {
						respond();
						return up();
					}
					case "down":{
						respond();
						return down();
					}
					case "clean":{
						respond();
						return clean();
					}
					default:{
					}
				}

			}

			if(type.equalsIgnoreCase("rb")){
				switch(rootW){
					case "right": {
						respond();
						return right();
					}
					case "left":{
						respond();
						return left();
					}
					case "up": {
						respond();
						return up();
					}
					case "down":{
						respond();
						return down();
					}
					case "clean":{
						respond();
						return clean();
					}
					default:{
					}
				}

			}

			if(type.equalsIgnoreCase("vbp")){
				switch(rootW){
					case "thank":{
						youreWelcome();
						return Action.DO_NOTHING;
					}
					default:{
					}
				}

			}
			if(name.contains("up")){
				keywordResponse("move up.");
				return up();
			}
			else if(name.contains("down")){
				keywordResponse("move down.");
				return down();
			}
			else if(name.contains("right")){
				keywordResponse("move right.");
				return right();
			}
			else if(name.contains("left")){
				keywordResponse("move left.");
				return left();
			}
			else if(name.contains("clean")){
				keywordResponse("clean.");
				return clean();
			}
			else if(name.contains("name")){
				keywordResponse("tell you my name.");
				name();
				return Action.DO_NOTHING;
			}

	    }
	    unableResponse();
	    return Action.DO_NOTHING;
	}

	public Action up(){
		if(recording){
			currentPlan.add(Action.MOVE_UP);
		}
		this.lastAction = Action.MOVE_UP;
		numMoves++;
		return Action.MOVE_UP;
	}

	public Action down(){
		if(recording){
			currentPlan.add(Action.MOVE_DOWN);
		}
		this.lastAction = Action.MOVE_DOWN;
		numMoves++;
		return Action.MOVE_DOWN;
	}

	public Action left(){
		if(recording){
			currentPlan.add(Action.MOVE_LEFT);
		}
		this.lastAction = Action.MOVE_LEFT;
		numMoves++;
		return Action.MOVE_LEFT;
	}

	public Action right(){
		if(recording){
			currentPlan.add(Action.MOVE_RIGHT);
		}
		this.lastAction = Action.MOVE_RIGHT;
		numMoves++;
		return Action.MOVE_RIGHT;
	}

	public Action clean(){
		if(recording){
			currentPlan.add(Action.CLEAN);
		}
		this.lastAction = Action.CLEAN;
		numMoves++;
		return Action.CLEAN;
	}

	public Action again(){
		if(recording){
			currentPlan.add(this.lastAction);
		}
		numMoves++;
		return this.lastAction;
	}

	public void startRecord(){
		if (!recording){
			currentPlan = new ArrayList<>();
		}
		recording = true;
	}
	public void endRecord(){
		if(recording){
			recording= false;
		}
		else{
			speakAndPrint("I was not recording.");
		}
	}

	public void storePlan(String name){
		if(currentPlan.isEmpty()){
			speakAndPrint("Current plan is empty.");
		}
		else if(plans.containsKey(name)){
			String error = String.format("My apologies, there is already a plan with the name %s.%n" +
					"Please try again with a different name.", name);
			speakAndPrint(error);
		}
		else{
			plans.put(name, currentPlan);
			String answer = String.format("Named the plan %s.", name);
			speakAndPrint(answer);
			currentPlan = new ArrayList<>();
			recording = false;
		}
	}
	public void setupPlanToExecute(String name){
		if(!plans.containsKey(name)){
			String error = String.format("My apologies, there is no plan with the name %s.", name);
			speakAndPrint(error);
		}
		else{
			String starting = String.format("Starting plan %s.", name);
			speakAndPrint(starting);
			recording = false;
			currentPlan = (ArrayList<Action>)plans.get(name).clone();
			executingPlan = true;
		}
	}

	public void executeSymmetricPlan(String name){
		if(!plans.containsKey(name)){
			String error = String.format("My apologies, there is no plan with the name %s.", name);
			speakAndPrint(error);
		}
		else{
			String starting = String.format("Starting symmetric plan %s.", name);
			speakAndPrint(starting);
			recording = false;
			ArrayList<Action> temp = (ArrayList<Action>)plans.get(name).clone();
			currentPlan = giveSymmetricPlan(temp);
			executingPlan = true;
		}
	}
	public ArrayList<Action> giveSymmetricPlan(ArrayList<Action> orig){
		if(orig.isEmpty()){
			return orig;
		}
		ArrayList<Action> symmetric = new ArrayList<>();
		for(Action a: orig){
			switch (a){
				case MOVE_UP:
					symmetric.add(Action.MOVE_DOWN);
					break;
				case MOVE_DOWN:
					symmetric.add(Action.MOVE_UP);
					break;
				case MOVE_LEFT:
					symmetric.add(Action.MOVE_RIGHT);
					break;
				case MOVE_RIGHT:
					symmetric.add(Action.MOVE_LEFT);
					break;
				case CLEAN:
					symmetric.add(Action.CLEAN);
					break;
				default:
					symmetric.add(Action.DO_NOTHING);
					break;
			}
		}
		return symmetric;
	}
	public void stopExecution(){
		currentPlan = new ArrayList<>();
		executingPlan = false;
	}
	public void combinePlans(String name){
		if(name.length()<=13){
			String error = String.format("My apologies, I can only accept this command given in the form: " +
					"combine plan (plan1 name) and (plan2 name).");
			speakAndPrint(error);
			return;
		}
		String test = name.substring(13);
		if(!test.contains(" ")){
			String error = String.format("My apologies, I can only accept this command given in the form: " +
					"combine plan (plan1 name) and (plan2 name).");
			speakAndPrint(error);
		}
		else if(!test.contains("and")){
			String error = String.format("My apologies, I can only accept this command given in the form: " +
					"combine plan (plan1 name) and (plan2 name).");
			speakAndPrint(error);
		}
		else{
			String[] planString = test.split(" and ");
			if(planString.length!= 2){
				String error = String.format("My apologies, I can only accept this command given in the form: " +
						"combine plan (plan1 name) and (plan2 name).");
				speakAndPrint(error);
			}
			else{
				String plan1 = planString[0];
				String plan2 = planString[1];
				if(plans.containsKey(plan1)){
					if(plans.containsKey(plan2)){
						recording = false;
						executingPlan = false;
						this.currentPlan = (ArrayList<Action>)plans.get(plan1).clone();
						this.currentPlan.addAll((ArrayList<Action>)plans.get(plan2).clone());
						speakAndPrint("I combined the plans successfully.");
						speakAndPrint("You should store this plan using the command \"name the plan ____\".");
					}
					else{
						String error = String.format("My apologies, there is no plan with the name %s.", plan2);
						speakAndPrint(error);
					}
				}
				else{
					String error = String.format("My apologies, there is no plan with the name %s.", plan1);
					speakAndPrint(error);
				}
			}
		}
	}

	public static void respond(){
		int index = random.nextInt(responses.size());
		speakAndPrint(responses.get(index));
		int praise = random.nextInt(15);
		if(praise == 8) praiseUser();
	}
	public static void keywordResponse(String direction){
		int index = random.nextInt(keywordResponses.size());
		speakAndPrint(keywordResponses.get(index)+direction);
	}
	public static void unableResponse(){
		int index = random.nextInt(unableResponses.size());
		speakAndPrint(unableResponses.get(index));
	}
	public static void praiseResponse(){
		int index = random.nextInt(praiseResponses.size());
		speakAndPrint(praiseResponses.get(index));
	}
	public static void youreWelcome(){
		speakAndPrint("You're welcome!");
	}
	public static void name(){
		speakAndPrint("My name is Avis, it stands for A Very Intelligent Sweeper.");
		if(!learnedUserName){
			try{
				TimeUnit.SECONDS.sleep(2);
			}
			catch(Exception e){
				//dont log it, this is just for user feel
			}
			speakAndPrint("What is your name?");
			System.out.printf("> ");
			Scanner ns = new Scanner(System.in);
			String name = ns.nextLine();
			name = name.toLowerCase();
			name = name.substring(0,1).toUpperCase()+ name.substring(1);
			learnedUserName = true;
			userName = name;
			String input = String.format("Pleasure to meet you %s!", userName);
			System.out.println(input);
			speakText(input);
			addNameResponses();
		}
	}
	public static void addNameResponses(){
		responses.add(String.format("Sure thing %s!", userName));
		responses.add(String.format("Just for you %s.", userName));
		unableResponses.add(String.format("I'm not sure I understand that %s. Could you try again?", userName));
		unableResponses.add(String.format("I don't get what you mean by that %s. Please try again.", userName));
		unableResponses.add(String.format("I don't know that command %s. Please try again.", userName));
		unableResponses.add(String.format("Sorry %s, I don't understand that. Try again please!", userName));
		praiseResponses.add(String.format("Thank you %s!", userName));
		praiseResponses.add(String.format("I appreciate it %s!", userName));
		praiseResponses.add(String.format("Thanks %s, I'm trying my best!", userName));
		exitResponses.add(String.format("Thanks for working with me %s!", userName));
		exitResponses.add(String.format("It has been a pleasure, %s.", userName));
		exitResponses.add(String.format("It was great working with you %s!",userName));
		exitResponses.add(String.format("Glad to help%s! Have a nice day!", userName));
		exitResponses.add(String.format("It was a pleasure working with you %s. Have a great day!", userName));
	}
	public static void exit(){
		int index = random.nextInt(exitResponses.size());
		speakAndPrint(exitResponses.get(index));
		System.exit(0);
	}

	public static void moveCount(){
		String input = String.format("I have made %d moves this session.", numMoves);
		System.out.println(input);
		speakText(input);
	}
	public static void speakText(String input){
		System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
		Voice voice;
		voice = VoiceManager.getInstance().getVoice("kevin16");
		if(voice!=null){
			voice.allocate();
		}
		try {
			voice.setRate(190);
			voice.setPitch(150);
			voice.setVolume(50);
			voice.speak(input);
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	public static void speakAndPrint(String input){
		System.out.println(input);
		speakText(input);
	}

	public static void praiseUser(){
		String input;
		if(learnedUserName){
			input = String.format("I really appreciate you giving me commands, it is a pleasure to work with you %s!", userName);
		}
		else{
			input = "I really appreciate you giving me commands, it is a pleasure to work with you!";
		}
		System.out.println(input);
		speakText(input);
	}

	static public void processDeterminer(SemanticGraph dependencies, IndexedWord root){
		List<Pair<GrammaticalRelation,IndexedWord>> s = dependencies.childPairs(root);

		System.out.println("Identity of object: " + root.originalText().toLowerCase());
	}

	//Processes: {That, this, the} {block, sphere}
	static public void processNounPhrase(SemanticGraph dependencies, IndexedWord root){
		List<Pair<GrammaticalRelation,IndexedWord>> s = dependencies.childPairs(root);

		System.out.println("Identity of object: " + root.originalText().toLowerCase());
		System.out.println("Type of object: " + s.get(0).second.originalText().toLowerCase());
	}

	// Processes: {Pick up, put down} {that, this} {block, sphere}
	static public void processVerbPhrase(SemanticGraph dependencies, IndexedWord root){
		List<Pair<GrammaticalRelation,IndexedWord>> s = dependencies.childPairs(root);
		Pair<GrammaticalRelation,IndexedWord> prt = s.get(0);
		Pair<GrammaticalRelation,IndexedWord> dobj = s.get(1);

		List<Pair<GrammaticalRelation,IndexedWord>> newS = dependencies.childPairs(dobj.second);

		System.out.println("Action: " + root.originalText().toLowerCase() + prt.second.originalText().toLowerCase());
		System.out.println("Type of object: " + dobj.second.originalText().toLowerCase());
		System.out.println("Identity of object: " + newS.get(0).second.originalText().toLowerCase());
	}
	public void bfs(int row, int col) {
		Tile[][] tiles = this.env.getTiles();
		Queue<State> open = new LinkedList<State>();
		LinkedList<Tile> openTiles = new LinkedList<Tile>();
		LinkedList<Tile> closed = new LinkedList<Tile>();
		//get the locations of the targets
		//LinkedList<Position> targets = env.getTargets();
		//add the root to the open queue
		open.offer(new State(new Position(this.posRow, this.posCol), new ArrayList<>()));
		openTiles.add(tiles[this.posRow][this.posCol]);

		while(true) {
			//failure criteria
			if(open.size() == 0) {
				System.out.println("hit failure criteria");
				return;
			}
			State current = open.poll();
			int cRow = current.getPos().row;
			int cCol = current.getPos().col;
			closed.add(tiles[cRow][cCol]);
			openTiles.remove(tiles[cRow][cCol]);

			boolean locationReached = false;
			if(cRow == row && cCol == col) {
				locationReached = true;
				this.currentPlan = current.getActions();
				this.pathFound = true;
				return;
			}

			if(!locationReached) {
				//create states for the children
				//checking tile below current
				if(env.validPos(cRow + 1, cCol)) {
					if(openTiles.contains(tiles[cRow + 1][cCol]) || closed.contains(tiles[cRow + 1][cCol])) {
						//do nothing
					} else {
						ArrayList<Action> newActions = current.getActions();
						newActions.add(Action.MOVE_DOWN);
						open.offer(new State(new Position(cRow + 1, cCol), newActions));
						openTiles.add(tiles[cRow + 1][cCol]);
					}
				}
				//checking tile above current
				if(env.validPos(cRow - 1, cCol)) {
					if(openTiles.contains(tiles[cRow - 1][cCol]) || closed.contains(tiles[cRow - 1][cCol])) {
						//do nothing
					} else {
						ArrayList<Action> newActions = current.getActions();
						newActions.add(Action.MOVE_UP);
						open.offer(new State(new Position(cRow - 1, cCol), newActions));
						openTiles.add(tiles[cRow - 1][cCol]);
					}
				}
				//checking tile to the right of current
				if(env.validPos(cRow, cCol + 1)) {
					if(openTiles.contains(tiles[cRow][cCol + 1]) || closed.contains(tiles[cRow][cCol + 1])) {
						//do nothing
					} else {
						ArrayList<Action> newActions = current.getActions();
						newActions.add(Action.MOVE_RIGHT);
						open.offer(new State(new Position(cRow, cCol + 1), newActions));
						openTiles.add(tiles[cRow][cCol + 1]);
					}
				}
				//checking tile to the left of current
				if(env.validPos(cRow, cCol - 1)) {
					if(openTiles.contains(tiles[cRow][cCol -1]) || closed.contains(tiles[cRow][cCol - 1])) {
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

	//this class is used while searching. it stores a tile and its position
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


}