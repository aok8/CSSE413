public class TrainingParity {

	// Even parity bit
	public static void main(String[] args){
		ParityNet n = new ParityNet(7,7,11,1);
		double[][] inputs = generateInputs(7);
		double[][] desiredOutput = generateOutputs(inputs);	
		
		for (int i = 0; i < inputs.length; i++){
			for (int j = 0; j < inputs[i].length; j++){
				System.out.print(inputs[i][j] + " ");
			}
		System.out.print(" " + desiredOutput[i][0]);
		System.out.println();
		}
	
		
		n.initNetwork(inputs, desiredOutput);
//		n.printWeights();
		n.testNetwork();
	}


	private static double[][]generateInputs(int numberBits){
		double[][] inputs = new double[(int)Math.pow(2,numberBits)][numberBits];
		for (int i = 0; i < numberBits; i++) {
			inputs[0][i] = 0.0; 
		}
		for (int i = 1; i < inputs.length; i++){
			inputs[i] = inputs[i-1].clone();
			add1(inputs[i], numberBits-1);
		}
		return inputs;
	}
	
	private static void add1(double[] a, int index){
		if (index == -1) return;
		if (a[index] == 1.0) {
			a[index] = 0.0;
			add1(a, index-1);
			return;
		}
		a[index] = 1.0;
		return; 
	}
	
	private static double[][]generateOutputs(double[][] inputs){
		double[][] outputs = new double[inputs.length][1];
		for (int i = 0; i < inputs.length; i++){
			int count = 0;
			for (int j = 0; j < inputs[i].length; j++){
				count += inputs[i][j];
			}
			outputs[i][0] = count%2;
		}
		return outputs;
	}
	
}