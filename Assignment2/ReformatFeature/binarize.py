# Change the original features into binary ones
import sys

range_avg = 0.00224555456054
avg_avg = 1.32092311367
spread_avg = 6.04483585036e-05
for line in sys.stdin:
	features = line.split(",")
	if (float(features[0]) < range_avg):
		range_bit = 0
	else: 
		range_bit = 1
	if (float(features[1]) < avg_avg):
		avg_bit = 0
	else:
		avg_bit = 1
	if (float(features[2]) < spread_avg):
		spread_bit = 0
	else:
		spread_bit = 1
	sys.stdout.write(str(range_bit) + "," + str(avg_bit) + "," + str(spread_bit))
	for i in range(3, 7):
		sys.stdout.write("," + features[i])
