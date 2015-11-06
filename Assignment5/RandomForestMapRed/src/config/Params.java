package config;

public class Params {
	//The directory of train and test files
	public static String DATA_DIRECTORY = "/Users/eryue/Documents/11-676/assignment3/";
	public static int FEATURE_TOTAL = 6;	//The number of features in total
	public static int FEATURE_NUM = 3;		//The number of features used to build tree
	public static String[] FEATURE_NAME= {"range", "average", "spread", "gbp", "eur", "aud", "label"};
}
