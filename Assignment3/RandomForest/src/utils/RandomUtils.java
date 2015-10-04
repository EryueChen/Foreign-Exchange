package utils;

import java.util.ArrayList;
import java.util.Random;

import config.Params;

public class RandomUtils {
	/**
	 * Generates all possible combination of features and return a random one.
	 * @return A set of random chosen features
	 */
	public static ArrayList<Integer> randomSet() {
		ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();
		for (int i = 0; i < Params.FEATURE_TOTAL; i++) {
			ArrayList<Integer> r = new ArrayList<Integer>();
			r.add(i);
			result.add(r);
		}
		int level = 1;
		
		//Create all the possible combination of features
		while (level < Params.FEATURE_NUM) {
			ArrayList<ArrayList<Integer>> tmpresult = new ArrayList<ArrayList<Integer>>();
			for (int i = 0; i < result.size(); i++) {
				ArrayList<Integer> r = result.get(i);
				int last = r.get(r.size() - 1);
				for (int j = last + 1; j < Params.FEATURE_TOTAL; j++) {
					ArrayList<Integer> tmp = new ArrayList<Integer>();
					tmp.addAll(r);
					tmp.add(j);
					tmpresult.add(tmp);
				}
			}
			result.clear();
			result.addAll(tmpresult);
			level++;
		}
		//Random a number and return the corresponding tree
		Random random = new Random();
		int i = random.nextInt(result.size());
		return result.get(i);
	}
}
