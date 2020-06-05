import java.util.Scanner;

public class temp
{
	public static void main(String[] args)
	{
		double[] board = new double[42];
		
		Scanner sc = new Scanner(System.in);
		
		for(int i = 0; i < 100; i++)
		{
			board = getNegative(play(board, randomChoose(softMax(choice(board, 6)))));
			
			show(board);
			
			board = getNegative(play(board, randomChoose(softMax(choice(board, 6)))));
			
			show(getNegative(board));
		}
			

	}
	
	public static int randomChoose(double[] input)
	{
		double pos = Math.random();
		
		double sum = 0;
		
		for(int i = 0; i < input.length; i++)
		{
			sum += input[i];
			
			if(sum > pos)
				return i;
		}
		
		return -1;
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

	public static double[] choice(double[] board, int depth)
	{
		double[] play = new double[7];
		
		for(int i = 0; i < play.length; i++)
		{
			if(play(board, i) != null && isWin(play(board, i)))
				play[i] = 1;
			else if(play(board, i) != null && depth != 0)
			{
				double[] curChoice = choice(getNegative(play(board, i)), depth - 1);
				
				double max = curChoice[0];
				
				for(double value: curChoice)
					max = Math.max(max, value);
				
				play[i] = (1 - max);
			}
			
		}
		
		return play;
	}

	public static double[] softMax(double[] play)
	{
		double[] output = new double[play.length];

		double sum = 0;

		for (int i = 0; i < play.length; i++)
		{
			output[i] = Math.exp(play[i]);
			sum += Math.exp(play[i]);
		}

		for (int i = 0; i < play.length; i++)
			output[i] /= sum;

		return output;
	}

	public static boolean isWin(double[] gameBoard)
	{
		double[][] board = new double[7][6];

		for (int i = 0; i < gameBoard.length; i++)
			board[i / 6][i % 6] = gameBoard[i];

		for (int i = 0; i < board.length; i++)
		{
			for (int j = 0; j < board[0].length; j++)
			{
				if (i < board.length - 3 && board[i][j] == board[i + 1][j] && board[i + 1][j] == board[i + 2][j]
						&& board[i + 2][j] == board[i + 3][j] && board[i][j] != 0)
					return true;

				if (j < board[0].length - 3 && board[i][j] == board[i][j + 1] && board[i][j + 1] == board[i][j + 2]
						&& board[i][j + 2] == board[i][j + 3] && board[i][j] != 0)
					return true;

				if (i < board.length - 3 && j < board[0].length - 3 && board[i][j] == board[i + 1][j + 1]
						&& board[i + 1][j + 1] == board[i + 2][j + 2] && board[i + 2][j + 2] == board[i + 3][j + 3]
						&& board[i][j] != 0)
					return true;

				if (i < board.length - 3 && j >= 3 && board[i][j] == board[i + 1][j - 1]
						&& board[i + 1][j - 1] == board[i + 2][j - 2] && board[i + 2][j - 2] == board[i + 3][j - 3]
						&& board[i][j] != 0)
					return true;
			}
		}
		return false;
	}

	public static double[] play(double[] gameBoard, int pos)
	{
		double[] board = gameBoard.clone();

		boolean isFullRow = true;

		for (int i = 0; i < 6 && isFullRow; i++)
		{
			if (board[pos * 6 + i] == 0)
			{
				board[pos * 6 + i] = 1;

				isFullRow = false;
			}
		}

		if (isFullRow)
			return null;

		return board;
	}

	public static double[] getNegative(double[] input)
	{
		double[] output = new double[input.length];

		for (int i = 0; i < input.length; i++)
			output[i] -= input[i];

		return output;
	}
}
