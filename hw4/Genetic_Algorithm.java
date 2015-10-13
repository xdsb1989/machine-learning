import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;

public class Genetic_Algorithm {
	static class Attributes{//create a list to store all the attributes
		String attr_name;
		String value_rang[];//store the attribute value range
		int attr_or_class;
		int contious;//check if it is continuous value
		int statr_index;//start at this position in a single rule
		int end_index;//end at this position in a single rule
	}
	
	static class Hypotheses{//create a hypotheses list
		int hypotheses [];
		int hyp_length;
		double fitness;
		int choosen;//check if this hypotheses is selected or not
		double fitness_prob;
		double statr_range;//include this value
		double end_range;//exclude this value
		int rank_number;//give a rank number for "Rank" strategies
	}
	static int length_of_rule = 0;
	public static void main(String args[]) throws IOException{
		Scanner in = new Scanner(System.in);
		System.out.println("Input the attributes file name:");
		String attri_file = in.nextLine();
		System.out.println("Input the training file name:");
		String train_filename = in.nextLine();
		System.out.println("Input the testing file name:");
		String test_filename = in.nextLine();
		
		FileReader fr = new FileReader(attri_file);
		BufferedReader br =  new BufferedReader(fr);
		ArrayList<Attributes> Attr_list = new ArrayList<Attributes>();
		String Line;
		int flag=0;
		while ((Line = br.readLine()) != null){
			if(Line.equals("")){
				flag = 1;
				continue;
			}
			if (flag == 0){
				Attributes node = new Attributes();
				node.value_rang = Line.split(" ");
				node.attr_or_class = 0;
				node.attr_name = node.value_rang[0];
				if (node.value_rang[1].equals("continuous"))
					node.contious = 1;
				else
					node.contious = 0;
				Attr_list.add(node);
			}
			if (flag == 1){
				Attributes node = new Attributes();
				node.value_rang = Line.split(" ");
				node.attr_or_class = 1;
				node.attr_name = node.value_rang[0];
				node.contious = 0;
				Attr_list.add(node);
			}
		}
		br.close();
		fr.close();

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
		
		fr = new FileReader(test_filename);
		br =  new BufferedReader(fr);
		int test_examples = 0;
		while ((Line = br.readLine()) != null){
			test_examples++;
		}
		String test [][] = new String [test_examples][];
		br.close();
		fr.close();
		fr = new FileReader(test_filename);
		br =  new BufferedReader(fr);
		test_examples = 0;
		while ((Line = br.readLine()) != null){
			test[test_examples] = Line.split(" ");
			test_examples++;
		}
		br.close();
		fr.close();
		
		System.out.print("population size:");
		int population = in.nextInt();
		System.out.print("replacement rate:");
		double replacement_rate = in.nextDouble();
		System.out.print("mutation rate:");
		double mutation_rate = in.nextDouble();
		System.out.print("fitness threshold:");
		double fitness_thrould = in.nextDouble();
		System.out.print("Max generations:");
		int Max_generation = in.nextInt();
		System.out.print("selection strategy, 1 is fitness-proportional, 2 is tournament, 3 is rank:");
		int strategy = in.nextInt();
		/*
		int population = 60;
		double replacement_rate = 0.3;
		double mutation_rate = 0.1;
		double fitness_thrould = 0.96;
		int Max_generation = 500;
		int strategy = 3;
		*/
		int number_corrsover = (int) (population*replacement_rate);
		int number_direct_inherit = population - number_corrsover;
		int number_of_mutation = (int) (population*mutation_rate);
		
		Set_start_end_index_for_attribute(Attr_list);//set the start index and end index for each attribute
		ArrayList<Hypotheses> hyp_list = new ArrayList<Hypotheses>();
		Initial_hyp(population, hyp_list, Attr_list);//get the initial hypothese list
		
		double max_fitness = 0.0;
		int best_hp_index = 0;
		for (int i=0;i<hyp_list.size();i++){
			double correct_rate = Correct_rate(hyp_list.get(i),train,examples, Attr_list);//caculate correct rate for each hypo in data set
			hyp_list.get(i).fitness = correct_rate * correct_rate;
			hyp_list.get(i).choosen = 0;//set each of them is not selected
			if (hyp_list.get(i).fitness > max_fitness){
				max_fitness = hyp_list.get(i).fitness;
				best_hp_index = i;
			}
		}
		hyp_list.get(best_hp_index).choosen = 1;//set the best one to be selected, so next generation can not be worse
		
		int generations = 0;
		while (max_fitness < fitness_thrould){
			if (strategy == 1){
				Set_prob_for_hyp(hyp_list);//give the each hypotheses a probability
			}
			else if (strategy == 2){
				Tournament(hyp_list);
			}
			else if (strategy == 3){
				Rank(hyp_list);
			}
			double max_distribution = 1.0 - hyp_list.get(best_hp_index).fitness_prob;
			Re_find_the_boundary(hyp_list);
			
			Random rd = new Random();
			double luck_number;
			int number_of_selected = 2;
			while (number_of_selected<=number_direct_inherit){
				luck_number = rd.nextDouble();
				luck_number = max_distribution * luck_number;
				int choosen_index;
				choosen_index = find_the_hyp(luck_number,hyp_list);
				
				max_distribution = max_distribution - hyp_list.get(choosen_index).fitness_prob;
				Re_find_the_boundary(hyp_list);
				number_of_selected++;
			}//finish the inherit part, below is about the corrsover.
			
			int number_of_corrsover = 0;
			while (number_of_corrsover < number_corrsover){//this loop is for getting the corrsover hypotheses
				int first_id, second_id;
				first_id = rd.nextInt(population);
				second_id = rd.nextInt(population);
				if ( Corrsover(hyp_list.get(first_id), hyp_list.get(second_id), hyp_list, Attr_list) )
					number_of_corrsover = number_of_corrsover + 2;
				
			}
			if (number_of_corrsover > number_corrsover){//if get one more hypothese
				int index = hyp_list.size();
				hyp_list.remove(index-1);
			}
			
			for (int i=hyp_list.size()-1; i>=0 ;i--){//remove the hypothese which is not selected
				if (hyp_list.get(i).choosen == 0)
					hyp_list.remove(i);
			}
			Mutation(hyp_list,number_of_mutation,Attr_list);//doing the mutation
			max_fitness = 0;
			best_hp_index = 0;
			for (int i=0; i<hyp_list.size() ;i++){//update all the fitness after the mutation,and find the max_fitness
				double correct_rate = Correct_rate(hyp_list.get(i),train,examples, Attr_list);
				hyp_list.get(i).fitness = correct_rate * correct_rate;
				hyp_list.get(i).choosen = 0;
				if (hyp_list.get(i).fitness > max_fitness){
					max_fitness = hyp_list.get(i).fitness;
					best_hp_index = i;
				}
			}
			hyp_list.get(best_hp_index).choosen = 1;//set the best one to be selected, so next generation can not be worse
			System.out.printf("generation is: %d    fitness is : %.4f\n",generations,hyp_list.get(best_hp_index).fitness);
			generations++;
			if (generations > Max_generation)
				break;
		}
		
		double train_accuracy;
		double test_accuracy;
		train_accuracy = Correct_rate(hyp_list.get(best_hp_index),train,examples, Attr_list);
		test_accuracy = Correct_rate(hyp_list.get(best_hp_index),test,test_examples, Attr_list);
		System.out.println("The print rules are:");
		Print_readable_rule(hyp_list.get(best_hp_index), Attr_list);
		System.out.println("the accuracy of train set is:\t"+train_accuracy);
		System.out.println("the accuracy of test set is:\t"+test_accuracy);
	}
	
