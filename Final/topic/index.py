__author__ = 'eryue'

import csv
import os
import fnmatch

with open('data/sessions.csv', 'wb') as csvfile:
    spamwriter = csv.writer(csvfile, delimiter=',', quotechar='|', quoting=csv.QUOTE_MINIMAL)
    id = 0
    spamwriter.writerow(['docId', 'docName'])
    for root, dir, files in os.walk('data/sessions/'):
        for items in fnmatch.filter(files, "*.nxml"):
            filename = root + "/" + items
            spamwriter.writerow([id, filename])
            id += 1