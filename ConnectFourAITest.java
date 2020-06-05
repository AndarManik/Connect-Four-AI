import java.util.Scanner;

public class ConnectFourAITest
{
	public static void main(String[] args)
	{
		
		NeuralNetwork policy = new NeuralNetwork("C:\\Users\\Agi\\eclipse-workspace\\Connect Four AI\\src\\Save.txt", (x) -> (x > 0) ? x : 0, (x, y) -> (y > 0) ? 1 : 0);
		
		policy.setOutputActivation((x) -> (1 / (Math.exp(x * -1) + 1)), (x, y) -> x - x * x);
		
		double[] board = new double[42];
		
		int policyWin = 0;
		int randomWin = 0;
		
		Scanner sc = new Scanner(System.in);
		
		for(int i = 0; i >= 0; i++)
		{
			double[] play = policy.calc(board);
			
			while(play(board, argMax(play)) == null)
				play[argMax(play)] = 0;
			
			board = getNegative(play(board, argMax(play)));
			
			show(board);
			
			for(double value: play)
				System.out.print(value + " ");
			System.out.println();
			
			if(isWin(board))
			{
				policyWin++;
				board = new double[42];
				System.out.println(policyWin / Math.max(1, randomWin));
			}
				
			if(isFull(board))
				board = new double[42];
			
			
			play = new double[7];
			for(int j = 0; j < play.length; j++)
				play[j] = Math.random();
			
			
			
			board = getNegative(play(board, sc.nextInt() - 1));
			
			//show(getNegative(board));
			
			if(isWin(board))
			{
				randomWin++;
				board = new double[42];
				System.out.println(policyWin / Math.max(1, randomWin));
			}
				
			if(isFull(board))
				board = new double[42];
		}
	}
	
	public static double[] getNegative(double[] input)
	{
		double[] output = new double[input.length];
		
		for(int i = 0; i < input.length; i++)
			output[i] -= input[i];
		
		return output;
	}
	
	public static double[] play(double[] gameBoard, int pos)
	{
		double[] board = gameBoard.clone();
		
		boolean isFullRow = true;
		
		for(int i = 0; i < 6 && isFullRow; i++)
		{
			if(board[pos * 6 + i] == 0)
			{
				board[pos * 6 + i] = 1;
				
				isFullRow = false;
			}
		}
		
		if(isFullRow)
			return null;
		
		return board;
	}
	
	public static boolean isWin(double[] gameBoard)
	{
		double[][] board = new double[7][6];
		
		for(int i = 0; i < gameBoard.length; i++)
			board[i / 6][i % 6] = gameBoard[i];
		
		for(int i = 0; i < board.length; i++)
		{
			for(int j = 0; j < board[0].length; j++)
			{
				if(i < board.length - 3 &&
						board[i][j] == board[i+1][j] && 
						board[i+1][j] == board[i+2][j] && 
						board[i+2][j] == board[i+3][j] && 
						board[i][j] != 0)
					return true;
				
				if(j < board[0].length - 3 &&
						board[i][j] == board[i][j+1] && 
						board[i][j+1] == board[i][j+2] && 
						board[i][j+2] == board[i][j+3] && 
						board[i][j] != 0)
					return true;
				
				if(i < board.length - 3 && j < board[0].length - 3 &&
						board[i][j] == board[i+1][j+1] && 
						board[i+1][j+1] == board[i+2][j+2] && 
						board[i+2][j+2] == board[i+3][j+3] && 
						board[i][j] != 0)
					return true;
				
				if(i < board.length - 3 && j >= 3 &&
						board[i][j] == board[i+1][j-1] && 
						board[i+1][j-1] == board[i+2][j-2] && 
						board[i+2][j-2] == board[i+3][j-3] && 
						board[i][j] != 0)
					return true;
			}
		}
		return false;
	}
	
	public static boolean isFull(double[] input)
	{
		for(double value: input)
			if(value == 0)
				return false;
		
		return true;
	}
	
	public static int argMax(double[] input)
	{
		int index = 0;
		
		for(int i = 0; i < input.length; i++)
			if(input[index] < input[i])
				index = i;
		
		return index;
	}
	
	public static void show(double[] board)
	{
		for(int i = 5; i >= 0; i--)
		{
			for(int j = 0; j < 7; j++)
			{
				if(board[j * 6 + i] == 1.0)
					System.out.print("X ");
				if(board[j * 6 + i] == -1.0)
					System.out.print("O ");
				if(board[j * 6 + i] == 0.0)
					System.out.print(". ");
			}
			System.out.println();	
		}
		System.out.println("1 2 3 4 5 6 7 ");
	}
}