	private static void Print_readable_rule(Hypotheses hypotheses,
			ArrayList<Attributes> attr_list) {
		int number_of_rule = hypotheses.hyp_length / length_of_rule;
		for (int i=0;i<number_of_rule;i++){
			int temp_rule[] = new int [length_of_rule];
			for (int j=0, k = i*length_of_rule; j<temp_rule.length; j++, k++)
				temp_rule[j] = hypotheses.hypotheses[k];
			translate_rule(temp_rule, attr_list);//for each rule, translate it into readable way
		}
		System.out.println("default class is:"+attr_list.get(attr_list.size()-1).value_rang[1]);
	}
	private static void translate_rule(int[] temp_rule, ArrayList<Attributes> attr_list) {
		for (int i=0;i<attr_list.size()-1;i++){
			if (attr_list.get(i).contious == 0){
				System.out.print(attr_list.get(i).attr_name+" = ");
				int index;
				int k;
				for (index = attr_list.get(i).statr_index, k=1; index <= attr_list.get(i).end_index; index++, k++)
					if (temp_rule[index] == 1)
						break;
				System.out.print(attr_list.get(i).value_rang[k]);
			}
			else if (attr_list.get(i).contious == 1){
				String name = attr_list.get(i).attr_name;
				int index;
				int k;
				int first = 0, second = 0;
				for (index = attr_list.get(i).statr_index, k=1; index <= attr_list.get(i).end_index; index++, k++){
					if (k == 1)
						first = temp_rule[index]*10;
					else if (k == 2)
						first = first + temp_rule[index];
					else if (k == 3)
						second = temp_rule[index]*10;
					else if (k == 4)	
						second = second + temp_rule[index];
				}
				double firs, secon;
				firs = (double)first/10;
				secon = (double)second/10;
				System.out.print(firs+"<="+name+"<="+secon);
			}
			if (i < attr_list.size()-2)
				System.out.print(" && ");
			else
				System.out.print(" => ");
		}
		int class_index = attr_list.size()-1;
		System.out.print(attr_list.get(class_index).attr_name+" = ");
		int index;
		int k;
		for (index = attr_list.get(class_index).statr_index, k=1; index <= attr_list.get(class_index).end_index; index++, k++)
			if (temp_rule[index] == 1)
				break;
		System.out.println(attr_list.get(class_index).value_rang[k]);
	}
	
