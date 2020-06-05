import java.util.*;

public class ConnectFourAI
{
	public static void main(String[] args)
	{
		int[] dim = {42, 40, 30, 20, 7};

		NeuralNetwork policy = new NeuralNetwork(dim, (x) -> (x > 0) ? x : 0, (x, y) -> (y > 0) ? 1 : 0);
		policy.setOutputActivation((x) -> (1 / (Math.exp(x * -1) + 1)), (x, y) -> x - x * x);

		List<double[]> boardSet = new ArrayList<double[]>();
		List<double[]> playSet = new ArrayList<double[]>();
		
		double threshold = 0;

		for(int counter = 0; counter >= 0; counter++)
		{
			List<List<double[]>> data = getAmplifyData(policy, 0, threshold, (x) -> randomChoose(x));
		
			for(int i = data.get(1).size() - 1; i >= 0; i--)
			{
				boolean has1 = false;
				for(double value: data.get(1).get(i))
					if(value == 1.0)
						has1 = true;
				if(has1)
				{
					boardSet.add(data.get(0).remove(i));
					playSet.add(data.get(1).remove(i));
				}
			}

			while(boardSet.size() > data.get(1).size() * 3)
			{
				boardSet.remove(0);
				playSet.remove(0);
			}
			
			int size = data.get(0).size();
			
			//if(size < 9000)
				//threshold -= 0.001;
			
			//if(size > 15000 )
				//threshold += 0.01;
			
			
 
			double[] nonCert = train(policy, data.get(0), data.get(1), 500, 25, (x) -> randomChoose(x));

			double[] fullCert = train(policy, boardSet, playSet, 500, 25, (x) -> randomChoose(x));

			policy.saveNetwork("C:\\Users\\Agi\\eclipse-workspace\\Connect Four AI\\src\\Save.txt");
			
			System.out.printf("ER:  %.5f  CR:  %.5f  l2:  %.2f  SZ:  %d  ", 
					100 * nonCert[7], 
					100 * nonCert[8],
					nonCert[9],
					data.get(0).size());

			System.out.printf("ER:  %.5f  CR:  %.5f  SZ:  %d  ", 
					100 * fullCert[7], 
					100 * fullCert[8], 
					boardSet.size());

			double sum = 0;

			for(int i = 0; i < 7; i++)
				sum += fullCert[i] * fullCert[i];

			for(int i = 0; i < 7; i++)
				System.out.printf("%.3f  ", fullCert[i] / Math.sqrt(sum));

			System.out.printf("%.3f  \n", threshold);
		}
	}

	public static double[] train(NeuralNetwork neuralNet, List<double[]> input, List<double[]> output, int batchSize,
			int epoch, Selecter s)
	{
		double error = 0;
		double certainty = 0;

		double[] data = new double[10];

		for(int counter = 0; counter < epoch; counter++)
		{
			for(int i = 0; i < batchSize; i++)
			{
				int pos = i * input.size() / batchSize + (int) (Math.random() * input.size() / batchSize); // non
																											// coliding
																											// random
				error += neuralNet.backProp(input.get(pos), l2Norm(output.get(pos)));
				double[] play = neuralNet.calc(input.get(pos));
				certainty += play[s.choose(l2Norm(output.get(pos)))];

				data[s.choose(l2Norm(output.get(pos)))]++;
				
				data[9] += l2(play);
			}

			neuralNet.updateWeight();
		}

		data[7] = error;
		data[8] = certainty;

		for(int i = 0; i < data.length; i++)
			data[i] /= (batchSize * epoch);
		return data;
	}

	public static List<List<double[]>> getAmplifyData(NeuralNetwork policy, int depth, double threshold, Selecter s)
	{
		ArrayList<List<double[]>> output = new ArrayList<List<double[]>>();

		output.add(new ArrayList<double[]>());
		output.add(new ArrayList<double[]>());

		for(int pos = 0; pos < 7; pos++)
		{
			double[] board = getNegative(play(new double[42], pos));
			double[] play = normalize(getAmpPlay(policy, output, board, depth, threshold));
			output.get(0).add(board);
			output.get(1).add(play);

			while(!isWin(board) && !isFull(board))
			{
				double[] curPlay = l2Norm(play);

				while(play(board, s.choose(curPlay)) == null)
					curPlay[s.choose(curPlay)] = 0;

				board = getNegative(play(board, s.choose(curPlay)));
				play = normalize(getAmpPlay(policy, output, board, depth, threshold));
				output.get(0).add(board);
				output.get(1).add(play);
			}

			output.get(0).remove(output.get(0).size() - 1);
			output.get(1).remove(output.get(1).size() - 1);
		}

		return output;
	}

	public static double[] getAmpPlay(NeuralNetwork policy, List<List<double[]>> store, double[] board, int depth,
			double threshold)
	{

		double[] play = policy.calc(board);

		for(int i = 0; i < play.length; i++)
		{
			if(play(board, i) != null && isWin(play(board, i)))
				play[i] = 1;
			else if(play(board, i) != null && depth != 0 && play[i] > threshold)
			{
				double[] curChoice = getAmpPlay(policy, store, getNegative(play(board, i)), depth - 1, threshold);

				double max = curChoice[0];

				for(double value: curChoice)
					max = Math.max(max, value);

				play[i] = (1 - max);
			}
		}

		store.get(0).add(board);
		store.get(1).add(normalize(play));
		return play;
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
		}

		return temp;
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

	public static int randomChoose(double[] input)
	{
		double prod = 1;
		double sum = 0;

		for(double value: input)
		{
			prod *= Math.PI + value;
			sum += value;
		}

		prod -= Math.floor(prod);

		double pos = sum * prod;

		sum = 0;

		for(int i = 0; i < input.length; i++)
		{
			sum += input[i];

			if(sum > pos)
				return i;
		}

		return -1;
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

	interface Selecter
	{
		int choose(double[] val);
	}
}