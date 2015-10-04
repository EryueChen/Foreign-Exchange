1 Generate the random forest

1) Generate random sets of features and build trees with these features.

2) For each line of test, correlate the results of all the trees and compare with actual result.

3) Calculate the accuracy.

2 Project Structure

To make the project in a better coding style, I reorganized the classes and functions.

1) package config: Params.java, including all the configurable parameters for the random forest.

2) package decisiontree: BuildTree.java, the main function used to build a decision tree.

3) package main: Main.java, the main function of the project, used to call test Random Forest.

4) package struct: TreeNode.java, the class for TreeNode data structure.

5) package test: 
    TestDecisionTree.java, used to test Decision Tree and calculate accuracy;
    TestRandomForest.java, used to test Random Forest and calculate accuracy.

6) pacakge utils:
    FileUtils.java, used to generate/get the file name, split the files for output;
    PrintTree.java, used to print the Decision Tree in level order;
    RandomUtils.java, used to random pick a set of features.

3 Experiment

The training dataset has 30374 lines of data.

The test dataset has 7593 lines of data.

There are 3 features used among all 6 features and 5 decision trees are used.

The accuracy result is 54.93%

The sample output is as below: (First print each feature set and tree, and then the accuracy.)

0 1 4 
4 
0 0 
1 1 1 1 
0 0 0 0 1 1 1 1

0 1 5 
5 
0 0 
1 1 1 1 
0 0 0 0 1 0 1 1 

0 2 4 
4 
0 0 
2 2 2 2 
0 0 0 0 1 1 1 1 

2 3 5 
3 
5 5 
2 2 2 2 
0 0 0 0 1 1 1 1 

1 2 5 
5 
1 2 
2 2 1 1 
0 0 0 0 1 1 1 1 
Acurracy of Tree 0: 0.54721457
Acurracy of Tree 1: 0.5331226
Acurracy of Tree 2: 0.54721457
Acurracy of Tree 3: 0.53391284
Acurracy of Tree 4: 0.53509814
Total Accuracy: 0.5493217706680298
