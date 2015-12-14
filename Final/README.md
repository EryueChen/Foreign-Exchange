Files:

topic/index.py: Build index file for all the documents, including the docid and the path of the document.

topic/topics.py: Topic detection of all the documents. Output the topics and store the LDA model matrix in file.

topic/classify.py: Accept user input and calculate weight of term with each topic. Output the most common five topics to json file.

topic/data/flare.json: The output of the top five topics.

topic/index.html: The web page of the D3 visualization.

References:
https://en.wikipedia.org/wiki/Latent_Dirichlet_allocation
http://www.markhneedham.com/blog/2015/03/05/python-scikit-learnlda-extracting-topics-from-qcon-talk-abstracts/
http://bl.ocks.org/mbostock/7607535

