import sys

last_EUR = None
last_GBP = None
last_AUD = None
last_date = None
current_EUR = None
current_GBP = None 
current_AUD = None
current_lines = []
for line in sys.stdin:
	items = line.split(",")
	date = items[0]
	currency = items[1]
	price = float(items[4])
	if (last_date == date):
		#Parse line to get the currency type
		if (currency.find("GBP") != -1): current_GBP = price
		if (currency.find("EUR") != -1): current_EUR = price
		if (currency.find("AUD") != -1): current_AUD = price
		current_lines.append(line)	
	else:
		#Compare GBP/USD, EUR/USD, AUD/USD to get directionality features
		if (last_date):
			if (current_GBP > last_GBP): 
				label = "1,"
			else: 
				label = "0,"
			if (current_EUR > last_EUR): 
				label += "1,"
			else:
				label += "0,"
			if (current_AUD > last_AUD):
				label += "1"
			else:
				label += "0"
			for current_line in current_lines:
				sys.stdout.write(current_line[0:-2] + label + "," + current_line[-2:])
		last_date = date
		last_GBP = current_GBP
		last_EUR = current_EUR
		last_AUD = current_AUD
		if (currency.find("GBP") != -1): current_GBP = price
		if (currency.find("EUR") != -1): current_EUR = price
		if (currency.find("AUD") != -1): current_AUD = price
		current_lines = []
		current_lines.append(line)
if (current_GBP > last_GBP): 
	label = "1,"
else: 
	label = "0,"
if (current_EUR > last_EUR): 
	label += "1,"
else:
	label += "0,"
if (current_AUD > last_AUD):
	label += "1"
else:
	label += "0"
for current_line in current_lines:
	print current_line
	sys.stdout.write(current_line[0:-2] + label + "," + current_line[-2:])
	

