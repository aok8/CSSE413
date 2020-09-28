public class Training {

	public static void main(String[] args){
		FeedForwardNetwork n = new FeedForwardNetwork(2, 3, 1, 1);
		double[][] inputs = {		
				{0,0},
				{0,1},
				{1,0},
				{1,1}
		}; 
		double[][] desiredOutput = {{0},{1},{1},{0}};		
		n.initNetwork(inputs, desiredOutput, 0.3, 0);
		n.trainNetwork(10000, false);
		n.printWeights();
		n.testNetwork();
	}
	
}