package ai.project.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import ai.project.common.GuessEvaluator;
import ai.project.player.MasterMindPlayer;

public class MasterMindGame {

	private static final String IGNORE_SYMBOLS = "(\r\n|[\n\r\u2028\u2029\u0085])?";
	private static final int ALLOWED_STEPS = 20;

	public static void main(String[] args) {
		/** TODO Check if we need console read/ file read for inputs. **/
		System.out.println("Enter number of colors and the length:");
		Scanner s = new Scanner(System.in);
		int colors = Integer.parseInt(s.nextLine());
		s.skip(IGNORE_SYMBOLS);
		int codeLen = Integer.parseInt(s.nextLine());
		s.close();
		start(colors, codeLen);
	}

	private static void start(int colors, int codeLen) {
		int[] secret = SecretCodeGenerator.generate(codeLen, colors);
		GuessEvaluator evaluator = new GuessEvaluator(secret);
		List<Integer> eliminateColors = null;
		if (colors >= 10) {
			eliminateColors = new ArrayList<>();
			int[] hint = new int[2];
			int[] guess = new int[codeLen];
			//TODO Finalize Strategy
			for (int i = 0; i < 10; i += 2) {
				// evaluate hint for 01, 23, 45, 67, 89
				for (int j = 0; j < codeLen; j++) {
					guess[j] = j >= (codeLen / 2) ? i : i + 1;
				}
				hint = evaluator.evaluate(guess);
				if (hint[0] == 0 && hint[1] == 0) {
					eliminateColors.add(i);
					eliminateColors.add(i+1);
				}
			}
		}
		MasterMindPlayer player = new MasterMindPlayer(codeLen, colors, eliminateColors);

		int[] hints = new int[2];

		/** Setting default empty hints **/
		Arrays.fill(hints, 0);
		int stepCount = 0;
		while (!evaluator.isFoundAnswer() && evaluator.getStepCount() < ALLOWED_STEPS) {
			stepCount++;
			int[] guess = player.guess(hints);
			System.out.println("Step " + stepCount + ": " + Arrays.toString(guess));
			hints = evaluator.evaluate(guess);
			System.out.println("Hint " + stepCount + ":" + Arrays.toString(hints));
		}

		if (!evaluator.isFoundAnswer()) {
			System.out.println("Oops, the secret is: " + Arrays.toString(secret));
			System.out.println("Total time taken: " + player.getTotalTime());
		} else {
			System.out.println("You win!! With just " + evaluator.getStepCount() + " guesses");
			System.out.println("You are right, the secret was: " + Arrays.toString(secret));
			System.out.println("Total time taken: " + player.getTotalTime());
		}

	}
}
