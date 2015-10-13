/*
 * this experiment is to test the noise in the iris file,so it won't print the tree.
 */
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Scanner;

public class testIrisNoisy {
	static class Attribute_list{//this is the class to store each attribute.If the "continuous" is 1,then jump to Continuous_Attribute_list
		String attribute_range [];
		int continuous;
	}

	static class Continuous_Attribute_list{//store all the continuous attribute and the attribute position
		String name;
		int position;
		double thresholds;
	}

	static class Value_And_Class{//use this class to sort the continue attribute
		double value;
		String class_type;
	}

	static class rule{//for each  class,it is an expression which contains the attribute_name,attribute index, continuous flag,....and so on
		String attribute_name;
		String class_name;
		int attri_index;
		int final_class;
		double rule_accuracy;
		int continuous;
		String value;
		int fatherindex;
		double thresholds;
		int flag_small;
	}

	static class Tree_node{//for each node,it has an continuous flag indicate,name of attribute,choosing value or thresholds
		String attribute;
		int index;
		String tpye;
		String values [];
		int range;
		Tree_node value_range[];
		Tree_node large;
		Tree_node small;
		int continuous;
		double thresholds;
	}
	
	static int rule_index[][] = null;//store the rules' index which hasn't been sorted.
	static int sort_rule_index[][] = null;//using the accuracy of each rule to sort the rule sets.
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
		ArrayList<Attribute_list> attribute_list = new ArrayList<Attribute_list>();
		
		String Line;
		while ((Line = br.readLine()) != null){
			Attribute_list node = new Attribute_list();
			node.attribute_range = Line.split(" ");
			attribute_list.add(node);//the attribute_list contains all the attributes and the value range of each attribute
		}
		br.close();
		fr.close();
		
		String Class_type[] = new String [attribute_list.get(attribute_list.size()-1).attribute_range.length-1];
		for (int i=0, j=1;i<attribute_list.get(attribute_list.size()-1).attribute_range.length-1;i++,j++)
			Class_type[i] = attribute_list.get(attribute_list.size()-1).attribute_range[j];//using the last line to assign the classes to the array
		
		for (int i=0; i < attribute_list.size()-1; i++){
			if (attribute_list.get(i).attribute_range[1].equals("continuous"))
				attribute_list.get(i).continuous = 1;
			else
				attribute_list.get(i).continuous = 0;
		}
		
		fr = new FileReader(train_filename);
		br =  new BufferedReader(fr);
		int examples = 0;
		while ((Line = br.readLine()) != null){
			examples++;
		}
		String orginal_train [][] = new String [examples][];
		br.close();
		fr.close();
		fr = new FileReader(train_filename);
		br =  new BufferedReader(fr);
		examples = 0;
		while ((Line = br.readLine()) != null){
			orginal_train[examples] = Line.split(" ");
			examples++;
		}
		br.close();
		fr.close();
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
		int loop_time = 1;
		