	private static void Mutation(ArrayList<Hypotheses> hyp_list,
			int number_of_mutation, ArrayList<Attributes> attr_list) {
		int done = 0;
		Random rd = new Random();
		while (done < number_of_mutation){
			int index = rd.nextInt(hyp_list.size());
			change_bit(hyp_list.get(index),attr_list);//random choose a hypo,and mutate it
			done++;
		}
	}

	private static void change_bit(Hypotheses hypotheses,
			ArrayList<Attributes> attr_list) {
		Random rd = new Random();
		int temp_array[] = new int [hypotheses.hyp_length];
		for (int i=0;i<temp_array.length;i++)
			temp_array[i] = hypotheses.hypotheses[i];
		
		int mutation_index = rd.nextInt(temp_array.length);
		int number_rules = mutation_index/length_of_rule;
		int rule_index = mutation_index%length_of_rule;
		
		for (int i=0;i<attr_list.size();i++){
			if ( rule_index >= attr_list.get(i).statr_index && rule_index <= attr_list.get(i).end_index ){
				int start_index = number_rules*length_of_rule + attr_list.get(i).statr_index;
				int end_index = number_rules*length_of_rule + attr_list.get(i).end_index;
				if (attr_list.get(i).contious == 0){
					int attribute_length =  attr_list.get(i).value_rang.length-1;
					int position_of_1 = rd.nextInt(attribute_length);
					//change the value of the current attribute
					for (int point = start_index, k=0; point<=end_index;point++, k++){
						if (k == position_of_1)
							temp_array[point] = 1;
						else
							temp_array[point] = 0;
					}
				}
				else if (attr_list.get(i).contious == 1){
					while (true){//get two new numbers until it match the request
						int first=0,second=0;
						for (int point = start_index, k=1; point<=end_index; point++, k++){
							int value = rd.nextInt(10);
							temp_array[point] = value;
							if (k == 1)
								first = temp_array[point]*10;
							else if (k == 2)
								first = first + temp_array[point];
							else if (k == 3)
								second = temp_array[point]*10;
							else if (k == 4)	
								second = second + temp_array[point];	
						}
						if ( first <= second )
							break;
					}
				}
				break;
			}
		}
		for (int i=0;i<temp_array.length;i++)
			hypotheses.hypotheses[i] = temp_array[i];
	}

