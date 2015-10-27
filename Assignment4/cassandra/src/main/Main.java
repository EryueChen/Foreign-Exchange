package main;

import test.TestRandomForest;

public class Main {
	public static void main(String[] args) {
		double accuracy = TestRandomForest.testRandomForest();
		System.out.println("Total Accuracy: " + accuracy);
	}
}
