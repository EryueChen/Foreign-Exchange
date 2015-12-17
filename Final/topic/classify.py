__author__ = 'eryue'
import csv
import nltk
import json
import operator
import numpy as np

feature_names = []
topic_word = []

with open("data/model.csv", "r") as modelfile:
    reader = csv.reader(modelfile, delimiter = ",")
    feature_names = [x for x in reader.next()]
    for row in reader:
        word = []
        for x in row:
            word.append(float(x))
        topic_word.append(word)

jsondata = {}
jsondata["name"] = "flare"
jsondata["children"] = []

n_top_words = 20
lemmatizer = nltk.WordNetLemmatizer()
while (True):
    term = raw_input("Input term: ")
    if (term == ""): break

    termjson = {}
    termjson["name"] = term
    termjson["children"] = []

    terms = term.split(" ")
    scores = {}
    for i in range(0, 10):
        scores[i] = 0
    for t in terms:
        t = lemmatizer.lemmatize(t)
        for i in range(0, len(feature_names)):
            if (t == feature_names[i]):
                for j in range (0, len(topic_word)):
                    scores[j] = scores[j] + topic_word[j][i]
    print scores
    sort_scores = sorted(scores.iteritems(), key=operator.itemgetter(1), reverse=True)
    count = 0
    for (k, v) in sort_scores:
        print v
        topic_words = np.array(feature_names)[np.argsort(topic_word[k])][:-n_top_words:-1]
        print('Topic {}: {}'.format(k, ' '.join(topic_words).encode('utf-8')))
        topicjson = {}
        topicjson["name"] = "Topic" + str(k)
        topicjson["size"] = v
        topicjson["children"] = []
        sum = 0
        for t in topic_words:
            tjson = {}
            tjson["name"] = t
            for i, word in enumerate(feature_names):
                if (t == word):
                    tjson["size"] = topic_word[k][i]
                    sum += topic_word[k][i]
            topicjson["children"].append(tjson)
        for topic in topicjson["children"]:
            topic["size"] = (topic["size"] / sum) * v
        termjson["children"].append(topicjson)
        count += 1
        if (count >= 5): break
    jsondata["children"].append(termjson)

with open("data/flare.json", "w") as jsonfile:
    jsonfile.write(json.dumps(jsondata))
