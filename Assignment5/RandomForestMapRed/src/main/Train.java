package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.NLineInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

import config.Params;
import decisiontree.BuildTree;
import struct.TreeNode;
import utils.TreeUtils;
import utils.RandomUtils;


public class Train {
	/* Mapper: read from train data and build tree in each mapper */
	public static class Map extends Mapper<LongWritable, Text, LongWritable, Text> {
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			ArrayList<Integer> set = new ArrayList<Integer>();
			ArrayList<Integer> feature_set = RandomUtils.randomSet();
			TreeNode root = BuildTree.genTree(0, set, feature_set, value.toString());
			String treestring = TreeUtils.serialize(root);
			context.write(key, new Text(treestring));
		}

	}

	/* Reducer: Get tree string from mapper output, build forest and output in JSON */
	public static class Reduce extends Reducer<Text, Text, Text, Text> {
		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			String result = "";
			for (Text value : values) {
				result += value.toString() + "\n";
			}
			context.write(key, new Text(result));
		}
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();

		Job job = new Job(conf, "randomforest");
		job.setJarByClass(Train.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		job.setMapperClass(Map.class);
		job.setReducerClass(Reduce.class);

		int count = readTrainData();
		job.setInputFormatClass(NLineInputFormat.class);
		NLineInputFormat.addInputPath(job, new Path("/train_0"));
		job.getConfiguration().setInt("mapreduce.input.lineinputformat.linespermap", count / Integer.parseInt(args[0]));
		job.setOutputFormatClass(TextOutputFormat.class);
		FileOutputFormat.setOutputPath(job, new Path("/trees"));

		job.waitForCompletion(true);
	}

	public static int readTrainData() {
		Cluster cluster = Cluster.builder().addContactPoint("127.0.0.1").build();
		Session session = cluster.connect("financial");
		ResultSet results = session.execute("SELECT * FROM train_0");
		PrintWriter out = null;
		int count = 0;
		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter("/train_0")));
		    for (Row row : results) {
		    	for (int i = 0; i <= Params.FEATURE_TOTAL; i++) {
		        	out.write(row.getInt(Params.FEATURE_NAME[i]));
		        	if (i != Params.FEATURE_TOTAL) out.write(",");
		        }
		    	out.write("\n");
		    	count++;
		    }
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			out.close();
			cluster.close();
			session.close();
		}
		return count;
	}

}
