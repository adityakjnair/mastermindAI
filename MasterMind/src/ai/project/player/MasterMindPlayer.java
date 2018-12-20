package ai.project.player;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import ai.project.common.GuessEvaluator;

/**
 * @author shamalip, adityan, prernas The AI player implementation.
 */
public class MasterMindPlayer {
	private int colors;
	private int codeLen;
	private Set<int[]> allGuesses;
	private List<int[]> possibleHints;
	private Map<String, Integer> hintMatrix;
	private int[] lastGuess = null;
	private long generateGuessTime = 0;
	private long filterTime = 0;
	private long guessingTime = 0;
	private long totalTime;

	public long getTotalTime() {
		return generateGuessTime + filterTime + guessingTime + totalTime;
	}

	public MasterMindPlayer(int codeLen, int colors, List<Integer> eliminateColors) {
		this.codeLen = codeLen;
		this.colors = colors;
		if(null != eliminateColors) {
			generateEliminatedGuesses(eliminateColors);
		} else {
			/** generate the set of all guesses for given colors & codeLen **/
			generateAllGuesses();		
		}
		generateAllPossibleHints();
	}

	private void generateEliminatedGuesses(List<Integer> eliminateColors) {
		int[] colorArr = new int[colors-eliminateColors.size()];
		for(int i =0 ; i < colors; i++) {
			if(eliminateColors.contains(i)) 
				break;
			colorArr[i] = i;
		}
		
	}

	/**
	 * Generate an list of all possible hints so as to refer them in min-max
	 * algorithm.
	 *
	 **/
	private void generateAllPossibleHints() {
//		possibleHints = new ArrayList<>();
		hintMatrix = new HashMap<String, Integer>();
		for (int i = 0; i <= codeLen; i++) {
			for (int j = 0; j <= codeLen; j++) {
				if (!(j == codeLen - 1 && i == 1) && i + j <= codeLen) {
//					int[] hintItem = new int[2];
//					hintItem[0] = i;
//					hintItem[1] = j;
//					possibleHints.add(hintItem);
					hintMatrix.put(Integer.toString(i)+Integer.toString(j), 0);
				}
			}
		}
	}

