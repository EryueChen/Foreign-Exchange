package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import config.Params;

public class FileUtils {
	/**
	 * Generate the filename for next level or get the filename of this level
	 * @param level: The current level or the level to generate file for
	 * @param set: The set of features that have been used
	 * @return The filename for left and right children
	 */
	public static String[] getFilename(int level, ArrayList<Integer> set) {
		if (level == 0) {
			String[] files = new String[1];
			files[0] = "train_0";
			return files;
		} else {
			String filename = "train_" + level;
			for (int i = 0; i < set.size(); i++) {
				filename += "_" + set.get(i);
			}
			String[] files = new String[2];
			files[0] = filename + "_1";
			files[1] = filename + "_0";
			return files;
		}
	}
	
	/**
	 * Split the original file into two files which are separated by value of a feature
	 * @param filename: The input filename
	 * @param outputFiles: The filenames of output files
	 * @param minKey: The feature that is split upon
	 */
	public static void splitFiles(String filename, String[] outputFiles, int minKey) {
		PrintWriter out0 = null, out1 = null;
		BufferedReader br = null;
		try {
			out0 = new PrintWriter(new BufferedWriter(new FileWriter(Params.DATA_DIRECTORY + outputFiles[0])));
			out1 = new PrintWriter(new BufferedWriter(new FileWriter(Params.DATA_DIRECTORY + outputFiles[1])));
			br = new BufferedReader(new FileReader(Params.DATA_DIRECTORY + filename));
		    String line;
		    while ((line = br.readLine()) != null) {
		    	String[] features = line.split(",");
		    	if (Integer.parseInt(features[minKey]) == 0) {
		    		out0.write(line + "\n");
		    	} else {
		    		out1.write(line + "\n");
		    	}
		    }
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				out0.close();
				out1.close();
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
