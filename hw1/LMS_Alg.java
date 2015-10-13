/*
 * in the program, you need to input the file name, teacher_train_set.txt is with teacher, 
 * withouteacher_train_set.txt is without teacher. Input the absolute path for the file.
 */
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

class Game {//each object is one game
	String one_game [][];//use the matrix to store a game,each row is one board state
	int number_row;
}

public class LMS_Alg {
	static int xi[] = new int [6];//this is the weight value
	static int x_win = 0, o_win = 0, tie = 0;
	
	public static void main(String args[]) throws IOException{
		
		String file = null;
		Scanner in = new Scanner(System.in);
		System.out.print("input the file path:");
		file = in.nextLine();
		
		FileReader fr = new FileReader(file);
		BufferedReader br =  new BufferedReader(fr);
		ArrayList<Game> game_list = new ArrayList<Game>();
		String Line;
		int number = 0;
		while ((Line = br.readLine()) != null){//this loop is to store number of rows in each game
			number++;
			if (Line.equals("")){
				Game node = new Game();
				node.number_row = number-1;
				node.one_game = new String[number][];
				game_list.add(node);
				number=0;
				continue;
			}
		}
		
		br.close();
		fr.close();
		fr = new FileReader(file);
		br =  new BufferedReader(fr);
		int game_index = 0;
		int current_row = 0;
		while ((Line = br.readLine()) != null){//create a String matrix to store one game
			if (Line.equals("")){
				game_index++;
				current_row = 0;
				continue;
			}
			game_list.get(game_index).one_game[current_row] = Line.split(" ");
			current_row++;
		}
		
		double wi[] = new double [6];
		wi[1]=1; wi[2]=2; wi[3]=3;
		wi[4]=-1; wi[5]=-2; //wi[6]=-3;
		for (int m=1;m<=50;m++){
			for (int i=0;i<game_list.size();i++){
				int j = game_list.get(i).number_row;
				double endgame = 0;//this is value of the final board, 100 or -100 or 0
				if (j == 6 || j == 8)
					endgame = -100;
				else if (j == 5 || j == 7)
					endgame = 100;
				else if (j == 9){//if the board has 9 spots,means either x win or tie
					if (checkwin(game_list.get(i).one_game[j-1]))
						endgame = 100;
					else
						endgame = 0;
				}
				int num = (j+1)/2;//num is the number of boards we need in one game
				double train [] = new double [num];
				num--;
				train[num] = endgame;
				xi[1]=xi[2]=xi[3]=xi[4]=xi[5]=0;//initial the xi
				Get_Xi(game_list.get(i).one_game[num*2]);//get the x1~x5
				for (int k=1;k<=5;k++){
					double V_hat = wi[1]*xi[1] + wi[2]*xi[2] + wi[3]*xi[3] + wi[4]*xi[4] + wi[5]*xi[5];
					wi[k] = wi[k] + 0.1*xi[k]*(train[num] - V_hat);
				}
				num--;//back to the previous board
				while (num>=0){//run all the board in one game
					xi[1]=xi[2]=xi[3]=xi[4]=xi[5]=0;
					Get_Xi(game_list.get(i).one_game[(num+1)*2]);//use the successor's xi[] value
					train[num] = wi[1]*xi[1] + wi[2]*xi[2] + wi[3]*xi[3] + wi[4]*xi[4] + wi[5]*xi[5];//sign the successor's V_hat value to train value
					
					xi[1]=xi[2]=xi[3]=xi[4]=xi[5]=0;
					Get_Xi(game_list.get(i).one_game[num*2]);//this time, find out the current board xi's value
					for (int k=1;k<=5;k++){
						double V_hat = wi[1]*xi[1] + wi[2]*xi[2] + wi[3]*xi[3] + wi[4]*xi[4] + wi[5]*xi[5];
						wi[k] = wi[k] + 0.1*xi[k]*(train[num] - V_hat);
					}
					num--;//back to the previous board
				}
			}
		}
		for (int i=1;i<=5;i++){
			System.out.println("W"+ i +" is:"+wi[i]);
		}
		PlayGame(wi);//play the game
	}

	private static void PlayGame(double[] wi) {
		Scanner in = new Scanner(System.in);
		while (true){
			System.out.print("Computer plays first:y/n? or end?:");
			String str;
			str = in.next();
			if (!str.equals("y") && !str.equals("n"))
				break;
			
			String board [] = {"b","b","b","b","b","b","b","b","b"};
			System.out.println("The initial board:");
			printboard(board);
			while (true){
				if (str.equals("n")){
					System.out.print("your move (1~9):");
					int move = in.nextInt();
					board[move-1] = "o";
					printboard(board);
					if (endGame(board)){
						if (x_win == 1){
							x_win = 0;
							System.out.println("X win!");
						}
						else if (o_win == 1){
							o_win = 0;
							System.out.println("O win!");
						}
						else{
							tie = 0;
							System.out.println("Tie game!");
						}
						break;
					}
				}
				
				double score [] = new double [9];
				for (int i=0;i<9;i++)
					score [i] = -1000;
				
				for (int i=0;i<9;i++){
					String temp_board [] = new String [9];
					for (int j=0;j<9;j++)
						temp_board[j] = board[j];
					
					if (temp_board[i].equals("b")){
						temp_board[i] = "x";
						xi[1]=xi[2]=xi[3]=xi[4]=xi[5]=0;
						Get_Xi(temp_board);
						score[i] = wi[1]*xi[1] + wi[2]*xi[2] + wi[3]*xi[3] + wi[4]*xi[4] + wi[5]*xi[5];		
					}
				}
				double max = score[0];
				int next_move=0;
				for (int i=0;i<9;i++){
					if (score[i]>=max){
						max = score[i];
						next_move = i;
					}
				}
				board[next_move] = "x";
				System.out.println("Computer move:");
				printboard(board);
				if (endGame(board)){
					if (x_win == 1){
						x_win = 0;
						System.out.println("X win!");
					}
					else if (o_win == 1){
						o_win = 0;
						System.out.println("O win!");
					}
					else{
						tie = 0;
						System.out.println("Tie game!");
					}
					break;
				}
				if (str.equals("y")){
					System.out.print("your move (1~9):");
					int move = in.nextInt();
					board[move-1] = "o";
					printboard(board);
					if (endGame(board)){
						if (x_win == 1){
							x_win = 0;
							System.out.println("X win!");
						}
						else if (o_win == 1){
							o_win = 0;
							System.out.println("O win!");
						}
						else{
							tie = 0;
							System.out.println("Tie game!");
						}
						break;
					}
				}
			}
		}
	}

