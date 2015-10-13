import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class teacher_board {
	public static void main(String args[]) throws IOException{
		FileWriter fw = new FileWriter("E:/Machine Learning/teacher_train_set.txt",true);
		BufferedWriter bw = new BufferedWriter(fw);
		
		String input_piece;
		Scanner in = new Scanner(System.in);
		for (int i=1;i<=30;i++){
			String board [] = {"b","b","b","b","b","b","b","b","b"};
			
			while (endGame(board)){
				printboard(board);
				System.out.print("input the piece 'x' or 'o' and its position:");
				int position;
				input_piece = in.next();
				position = in.nextInt();
				board [position-1] =  input_piece;
				String inputLine = board[0] + " " + board[1] + " " + board[2] + " " + board[3] + " " + board[4] + " " + 
									board[5] + " " + board[6] + " " + board[7] + " " + board[8] + "\n";
				bw.write(inputLine);
				bw.flush();
			}
			bw.write("\n");
			bw.flush();
		}
	}

	private static boolean endGame(String[] board) {
		if (Check_same(board[0],board[1],board[2]))
			return false;
		else if (Check_same(board[3],board[4],board[5]))
			return false;
		else if (Check_same(board[6],board[7],board[8]))
			return false;
		else if (Check_same(board[0],board[3],board[6]))
			return false;
		else if (Check_same(board[1],board[4],board[7]))
			return false;
		else if (Check_same(board[2],board[5],board[8]))
			return false;
		else if (Check_same(board[0],board[4],board[8]))
			return false;
		else if (Check_same(board[2],board[4],board[6]))
			return false;
		else {
			int num = 0;
			for (int i = 0;i<9;i++)
				if (!board[i].equals("b"))
					num++;
			if (num == 9)
				return false;
			else
				return true;
		}
	}

	private static boolean Check_same(String str1, String str2,
			String str3) {
		if (str1.equals(str2)){
			if (str2.equals(str3)){
				if (str3.equals("b"))
					return false;
				else
					return true;
			}
			else
				return false;//not game over
		}
		else
			return false;//not game over
	}

	private static void printboard(String[] board) {
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
}
