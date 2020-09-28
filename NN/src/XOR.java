
public class XOR {
	
	private static int[][] trainingData = {		
		{0,0,0},
		{0,1,1},
		{1,0,1},
		{1,1,0}}; 

	private static int trainingSetSize = 0;
	private static int inputLayerSize = 2;
	private static int hiddenLayerSize = 3;
	private static double[][] hiddenLayerWeights; // [from][to]
	private static double[] outputLayerWeights;
	private static double learningRate = 0.1; //experiment 
	
	private static void initNetwork() {
		trainingSetSize = trainingData.length;
		if (trainingSetSize == 0) {
			System.out.println("No training data.");
			return;
		}

		hiddenLayerWeights = new double[inputLayerSize][hiddenLayerSize]; 
		for (int i = 0; i < inputLayerSize; i++){
			for (int j = 0; j < hiddenLayerSize; j++){
				hiddenLayerWeights[i][j] = Math.random(); 	
			}
		}
		
		outputLayerWeights = new double[hiddenLayerSize]; 
		for (int i = 0; i < hiddenLayerSize; i++){
			outputLayerWeights[i] = Math.random(); 	// This network cannot learn if the initial weights are set to zero. Exercise: Study the algorithm and explain why
		}
	}

	private static double sigmoidActivationFunction(double input){
		return 1.0 / (1 + Math.exp(-1.0 * input));
	}
	
	private static double derivative(double value){
		return value * (1 - value);
	}
	
	public static void main(String[] args){
		initNetwork();
		trainNetwork();
//		printWeights();
		testNetwork();
	}
	
	private static void trainNetwork(){
		// k episodes
		for (int k = 0; k < 1000; k++){
			//Run through entire training set once.
			for (int example = 0; example < trainingSetSize; example++){
				double[] activationInput = new double[inputLayerSize]; // We store the activation of each node (over all input and hidden layers) as we need that data during back propagation.
				// initialize input layer with training data
				for (int inputNode = 0; inputNode < inputLayerSize; inputNode++){
					activationInput[inputNode] = trainingData[example][inputNode];
				}
				double[] activationHidden = new double[hiddenLayerSize];
				// calculate activations of hidden layers (for now, just one hidden layer)
				for (int hiddenNode = 0; hiddenNode < hiddenLayerSize; hiddenNode++) {
					double inputToNeuron = 0;
					for (int inputNode = 0; inputNode < 2; inputNode++){
						inputToNeuron += hiddenLayerWeights[inputNode][hiddenNode] * activationInput[inputNode];
					}
					activationHidden[hiddenNode] = sigmoidActivationFunction(inputToNeuron);
				}

				// For the XOR network, we assume one output node.
				double inputAtOutput = 0;
				for (int hiddenNode = 0; hiddenNode < hiddenLayerSize; hiddenNode++){
					inputAtOutput += outputLayerWeights[hiddenNode] * activationHidden[hiddenNode];
				}
				double activationOutput = sigmoidActivationFunction(inputAtOutput);
				
				// calculating errors
				// desired output is on array location 2; need to get rid of constant
				double errorOfOutputNode = derivative(activationOutput) * (trainingData[example][2] - activationOutput);

				// Calculating error of hidden layer. Special calculation since we only have one output node; i.e. no summation over next layer nodes
				// Also adjusting weights of output layer
				double[] errorOfHiddenNode = new double[hiddenLayerSize];
				for (int hiddenNode = 0; hiddenNode < hiddenLayerSize; hiddenNode++){
					errorOfHiddenNode[hiddenNode] = outputLayerWeights[hiddenNode] * errorOfOutputNode;
					errorOfHiddenNode[hiddenNode] *= derivative(activationHidden[hiddenNode]); 
				}
				
				//adjusting weights
				//adjusting weights at output layer
				for (int hiddenNode = 0; hiddenNode < hiddenLayerSize; hiddenNode++){
					outputLayerWeights[hiddenNode] += learningRate * activationHidden[hiddenNode] * errorOfOutputNode;
				}
				
				// Adjusting weights at hidden layer.
				for (int inputNode = 0; inputNode < inputLayerSize; inputNode++){
					for (int hiddenNode = 0; hiddenNode < hiddenLayerSize; hiddenNode++){
						hiddenLayerWeights[inputNode][hiddenNode] += learningRate * activationInput[inputNode] * errorOfHiddenNode[hiddenNode];
					}
				}
			}
		}
	}

	
	private static void printWeights(){
		System.out.println("Hidden layer.");
		for (int i = 0; i < inputLayerSize; i++){
			for (int j = 0; j < hiddenLayerSize; j++){
				System.out.printf("Weight from input %d to hidden %d is %f. ", i, j, hiddenLayerWeights[i][j]);
			}
		}
		System.out.println("\nOutput layer");
		for (int j = 0; j < hiddenLayerSize; j++){
			System.out.printf("Weight from hidden %d is %f. ", j, outputLayerWeights[j]);
		}
		System.out.println();
}
	
	private static void testNetwork(){
		for (int example = 0; example < trainingSetSize; example++){
			double[] activationInput = new double[inputLayerSize]; // We store the activation of each node (over all input and hidden layers) as we need that data during back propagation.
			// initialize input layer with training data
			for (int inputNode = 0; inputNode < inputLayerSize; inputNode++){
				activationInput[inputNode] = trainingData[example][inputNode];
			}
			double[] activationHidden = new double[hiddenLayerSize];
			// calculate activations of hidden layers (for now, just one hidden layer)
			for (int hiddenNode = 0; hiddenNode < hiddenLayerSize; hiddenNode++) {
				double inputToNeuron = 0;
				for (int inputNode = 0; inputNode < 2; inputNode++){
					inputToNeuron += hiddenLayerWeights[inputNode][hiddenNode] * activationInput[inputNode];
				}
				activationHidden[hiddenNode] = sigmoidActivationFunction(inputToNeuron);
			}

			// For the XOR network, we assume one output node.
			double inputAtOutput = 0;
			for (int hiddenNode = 0; hiddenNode < hiddenLayerSize; hiddenNode++){
				inputAtOutput += outputLayerWeights[hiddenNode] * activationHidden[hiddenNode];
			}
			double activationOutput = sigmoidActivationFunction(inputAtOutput);

			System.out.println("Example " + example + " has: " + activationOutput + " should be: " + trainingData[example][2]);	
		}
	}
	

}