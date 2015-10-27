package utils;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

import config.Params;

public class CassandraUtils {

	/**
	 * Split the original table into two tables which are separated by value of a feature
	 * @param filename: The input tablename
	 * @param outputFiles: The tablenames of output files
	 * @param minKey: The feature that is split upon
	 */
	public static void splitFiles(String filename, String[] outputFiles, int minKey) {
		Cluster cluster;
		Session session;
		cluster = Cluster.builder().addContactPoint("127.0.0.1").build();
		session = cluster.connect("financial");
		//Create new tables to store intermediate results
		session.execute("CREATE TABLE IF NOT EXISTS " + outputFiles[0] + " (id int primary key, range int, average int, spread int, gbp int, eur int, aud int, label int)");
		session.execute("CREATE TABLE IF NOT EXISTS " + outputFiles[1] + " (id int primary key, range int, average int, spread int, gbp int, eur int, aud int, label int)");
		session.execute("truncate " + outputFiles[0]);
		session.execute("truncate " + outputFiles[1]);
		ResultSet results = session.execute("select * from " + filename);
		for (Row row : results) {
			String params = String.valueOf(row.getInt("id"));
			String columns = "id";
			for (int i = 0; i <= Params.FEATURE_TOTAL; i++) {
				params += "," + row.getInt(Params.FEATURE_NAME[i]);
				columns += "," + Params.FEATURE_NAME[i];
			}
		    if (row.getInt(Params.FEATURE_NAME[minKey]) == 0) {
		    	session.execute("INSERT INTO " + outputFiles[0] + " (" + columns + ") VALUES (" + params + ")");
		   	} else {
		   		session.execute("INSERT INTO " + outputFiles[1] + " (" + columns + ") VALUES (" + params + ")");
		    }
		}
		session.close();
		cluster.close();
	}
	

}
