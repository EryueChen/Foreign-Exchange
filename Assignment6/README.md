Source file:

simpleApp/src/main/scala/SimpleApp.scala

It reads from Cassandra for training and test data and reformat it into RDD[LabeledPoint]. 

Then it ran the Random Forest using MLLIB.

It writes the accuracy back to Cassandra.

The output:

I used 10 trees and the output table in Cassandra is shown as below:

id                       | accuracy

--------------------------+----------

 2015-11-16 13:14:22-0500 |  0.54721

 2015-11-16 13:12:36-0500 |  0.54695

 2015-11-16 13:13:59-0500 |  0.54721

 2015-11-16 13:13:44-0500 |  0.54721

 2015-11-16 13:14:44-0500 |  0.54721

 2015-11-16 13:11:21-0500 |  0.54274

 2015-11-16 13:13:10-0500 |  0.54721

 2015-11-16 13:13:26-0500 |  0.54721

 2015-11-16 11:27:20-0500 |  0.54682

 2015-11-16 13:12:51-0500 |  0.54721


Reference:

https://spark.apache.org/docs/1.2.0/mllib-ensembles.html#random-forests

http://spark.apache.org/docs/latest/quick-start.html

http://spark.apache.org/docs/latest/mllib-data-types.html