	/***
	 * Generates all possible guess that can later be eliminated.
	 *
	 **/
	private void generateAllGuesses() {
		long startTime = System.currentTimeMillis();
		allGuesses = new HashSet<>();
		String fileName = colors + "x" + codeLen + ".txt";
		try {
			guessGenerator();
			// readOrWriteGuesses(fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("****Number of total Possibilities******   " + allGuesses.size());
		long endTime = System.currentTimeMillis();
		generateGuessTime = endTime - startTime;
		System.out.println("Generated All Guesses in: " + generateGuessTime + " ms");
	}

	private void guessGenerator() {
		int[] guess = new int[codeLen];
		for (int i = 0; i < Math.pow(colors, codeLen); ++i) {

			int[] copyOfGuess = new int[guess.length];
			System.arraycopy(guess, 0, copyOfGuess, 0, guess.length);

			/** adding the copy of the current guess **/
			allGuesses.add(copyOfGuess);

			/** increment the last element by 1 **/
			++guess[guess.length - 1];

			/** iterate over each "column" from right to left **/
			for (int j = codeLen - 1; j > 0; --j) {
				/***
				 * if we exceed our numeric base set it back to zero and increment the column to
				 * the left
				 **/
				if (guess[j] == colors) {
					guess[j] = 0;
					++guess[j - 1];
				}
			}
		}
	}

	private void readOrWriteGuesses(String fileName) throws IOException {
		int[] guess = new int[codeLen];
		File file = new File(fileName);
		if (file.exists() && !file.isDirectory()) {
			System.out.println("Reading from File");
			Scanner scanner = new Scanner(file);
			int[] guessFromFile = new int[codeLen];
			while (scanner.hasNextLine()) {
				String[] guessString = scanner.nextLine().split(",");
				for (int i = 0; i < guessString.length; i++) {
					guessFromFile[i] = Integer.parseInt(guessString[i]);
				}
				allGuesses.add(guessFromFile);
			}
			scanner.close();
		} else {
			FileWriter fileWriter = new FileWriter(fileName);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			System.out.println("Writing into File");
			/** iterate colors^codeLen times **/
			for (int i = 0; i < Math.pow(colors, codeLen); ++i) {

				int[] copyOfGuess = new int[guess.length];
				System.arraycopy(guess, 0, copyOfGuess, 0, guess.length);

				/** adding the copy of the current guess **/
				allGuesses.add(copyOfGuess);
				String currentGuess = IntStream.of(copyOfGuess).mapToObj(Integer::toString)
						.collect(Collectors.joining(","));
				bufferedWriter.write(currentGuess);
				bufferedWriter.newLine();

				/** increment the last element by 1 **/
				++guess[guess.length - 1];

				/** iterate over each "column" from right to left **/
				for (int j = codeLen - 1; j > 0; --j) {
					/***
					 * if we exceed our numeric base set it back to zero and increment the column to
					 * the left
					 **/
					if (guess[j] == colors) {
						guess[j] = 0;
						++guess[j - 1];
					}
				}
			}
			bufferedWriter.close();
		}
	}

	/**
	 * returns the next guess based on the hint provided.
	 * 
	 * @param hint
	 * @return
	 */
	public int[] guess(int[] hint) {
		if (null != lastGuess) {
			filterGuessesBasedOnHint(hint);
			lastGuess = applyMinMax();
		} else {
			lastGuess = new int[codeLen];
			for (int i = 0; i < codeLen; i++) {
				lastGuess[i] = i >= (codeLen / 2) ? 0 : 1;
			}
		}
		return lastGuess;
	}

	/**
	 * Returns the possible guesses that are filtered by provided hint.
	 * 
	 * @param hint
	 * @return
	 */
	private void filterGuessesBasedOnHint(int[] hint) {
		long startTime = System.currentTimeMillis();
		long initSize = allGuesses.size();
		Iterator<int[]> iterator = allGuesses.iterator();
		while (iterator.hasNext()) {
			int[] possibleGuess = iterator.next();
			if (GuessEvaluator.getColorCorrectPositionIncorrect(possibleGuess, lastGuess) != hint[0]
					|| GuessEvaluator.getColorCorrectPositionCorrect(possibleGuess, lastGuess) != hint[1]) {
				iterator.remove();
			}
		}
		long endTime = System.currentTimeMillis();
		long finalSize = allGuesses.size();
		filterTime += endTime - startTime;
		System.out.println(
				"Reduced guesses from " + initSize + " to " + finalSize + " in: " + (endTime - startTime) + "ms");
	}

	/**
	 * The method returns a guess value by applying minimax algorithm in such a way
	 * that next time the hint will be able to provide more information (narrows
	 * possibilities for next step).
	 * 
	 * @param allGuesses
	 * @return
	 */
	private int[] applyMinMax() {
		long startTime = System.currentTimeMillis();
		int min = Integer.MAX_VALUE;
		int cutOff = Integer.MAX_VALUE;
		int[] minimizedGuess = new int[codeLen];
		System.out.println("****Number of Possibilities******   " + allGuesses.size());
		int[] hint = new int[2];
		for (int[] guess : allGuesses) {
			int max = 0;
			for (int[] solution : allGuesses) {
				hint = GuessEvaluator.getHint(guess, solution);
				hintMatrix.put(Integer.toString(hint[0])+Integer.toString(hint[0]), hintMatrix.getOrDefault(hint, 0) + 1);
			}
			max = maxHintOccurence(hintMatrix);
			if (max < min) {
				System.out.println("Count:" + max);
				min = max;
				minimizedGuess = guess;
				if (min / allGuesses.size() < 0.20) {
					break;
				}
			}
		}
		long endTime = System.currentTimeMillis();
		guessingTime += endTime - startTime;
		System.out.println("Made a guess in " + (endTime - startTime) + "ms\n");
		return minimizedGuess;
	}

	public <K, V extends Comparable<V>> V maxHintOccurence(Map<K, V> map) {
		Entry<K, V> maxEntry = Collections.max(map.entrySet(),
				(Entry<K, V> e1, Entry<K, V> e2) -> e1.getValue().compareTo(e2.getValue()));
		return maxEntry.getValue();
	}
}