	private static boolean Corrsover(Hypotheses hypotheses1,
			Hypotheses hypotheses2, ArrayList<Hypotheses> hyp_list, ArrayList<Attributes> attr_list) {
		Random rd = new Random();
		for (int times=1; times<=3; times++){//loop 3 times for different d1 and d2
			int d1, d2;
			d1 = rd.nextInt(length_of_rule);
			d2 = rd.nextInt(length_of_rule);
			while (d1 >= d2){
				d1 = rd.nextInt(length_of_rule);
				d2 = rd.nextInt(length_of_rule);
			}
			int rule_number_h1 = hypotheses1.hyp_length / length_of_rule;
			int rule_number_h2 = hypotheses2.hyp_length / length_of_rule;
			int h1_d1_rules, h1_d2_rules, h2_d1_rules, h2_d2_rules;
			h1_d1_rules = rd.nextInt(rule_number_h1);
			h1_d2_rules = rd.nextInt(rule_number_h1);
			h2_d1_rules = rd.nextInt(rule_number_h2);
			h2_d2_rules = rd.nextInt(rule_number_h2);
			
			while(h1_d1_rules > h1_d2_rules || h2_d1_rules > h2_d2_rules){
				h1_d1_rules = rd.nextInt(rule_number_h1);
				h1_d2_rules = rd.nextInt(rule_number_h1);
				h2_d1_rules = rd.nextInt(rule_number_h2);
				h2_d2_rules = rd.nextInt(rule_number_h2);
			}
			int h1_d1_index, h1_d2_index, h2_d1_index, h2_d2_index;
			h1_d1_index = d1 + h1_d1_rules*length_of_rule;
			h1_d2_index = d2 + h1_d2_rules*length_of_rule;
			h2_d1_index = d1 + h2_d1_rules*length_of_rule;
			h2_d2_index = d2 + h2_d2_rules*length_of_rule;
			
			int section1[] = new int [h1_d1_index];//using two point variable-length individuals, so I seperate the two hypo into 6 sections
			int section2[] = new int [h1_d2_index - h1_d1_index];
			int section3[] = new int [hypotheses1.hyp_length - h1_d2_index];
			int section4[] = new int [h2_d1_index];
			int section5[] = new int [h2_d2_index - h2_d1_index];
			int section6[] = new int [hypotheses2.hyp_length - h2_d2_index];
			for (int i=0; i<section1.length; i++)
				section1[i] = hypotheses1.hypotheses[i];
			for (int i=0, j=h1_d1_index; i<section2.length; i++, j++)
				section2[i] = hypotheses1.hypotheses[j];
			for (int i=0, j=h1_d2_index; i<section3.length; i++, j++)
				section3[i] = hypotheses1.hypotheses[j];
			
			for (int i=0; i<section4.length; i++)
				section4[i] = hypotheses2.hypotheses[i];
			for (int i=0, j=h2_d1_index; i<section5.length; i++, j++)
				section5[i] = hypotheses2.hypotheses[j];
			for (int i=0, j=h2_d2_index; i<section6.length; i++, j++)
				section6[i] = hypotheses2.hypotheses[j];
			
			if ( Get_new_hyppthese(section1,section2,section3,section4,section5,section6,hyp_list,attr_list) )//if the new one success
					return true;
		}
		return false;
	}

