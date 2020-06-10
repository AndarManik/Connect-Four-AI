import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class temp
{
	public static void main(String[] args)
	{
		int[] dim = {42, 7};
		NeuralNetwork policy = new NeuralNetwork("C:\\Users\\Agi\\eclipse-workspace\\Connect Four AI\\src\\Save.txt",
				(x) -> (x > 0) ? x : 0, (x, y) -> (y > 0) ? 1 : 0);
		NeuralNetwork random = new NeuralNetwork(dim, (x) -> (x > 0) ? x : 0, (x, y) -> (y > 0) ? 1 : 0);
		policy.setOutputActivation((x) -> (1 / (Math.exp(x * -1) + 1)), (x, y) -> x - x * x);
		random.setOutputActivation((x) -> (1 / (Math.exp(x * -1) + 1)), (x, y) -> x - x * x);
		Scanner sc = new Scanner(System.in);
		double[] board = new double[42];

		int rand = 0;
		int dist = 0;
		while(true)
		{
			double[] play = policy.calc(board);

			while(play(board, argMax(play)) == null)
				play[argMax(play)] = 0;

			board = getNegative(play(board, argMax(play)));

			if(isWin(board))
			{
				board = new double[42];
				dist++;
			}
			if(isFull(board))
				board = new double[42];

			show(getNegative(board));

			board = getNegative(play(board, sc.nextInt() - 1));

			if(isWin(board))
			{
				board = new double[42];
				rand++;
			}
			if(isFull(board))
				board = new double[42];

			show(board);
		}
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

	public static List<List<double[]>> getDistroData(NeuralNetwork policy, double[] board, int volume)
	{
		List<double[]> boards = new ArrayList<double[]>();
		List<double[]> plays = new ArrayList<double[]>();

		boards.add(board);
		plays.add(policy.calc(board));

		List<double[]> boardStor = new ArrayList<double[]>();
		List<double[]> playStor = new ArrayList<double[]>();
		List<Double> prob = new ArrayList<Double>();

		for(double value: plays.get(0))
		{
			boardStor.add(boards.get(0));
			playStor.add(plays.get(0));
			prob.add(value);
		}

		int counter = 0;

		while(counter < volume - 1 && prob.size() > 0)
		{
			int index = indexMax(prob);

			int pos = indexOf(playStor.get(index), prob.get(index));

			double[] curBoard = play(boardStor.get(index), pos);

			while((curBoard == null || isWin(curBoard) || isFull(curBoard) || boards.contains(curBoard))
					&& prob.size() > 0)
			{
				if(curBoard == null)
					playStor.get(index)[pos] = 0;
				else if(isWin(curBoard) || isFull(curBoard))
					playStor.get(index)[pos] = 1;

				boardStor.remove(index);
				playStor.remove(index);
				prob.remove(index);

				index = indexMax(prob);

				if(prob.size() > 0)
				{
					pos = indexOf(playStor.get(index), prob.get(index));
					curBoard = play(boardStor.get(index), pos);
				}
			}

			if(prob.size() > 0)
			{
				curBoard = getNegative(curBoard);
				boards.add(curBoard);
				plays.add(policy.calc(curBoard));
				counter++;

				boardStor.remove(index);
				playStor.remove(index);
				prob.remove(index);

				for(double value: plays.get(counter))
				{
					boardStor.add(boards.get(counter));
					playStor.add(plays.get(counter));
					prob.add(value);
				}
			}
		}
		List<double[]> subBoards;
		List<double[]> subPlays;
		for(int i = 2; i < boards.size() + 1; i++)
		{
			subBoards = boards.subList(boards.size() - 2, boards.size());
			subPlays = plays.subList(plays.size() - 2, plays.size());

			for(int pos = 0; pos < 7; pos++)
			{
				int index = indexOf(subBoards, play(subBoards.get(0), pos));

				if(index != -1)
				{
					double[] play = subBoards.get(index);
					subPlays.get(0)[pos] = 1 - play[argMax(play)];
				}
			}
		}

		List<List<double[]>> output = new ArrayList<List<double[]>>();

		output.add(boards);
		output.add(plays);

		return output;
	}

	public static double maxVal(double[] input)
	{
		double max = input[0];

		for(double val: input)
			if(max < val)
				max = val;

		return max;
	}

	public static int indexOf(List<double[]> input, double[] item)
	{
		if(item == null)
			return -1;
		for(int i = 0; i < input.size(); i++)
		{
			boolean isSame = true;
			for(int j = 0; j < input.get(i).length; j++)
				if(input.get(i)[j] != item[j])
					isSame = false;

			if(isSame)
				return i;
		}
		return -1;
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
				if(i < board.length - 3 && board[i][j] == board[i + 1][j] && board[i + 1][j] == board[i + 2][j]
						&& board[i + 2][j] == board[i + 3][j] && board[i][j] != 0)
					return true;

				if(j < board[0].length - 3 && board[i][j] == board[i][j + 1] && board[i][j + 1] == board[i][j + 2]
						&& board[i][j + 2] == board[i][j + 3] && board[i][j] != 0)
					return true;

				if(i < board.length - 3 && j < board[0].length - 3 && board[i][j] == board[i + 1][j + 1]
						&& board[i + 1][j + 1] == board[i + 2][j + 2] && board[i + 2][j + 2] == board[i + 3][j + 3]
						&& board[i][j] != 0)
					return true;

				if(i < board.length - 3 && j >= 3 && board[i][j] == board[i + 1][j - 1]
						&& board[i + 1][j - 1] == board[i + 2][j - 2] && board[i + 2][j - 2] == board[i + 3][j - 3]
						&& board[i][j] != 0)
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

	public static int indexOf(double[] array, double item)
	{
		for(int i = 0; i < array.length; i++)
			if(array[i] == item)
				return i;

		return -1;
	}

	public static int indexMax(List<Double> prob)
	{
		int index = 0;

		for(int i = 0; i < prob.size(); i++)
			if(prob.get(index) < prob.get(i))
				index = i;

		return index;
	}

	public static int argMax(double[] input)
	{
		int index = 0;

		for(int i = 0; i < input.length; i++)
			if(input[index] < input[i])
				index = i;

		return index;
	}

}
