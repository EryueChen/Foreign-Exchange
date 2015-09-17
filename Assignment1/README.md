1. Run the code
python prepdata.py $FXDATA_DIRECTORY > output
sort output -t ',' -k 1 > sorted_output
cat sorted_output | python merge_currency.py > featured_data

2. Label
The label is the directionality of price in next hour. 1 represents increase, 0 represents decrease

3. Feature description
1) Timestamp: The timestamp based on hour
2) Currency: The kind of currency, all of them are exchanged with USD
3) Max Price: The highest price during one hour period of the mid price. The mid price is the average of bid and ask price, which can approximately represent transaction price. 
4) Min Price: The lowest price during one hour period of the mid price.
5) Average Price: The average price during one hour period of the mid price.
6) Average Spread: The average during one hour period of the spread. The spread is (ask - bid) / bid, which represents the ask-bid difference.
7) GBP/USD Directionality: The directionality of GBP/USD comparing to last hour. 1 represents increase, 0 represents decrease
8) EUR/USD Directionality: The directionality of EUR/USD comparing to last hour. 1 represents increase, 0 represents decrease
9) AUD/USD Directionality: The directionality of AUD/USD comparing to last hour. 1 represents increase, 0 represents decrease