	private static boolean Get_new_hyppthese(int[] section1, int[] section2,
			int[] section3, int[] section4, int[] section5, int[] section6,
			ArrayList<Hypotheses> hyp_list, ArrayList<Attributes> attr_list) {
		
		int new_hyp_1[] = new int [section1.length + section5.length + section3.length];//the two new bit arrays
		int new_hyp_2[] = new int [section4.length + section2.length + section6.length];
		
		int j=0,k=0,m=0;
		for (int i=0;i<new_hyp_1.length;i++){//getting the first new array
			if (i < section1.length){
				new_hyp_1[i] = section1[j];
				j++;
			}
			else if (i >= section1.length && i < section1.length + section5.length){
				new_hyp_1[i] = section5[k];
				k++;
			}
			else if (i >= section1.length + section5.length){
				new_hyp_1[i] = section3[m];
				m++;
			}
		}
		j=0;k=0;m=0;
		for (int i=0;i<new_hyp_2.length;i++){//get the second new array
			if (i < section4.length){
				new_hyp_2[i] = section4[j];
				j++;
			}
			else if (i >= section4.length && i < section4.length + section2.length){
				new_hyp_2[i] = section2[k];
				k++;
			}
			else if (i >= section4.length + section2.length){
				new_hyp_2[i] = section6[m];
				m++;
			}
		}
		
		if ( check_illeague(new_hyp_1, attr_list) ){//if the first new array ok,then check the second array
			if ( check_illeague(new_hyp_2, attr_list) ){//check the second array also ok, then create two hypotheses
				Hypotheses node1 = new Hypotheses();
				Hypotheses node2 = new Hypotheses();
				
				node1.hyp_length = new_hyp_1.length;
				node1.hypotheses = new int [node1.hyp_length];
				for (int i=0;i<node1.hyp_length;i++)
					node1.hypotheses[i] = new_hyp_1[i];
				node1.choosen = 1;
				hyp_list.add(node1);
				
				node2.hyp_length = new_hyp_2.length;
				node2.hypotheses = new int [node2.hyp_length];
				for (int i=0;i<node2.hyp_length;i++)
					node2.hypotheses[i] = new_hyp_2[i];
				node2.choosen = 1;
				hyp_list.add(node2);
				return true;
			}
			else
				return false;
		}
		else
			return false;
	}

	private static boolean check_illeague(int[] new_hyp_1, ArrayList<Attributes> attr_list) {
		int rule_number = new_hyp_1.length / length_of_rule;
		int illeague = 1;
		for (int nth=0;nth<rule_number;nth++){//for each single rule
			int flag = 0;
			int current_position = nth*length_of_rule;
			
			for (int i=0;i<attr_list.size();i++){//each attribute,check the boundary
				if (attr_list.get(i).contious == 0){
					int number_of_1 = 0;
					for (int j = current_position + attr_list.get(i).statr_index; j <= current_position + attr_list.get(i).end_index; j++){
						if (new_hyp_1[j] == 1)
							number_of_1++;
					}
					if (number_of_1 == 0 || number_of_1 > 1){
						flag = 1;
						break;
					}
				}
				else if (attr_list.get(i).contious == 1){
					int first = 0, second = 0;
					for (int j = current_position + attr_list.get(i).statr_index, k=1; j <= current_position + attr_list.get(i).end_index; j++,k++){
						if (k == 1)
							first = new_hyp_1[j]*10;
						else if (k == 2)
							first = first + new_hyp_1[j];
						else if (k == 3)
							second = new_hyp_1[j]*10;
						else if (k == 4)	
							second = second + new_hyp_1[j];
					}
					
					if (first > second){
						flag = 1;
						break;
					}
				}
			}
			if (flag == 1){
				illeague = 0;
				break;
			}
		}
		if (illeague == 0)
			return false;
		else
			return true;
	}

	private static int find_the_hyp(double luck_number,
			ArrayList<Hypotheses> hyp_list) {
		int choosen_index = 0;
		for (int i=0;i<hyp_list.size();i++){//check the luck number falls down in which area
			if (hyp_list.get(i).choosen == 0){
				if (luck_number >= hyp_list.get(i).statr_range && luck_number < hyp_list.get(i).end_range){
					hyp_list.get(i).choosen = 1;
					choosen_index = i;
					break;
				}
			}
		}
		return choosen_index;
	}

