import java.util.*;

public class ConnectFourAI
{
	public static void main(String[] args)
	{
		int[] dim = {42, 40, 30, 20, 7};

		NeuralNetwork policy = new NeuralNetwork(dim,
				(x) -> (x > 0) ? x : 0, (x, y) -> (y > 0) ? 1 : 0);
		policy.setOutputActivation((x) -> (1 / (Math.exp(x * -1) + 1)), (x, y) -> x - x * x);

		double[] board = new double[42];

		int volume = 1000;

		double period = 100;

		for(int counter = 0; counter >= 0; counter++)
		{
			double threshold = Math.cos(counter / period * Math.PI) * Math.cos(counter / period * Math.PI);
			
			List<List<double[]>> data = getAmplifyData(policy, board, volume,
					threshold);

			double[] play = data.get(1).get(0);

			while(play(board, argMax(play)) == null)
				play[argMax(play)] = 0;

			board = negative(play(board, argMax(play)));

			if(isWin(board) || isFull(board))
				board = new double[42];

			double[] info = train(policy, data.get(0), data.get(1), 50, 100);

			System.out.printf("%.5f      %.5f      %.5f\n", 100 * info[0], 100 * info[1], threshold);

			policy.saveNetwork("C:\\Users\\Agi\\eclipse-workspace\\Connect Four AI\\src\\Save.txt");
		}
	}

	public static double[] train(NeuralNetwork neuralNet, List<double[]> input, List<double[]> output, int batchSize,
			int epoch)
	{
		double error = 0;
		double certainty = 0;

		double[] data = new double[2];

		for(int counter = 0; counter < epoch; counter++)
		{
			for(int i = 0; i < batchSize; i++)
			{
				int pos = i * input.size() / batchSize + (int) (Math.random() * input.size() / batchSize); // non
																											// coliding
																											// // random
				error += neuralNet.backProp(input.get(pos), normalize(output.get(pos)));
				double[] play = neuralNet.calc(input.get(pos));
				certainty += play[argMax(output.get(pos))];
			}

			neuralNet.updateWeight();
		}

		data[0] = error;
		data[1] = certainty;

		for(int i = 0; i < data.length; i++)
			data[i] /= (batchSize * epoch);
		return data;
	}

	public static List<List<double[]>> getAmplifyData(NeuralNetwork policy, double[] board, int volume,
			double threshold)
	{
		List<BoardPlay> boardPlay = new ArrayList<BoardPlay>();
		List<BoardStor> boardStor = new ArrayList<BoardStor>();
		boardPlay.add(new BoardPlay(board, policy.calc(board)));
		
		int counter = 0;
		
		while(counter < volume && (boardStor.size() > 0 || counter == 0))
		{
			for(int i = 0; i < 7; i++)
			{
				double[] curBoard = play(boardPlay.get(counter).board, i);
				
				if(curBoard == null)
					boardPlay.get(counter).play[i] = 0;
				else if(isWin(curBoard) || isFull(curBoard))
					boardPlay.get(counter).play[i] = 1;
				else 
					add(boardStor, new BoardStor(curBoard, counter, i, boardPlay.get(counter).play[i]));
			}
			
			boardPlay.add(new BoardPlay(negative(boardStor.get(0).board), policy.calc(boardStor.get(0).board)));
			boardPlay.get(boardStor.get(0).ind).refadd(boardStor.get(0).pos, counter);
			
			boardStor.remove(0);
			
			counter++;
		}
		List<List<double[]>> output = new ArrayList<List<double[]>>();
		output.add(new ArrayList<double[]>());
		output.add(new ArrayList<double[]>());

		for(int i = boardPlay.size() - 1; i >= 0; i--)
		{
			if(boardPlay.get(i).ref.size() != 0)
				for(int[] combine: boardPlay.get(i).ref)
					boardPlay.get(i).play[combine[0]] = 1 - maxVal(boardPlay.get(combine[1]).play);
			
			output.get(0).add(boardPlay.get(i).board);
			output.get(1).add(boardPlay.get(i).play);
		}
		
		return output;
	}

	public static int argMax(double[] input)
	{
		int index = 0;

		for(int i = 0; i < input.length; i++)
			if(input[index] < input[i])
				index = i;

		return index;
	}
	
	public static void add(List<BoardStor> list, BoardStor obj)
	{
		int dif = list.size() / 4;
		int index = 2 *dif;
		
		while(dif >= 1)
		{
			index += (list.get(index).prob < obj.prob) ? dif : -1 * dif;
			dif /= 2;
		}
		
		list.add(index, obj);
	}
	
	public static double[] normalize(double[] input)
	{
		boolean has1 = false;

		double[] temp = input.clone();

		for(double value: temp)
			if(value == 1.0)
				has1 = true;

		if(has1)
		{
			for(int i = 0; i < temp.length; i++)
				if(temp[i] != 1.0)
					temp[i] = 0;
			return temp;
		}

		return l2Norm(temp);
	}

	public static double[] negative(double[] input)
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

	public static double[] l2Norm(double[] input)
	{
		double[] output = input.clone();

		for(int i = 0; i < output.length; i++)
			output[i] += Math.random() * 0.0001;

		double l2 = l2(output);

		for(int i = 0; i < output.length; i++)
			output[i] /= l2;

		return output;
	}

	public static double l2(double[] input)
	{
		double sum = 0;

		for(double value: input)
			sum += value * value;

		return Math.sqrt(sum);
	}

	public static double maxVal(double[] input)
	{
		double max = input[0];

		for(double val: input)
			if(max < val)
				max = val;

		return max;
	}
	
	static public class BoardPlay
	{
		double[] board;
		double[] play;
		List<int[]> ref;
		
		public BoardPlay(double[] inBoard, double[] inPlay)
		{
			board = inBoard;
			play = inPlay;
			ref = new ArrayList<int[]>();
		}
		
		public void refadd(int index, int reference)
		{
			int[] combine = {index, reference};
			ref.add(combine);
		}
	}
	
	static public class BoardStor
	{
		double[] board;
		int ind;
		int pos;
		double prob;
		
		public BoardStor(double[] inBoard, int index, int posit, double probab)
		{
			board = inBoard;
			ind = index;
			pos = posit;
			prob = probab;
		}
	}
}