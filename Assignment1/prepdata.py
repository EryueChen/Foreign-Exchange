import os
import sys
import fnmatch
import zipfile

current_date = None
current_max = 0
current_min = sys.float_info.max
current_sum = 0
current_count = 0
current_spread = 0
last_line = None
last_avg = 0

for root, dir, files in os.walk(sys.argv[1]):
	#Traverse the dataset directory
	for items in fnmatch.filter(files, "*USD-*.zip"):
		zfile = zipfile.ZipFile(root + "/" + items)
		for finfo in zfile.infolist():
			#Read lines from zipfile
			file = zfile.open(finfo)
			for line in file:
				#Parse lines and calculate the prices 
				currency, timestamp, bid, ask = line.split(",")
				date = timestamp.split(":")[0]
				mid = (float(bid) + float(ask)) / 2
				spread = (float(ask) - float(bid)) / float(bid)
				if (current_date == date):
					if (mid > current_max): current_max = mid
					if (mid < current_min): current_min = mid
					current_sum += mid
					current_spread += spread
					current_count += 1
				else:
					#If the timestamp is different from previous one, output the previous one
					if (current_date):
						if (last_line):
							if (current_sum / current_count > last_avg):
								label = 1
							else:
								label = 0
							sys.stdout.write(last_line + "," + str(label) + "\n")
						last_avg = current_sum / current_count
						last_line = current_date + "," + currency + "," + str(current_max) + "," + str(current_min) + "," + str(last_avg) + "," + str(current_spread / current_count)
					current_date = date
					current_max = mid
					current_min = mid
					current_sum = mid
					current_count = 1
					current_spread = spread
			current_date = None
		if (current_date == date):
			if (last_line):
				if (current_sum / current_count > last_avg):
					label = 1
				else:
					label = 0
				sys.stdout.write(last_line + "," + str(label) + "\n")