	private static void Re_find_the_boundary(ArrayList<Hypotheses> hyp_list) {
		double current_range=0.0;
		for (int i=0;i<hyp_list.size();i++){
			if (hyp_list.get(i).choosen == 0){//this time,only check the hypo which has not been choosen,and resign the boundary for each of them
				hyp_list.get(i).statr_range = current_range;
				hyp_list.get(i).end_range = current_range + hyp_list.get(i).fitness_prob;
				current_range = hyp_list.get(i).end_range;
			}
		}
	}

	private static void Rank(ArrayList<Hypotheses> hyp_list) {
		int number_of_hp = hyp_list.size();
		int select_index[] = new int [number_of_hp];
		
		double previous_max = 0.0;
		int previous_rank = number_of_hp;//set the number of hypo as the highest rank number
		int sum_rank = 0;
		for (int i=0;i<number_of_hp;i++){
			double current_max = 0.0;
			int max_index = 0;
			int current_rank = 0;
			for (int j=0;j<number_of_hp;j++){
				if ( select_index[j] == 0 && hyp_list.get(j).fitness > current_max){
					current_max = hyp_list.get(j).fitness;
					max_index = j;
				}
			}
			select_index[max_index] = 1;
			if (current_max >= previous_max){//if the current max is the same as previous,give it the same rank number
				previous_max = current_max;
				current_rank = previous_rank;
			}
			else if (current_max < previous_max){
				previous_max = current_max;
				current_rank = previous_rank-1;
			}
			hyp_list.get(max_index).rank_number = current_rank;
			sum_rank = current_rank + sum_rank;
		}
		double current_range=0.0;
		for (int i=0;i<number_of_hp;i++){//set the boundary for each hypo base on the probability
			hyp_list.get(i).fitness_prob = (double)hyp_list.get(i).rank_number / sum_rank;
			hyp_list.get(i).statr_range = current_range;
			hyp_list.get(i).end_range = current_range + hyp_list.get(i).fitness_prob;
			current_range = hyp_list.get(i).end_range;
		}
	}

	private static void Tournament(ArrayList<Hypotheses> hyp_list) {
		int number_of_hp = hyp_list.size();
		int select_index[] = new int [number_of_hp];
		Random rd = new Random();
		
		double win_prob = 0;
		double lose_prob = 0;
		if (number_of_hp%2 == 0){
			win_prob = 0.7/(number_of_hp/2);//set the higher fitness with a probability
			lose_prob = 0.3/(number_of_hp/2);//set the lower fitness with a probability
		}
		else if (number_of_hp%2 == 1){
			win_prob = 0.7/(number_of_hp/2 + 0.7);
			lose_prob = 0.3/(number_of_hp/2 + 0.7);
		}
		while (number_of_hp > 2){
			int first_index = 0;
			int second_index = 0;
			int first_random = rd.nextInt(number_of_hp);
			int count = -1;
			for (int i=0;i<select_index.length;i++){
				if (select_index[i] == 0)//"count" means the i_th random position.
					count++;
				if (count == first_random){
					first_index = i;
					count = -1;//reset the count as -1
					select_index[i] = 1;
					number_of_hp = number_of_hp - 1;
					break;
				}
			}
			int second_random = rd.nextInt(number_of_hp);
			for (int i=0;i<select_index.length;i++){
				if (select_index[i] == 0)//"count" means the i_th random position.
					count++;
				if (count == second_random){
					second_index = i;
					count = -1;//reset the count as -1
					select_index[i] = 1;
					number_of_hp = number_of_hp - 1;
					break;
				}
			}
			if (hyp_list.get(first_index).fitness >= hyp_list.get(second_index).fitness){
				hyp_list.get(first_index).fitness_prob = win_prob;
				hyp_list.get(second_index).fitness_prob = lose_prob;
			}
			else{
				hyp_list.get(first_index).fitness_prob = lose_prob;
				hyp_list.get(second_index).fitness_prob = win_prob;
			}
		}
		if (number_of_hp == 2){
			int first_index = 0;
			int second_index = 0;
			for (int i=0;i<select_index.length;i++){
				if (select_index[i] == 0){
					first_index = i;
					select_index[i] = 1;
					break;
				}
			}
			for (int i=0;i<select_index.length;i++){
				if (select_index[i] == 0){
					second_index = i;
					select_index[i] = 1;
					break;
				}
			}
			if (hyp_list.get(first_index).fitness >= hyp_list.get(second_index).fitness){
				hyp_list.get(first_index).fitness_prob = win_prob;
				hyp_list.get(second_index).fitness_prob = lose_prob;
			}
			else{
				hyp_list.get(first_index).fitness_prob = lose_prob;
				hyp_list.get(second_index).fitness_prob = win_prob;
			}
		}
		else if (number_of_hp == 1){
			int index = 0;
			for (int i=0;i<select_index.length;i++){
				if (select_index[i] == 0){
					index = i;
					select_index[i] = 1;
					break;
				}
			}
			hyp_list.get(index).fitness_prob = win_prob;
		}
		double current_range=0.0;
		for (int i=0;i<hyp_list.size();i++){//set the boundary for each hypo base on the probability
			hyp_list.get(i).statr_range = current_range;
			hyp_list.get(i).end_range = current_range + hyp_list.get(i).fitness_prob;
			current_range = hyp_list.get(i).end_range;
		}
	}

