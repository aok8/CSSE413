import java.util.*;
import java.util.Properties;

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
		}
	};

	private static ArrayList<String> unableResponses = new ArrayList<String>(){
		{
			add("I don't think I understood that. Could you please try again?");
			add("I'm not sure what you meant by that. Could you try a different command please?");
			add("I don't understand that command. Would you mind trying again?");
			add("I didn't get that. Could you try again please?");
			add("I can't understand that command. Would you try again please?");
			add("I don't know that command. Please try again.");
			add("Sorry, I don't understand that. Try again please!");
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
			String rootW = root.toString().substring(0, root.toString().length()-3);

			if(pair.size()==0){
				String command = root.toString().toLowerCase();
				command = command.split("(\\/)(?!.*\\/)")[0];
				switch(command){
					case "right": {
						this.lastAction = Action.MOVE_RIGHT;
						respond();
						return Action.MOVE_RIGHT;
					}
					case "left":{
						this.lastAction = Action.MOVE_LEFT;
						respond();
						return Action.MOVE_LEFT;
					}
					case "up": {
						this.lastAction = Action.MOVE_UP;
						respond();
						return Action.MOVE_UP;
					}
					case "down":{
						this.lastAction = Action.MOVE_DOWN;
						respond();
						return Action.MOVE_DOWN;
					}
					case "clean":{
						this.lastAction = Action.CLEAN;
						respond();
						return Action.CLEAN;
					}
					case "more":
					case "further":
					case "again":{
						respond();
						return this.lastAction;
					}
					case "thanks": {
						youreWelcome();
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
							type1.equalsIgnoreCase("jj")
					){
						switch(word){
							case "right":{
								this.lastAction = Action.MOVE_RIGHT;
								respond();
								return Action.MOVE_RIGHT;
							}
							case "left":{
								this.lastAction = Action.MOVE_LEFT;
								respond();
								return Action.MOVE_LEFT;
							}
							case "up":{
								this.lastAction = Action.MOVE_UP;
								respond();
								return Action.MOVE_UP;
							}
							case "down":{
								this.lastAction = Action.MOVE_DOWN;
								respond();
								return Action.MOVE_DOWN;
							}
							case "clean":{
								this.lastAction = Action.CLEAN;
								respond();
								return Action.CLEAN;
							}
							case "again":{
								respond();
								return this.lastAction;
							}
							case "name":{
								name();
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
						return Action.MOVE_RIGHT;
					}
					case "left":{
						this.lastAction = Action.MOVE_LEFT;
						respond();
						return Action.MOVE_LEFT;
					}
					case "up":{
						this.lastAction = Action.MOVE_UP;
						respond();
						return Action.MOVE_UP;
					}
					case "down":{
						this.lastAction = Action.MOVE_DOWN;
						respond();
						return Action.MOVE_DOWN;
					}
					case "clean":{
						this.lastAction = Action.CLEAN;
						respond();
						return Action.CLEAN;
					}
					default:{
						this.lastAction = Action.DO_NOTHING;
						return Action.DO_NOTHING;
					}
				}

			}

			if(type.equalsIgnoreCase("jj")){
				switch(rootW){
					case "right":{
						this.lastAction = Action.MOVE_RIGHT;
						respond();
						return Action.MOVE_RIGHT;
					}
					case "left":{
						this.lastAction = Action.MOVE_LEFT;
						respond();
						return Action.MOVE_LEFT;
					}
					case "up":{
						this.lastAction = Action.MOVE_UP;
						respond();
						return Action.MOVE_UP;
					}
					case "down":{
						this.lastAction = Action.MOVE_DOWN;
						respond();
						return Action.MOVE_DOWN;
					}
					case "clean":{
						this.lastAction = Action.CLEAN;
						respond();
						return Action.CLEAN;
					}
					default:{
						this.lastAction = Action.DO_NOTHING;
						return Action.DO_NOTHING;
					}
				}

			}
			if(type.equalsIgnoreCase("rb")){
				switch(rootW){
					case "right":{
						this.lastAction = Action.MOVE_RIGHT;
						respond();
						return Action.MOVE_RIGHT;
					}
					case "left":{
						this.lastAction = Action.MOVE_LEFT;
						respond();
						return Action.MOVE_LEFT;
					}
					case "up":{
						this.lastAction = Action.MOVE_UP;
						respond();
						return Action.MOVE_UP;
					}
					case "down":{
						this.lastAction = Action.MOVE_DOWN;
						respond();
						return Action.MOVE_DOWN;
					}
					case "clean":{
						this.lastAction = Action.CLEAN;
						respond();
						return Action.CLEAN;
					}
					default:{
						this.lastAction = Action.DO_NOTHING;
						return Action.DO_NOTHING;
					}
				}

			}
			if(name.contains("up")){
				this.lastAction = Action.MOVE_UP;
				keywordResponse("move up.");
				return Action.MOVE_UP;
			}
			else if(name.contains("down")){
				this.lastAction = Action.MOVE_DOWN;
				keywordResponse("move down.");
				return Action.MOVE_DOWN;
			}
			else if(name.contains("right")){
				this.lastAction = Action.MOVE_RIGHT;
				keywordResponse("move right.");
				return Action.MOVE_RIGHT;
			}
			else if(name.contains("left")){
				this.lastAction = Action.MOVE_LEFT;
				keywordResponse("move left.");
				return Action.MOVE_LEFT;
			}
			else if(name.contains("clean")){
				this.lastAction = Action.CLEAN;
				keywordResponse("clean.");
				return Action.CLEAN;
			}
			else if(name.contains("name")){
				keywordResponse("tell you my name.");
				name();
				return Action.DO_NOTHING;
			}

	    }
	    if(!sentences.isEmpty()){
			unableResponse();
		}
	    return Action.DO_NOTHING;
	}
	public static void respond(){
		int index = random.nextInt(responses.size());
		System.out.println(responses.get(index));
	}
	public static void keywordResponse(String direction){
		int index = random.nextInt(keywordResponses.size());
		System.out.println(keywordResponses.get(index)+direction);
	}
	public static void unableResponse(){
		int index = random.nextInt(unableResponses.size());
		System.out.println(unableResponses.get(index));
	}
	public static void youreWelcome(){
		System.out.println("You're welcome!");
	}
	public static void name(){
		System.out.println("Pleasure to meet you! My name is Avis, it actually stands for A Very Intelligent Sweeper.");
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