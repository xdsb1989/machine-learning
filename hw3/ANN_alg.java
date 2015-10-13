import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class ANN_alg {
	static class Input_list{//store all the attributes in an arrayList
		String value_range [];
		int continuous;
	}
	
	static class Output_list{//store all the output class in an arrayList
		String result_range [];
	}
	
	static class input_output{//covert the discrete value into numbers,and store all the training data in class array[]
		double input[];//input[] is all the attribute value in double format
		double output[];//one output[] only contains one "1",others are "0".If the first cell is "1",means the label is the first class type
	}
	
	public static void main(String args[]) throws IOException{
		String attri_filename;
		String train_filename;
		String test_filename;
		Scanner in = new Scanner(System.in);
		System.out.println("Input the attributes file name:");
		attri_filename = in.nextLine();
		System.out.println("Input the training file name:");
		train_filename = in.nextLine();
		System.out.println("Input the testing file name:");
		test_filename = in.nextLine();
		
		FileReader fr = new FileReader(attri_filename);
		BufferedReader br =  new BufferedReader(fr);
		ArrayList<Input_list> input_list = new ArrayList<Input_list>();
		ArrayList<Output_list> output_list = new ArrayList<Output_list>();
		
		String Line;
		int flag=0;
		while ((Line = br.readLine()) != null){
			if(Line.equals("")){
				flag = 1;
				continue;
			}
			if (flag == 0){
				Input_list node = new Input_list();
				node.value_range = Line.split(" ");
				input_list.add(node);
			}
			else if (flag == 1){
				Output_list node = new Output_list();
				node.result_range = Line.split(" ");
				output_list.add(node);
			}
		}
		br.close();
		fr.close();
		
		for (int i=0;i<input_list.size();i++){
			if (input_list.get(i).value_range[1].equals("continuous"))
				input_list.get(i).continuous = 1;
			else
				input_list.get(i).continuous = 0;
		}
		fr = new FileReader(train_filename);
		br =  new BufferedReader(fr);
		int examples = 0;
		while ((Line = br.readLine()) != null){
			examples++;
		}
		String train [][] = new String [examples][];
		br.close();
		fr.close();
		fr = new FileReader(train_filename);
		br =  new BufferedReader(fr);
		examples = 0;
		while ((Line = br.readLine()) != null){
			train[examples] = Line.split(" ");
			examples++;
		}
		br.close();
		fr.close();
		
		input_output train_array[] = new input_output [examples];//create an class array to store all the training data
		for (int i=0;i<examples;i++)
			train_array[i] = new input_output();
		int input_number = input_list.size();//"input number" means how many input units
		int output_number;//"output_number" means how many output units
		
		if (output_list.get(0).result_range[1].equals("0")){//check if the training file is a eight classes file
			output_number = output_list.size();
			for (int i=0;i<examples;i++){
				train_array[i].input = new double [input_list.size()];
				train_array[i].output = new double [output_list.size()];
				for (int j=0;j<input_list.size();j++)
					train_array[i].input[j] = Double.parseDouble(train[i][j]);
				
				for (int j=0, k=train[i].length-output_list.size() ;j<output_list.size();j++,k++)
					train_array[i].output[j] = Double.parseDouble(train[i][k]);
			}
		}
		
		else{//check if the training file is a normal file
			output_number = output_list.get(0).result_range.length-1;
			for (int i=0;i<examples;i++){
				train_array[i].input = new double [input_list.size()];
				train_array[i].output = new double [output_list.get(0).result_range.length-1];
				for (int j=0;j<input_list.size();j++){
					if (input_list.get(j).continuous == 0){
						for (int k=0; k<input_list.get(j).value_range.length; k++){
							if (train[i][j].equals(input_list.get(j).value_range[k]))
								train_array[i].input[j] = k-1;//using the index as value for each discrete value
						}
					}
					else if (input_list.get(j).continuous == 1){//if the value is continuous, just covert string to double
						train_array[i].input[j] = Double.parseDouble(train[i][j]);
					}
				}
				for (int j=0,k=1;k<output_list.get(0).result_range.length;j++,k++){
					if (train[i][train[i].length-1].equals(output_list.get(0).result_range[k]))
						train_array[i].output[j] = 1;
					else
						train_array[i].output[j] = 0;
				}
			}
		}
		System.out.print("Input the number of hidden units:");
		int hidden_number = in.nextInt();
		System.out.print("Input the number of iterations:");
		int number_iterations = in.nextInt();
		System.out.print("Input the learning rate:");
		double learning_rate = in.nextDouble();
		System.out.print("Input the momentum:");
		double momentum = in.nextDouble();
		double weight_hidden[][] = new double [hidden_number][input_number+1];
		//weight_hidden[i][j] means input unit j to hidden unit i
		//the reason I made it as input_number+1 is for the W0,the extra input weight
		double weight_output[][] = new double [output_number][hidden_number+1];//weight_output[i][j] means hidden unit j to output unit i
		Backpropagation(train_array, weight_hidden, weight_output, number_iterations, learning_rate,
				momentum, input_number,hidden_number,output_number);
		//after the alg, we get the weight_output[][] and weight_hidden[][],then we can do prediction
		fr = new FileReader(test_filename);
		br =  new BufferedReader(fr);
		int test_number = 0;
		while ((Line = br.readLine()) != null){
			test_number++;
		}
		String test [][] = new String [test_number][];
		br.close();
		fr.close();
		fr = new FileReader(test_filename);
		br =  new BufferedReader(fr);
		test_number = 0;
		while ((Line = br.readLine()) != null){
			test[test_number] = Line.split(" ");
			test_number++;
		}
		br.close();
		fr.close();
		
		input_output test_array[] = new input_output [test_number];//also using an class array to convert the test data into number values
		for (int i=0;i<test_number;i++)
			test_array[i] = new input_output();
		
		if (output_list.get(0).result_range[1].equals("0")){
			for (int i=0;i<test_number;i++){
				test_array[i].input = new double [input_list.size()];
				test_array[i].output = new double [output_list.size()];
				for (int j=0;j<input_list.size();j++)
					test_array[i].input[j] = Double.parseDouble(test[i][j]);
				
				for (int j=0, k=test[i].length-output_list.size() ;j<output_list.size();j++,k++)
					test_array[i].output[j] = Double.parseDouble(test[i][k]);
			}
		}
		
		else{
			for (int i=0;i<test_number;i++){
				test_array[i].input = new double [input_list.size()];
				test_array[i].output = new double [output_list.get(0).result_range.length-1];
				for (int j=0;j<input_list.size();j++){
					if (input_list.get(j).continuous == 0){
						for (int k=0; k<input_list.get(j).value_range.length; k++){
							if (test[i][j].equals(input_list.get(j).value_range[k]))
								test_array[i].input[j] = k-1;
						}
					}
					else if (input_list.get(j).continuous == 1){
						test_array[i].input[j] = Double.parseDouble(test[i][j]);
					}
				}
				for (int j=0,k=1;k<output_list.get(0).result_range.length;j++,k++){
					if (test[i][test[i].length-1].equals(output_list.get(0).result_range[k]))
						test_array[i].output[j] = 1;
					else
						test_array[i].output[j] = 0;
				}
			}
		}
		int correct_number;
		correct_number = Count_correct(test_array,hidden_number,output_number,weight_hidden,weight_output,output_list);
		double accuracy = (double)correct_number/test_number;
		System.out.println("The accuarcy on test file: "+accuracy);
		if (!output_list.get(0).result_range[1].equals("0")){
			correct_number = Count_correct(train_array,hidden_number,output_number,weight_hidden,weight_output,output_list);
			accuracy = (double)correct_number/examples;
			System.out.println("The accuarcy on train file: "+accuracy);
		}
	}

	private static int Count_correct(input_output[] test_array,
			int hidden_number, int output_number, double[][] weight_hidden,
			double[][] weight_output, ArrayList<Output_list> output_list) {
		
		int correct_number=0;
		for (int i=0;i<test_array.length;i++){//in each single data row
			double hidden_unit[] = new double [hidden_number];//using the hidden_unit[] to store the out come hidden values
			double predict_unit[] = new double [output_number];//using the predict_unit[] to store the out come values
			Prediction(predict_unit,hidden_unit,weight_hidden,weight_output,test_array[i]);
			if (output_list.get(0).result_range[1].equals("0")){
				System.out.print("Hidden valuse: ");
				for (int j=0;j<hidden_unit.length;j++)
					System.out.printf("%.5f ",hidden_unit[j]);
				System.out.println();
			}
			double max=0;
			int index=0;
			for (int j=0;j<predict_unit.length;j++){//I choosing the max number as "1"
				if (predict_unit[j]>max){
					max = predict_unit[j];
					index = j;
				}
			}
			int id=0;
			for (int j=0;j<test_array[i].output.length;j++)//if the index of the "1" which we predicted is the same as in the test file,then correct
				if (test_array[i].output[j] == 1)
					id = j;
			if (index == id)
				correct_number++;
		}
		return correct_number;
	}

	private static void Prediction(double[] predict_unit, double[] hidden_unit,
			double[][] weight_hidden, double[][] weight_output, input_output test_array) {
		for (int i=0;i<hidden_unit.length;i++){
			double sum = 0;
			for (int j=0;j<test_array.input.length;j++)
				sum = sum + test_array.input[j] * weight_hidden[i][j];
			sum = sum + weight_hidden[i][weight_hidden[i].length-1];
			hidden_unit[i] = sigmoid(sum);
		}
		for (int i=0;i<predict_unit.length;i++){
			double sum = 0;
			for (int j=0;j<hidden_unit.length;j++)
				sum = sum + hidden_unit[j] * weight_output[i][j];
			sum = sum + weight_output[i][weight_output[i].length-1];
			predict_unit[i] = sigmoid(sum);
		}
	}

	private static void Backpropagation(input_output[] train_array,
			double[][] weight_hidden, double[][] weight_output,
			int number_iterations, double learning_rate, double momentum,
			int input_number, int hidden_number, int output_number) {
		
		Random rd = new Random();
		for (int i=0;i<weight_hidden.length;i++)
			for (int j=0;j<weight_hidden[i].length;j++)
				weight_hidden[i][j] = (rd.nextDouble()-0.5)/10; 
		for (int i=0;i<weight_output.length;i++)
			for (int j=0;j<weight_output[i].length;j++)
				weight_output[i][j] = (rd.nextDouble()-0.5)/10; 		
		
		double delta_weight_hidden[][] = new double [hidden_number][input_number+1];//this is the change for each weight in hidden layer
		double delta_weight_output[][] = new double [output_number][hidden_number+1];//this is the change for each weight in output layer
		
		double previous_delta_weight_hidden[][] = new double [hidden_number][input_number+1];//for momentum
		double previous_delta_weight_output[][] = new double [output_number][hidden_number+1];//for momentum
		
		for (int time=1;time<=number_iterations;time++){
			for (int i=0;i<train_array.length;i++){
				double error_output[] = new double[output_number];//error_output[i] is the error rate in each output unit
				double error_hidden[] = new double[hidden_number];//error_hidden[i] is the error rate in each hidden unit
				
				double Ok[] = new double [output_number];//Ok[i] is the predict output unit
				double Oh[] = new double [hidden_number];//Oh[i] is the calculated hidden unit
				
				for (int j=0;j<hidden_number;j++)
					Oh[j] = Caculate_hidden(train_array[i], weight_hidden,j);//Calculate each Oh[i],it will use sigmod funciton
				for (int j=0;j<output_number;j++)//after getting each Oh[i],then calculate each Ok[i],it will use sigmod funciton
					Ok[j] = Caculate_output(Oh, weight_output,j,hidden_number);
				
				for (int j=0;j<output_number;j++){
					double tk = train_array[i].output[j];
					error_output[j] = Ok[j]*(1-Ok[j])*(tk-Ok[j]);//get the error rate for output unit
				}
				
				for (int j=0;j<hidden_number;j++){
					double sum = 0;
					for (int k=0;k<output_number;k++)
						sum = sum + weight_output[k][j]*error_output[k];
					error_hidden[j] = Oh[j]*(1-Oh[j])*sum;//get the error rate for output unit
				}
				
				for (int j=0;j<hidden_number;j++){//this is for updating the weight_hidden[i][j]
					for (int k=0;k<input_number;k++){
						//using the momentum equation
						delta_weight_hidden[j][k] = learning_rate*error_hidden[j]*train_array[i].input[k] + momentum*previous_delta_weight_hidden[j][k];
						weight_hidden[j][k] = weight_hidden[j][k] + delta_weight_hidden[j][k];
						previous_delta_weight_hidden[j][k] = delta_weight_hidden[j][k];//store the previous delta w for the next iterations
					}
					delta_weight_hidden[j][input_number] = learning_rate*error_hidden[j] + momentum*previous_delta_weight_hidden[j][input_number];
					weight_hidden[j][input_number] = weight_hidden[j][input_number] + delta_weight_hidden[j][input_number];
					previous_delta_weight_hidden[j][input_number] = delta_weight_hidden[j][input_number];
				}
				
				for (int j=0;j<output_number;j++){//this is for updating the weight_output[i][j]
					for (int k=0;k<hidden_number;k++){
						delta_weight_output[j][k] = learning_rate*error_output[j]*Oh[k] + momentum*previous_delta_weight_output[j][k];
						weight_output[j][k] = weight_output[j][k] + delta_weight_output[j][k];
						previous_delta_weight_output[j][k] = delta_weight_output[j][k];
					}
					delta_weight_output[j][hidden_number] = learning_rate*error_output[j] + momentum*previous_delta_weight_output[j][hidden_number];
					weight_output[j][hidden_number] = weight_output[j][hidden_number] + delta_weight_output[j][hidden_number];
					previous_delta_weight_output[j][hidden_number] = delta_weight_output[j][hidden_number];
				}
			}
		}
	}

	private static double Caculate_output(double[] Oh,
			double[][] weight_output, int j,int hidden_number) {
		double sum = 0;
		for (int i=0;i<hidden_number;i++)
			sum = sum + weight_output[j][i] * Oh[i];
		
		sum = sum + weight_output[j][weight_output[j].length-1];
		double result = sigmoid(sum);
		return result;
	}

	private static double Caculate_hidden(input_output one_example,
			double[][] weight_hidden, int j) {
		double sum=0;
		for (int i=0;i<one_example.input.length;i++)
			sum = sum + one_example.input[i]*weight_hidden[j][i];
		
		sum = sum + weight_hidden[j][weight_hidden[j].length-1];
		double result = sigmoid(sum);//using sigmoid function
		return result;
	}

	private static double sigmoid(double sum) {
		return 1/(1+Math.pow(Math.E, -sum));
	}
}