	private static void Set_prob_for_hyp(ArrayList<Hypotheses> hyp_list) {
		double sum = 0;
		for (int i=0;i<hyp_list.size();i++)
			sum = hyp_list.get(i).fitness + sum;
		double current_range=0.0;
		for (int i=0;i<hyp_list.size();i++){//set the boundary for each hypo base on the probability
			hyp_list.get(i).fitness_prob = hyp_list.get(i).fitness/sum;
			hyp_list.get(i).statr_range = current_range;
			hyp_list.get(i).end_range = current_range + hyp_list.get(i).fitness_prob;
			current_range = hyp_list.get(i).end_range;
		}
	}

	private static double Correct_rate(Hypotheses hypotheses,
			String[][] train, int examples, ArrayList<Attributes> attr_list) {
		
		int number_of_rules = hypotheses.hyp_length/length_of_rule;
		int number_of_correct = 0;
		for (int eg_index = 0;eg_index < examples;eg_index++){//for each coming instance,check if it matches or not
			int default_flag = 0;
			for (int nth=0;nth<number_of_rules;nth++){
				int temp_single_rule[] = new int [length_of_rule];
				for (int i=0, j=nth*length_of_rule; i<length_of_rule; i++, j++)
					temp_single_rule[i] = hypotheses.hypotheses[j];
				int indicate = check_match_and_correct(train[eg_index], temp_single_rule, attr_list);//get the indicate number
				if ( indicate == 1 ){//current rule can not match attributes,check the next rule
					default_flag = 1;
					continue;
				}
				else if ( indicate == 2 ){//current rule match the instance,but predicate wrong
					default_flag = 0;
					break;
				}
				else if (indicate == 3){//current rule match the instance,and predict right
					default_flag = 0;
					number_of_correct++;
					break;
				}
			}
			if (default_flag == 1){
				String default_class_value = attr_list.get(attr_list.size()-1).value_rang[1];
				if (default_class_value.equals(train[eg_index][train[eg_index].length-1]))
					number_of_correct++;
			}
		}
		if (number_of_correct == 0)
			return 0.001;
		else{
			double rate = (double) number_of_correct/examples;
			return rate;
		}
	}

