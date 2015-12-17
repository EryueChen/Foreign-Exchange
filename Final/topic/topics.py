import csv

from sklearn.feature_extraction.text import TfidfVectorizer, CountVectorizer
from bs4 import BeautifulSoup, NavigableString
from soupselect import select
import nltk
from nltk.corpus import stopwords
from nltk.tokenize import wordpunct_tokenize

sessions = {}
lemmatizer = nltk.WordNetLemmatizer()
stopwords = stopwords.words('english')
with open("data/sessions.csv", "r") as sessions_file:
    reader = csv.reader(sessions_file, delimiter = ",")
    reader.next() # header
    for row in reader:
        session_id = int(row[0])
        filename = row[1]
        page = open(filename).read()
        soup = BeautifulSoup(page)
        bodies = select(soup, "body p")
        content = ""
        for body in bodies:
            for word in wordpunct_tokenize(body.text):
                if word not in stopwords:
                    lemmed = lemmatizer.lemmatize(word).lower()
                    if (len(lemmed) < 3 or (not lemmed.isalpha())): continue
                    content += lemmed + " "
        sessions[session_id] = {"abstract" : content, "title": filename}

corpus = []
titles = []
for id, session in sorted(sessions.iteritems(), key=lambda t: int(t[0])):
    corpus.append(session["abstract"])
    titles.append(session["title"])

#vectorizer = TfidfVectorizer(analyzer='word', ngram_range=(1,1), min_df = 0, stop_words = 'english')
vectorizer = CountVectorizer(analyzer='word', ngram_range=(1,1), min_df = 0, stop_words = 'english')
matrix =  vectorizer.fit_transform(corpus)
feature_names = vectorizer.get_feature_names()

import lda
import numpy as np

vocab = feature_names

model = lda.LDA(n_topics=10, n_iter=500, random_state=1)
model.fit(matrix)
topic_word = model.topic_word_
n_top_words = 20

with open('data/model.csv', 'wb') as csvfile:
    spamwriter = csv.writer(csvfile, delimiter=',', quotechar='|', quoting=csv.QUOTE_MINIMAL)
    spamwriter.writerow([x.encode('utf-8') for x in feature_names])
    for y in range(len(model.topic_word_)):
        spamwriter.writerow([x for x in model.topic_word_[y]])

for i, topic_dist in enumerate(topic_word):
    topic_words = np.array(vocab)[np.argsort(topic_dist)][:-n_top_words:-1]
    print('Topic {}: {}'.format(i, ' '.join(topic_words).encode('utf-8')))

doc_topic = model.doc_topic_
for i in range(0, len(titles)):
    print("{} (top topic: {})".format(titles[i], doc_topic[i].argmax()))
    print(doc_topic[i].argsort()[::-1][:3])

with open("data/topics.csv", "w") as file:
    writer = csv.writer(file, delimiter=",")
    writer.writerow(["topicId", "word"])

    for i, topic_dist in enumerate(topic_word):
        topic_words = np.array(vocab)[np.argsort(topic_dist)][:-n_top_words:-1]
        for topic_word in topic_words:
            writer.writerow([i, topic_word.encode('utf-8')])

with open("data/sessions-topics.csv", "w") as file:
    writer = csv.writer(file, delimiter=",")
    writer.writerow(["sessionId", "topicId"])

    doc_topic = model.doc_topic_
    for i in range(0, len(titles)):
        writer.writerow([i, doc_topic[i].argmax()])
        print("{} (top topic: {})".format(titles[i], doc_topic[i].argmax()))
        print(doc_topic[i].argsort()[::-1][:3])