	private static void printboard(String[] board) {//given a board state,print the board
		for (int i=0;i<3;i++){
			if (board[i].equals("b"))
				System.out.print("- ");
			else
				System.out.print(board[i]+" ");
		}
		System.out.println();
		for (int i=3;i<6;i++){
			if (board[i].equals("b"))
				System.out.print("- ");
			else
				System.out.print(board[i]+" ");
		}
		System.out.println();
		for (int i=6;i<9;i++){
			if (board[i].equals("b"))
				System.out.print("- ");
			else
				System.out.print(board[i]+" ");
		}
		System.out.println();
	}

	private static boolean endGame(String[] board) {//this is for playing the game,while we need to know if someone wins or not
		if (Check_end(board[0],board[1],board[2]))
			return true;
		else if (Check_end(board[3],board[4],board[5]))
			return true;
		else if (Check_end(board[6],board[7],board[8]))
			return true;
		else if (Check_end(board[0],board[3],board[6]))
			return true;
		else if (Check_end(board[1],board[4],board[7]))
			return true;
		else if (Check_end(board[2],board[5],board[8]))
			return true;
		else if (Check_end(board[0],board[4],board[8]))
			return true;
		else if (Check_end(board[2],board[4],board[6]))
			return true;
		else {//end of the game and it is a tie game
			int num = 0;
			for (int i = 0;i<9;i++)
				if (!board[i].equals("b"))
					num++;
			if (num == 9){
				tie = 1;
				return true;
			}
			else
				return false;
		}
	}

	private static boolean Check_end(String str1, String str2, String str3) {
		if (str1.equals(str2)){
			if (str2.equals(str3)){
				if (str3.equals("b"))//same chars but not the "b"
					return false;
				else{
					if (str1.equals("x"))
						x_win = 1;
					else
						o_win = 1;
					return true;//game over
				}
			}
			else
				return false;//not game over
		}
		else
			return false;//not game over
	}

	private static void Get_Xi(String[] str) {
		checkLine(str[0],str[1],str[2]);
		checkLine(str[3],str[4],str[5]);
		checkLine(str[6],str[7],str[8]);
		checkLine(str[0],str[3],str[6]);
		checkLine(str[1],str[4],str[7]);
		checkLine(str[2],str[5],str[8]);
		checkLine(str[0],str[4],str[8]);
		checkLine(str[2],str[4],str[6]);
	}
	
	private static void checkLine(String str1, String str2, String str3) {
		//in each board state,get the x1~x5
		int x=0, o=0, b=0;
		if (str1.equals("x"))
			x++;
		else if (str1.equals("o"))
			o++;
		else if (str1.equals("b"))
			b++;
		if (str2.equals("x"))
			x++;
		else if (str2.equals("o"))
			o++;
		else if (str2.equals("b"))
			b++;
		if (str3.equals("x"))
			x++;
		else if (str3.equals("o"))
			o++;
		else if (str3.equals("b"))
			b++;
		
		if (x == 3)//three x in one line
			xi[3]++;
		else if (x == 2 && b == 1)//two x in one line,and no o in this line
			xi[2]++;
		else if (o == 2 && b == 1)//two o in one line,and no x in this line
			xi[5]++;
		else if (x == 1 && b == 2)//one x in one line,and no o in this line
			xi[1]++;
		else if (o == 1 && b == 2)//one o in one line,and no x in this line
			xi[4]++;
	}
	
	private static boolean checkwin(String[] str) {//analyze each row, column, diagonal to see if the x is win or tie
		if (Check_same(str[0],str[1],str[2]))
			return true;
		else if (Check_same(str[3],str[4],str[5]))
			return true;
		else if (Check_same(str[6],str[7],str[8]))
			return true;
		else if (Check_same(str[0],str[3],str[6]))
			return true;
		else if (Check_same(str[1],str[4],str[7]))
			return true;
		else if (Check_same(str[2],str[5],str[8]))
			return true;
		else if (Check_same(str[0],str[4],str[8]))
			return true;
		else if (Check_same(str[2],str[4],str[6]))
			return true;
		else
			return false;
	}

	private static boolean Check_same(String str1, String str2, String str3) {
		//check the three spots if they are the same char
		if (str1.equals(str2)){
			if (str2.equals(str3))
				return true;
			else
				return false;
		}
		else
			return false;
	}
}