	private static int check_match_and_correct(String[] instance,
			int[] temp_single_rule, ArrayList<Attributes> attr_list) {
		
		for (int i=0;i<attr_list.size()-1;i++){//this loop is to check the instance if it matches the rule
			if (attr_list.get(i).contious == 0){
				int position_1 = 0;
				for (int index=attr_list.get(i).statr_index, k=1; index <= attr_list.get(i).end_index; index++, k++)
					if (temp_single_rule[index] == 1)
						position_1 = k;
				
				String value_name;
				value_name = attr_list.get(i).value_rang[position_1];
				if (!instance[i].equals(value_name))
					return 1;//doesn't match
			}
			else if (attr_list.get(i).contious == 1){
				double number = Double.parseDouble(instance[i]);
				int first = 0, second = 0;
				for (int index=attr_list.get(i).statr_index, k=1; index <= attr_list.get(i).end_index; index++, k++){
					if (k == 1)
						first = temp_single_rule[index]*10;
					else if (k == 2)
						first = first + temp_single_rule[index];
					else if (k == 3)
						second = temp_single_rule[index]*10;
					else if (k == 4)	
						second = second + temp_single_rule[index];
				}
				double small, big;
				small = (double)first/10;
				big = (double)second/10;
				
				if ( !(number <= big && number >= small) )
					return 1;//doesn't match
			}
		}
		int class_position = 0;
		int class_index = attr_list.size()-1;
		for (int index=attr_list.get(class_index).statr_index, k=1; index <= attr_list.get(class_index).end_index; index++, k++)
			if (temp_single_rule[index] == 1)
				class_position = k;
		String value_name;
		value_name = attr_list.get(class_index).value_rang[class_position];
		if (!instance[class_index].equals(value_name))
			return 2;//means it matches the attribute but wrong class
		else
			return 3;//means it matches the attribute and class
	}

	private static void Initial_hyp(int population,
			ArrayList<Hypotheses> hyp_list, ArrayList<Attributes> attr_list) {
		
		int number = 1;
		while(number <= population){
			Hypotheses node = new Hypotheses();
			node.hyp_length = length_of_rule*2;
			node.hypotheses = new int [node.hyp_length];
			Random rd = new Random();
			
			int illeague = 1;
			for (int nth=0;nth<2;nth++){//this loop is for getting each single rule
				int flag = 0;
				int current_position = nth*length_of_rule;
				
				for (int i=0;i<attr_list.size();i++){//in each single single rule, check each attribute
					if (attr_list.get(i).contious == 0){
						int attribute_length =  attr_list.get(i).value_rang.length-1;
						int position_of_1 = rd.nextInt(attribute_length);//random a number in the range of the attribute
						for (int j = current_position + attr_list.get(i).statr_index, k=0; j <= current_position + attr_list.get(i).end_index; j++, k++){
							if ( k == position_of_1)//set the randomly position as 1
								node.hypotheses[j] = 1;
							else
								node.hypotheses[j] = 0;
						}
					}
					else if (attr_list.get(i).contious == 1){//set continous value
						int first = 0, second = 0;
						for (int j = current_position + attr_list.get(i).statr_index, k=1; j <= current_position + attr_list.get(i).end_index; j++,k++){
							node.hypotheses[j] = rd.nextInt(10);
							if (k == 1)
								first = node.hypotheses[j]*10;
							else if (k == 2)
								first = first + node.hypotheses[j];
							else if (k == 3)
								second = node.hypotheses[j]*10;
							else if (k == 4)	
								second = second + node.hypotheses[j];
						}
						
						if (first > second){//if the first number bigger than the second
							flag = 1;
							break;
						}
					}
				}
				if (flag == 1){//which means illeague
					illeague = 0;
					break;
				}
			}
			if (illeague == 0)//if illeague, then continue the next loop,else increase the number
				continue;
			else
				number++;
			hyp_list.add(node);
		}
	}

	private static void Set_start_end_index_for_attribute(ArrayList<Attributes> Attr_list){
		int current_index = 0;
		for (int i=0;i<Attr_list.size();i++){
			if (Attr_list.get(i).contious == 0)
				length_of_rule = length_of_rule + (Attr_list.get(i).value_rang.length-1);
			else if (Attr_list.get(i).contious == 1)
				length_of_rule = length_of_rule + 4;
			Attr_list.get(i).statr_index = current_index;
			Attr_list.get(i).end_index = length_of_rule-1;
			current_index = length_of_rule;
		}
	}
}
