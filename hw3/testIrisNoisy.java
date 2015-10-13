import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class testIrisNoisy {
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
		
		input_output orgina_train_array[] = new input_output [examples];//create an class array to store all the training data
		for (int i=0;i<examples;i++)
			orgina_train_array[i] = new input_output();
		int input_number = input_list.size();//"input number" means how many input units
		int output_number;//"output_number" means how many output units
		output_number = output_list.get(0).result_range.length-1;
		
		for (int i=0;i<examples;i++){
			orgina_train_array[i].input = new double [input_list.size()];
			orgina_train_array[i].output = new double [output_list.get(0).result_range.length-1];
			for (int j=0;j<input_list.size();j++){
				if (input_list.get(j).continuous == 0){
					for (int k=0; k<input_list.get(j).value_range.length; k++){
						if (train[i][j].equals(input_list.get(j).value_range[k]))
							orgina_train_array[i].input[j] = k-1;
					}
				}
				else if (input_list.get(j).continuous == 1){
					orgina_train_array[i].input[j] = Double.parseDouble(train[i][j]);
				}
			}
			for (int j=0,k=1;k<output_list.get(0).result_range.length;j++,k++){
				if (train[i][train[i].length-1].equals(output_list.get(0).result_range[k]))
					orgina_train_array[i].output[j] = 1;
				else
					orgina_train_array[i].output[j] = 0;
			}
		}
		
		System.out.print("Input the number of hidden units:");
		int hidden_number = in.nextInt();
		System.out.print("Input the Max number of iterations:");
		int Max_times = in.nextInt();
		System.out.print("Input the learning rate:");
		double learning_rate = in.nextDouble();
		System.out.print("Input the momentum:");
		double momentum = in.nextDouble();
		
		/*I used the initial weight set is to make sure for the next 10 times corrupts, we still have the 
		same initial weight value to do the compare
		*/
		double intial_weight_hidden[][] = new double [hidden_number][input_number+1];
		double intial_weight_output[][] = new double [output_number][hidden_number+1];
		Random rd = new Random();
		for (int i=0;i<intial_weight_hidden.length;i++)
			for (int j=0;j<intial_weight_hidden[i].length;j++)
				intial_weight_hidden[i][j] = (rd.nextDouble()-0.5)/10; 
		for (int i=0;i<intial_weight_output.length;i++)
			for (int j=0;j<intial_weight_output[i].length;j++)
				intial_weight_output[i][j] = (rd.nextDouble()-0.5)/10;
	
		int loop_time = 1;
		while( loop_time <= 10){
			
			double weight_hidden[][] = new double [hidden_number][input_number+1];
			double weight_output[][] = new double [output_number][hidden_number+1];
			double best_weight_hidden[][] = new double [hidden_number][input_number+1];
			double best_weight_output[][] = new double [output_number][hidden_number+1];
			int best_iterations  = 0;
			double best_accuracy = 0;
			//getting the same initial weight values
			for (int i=0;i<intial_weight_hidden.length;i++)
				for (int j=0;j<intial_weight_hidden[i].length;j++)
					weight_hidden[i][j] = intial_weight_hidden[i][j];
			for (int i=0;i<intial_weight_output.length;i++)
				for (int j=0;j<intial_weight_output[i].length;j++)
					weight_output[i][j] = intial_weight_output[i][j];
			
			double corrupt_amount = 0.02*loop_time*examples;
			System.out.printf("\nthe number of noisy data: %.0f \n",corrupt_amount);
			input_output train_array[] = new input_output [examples];
			for (int i=0;i<examples;i++){
				train_array[i] = new input_output();
				train_array[i].input = new double [input_list.size()];
				train_array[i].output = new double [output_list.get(0).result_range.length-1];
			}
			for (int i=0;i<examples;i++){
				for (int j=0;j<orgina_train_array[i].input.length;j++)
					train_array[i].input[j] = orgina_train_array[i].input[j];
				for (int j=0;j<orgina_train_array[i].output.length;j++)
					train_array[i].output[j] = orgina_train_array[i].output[j];
			}
			
			for (int i=0; i<corrupt_amount;i++){
				for (int j=0;j<train_array[i].output.length;j++){
					if (train_array[i].output[j] == 1){
						if (j+1<train_array[i].output.length){
							train_array[i].output[j] = 0;
							train_array[i].output[j+1] = 1;
						}
						else{
							train_array[i].output[j] = 0;
							train_array[i].output[0] = 1;
						}
					}
				}
			}
			int validation_set_examples = examples*1/3;
			int sub_train_examples = examples - validation_set_examples;
			input_output validation_set[] = new input_output [validation_set_examples];//validation_set[] is the class array for validation set
			input_output sub_train[] = new input_output [sub_train_examples];//sub_train[] is the class array for training set
			
			for (int i=0;i<validation_set_examples;i++){
				validation_set[i] = new input_output();
				validation_set[i].input = new double [input_list.size()];
				validation_set[i].output = new double [output_list.get(0).result_range.length-1];
				for (int j=0;j<validation_set[i].input.length;j++)
					validation_set[i].input[j] = train_array[i].input[j];
				for (int j=0;j<validation_set[i].output.length;j++)
					validation_set[i].output[j] = train_array[i].output[j];
			}
			for (int i=0,j=validation_set_examples;j<examples;i++,j++){
			
				sub_train[i] = new input_output();
				sub_train[i].input = new double [input_list.size()];
				sub_train[i].output = new double [output_list.get(0).result_range.length-1];
				for (int k=0;k<sub_train[i].input.length;k++)
					sub_train[i].input[k] = train_array[j].input[k];
				for (int k=0;k<sub_train[i].output.length;k++)
					sub_train[i].output[k] = train_array[j].output[k];
			}
			//using the corrupt set to do build the network
			Backpropagation(train_array, weight_hidden, weight_output, Max_times, learning_rate,
					momentum, input_number,hidden_number,output_number);
			
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
			
			input_output test_array[] = new input_output [test_number];
			for (int i=0;i<test_number;i++)
				test_array[i] = new input_output();
			
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
			//doing the prediction in the test set while using the network which without validation set
			int correct_number = Count_correct(test_array,hidden_number,output_number,weight_hidden,weight_output,output_list);
			double accuracy = (double)correct_number/test_number;
			System.out.println("The accuarcy without validation set is: "+accuracy);
			
			//initial the weight values
			for (int i=0;i<intial_weight_hidden.length;i++)
				for (int j=0;j<intial_weight_hidden[i].length;j++)
					weight_hidden[i][j] = intial_weight_hidden[i][j];
			for (int i=0;i<intial_weight_output.length;i++)
				for (int j=0;j<intial_weight_output[i].length;j++)
					weight_output[i][j] = intial_weight_output[i][j];
			/*the "Max_times" is the max iterations time,while in this case,we will find out which number of iterations
			 * has the highest accuracy in the validation set.Then use this iteration to build the network,and to do the prediction.
			 * For the time consuming,I suggest input the Max iterations less than 700.
			 */
			for (int iterations=1;iterations<=Max_times;iterations++){
				Backpropagation(sub_train, weight_hidden, weight_output, iterations, learning_rate,
						momentum, input_number,hidden_number,output_number);
				correct_number = Count_correct(validation_set,hidden_number,output_number,weight_hidden,weight_output,output_list);
				accuracy = (double)correct_number/validation_set_examples;
				
				if (accuracy > best_accuracy){//record the best accuracy
					best_accuracy = accuracy;
					best_iterations = iterations;
					for (int i=0;i<weight_hidden.length;i++)//store the best weight[][] which associated to the best accuracy
						for (int j=0;j<weight_hidden[i].length;j++)
							best_weight_hidden[i][j] = weight_hidden[i][j];
					for (int i=0;i<weight_output.length;i++)
						for (int j=0;j<weight_output[i].length;j++)
							best_weight_output[i][j] = weight_output[i][j];
				}
			}
			//System.out.println("the best times and accuracy:"+ best_iterations +" "+ best_accuracy);
			correct_number = Count_correct(test_array,hidden_number,output_number,best_weight_hidden,best_weight_output,output_list);
			accuracy = (double)correct_number/test_number;
			System.out.println("The accuarcy with validation set is: "+accuracy);
			loop_time++;
		}
	}

	private static int Count_correct(input_output[] test_array,
			int hidden_number, int output_number, double[][] weight_hidden,
			double[][] weight_output, ArrayList<Output_list> output_list) {
		
		int correct_number=0;
		for (int i=0;i<test_array.length;i++){
			double hidden_unit[] = new double [hidden_number];
			double predict_unit[] = new double [output_number];
			Prediction(predict_unit,hidden_unit,weight_hidden,weight_output,test_array[i]);
			double max=0;
			int index=0;
			for (int j=0;j<predict_unit.length;j++){
				if (predict_unit[j]>max){
					max = predict_unit[j];
					index = j;
				}
			}
			int id=0;
			for (int j=0;j<test_array[i].output.length;j++)
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
		
			
		
		double delta_weight_hidden[][] = new double [hidden_number][input_number+1];
		double delta_weight_output[][] = new double [output_number][hidden_number+1];
		
		double previous_delta_weight_hidden[][] = new double [hidden_number][input_number+1];//for momentum
		double previous_delta_weight_output[][] = new double [output_number][hidden_number+1];//for momentum
		
		for (int time=1;time<=number_iterations;time++){
			for (int i=0;i<train_array.length;i++){
				double error_output[] = new double[output_number];
				double error_hidden[] = new double[hidden_number];
				
				double Ok[] = new double [output_number];
				double Oh[] = new double [hidden_number];
				
				for (int j=0;j<hidden_number;j++)
					Oh[j] = Caculate_hidden(train_array[i], weight_hidden,j);
				for (int j=0;j<output_number;j++)
					Ok[j] = Caculate_output(Oh, weight_output,j,hidden_number);
				
				for (int j=0;j<output_number;j++){
					double tk = train_array[i].output[j];
					error_output[j] = Ok[j]*(1-Ok[j])*(tk-Ok[j]);
				}
				
				for (int j=0;j<hidden_number;j++){
					double sum = 0;
					for (int k=0;k<output_number;k++)
						sum = sum + weight_output[k][j]*error_output[k];
					error_hidden[j] = Oh[j]*(1-Oh[j])*sum;
				}
				
				for (int j=0;j<hidden_number;j++){
					for (int k=0;k<input_number;k++){
						delta_weight_hidden[j][k] = learning_rate*error_hidden[j]*train_array[i].input[k] + momentum*previous_delta_weight_hidden[j][k];
						weight_hidden[j][k] = weight_hidden[j][k] + delta_weight_hidden[j][k];
						previous_delta_weight_hidden[j][k] = delta_weight_hidden[j][k];
					}
					delta_weight_hidden[j][input_number] = learning_rate*error_hidden[j] + momentum*previous_delta_weight_hidden[j][input_number];
					weight_hidden[j][input_number] = weight_hidden[j][input_number] + delta_weight_hidden[j][input_number];
					previous_delta_weight_hidden[j][input_number] = delta_weight_hidden[j][input_number];
				}
				
				for (int j=0;j<output_number;j++){
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
		double result = sigmoid(sum);
		return result;
	}

	private static double sigmoid(double sum) {
		return 1/(1+Math.pow(Math.E, -sum));
	}
}
