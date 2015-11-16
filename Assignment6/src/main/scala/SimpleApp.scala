import org.apache.spark.mllib.tree.RandomForest
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import com.datastax.spark.connector._
import java.util.Calendar

object SimpleApp {
    def main(args: Array[String]) {
        val conf = new SparkConf().setAppName("Simple Application")
        conf.set("spark.cassandra.connection.host", "localhost") 
        val sc = new SparkContext(conf)
        // Read training and test data from Cassandra
        val train = sc.cassandraTable("financial", "train_0")
        val trainingData : RDD[LabeledPoint] = train.map { row  => { 
            (LabeledPoint(row.getInt("label").toDouble, Vectors.sparse(6, Array(0, 1, 2, 3, 4, 5), Array(row.getInt("average"), row.getInt("spread"), row.getInt("range"), row.getInt("aud"), row.getInt("eur"), row.getInt("gbp")))))
        }}
        val test = sc.cassandraTable("financial", "test")
        val testData : RDD[LabeledPoint] = test.map { row  => {
            (LabeledPoint(row.getInt("label").toDouble, Vectors.sparse(6, Array(0, 1, 2, 3, 4, 5), Array(row.getInt("average"), row.getInt("spread"), row.getInt("range"), row.getInt("aud"), row.getInt("eur"), row.getInt("gbp")))))
        }}

        // Train a RandomForest model.
        //  Empty categoricalFeaturesInfo indicates all features are continuous.
        val numClasses = 2
        val categoricalFeaturesInfo = Map[Int, Int]()
        val numTrees = 10
        val featureSubsetStrategy = "auto"
        val impurity = "gini"
        val maxDepth = 4
        val maxBins = 32

        val model = RandomForest.trainClassifier(trainingData, numClasses, categoricalFeaturesInfo, numTrees, featureSubsetStrategy, impurity, maxDepth, maxBins)

        // Evaluate model on test instances and compute test error
        val labelAndPreds = testData.map { point =>
            val prediction = model.predict(point.features)
            (point.label, prediction)
        }
        val testErr = labelAndPreds.filter(r => r._1 != r._2).count.toDouble / testData.count()
        println("Test Error = " + testErr)
        //Write the accuracy output to Cassandra
        val timestamp = Calendar.getInstance().getTime()
        val out = sc.parallelize(Seq((timestamp, 1.0 - testErr)))
        out.saveToCassandra("financial", "output", SomeColumns("id", "accuracy"))
        println("Learned classification forest model:\n" + model.toDebugString)
    }
}
