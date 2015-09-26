#Calculate the average of original features to generate the threshold
import sys

range = 0
avg = 0
spread = 0
count = 0
for line in sys.stdin:
	features = line.split(",")
	range += float(features[0])
	avg += float(features[1])
	spread += float(features[2])
	count += 1
print range / count, avg / count, spread / count
 
