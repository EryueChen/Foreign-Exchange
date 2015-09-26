1. Reformat the dataset to fit decision tree model

1) Label

The label is the directionality of EUR/USD price in next hour. 1 represents increase, 0 represents decrease

2) Feature description

Originally, there are features such as timestamp, currency type and some values that are not binary. To fit the decision tree model in this assignment, I made some changes to the features. I discarded timestamp and currency type and use only data for EUR/USD to train and test. For the non-binary values, I used average as a threshold to convert data into binary format. Also, the max, min and average price have similar general trends when comparing to average for whole dataset. Therefore, only average price feature is kept as a reprensentive.

The updated features are listed as follow:

1) Max-Min Price Range: The difference of highest price and lowest price during one hour period of the mid price. The mid price is the average of bid and ask price, which can approximately represent transaction price. If it is higher than average range, it has value 1. Otherwise, it has value 0.

2) Average Price: The average price during one hour period of the mid price. If it is higher than average price for entire dataset, it has value 1. Otherwise, it has value 0.

3) Average Spread: The average during one hour period of the spread. The spread is (ask - bid) / bid, which represents the ask-bid difference. If it is higher than average spread for entire dataset, it has value 1. Otherwise, it has value 0.

4) GBP/USD Directionality: The directionality of GBP/USD comparing to last hour. 1 represents increase, 0 represents decrease

5) EUR/USD Directionality: The directionality of EUR/USD comparing to last hour. 1 represents increase, 0 represents decrease

6) AUD/USD Directionality: The directionality of AUD/USD comparing to last hour. 1 represents increase, 0 represents decrease


2. Generate the decision tree

1) Use a recursive DFS method to generate the decision tree. 

2) Use the decision tree to test the correctness. 


3. Experiment
The training dataset has 30374 lines of data.

The test dataset has 7593 lines of data.

The accuracy result is 55.05%

The sample output is as below (Number represents the index of feature):

4 

0 0

1 3 2 3 

2 2 2 5 3 1 5 5 

5 5 5 5 1 5 1 1 5 5 3 3 1 2 2 1 

3 3 3 3 3 3 3 3 5 5 1 1 2 2 2 2 1 1 1 1 5 5 5 5 2 2 1 1 1 1 2 2 

Correctness: 0.5505070686340332

