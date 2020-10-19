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

			if(name.contains("not")) {
				this.lastAction = Action.DO_NOTHING;
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
						this.lastAction = Action.MOVE_RIGHT;
						respond();
						numMoves++;
						return Action.MOVE_RIGHT;
					}
					case "left":{
						this.lastAction = Action.MOVE_LEFT;
						respond();
						numMoves++;
						return Action.MOVE_LEFT;
					}
					case "up": {
						this.lastAction = Action.MOVE_UP;
						respond();
						numMoves++;
						return Action.MOVE_UP;
					}
					case "down":{
						this.lastAction = Action.MOVE_DOWN;
						respond();
						numMoves++;
						return Action.MOVE_DOWN;
					}
					case "clean":{
						this.lastAction = Action.CLEAN;
						respond();
						numMoves++;
						return Action.CLEAN;
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
						numMoves++;
						return this.lastAction;
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
							case "right":{
								this.lastAction = Action.MOVE_RIGHT;
								respond();
								numMoves++;
								return Action.MOVE_RIGHT;
							}
							case "left":{
								this.lastAction = Action.MOVE_LEFT;
								respond();
								numMoves++;
								return Action.MOVE_LEFT;
							}
							case "up":{
								this.lastAction = Action.MOVE_UP;
								respond();
								numMoves++;
								return Action.MOVE_UP;
							}
							case "down":{
								this.lastAction = Action.MOVE_DOWN;
								respond();
								numMoves++;
								return Action.MOVE_DOWN;
							}
							case "clean":{
								this.lastAction = Action.CLEAN;
								respond();
								numMoves++;
								return Action.CLEAN;
							}
							case "again":{
								respond();
								numMoves++;
								return this.lastAction;
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
					case "right":{
						this.lastAction = Action.MOVE_RIGHT;
						respond();
						numMoves++;
						return Action.MOVE_RIGHT;
					}
					case "left":{
						this.lastAction = Action.MOVE_LEFT;
						respond();
						numMoves++;
						return Action.MOVE_LEFT;
					}
					case "up":{
						this.lastAction = Action.MOVE_UP;
						respond();
						numMoves++;
						return Action.MOVE_UP;
					}
					case "down":{
						this.lastAction = Action.MOVE_DOWN;
						respond();
						numMoves++;
						return Action.MOVE_DOWN;
					}
					case "clean":{
						this.lastAction = Action.CLEAN;
						respond();
						numMoves++;
						return Action.CLEAN;
					}
					default:{
					}
				}

			}

			if(type.equalsIgnoreCase("jj")){
				switch(rootW){
					case "right":{
						this.lastAction = Action.MOVE_RIGHT;
						respond();
						numMoves++;
						return Action.MOVE_RIGHT;
					}
					case "left":{
						this.lastAction = Action.MOVE_LEFT;
						respond();
						numMoves++;
						return Action.MOVE_LEFT;
					}
					case "up":{
						this.lastAction = Action.MOVE_UP;
						respond();
						numMoves++;
						return Action.MOVE_UP;
					}
					case "down":{
						this.lastAction = Action.MOVE_DOWN;
						respond();
						numMoves++;
						return Action.MOVE_DOWN;
					}
					case "clean":{
						this.lastAction = Action.CLEAN;
						respond();
						numMoves++;
						return Action.CLEAN;
					}
					default:{
					}
				}

			}

			if(type.equalsIgnoreCase("rb")){
				switch(rootW){
					case "right":{
						this.lastAction = Action.MOVE_RIGHT;
						respond();
						numMoves++;
						return Action.MOVE_RIGHT;
					}
					case "left":{
						this.lastAction = Action.MOVE_LEFT;
						respond();
						numMoves++;
						return Action.MOVE_LEFT;
					}
					case "up":{
						this.lastAction = Action.MOVE_UP;
						respond();
						numMoves++;
						return Action.MOVE_UP;
					}
					case "down":{
						this.lastAction = Action.MOVE_DOWN;
						respond();
						numMoves++;
						return Action.MOVE_DOWN;
					}
					case "clean":{
						this.lastAction = Action.CLEAN;
						respond();
						numMoves++;
						return Action.CLEAN;
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
				this.lastAction = Action.MOVE_UP;
				keywordResponse("move up.");
				numMoves++;
				return Action.MOVE_UP;
			}
			else if(name.contains("down")){
				this.lastAction = Action.MOVE_DOWN;
				keywordResponse("move down.");
				numMoves++;
				return Action.MOVE_DOWN;
			}
			else if(name.contains("right")){
				this.lastAction = Action.MOVE_RIGHT;
				keywordResponse("move right.");
				numMoves++;
				return Action.MOVE_RIGHT;
			}
			else if(name.contains("left")){
				this.lastAction = Action.MOVE_LEFT;
				keywordResponse("move left.");
				numMoves++;
				return Action.MOVE_LEFT;
			}
			else if(name.contains("clean")){
				this.lastAction = Action.CLEAN;
				keywordResponse("clean.");
				numMoves++;
				return Action.CLEAN;
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
//			System.out.printf("Pleasure to meet you %s!%n", userName);
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


}