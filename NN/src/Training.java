import java.io.*;

public class Training {
	public static final String T10IMAGES = "t10k-images.idx3-ubyte";
	public static final String T10LABELS = "t10k-labels.idx1-ubyte";
	public static final String TRAINIMAGES = "train-images.idx3-ubyte";
	public static final String TRAINLABELS = "train-labels.idx1-ubyte";

	public static void main(String[] args){
		double[][] inputs = new double[60000][28*28];
		double[][] output = new double[60000][10];
		double[][] inputTest = new double[10000][28*28];
		double[][] outputTest = new double[10000][10];


		try{
			FileInputStream reader = new FileInputStream(TRAINIMAGES);

			for(int i=0; i<16; i++){
				reader.read();
			}


			for(int x =0; x< 60000; x++){
				for(int i = 0; i<28*28; i++ ){
					inputs[x][i]= reader.read();
					if(inputs[x][i]>127){
						inputs[x][i]=1;
					}
					else{
						inputs[x][i]=0;
					}
				}
			}
			System.out.println("READ 1");

			FileInputStream reader2 = new FileInputStream(T10IMAGES);

			for(int i=0; i<16; i++){
				reader2.read();
			}


			for(int x =0; x< 10000; x++){
				for(int i = 0; i<28*28; i++ ){
					inputTest[x][i]= reader2.read();
					if(inputTest[x][i]>127){
						inputTest[x][i]=1;
					}
					else{
						inputTest[x][i]=0;
					}
				}
			}
			System.out.println("READ 2");

			FileInputStream reader3 = new FileInputStream(TRAINLABELS);

			for(int i=0; i<8; i++){
				reader3.read();
			}


			for(int x =0; x< 60000; x++){
				int data = reader3.read();
				output[x][data] = 1;
			}
			System.out.println("READ 3");

			FileInputStream reader4 = new FileInputStream(T10LABELS);

			for(int i=0; i<8; i++){
				reader4.read();
			}


			for(int x =0; x< 10000; x++){
				int data = reader4.read();
				outputTest[x][data] = 1;
			}
			System.out.println("READ 4");
		}
		catch (Exception e){
			System.out.println(e);
		}


		FeedForwardNetwork n = new FeedForwardNetwork(28*28, 220, 2, 10);
		n.initNetwork(inputs, output, 0.7, 0.5);
		n.trainNetwork(35, true);
		n.printWeights();
		n.testNetwork();
		n.testNetworkBatch(10000,inputTest,outputTest,true);
	}
	
}