		while( loop_time <= 10){
			double corrupt_amount = 0.02*loop_time*examples;
			System.out.printf("\nthe number of noisy data: %.0f \n",corrupt_amount);
			String train[][] = new String [examples][];
			for (int i=0;i<examples;i++){//assign the original set to the train[][] set,then corrupt the train[][] set
				train[i] = new String [orginal_train[i].length];
				for (int j=0;j<orginal_train[i].length;j++)
					train[i][j] = orginal_train[i][j];
			}
			for (int i=examples-1;i>examples-corrupt_amount;i--){
				String correct_class = train[i][train[i].length-1];
				
				for (int j=0;j<Class_type.length;j++){
					if (correct_class.equals(Class_type[j])){
						if (j+1<Class_type.length)
							train[i][train[i].length-1] = Class_type[j+1];
						else
							train[i][train[i].length-1] = Class_type[0];
						
					}
				}
			}
			
			int validation_set_examples = examples*1/3;
			int sub_train_examples = examples - validation_set_examples;
			String sub_train[][] = new String [sub_train_examples][];
			String validation_set[][] = new String [validation_set_examples][];
			
			for (int i=0;i<validation_set_examples;i++){//sub_train set is to build the pruning tree
				validation_set[i] = new String [train[i].length];
				for (int j=0;j<validation_set[i].length;j++)
					validation_set[i][j] = train[i][j];
			}
			
			for (int i=validation_set_examples,j=0; i<examples; i++,j++){//validation_set is to test the accuracy for pruning
				sub_train[j] = new String [train[i].length];
				for (int k=0;k<sub_train[j].length;k++)
					sub_train[j][k] = train[i][k];
			}
			
			
			
			ArrayList<Continuous_Attribute_list> continuous_attribute_list = new ArrayList<Continuous_Attribute_list>();
			for (int i=0;i<attribute_list.size()-1;i++){
				if (attribute_list.get(i).continuous == 1){//for continuous attribute,then create a number of thresholds in continuous_attribute_list
					String attribute_thresholds_name = attribute_list.get(i).attribute_range[0];//save the name of the attribute
					Value_And_Class array[] = new Value_And_Class[examples];
					for (int j=0;j<examples;j++)
						array[j] = new Value_And_Class();
					for (int j=0;j<examples;j++){
						array[j].value = Double.parseDouble(train[j][i]);
						array[j].class_type = train[j][train[j].length-1];
					}
					double temp_value;
					String temp_class;
					for (int j=0;j<examples-1;j++){
						for (int k=0;k<examples-1;k++){
							if (array[k].value>array[k+1].value){
								temp_value = array[k].value;
								temp_class = array[k].class_type;
								array[k].value = array[k+1].value;
								array[k].class_type = array[k+1].class_type;
								array[k+1].value = temp_value;
								array[k+1].class_type = temp_class;
							}
						}
					}
					
					DecimalFormat df = new DecimalFormat("#.00"); 
					for (int j=1;j<examples;j++){
						if (!array[j].class_type.equals(array[j-1].class_type) && array[j].value != array[j-1].value){
							//double thresholds = (array[j].value + array[j-1].value)/2;
							double thresholds = Double.parseDouble(df.format((array[j].value + array[j-1].value)/2));
							Continuous_Attribute_list node = new Continuous_Attribute_list();
							node.name = attribute_thresholds_name;
							node.position = i;
							node.thresholds = thresholds;
							continuous_attribute_list.add(node);
						}
					}
				}
			}
			
			int attribute_index[] = new int [attribute_list.size()-1];
			int attribute_number = 0;
			for (int i=0;i<attribute_list.size()-1;i++)
				if (attribute_list.get(i).continuous == 0){
					attribute_index[i] = 1;
					attribute_number++;
				}
			int continuous_attribute_index [] = new int [continuous_attribute_list.size()];
			int continuous_attribute_number = continuous_attribute_list.size();
			for (int i=0;i<continuous_attribute_list.size();i++)
				continuous_attribute_index[i] = 1;
			
			Tree_node root = new Tree_node();
			//String str1 = "|-";
			//String str2 = "";
			//System.out.println("The whole training set to build the tree:");
			Build_tree(root,attribute_list,train,examples,
					attribute_number, continuous_attribute_number,attribute_index,attribute_number + continuous_attribute_number,Class_type,
						continuous_attribute_index,continuous_attribute_list);
			
			String result[] = new String[test_number];//store the predicted results in a String array
			for (int i = 0;i<test_number;i++)
				result[i] = Predict(root, test[i], Class_type);
			int correct=0;
			for (int i = 0;i<test_number;i++)
				if (result[i].equals(test[i][test[i].length-1]))
					correct++;
			double test_accuracy =(double) correct/test_number;
			
			String train_result[] = new String[examples];//store the predicted results in a String array
			for (int i = 0;i<examples;i++)
				train_result[i] = Predict(root, train[i], Class_type);
			correct=0;
			for (int i = 0;i<examples;i++)
				if (train_result[i].equals(train[i][train[i].length-1]))
					correct++;
			double train_accuracy =(double) correct/examples;
			//System.out.println("the accuracy on train set is:"+train_accuracy);
			System.out.println("the accuracy on test set without pruning is:\t"+test_accuracy);
			
			attribute_number=0;
			for (int i=0;i<attribute_list.size()-1;i++)
				if (attribute_list.get(i).continuous == 0){
					attribute_index[i] = 1;
					attribute_number++;
				}
			continuous_attribute_number = continuous_attribute_list.size();
			for (int i=0;i<continuous_attribute_list.size();i++)
				continuous_attribute_index[i] = 1;
			//System.out.println("This is the tree which built by sub_training set:");
			Tree_node pruning_root = new Tree_node();
			//str1 = "|-";
			//str2 = "";
			Build_tree(pruning_root,attribute_list,sub_train,sub_train_examples,
					attribute_number, continuous_attribute_number,attribute_index,attribute_number + continuous_attribute_number,Class_type,
						continuous_attribute_index,continuous_attribute_list);
			
			ArrayList<rule> expression_list = new ArrayList<rule>();
			Find_expression(pruning_root,-1,expression_list);
			/*
			for (int i =0;i<expression_list.size();i++)
				System.out.println(expression_list.get(i).attribute_name+"+++++++++"+ expression_list.get(i).final_class +"++++++"+expression_list.get(i).value+"    father    "+expression_list.get(i).fatherindex);
			*/
			Store_rule(expression_list);
			
			/*
			for (int i=0;i<rule_index.length;i++){
				for (int j=0;j<rule_index[i].length;j++)
					System.out.print(rule_index[i][j]+"  ");
				System.out.println();
			}*/
			
			Post_Rule_pruning(expression_list,validation_set);
			//System.out.println("The post porning rules are:");
			Rule_sort(expression_list);
			//Print_rule(expression_list);
			double rule_accuracy_train = Predict_rule(expression_list, train,Class_type);
			double rule_accuracy_test = Predict_rule(expression_list, test,Class_type);
			//System.out.println("the accuracy on train set is:"+rule_accuracy_train);
			System.out.println("the accuracy on test set with pruning is:\t"+rule_accuracy_test);
			loop_time++;
		}
	}

	private static void Rule_sort(ArrayList<rule> expression_list) {
		sort_rule_index = new int [rule_index.length][];
		int avaialble_index[] = new int[rule_index.length];//indicate each rule has been used or not, 0 is not used, 1 is used
		
		for (int i=0;i<rule_index.length;i++){
			double max_accuracy=0;
			int temp_index = 0;
			for (int j=0;j<rule_index.length;j++){
				if (avaialble_index[j] == 0){//not used
					int temp_rule_index = rule_index[j][0];
					if ( expression_list.get(temp_rule_index).rule_accuracy >= max_accuracy ){
						max_accuracy = expression_list.get(temp_rule_index).rule_accuracy;
						temp_index = j;
					}
				}
			}
			avaialble_index[temp_index] = 1;
			sort_rule_index[i] = new int [rule_index[temp_index].length];
			for (int j=0;j<sort_rule_index[i].length;j++)
				sort_rule_index[i][j] = rule_index[temp_index][j];
		}
	}

	private static double Predict_rule(ArrayList<rule> expression_list,
			String[][] test, String[] class_type) {
		int correct_number = 0;
		for (int i=0;i<test.length;i++){//for each test example
			
			for (int k=0;k<sort_rule_index.length;k++){//for each rule,the rule is in an order from high accuracy to low accuracy
				int match_flag = 1;
				for (int m=1;m<sort_rule_index[k].length;m++){//for each conjunct in the rule
					if (sort_rule_index[k][m] != -1){
						int index = sort_rule_index[k][m];
						if (expression_list.get(index).continuous == 1){
							if (expression_list.get(index).flag_small == 1){
								double value = Double.parseDouble(test[i][expression_list.get(index).attri_index]);
								if (value > expression_list.get(index).thresholds){
									match_flag = 0;
									break;
								}
							}
							else if (expression_list.get(index).flag_small == 0){
								double value = Double.parseDouble(test[i][expression_list.get(index).attri_index]);
								if (value <= expression_list.get(index).thresholds){
									match_flag = 0;
									break;
								}
							}
						}
						if (expression_list.get(index).continuous == 0){
							String value = expression_list.get(index).value;
							if (!value.equals(test[i][expression_list.get(index).attri_index])){
								match_flag = 0;
								break;
							}
						}
					}
				}
				if (match_flag == 1){
					if ( expression_list.get(sort_rule_index[k][0]).class_name.equals(test[i][test[i].length-1]) )
						correct_number++;
					break;
				}
			}
		}
		double accuracy = (double)correct_number/test.length;
		return accuracy;
	}

	private static void Print_rule(ArrayList<rule> expression_list) {
		for (int i=0;i<sort_rule_index.length;i++){
			for (int j=1;j<sort_rule_index[i].length;j++){//the j==0 imply the class type
				int temp_index = sort_rule_index[i][j];
				if (temp_index != -1){
					if (expression_list.get(temp_index).continuous == 1){
						if (expression_list.get(temp_index).flag_small == 1)
							System.out.print(expression_list.get(temp_index).attribute_name +" <= "+expression_list.get(temp_index).thresholds);
						
						else if (expression_list.get(temp_index).flag_small == 0)
							System.out.print(expression_list.get(temp_index).attribute_name +" > "+expression_list.get(temp_index).thresholds);
					}
					else if (expression_list.get(temp_index).continuous == 0){
						System.out.print(expression_list.get(temp_index).attribute_name +" = "+expression_list.get(temp_index).value);
					}
					if (j+1 < sort_rule_index[i].length)
						System.out.print(" && ");
				}
			}
			System.out.print(" => "+expression_list.get(sort_rule_index[i][0]).class_name+" "+ expression_list.get(sort_rule_index[i][0]).rule_accuracy);
			System.out.println();
		}
	}

	private static void Post_Rule_pruning(ArrayList<rule> expression_list,
			String[][] validation_set) {
		
		for (int i=0;i<rule_index.length;i++){//for each current rule
			
			int number_cover=0;
			int number_correct=0;
			for (int j=0;j<validation_set.length;j++){//for each single validation example
				int flag_match = 1;
				for (int k=1;k<rule_index[i].length;k++){//use each expression in the rule
					int exp_index = rule_index[i][k];
					if (expression_list.get(exp_index).continuous == 1){
						if (expression_list.get(exp_index).flag_small == 1){
							double value = Double.parseDouble(validation_set[j][expression_list.get(exp_index).attri_index]);
							if (value > expression_list.get(exp_index).thresholds){
								flag_match = 0;
								break;
							}
						}
						else if (expression_list.get(exp_index).flag_small == 0){
							double value = Double.parseDouble(validation_set[j][expression_list.get(exp_index).attri_index]);
							if (value <= expression_list.get(exp_index).thresholds){
								flag_match = 0;
								break;
							}
						}
					}
					if (expression_list.get(exp_index).continuous == 0){
						String value = expression_list.get(exp_index).value;
						if (!value.equals(validation_set[j][expression_list.get(exp_index).attri_index])){
							flag_match = 0;
							break;
						}
					}
				}
				if (flag_match == 1){//if one single example matches the rule
					number_cover++;
					if ( expression_list.get(rule_index[i][0]).class_name.equals(validation_set[j][validation_set[j].length-1]) )
						number_correct++;
				}
			}
			//System.out.println("cover: "+number_cover+"  correct: "+number_correct);
			double accuracy = 0;
			if (number_cover != 0)
				accuracy = (double)number_correct/number_cover;//initial accuracy
			else if (number_cover == 0)
				accuracy = 0;
			if (number_cover == number_correct){//which means no coverage or already 100% accuracy
				expression_list.get(rule_index[i][0]).rule_accuracy = accuracy;
				continue;
			}
			
			for (int k=1;k<rule_index[i].length;k++){//max number of loop is the number of the conjunt expressions
				//System.out.println("****************");
				int stop_flag = 1;
				for (int m=1;m<rule_index[i].length;m++){//for each available conjunt expression
					
					if (rule_index[i][m] != -1){//if the current expression dosen't be used
						int cover=0;
						int correct=0;
						double sub_accuracy;
						int temp_index;
						temp_index = rule_index[i][m];
						rule_index[i][m] = -1;
						
						/*for (int j=1;j<rule_index[i].length;j++)
							System.out.print(rule_index[i][j]+" ");
						System.out.println();
						*/
						for (int j=0;j<validation_set.length;j++){//for each single validation example
							int flag_match = 1;
							for (int n=1;n<rule_index[i].length;n++){//for current rule,and all its expressions
								if (rule_index[i][n] != -1){
									int exp_index = rule_index[i][n];
									if (expression_list.get(exp_index).continuous == 1){
										if (expression_list.get(exp_index).flag_small == 1){
											double value = Double.parseDouble(validation_set[j][expression_list.get(exp_index).attri_index]);
											if (value > expression_list.get(exp_index).thresholds){
												flag_match = 0;
												break;
											}
										}
										else if (expression_list.get(exp_index).flag_small == 0){
											double value = Double.parseDouble(validation_set[j][expression_list.get(exp_index).attri_index]);
											if (value <= expression_list.get(exp_index).thresholds){
												flag_match = 0;
												break;
											}
										}
									}
									if (expression_list.get(exp_index).continuous == 0){
										String value = expression_list.get(exp_index).value;
										if (!value.equals(validation_set[j][expression_list.get(exp_index).attri_index])){
											flag_match = 0;
											break;
										}
									}
								}
							}
							if (flag_match == 1){
								cover++;
								if ( expression_list.get(rule_index[i][0]).class_name.equals(validation_set[j][validation_set[j].length-1]) )
									correct++;
							}
						}
						//System.out.println("cover: "+cover+"  correct: "+correct);
						sub_accuracy = (double)correct/cover;
						if (sub_accuracy > accuracy){
							accuracy = sub_accuracy;//update the accuracy
							stop_flag=0;
							break;
						}
						else if (sub_accuracy <= accuracy){//if the accuracy doesn't improve
							rule_index[i][m] = temp_index;//get the index back,do not set it as -1
						}
					}
				}
				if (stop_flag == 1)//which means no improvement for each single expression
					break;
			}
			expression_list.get(rule_index[i][0]).rule_accuracy = accuracy;
		}
	}

	private static void Store_rule(ArrayList<rule> expression_list) {
		int number_rule = 0;
		for (int i=0;i<expression_list.size();i++){
			if (expression_list.get(i).final_class == 1)
				number_rule++;
		}
		rule_index = new int [number_rule][];//how many rules we have
		number_rule = 0;
		for (int i=0;i<expression_list.size();i++){
			if (expression_list.get(i).final_class == 1){
				
				int number_of_expression = 1;
				int father_index = expression_list.get(i).fatherindex;
				while(true){
					if (father_index == -1)
						break;
					number_of_expression++;
					father_index = expression_list.get(father_index).fatherindex;
				}
				rule_index[number_rule] = new int [number_of_expression];
				
				number_of_expression=0;
				father_index = expression_list.get(i).fatherindex;
				rule_index[number_rule][number_of_expression] = i;
				while(true){
					if (father_index == -1)
						break;
					number_of_expression++;
					rule_index[number_rule][number_of_expression] = father_index;
					father_index = expression_list.get(father_index).fatherindex;
				}
				number_rule++;
			}
		}
	}

	private static void Find_expression(Tree_node root, int fatherindex, ArrayList<rule> expression_list) {
		if (!root.tpye.equals(" ")){
			rule node = new rule();
			node.fatherindex = fatherindex;
			node.class_name = root.tpye;
			node.final_class = 1;
			expression_list.add(node);
		}
		else{
			if (root.continuous == 0){
				for (int i=1;i<=root.range;i++){
					rule node = new rule();
					node.continuous = 0;
					node.attri_index = root.index;
					node.fatherindex = fatherindex;
					node.final_class = 0;
					node.attribute_name = root.attribute;
					node.value = root.values[i];
					expression_list.add(node);
					int temp_index = expression_list.size()-1;
					Find_expression(root.value_range[i], temp_index, expression_list);
				}
			}
			else{
				for (int i=1;i<=2;i++){
					rule node = new rule();
					node.continuous = 1;
					node.attribute_name = root.attribute;
					node.attri_index = root.index;
					node.fatherindex = fatherindex;
					node.final_class = 0;
					node.thresholds = root.thresholds;
					if (i==1){
						node.flag_small = 1;
						expression_list.add(node);
						int temp_index = expression_list.size()-1;
						Find_expression(root.small, temp_index, expression_list);
					}
					else if (i==2){
						node.flag_small = 0;
						expression_list.add(node);
						int temp_index = expression_list.size()-1;
						Find_expression(root.large, temp_index, expression_list);
					}
				}
			}
		}
		
		
	}

	private static String Predict(Tree_node root, String test[], String Class_type []) {
		if (!root.tpye.equals(" ")){
			return root.tpye;
		}
		else{
			if (root.continuous == 0){
				int index = root.index;//find out which attribute it is
				String value = test[index];//find out what the value it is for the test set
				for (int i=1;i<=root.range;i++){
					if (value.equals(root.values[i])){
						return Predict(root.value_range[i],test,Class_type);
					}
				}
			}
			else{
				int index = root.index;//find out which attribute it is
				double value = Double.parseDouble(test[index]);
				if (value<=root.thresholds)
					return Predict(root.small,test,Class_type);
				else
					return Predict(root.large,test,Class_type);
			}
		}
		return null;
	}
	
	private static void Build_tree(Tree_node root,
			ArrayList<Attribute_list> attribute_list, String[][] train,
			int examples, int attribute_number, int continuous_attribute_number, int[] attribute_index, int current_attribute_number,
			String[] class_type, int[] continuous_attribute_index,
			ArrayList<Continuous_Attribute_list> continuous_attribute_list) {
		
		int Count_type_number[] = new int [class_type.length];
		for (int i=0;i<class_type.length;i++){
			Count_type_number[i] = 0;
		}
		
		for (int i=0;i<examples;i++){
			int lenth = train[i].length;
			for (int j=0;j<class_type.length;j++){
				if (train[i][lenth-1].equals(class_type[j])){
					Count_type_number[j]++;
					break;
				}
			}
		}
		//the first way to stop building a tree.
		for (int i=0;i<class_type.length;i++){
			if (Count_type_number[i] == examples){
				root.tpye = class_type[i];
				//System.out.println(str1+root.tpye+str2);
				return;
			}
		}
		//the second way to stop building a tree.
		if (current_attribute_number == 0){//means no more attributes,then return the majority as the value
			int max = 0;
			int mark = 0;
			for (int i=0;i<class_type.length;i++){//find out the majority of the current examples
				if (Count_type_number[i]>=max){
					max = Count_type_number[i];
					mark = i;
				}
			}
			root.tpye = class_type[mark];
			//System.out.println(str1+root.tpye+str2);
			return;
		}
		//if the first way and second way don't match,then continue to build a tree.
		double Probablity [] = new double [class_type.length];
		double Info_before = 0.0;
		int flag=0;
		for (int i=0;i<class_type.length;i++){
			if (Count_type_number[i] == examples){
				Info_before = 0.0;
				flag = 1;
				break;
			}
		}
		if (flag == 0){
			for (int i=0;i<class_type.length;i++)
				Probablity[i] = (double) Count_type_number[i]/examples;
			for (int i=0;i<class_type.length;i++){
				if (Count_type_number[i] == 0)
					Info_before = Info_before;
				else
					Info_before = -Probablity[i]*(Math.log(Probablity[i])/Math.log(2)) + Info_before;
			}
		}
		double infomation_gain[] = new double [attribute_number];
		for (int i=0;i<attribute_number;i++)
			infomation_gain[i] = -3.0;
		
		double continuous_info_gain[] = new double [continuous_attribute_number];
		for (int i=0;i<continuous_attribute_number;i++)
			continuous_info_gain[i] = -3.0;
		
		for (int i=0;i<attribute_number;i++){//try all the attributes
			if (attribute_index[i] == 1){//means this attribute is currently available
				int value_range = attribute_list.get(i).attribute_range.length - 1;
				
				double part_infor_gain [] = new double [value_range+1];
				double info_after = 0.0;
				for (int j=1;j<=value_range;j++){//for each value of the current attribute
					
					int count=0;//how many examples in the specific value
					int sub_tpye_number[] = new int [class_type.length];
					for (int k=0;k<class_type.length;k++)//creat the sub
						sub_tpye_number[k] = 0;
					
					for (int k=0;k<examples;k++){//test each example
						if (train[k][i].equals(attribute_list.get(i).attribute_range[j])){
							count++;
							for (int m =0;m<class_type.length;m++){
								if (train[k][train[k].length-1].equals(class_type[m])){
									sub_tpye_number[m]++;
								}
							}
						}
					}
					double info = 0;
					int stop = 0;
					for (int k=0;k<class_type.length;k++){
						if (sub_tpye_number[k] == count){
							info = 0.0;
							stop = 1;
							break;
						}
					}
					if (stop == 0){
						double probablit [] = new double [class_type.length];
						for (int k=0;k<class_type.length;k++)
							probablit[k] = (double) sub_tpye_number[k]/count;
						
						for (int k=0;k<class_type.length;k++){
							if (sub_tpye_number[k] == 0)
								info = info;
							else
								info = -probablit[k]*(Math.log(probablit[k])/Math.log(2)) + info;
						}
					}
					part_infor_gain[j] = info*count/examples;
				}
				for (int j =1;j<=value_range;j++){//summary all the branch information together to get the "after information"
					info_after = info_after + part_infor_gain[j];
				}
				infomation_gain[i] = Info_before - info_after;
			}
		}
		
		for (int i=0;i<continuous_attribute_number;i++){//try all the continuous_attributes
			if (continuous_attribute_index[i] == 1){//means this attribute is currently available
				double thresholds = continuous_attribute_list.get(i).thresholds;
				
				double part_infor_gain_small_equal;
				double part_infor_gain_large;
				double info_after = 0.0;
				
				int small_count=0;//how many examples in the specific value
				int large_count=0;
				int small_sub_tpye_number[] = new int [class_type.length];
				int large_sub_tpye_number[] = new int [class_type.length];
				
				for (int k=0;k<class_type.length;k++)//create the sub
					small_sub_tpye_number[k] = 0;
				for (int k=0;k<class_type.length;k++)//create the sub
					large_sub_tpye_number[k] = 0;
					
				int index = continuous_attribute_list.get(i).position;
				for (int k=0;k<examples;k++){//test each example
					double current_value = Double.parseDouble(train[k][index]);
					if (current_value<=thresholds){
						small_count++;
						for (int m =0;m<class_type.length;m++){
							if (train[k][train[k].length-1].equals(class_type[m])){
								small_sub_tpye_number[m]++;
							}
						}
					}
					else if (current_value>thresholds){
						large_count++;
						for (int m =0;m<class_type.length;m++){
							if (train[k][train[k].length-1].equals(class_type[m])){
								large_sub_tpye_number[m]++;
							}
						}
					}
					
				}
				double small_info = 0;
				int stop = 0;
				for (int k=0;k<class_type.length;k++){
					if (small_sub_tpye_number[k] == small_count){
						small_info = 0.0;
						stop = 1;
						break;
					}
				}
				if (stop == 0){
					double probablit [] = new double [class_type.length];
					for (int k=0;k<class_type.length;k++)
						probablit[k] = (double) small_sub_tpye_number[k]/small_count;
						
					for (int k=0;k<class_type.length;k++){
						if (small_sub_tpye_number[k] == 0)
							small_info = small_info;
						else
							small_info = -probablit[k]*(Math.log(probablit[k])/Math.log(2)) + small_info;
					}
				}
				part_infor_gain_small_equal = small_info*small_count/examples;
				
				double large_info = 0;
				int stop_1 = 0;
				for (int k=0;k<class_type.length;k++){
					if (large_sub_tpye_number[k] == large_count){
						large_info = 0.0;
						stop_1 = 1;
						break;
					}
				}
				if (stop_1 == 0){
					double probablit [] = new double [class_type.length];
					for (int k=0;k<class_type.length;k++)
						probablit[k] = (double) large_sub_tpye_number[k]/large_count;
						
					for (int k=0;k<class_type.length;k++){
						if (large_sub_tpye_number[k] == 0)
							large_info = large_info;
						else
							large_info = -probablit[k]*(Math.log(probablit[k])/Math.log(2)) + large_info;
					}
				}
				part_infor_gain_large = large_info*large_count/examples;
				//for (int j =1;j<=value_range;j++){//summary all the branch information together to get the "after information"
				info_after = part_infor_gain_large + part_infor_gain_small_equal;
				//}
				continuous_info_gain[i] = Info_before - info_after;
			}
		}
		
		double temp_1 = -3.0;
		int mark_1 = 0;//"mark" will be the index in the "attribute_list" 
		for (int i=0;i<attribute_number;i++){//choosing the best attribute which has the highest information gain.
			if (attribute_index[i] == 1){
				if (infomation_gain[i]>=temp_1){
					temp_1 = infomation_gain[i];
					mark_1 = i;
				}
			}
		}
		
		double temp_2 = -3.0;
		int mark_2 = 0;//"mark" will be the index in the "attribute_list" 
		for (int i=0;i<continuous_attribute_number;i++){//choosing the best attribute which has the highest information gain.
			if (continuous_attribute_index[i] == 1){
				if (continuous_info_gain[i]>=temp_2){
					temp_2 = continuous_info_gain[i];
					mark_2 = i;
				}
			}
		}
		
		if (temp_1>temp_2){
			current_attribute_number = current_attribute_number - 1;//reduce the available attributes
			attribute_index[mark_1] = 0;//take off one attribute from the "attribute_list".
			
			root.tpye = " ";
			root.attribute = attribute_list.get(mark_1).attribute_range[0];//name of the attribute
			root.index = mark_1;////////////////
			root.continuous = 0;
			//System.out.println(str1+"[ "+root.attribute+" ]"+str2);
			//str1 = str1 + "----";
			
			root.range = attribute_list.get(mark_1).attribute_range.length - 1;//how many different value of the current attribute
			root.values = new String [root.range+1];//store the different value of the attribute
			for (int i=1;i<=root.range;i++)
				root.values[i] = attribute_list.get(mark_1).attribute_range[i];
			
			root.value_range = new Tree_node[root.range+1];
			for (int i=0;i <= root.range;i++)
				root.value_range[i] = new Tree_node();
			for (int i=1;i <= root.range;i++){//this loop is for each value of the attribute,and extend it to be sub-tree
				String temp_examples [][];
				String value;
				value = root.values[i];
				int number = 0;//count how many examples have such a value in this attribute
				for (int j=0;j<examples;j++)
					if (value.equals(train[j][mark_1]))
						number++;
				temp_examples = new String [number][];//the temp_examples which have such a value in this attribute
				for (int j=0,k=0;j<examples;j++){//abstract some examples,and transfer them to the next level
					if (value.equals(train[j][mark_1])){
						temp_examples[k] = train[j];
						k++;
					}
				}
				//str2 = " : "+root.values[i];
				int temp_attribute_index [] = new int [attribute_number];
				for (int j=0;j<attribute_number;j++)
					temp_attribute_index[j] = attribute_index[j];
				int temp_current_attribute_number = current_attribute_number;
				
				int temp_con_attribute_index [] = new int [continuous_attribute_number];
				for (int j=0;j<continuous_attribute_number;j++)
					temp_con_attribute_index[j] = continuous_attribute_index[j];
				
				Build_tree(root.value_range[i],attribute_list,temp_examples,number,attribute_number,continuous_attribute_number,
						temp_attribute_index,temp_current_attribute_number,
							class_type, temp_con_attribute_index,continuous_attribute_list);
			}
		}
		if (temp_1<=temp_2){
			current_attribute_number = current_attribute_number - 1;//reduce the available attributes
			continuous_attribute_index[mark_2] = 0;//take off one attribute from the "attribute_list".
			
			root.tpye = " ";
			root.continuous = 1;
			root.attribute = continuous_attribute_list.get(mark_2).name;//name of the attribute
			root.index = continuous_attribute_list.get(mark_2).position;////////////////
			root.thresholds = continuous_attribute_list.get(mark_2).thresholds;
			//System.out.println(str1+"[ "+root.attribute+" ]"+str2);
			//str1 = str1 + "----";
			
			//root.range = 2;//how many different value of the current attribute
			//root.values = new String [root.range+1];//store the different value of the attribute
			//for (int i=1;i<=root.range;i++)
			//	root.values[i] = attribute_list.get(mark_1).attribute_range[i];
			
			root.small = new Tree_node();
			root.large = new Tree_node();
			
				String small_examples [][];
				String large_examples [][];
				
				int small_number = 0;//count how many examples have such a value in this attribute
				int large_number = 0;
				for (int j=0;j<examples;j++){
					double value = Double.parseDouble(train[j][root.index]);
					if (value <= root.thresholds)
						small_number++;
					else
						large_number++;
				}
				small_examples = new String [small_number][];//the temp_examples which have such a value in this attribute
				large_examples = new String [large_number][];//the temp_examples which have such a value in this attribute
				for (int j=0,k=0,m=0;j<examples;j++){//abstract some examples,and transfer them to the next level
					double value = Double.parseDouble(train[j][root.index]);
					if (value <= root.thresholds){
						small_examples[k] = train[j];
						k++;
					}
					else{
						large_examples[m] = train[j];
						m++;
					}
				}
				
				//str2 = " <= "+root.thresholds;
				int temp_attribute_index [] = new int [attribute_number];
				for (int j=0;j<attribute_number;j++)
					temp_attribute_index[j] = attribute_index[j];
				int temp_current_attribute_number = current_attribute_number;
				
				int temp_con_attribute_index [] = new int [continuous_attribute_number];
				for (int j=0;j<continuous_attribute_number;j++)
					temp_con_attribute_index[j] = continuous_attribute_index[j];
				
				Build_tree(root.small,attribute_list,small_examples,small_number,attribute_number,continuous_attribute_number,
						temp_attribute_index,temp_current_attribute_number,
							class_type, temp_con_attribute_index,continuous_attribute_list);
				
				//str2 = " > "+root.thresholds;
				Build_tree(root.large,attribute_list,large_examples,large_number,attribute_number,continuous_attribute_number,
						attribute_index,temp_current_attribute_number,
							class_type, continuous_attribute_index,continuous_attribute_list);
		}
	}
}

