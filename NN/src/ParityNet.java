import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class ParityNet {
	
	private int inputLayerSize = 0;
	private int hiddenLayerSize = 0;
	private int numHiddenLayers = 0;
	private int outputLayerSize = 0;
	private int maxLayerSize = 0;
	private static double threshold = 0.01;
	
	public ParityNet(int inputLayerSize, int hiddenLayerSize, int numHiddenLayers, int outputLayerSize){
		if (inputLayerSize <= 0) {
			System.out.println("Input layer must have at least one neuron.");
			System.exit(0);
		}
		this.inputLayerSize = inputLayerSize;
		
		if (hiddenLayerSize <= 0) {
			System.out.println("Hidden layer must have at least one neuron.");
			System.exit(0);
		}
		this.hiddenLayerSize = hiddenLayerSize;
		
		if (numHiddenLayers <= 0) {
			System.out.println("Must have at least one hidden layer.");
			System.exit(0);
		}
		this.numHiddenLayers = numHiddenLayers;
		
		if (outputLayerSize <= 0) {
			System.out.println("Output layer must have at least one neuron.");
			System.exit(0);
		}
		this.outputLayerSize = outputLayerSize;
		
		this.maxLayerSize = Math.max(Math.max(inputLayerSize, hiddenLayerSize), outputLayerSize);
	}

	private int trainingSetSize = 0;
	private double learningRate = 0;	
	private double inputs[][];
	private double desiredOutput[][];
	private double[][][] weights; // [layer][from][to]

	
	public void initNetwork(double[][] inputs, double[][] desiredOutput) {
		this.trainingSetSize = inputs.length;
		if (this.trainingSetSize == 0) {
			System.out.println("No training data.");
			System.exit(0);
		}
		if (inputs[0].length != this.inputLayerSize) {
			System.out.println("Mismatch between input layer size and training data length.");
			System.exit(0);
		}		
		this.inputs = inputs; 
		this.desiredOutput = desiredOutput;
		this.learningRate = learningRate;
		
		// To simplify the code, we generate a slightly oversized weight matrix. 
		// Not to worry though, we will only use what we need.
		this.weights = new double[this.numHiddenLayers+1][this.maxLayerSize][this.maxLayerSize]; 
		for (int l = 0; l < this.numHiddenLayers+1; l++) {
			for (int i = 0; i < this.hiddenLayerSize; i++){
				for (int j = 0; j < this.hiddenLayerSize; j++){
					this.weights[l][i][j] = 0;
				}
			}
		}
		
		//TODO: set the weights manually
		

	}

	private static double stepActivationFunction(double input){
		if (input >= threshold) return 1;
		return 0;
	}
	
	private void feedForward (double[][] activation, int fromLayerSize, int toLayerSize, int l){
		double inJ;
		for (int j = 0; j < toLayerSize; j++){
			inJ = 0;
			for (int i = 0; i < fromLayerSize; i++){
				inJ += this.weights[l][i][j] * activation[l][i];
			}
			// 0 is the first hidden layer
			activation[l+1][j] = stepActivationFunction(inJ);
		}
	}
	
	public void printWeights(){
		System.out.println("Input layer.");		
		for (int i = 0; i < this.inputLayerSize; i++){
			for (int j = 0; j < this.hiddenLayerSize; j++){
				System.out.printf("Weight from input node %d to node %d is %f.\n", i, j, this.weights[0][i][j]);
			}
		}
		
		System.out.println("\nHidden layers.");
		for (int l = 1; l < this.numHiddenLayers; l++) {
			for (int i = 0; i < this.hiddenLayerSize; i++){
				for (int j = 0; j < this.hiddenLayerSize; j++){
					System.out.printf("Weight at hidden layer %d from node %d to node %d is %f.\n", l, i, j, this.weights[l][i][j]);
				}
			}
		}
		
		System.out.println("\nOutput layer.");
		for (int i = 0; i < this.hiddenLayerSize; i++){
			for (int j = 0; j < this.outputLayerSize; j++){
				System.out.printf("Weight from last hidden layer node %d to output node %d is %f.\n", i, j, this.weights[this.numHiddenLayers][i][j]);
			}
		}
}
	
	public void testNetwork(){
		int count = 0;
		//Run through entire training set once.
		for (int example = 0; example < this.trainingSetSize; example++){
			// We store the activation of each node (over all input and hidden layers) because we need that data during back propagation.
			// Please notice that in order to simplify the code, we store the activation of the input layer too.
			double[][] activation = new double[this.numHiddenLayers+2][this.maxLayerSize];
			// Reading in the activation from the training data example
			for (int i = 0; i < this.inputLayerSize; i++){
				activation[0][i] = this.inputs[example][i];
			}

			// There may be different sizes for the input, hidden and output layers, hence there are three different calls for feedForeard
			// input to first hidden layer
			feedForward(activation, this.inputLayerSize, this.hiddenLayerSize, 0);
			// hidden to hidden layers
			for (int l = 1; l < this.numHiddenLayers; l++){
				feedForward(activation, this.hiddenLayerSize, this.hiddenLayerSize, l);					
			}
			// last hidden layer to output layer
			feedForward(activation, this.hiddenLayerSize, this.outputLayerSize, this.numHiddenLayers);

			for (int j = 0; j < this.outputLayerSize; j++){
				if (Math.abs(activation[this.numHiddenLayers+1][j] - this.desiredOutput[example][j]) > 0.1) {
					System.out.println("Output neuron " + j + " has: " + activation[this.numHiddenLayers+1][j] + " should be: " + this.desiredOutput[example][j]);
					count++;
				}
			}
		}
		System.out.println(count + " errors");
		System.out.println("Done testing.");
	}
	


}