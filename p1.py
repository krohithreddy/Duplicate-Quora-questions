import csv
from sklearn.model_selection import train_test_split
import numpy as np
import gensim
from gensim import corpora, models, similarities
from gensim.models import word2vec, tfidfmodel
from gensim.corpora import Dictionary
import logging
from sklearn.metrics.pairwise import cosine_similarity
import math
import re
#from itertools import zip

def tokenize(string):
  return string.split(" ")
  #return(list(re.findall(r"[a-zA-Z0-9+\-/*.]+", string)))

def sublinear_term_frequency(term, tokenized_document):
  count = tokenized_document.count(term)
  if count == 0:
    return 0
  return 1 + math.log(count)

def inverse_document_frequencies(tokenized_documents):
  idf_values = {}
  all_tokens_set = set([item for sublist in tokenized_documents for item in sublist])
  for tkn in all_tokens_set:
    contains_token = map(lambda doc: tkn in doc, tokenized_documents)
    idf_values[tkn] = 1 + math.log(len(tokenized_documents)/(sum(contains_token)))
  return idf_values

def tfidf(documents):
  tokenized_documents = documents
  idf = inverse_document_frequencies(tokenized_documents)
  tfidf_documents = []
  for document in tokenized_documents:
    doc_tfidf = []
    for term in idf.keys():
      tf = sublinear_term_frequency(term, document)
      doc_tfidf.append(tf * idf[term])
    tfidf_documents.append(doc_tfidf)
  return tfidf_documents, idf.keys()

def get_similarity_idf(model, X):
  n = []
  for i in X:
    s1 = tokenize(i[0])
    s2 = tokenize(i[1])
    documents = [s1, s2]
    tfidf_values, keys = tfidf(documents)
    keys = list(keys)
    w1 = [tfidf_values[0][keys.index(j)] for j in s1]
    s1_vec = np.asarray([model.wv[j] for j in s1])
    w_s1 = np.average(s1_vec, axis = 0, weights = w1)
    w2 = [tfidf_values[1][keys.index(j)] for j in s2]
    s2_vec = np.asarray([model.wv[j] for j in s2])
    w_s2 = np.average(s2_vec, axis = 0, weights = w2)
    n.append(cosine_measure(w_s1, w_s2))
  return np.asarray(n)

def dot_product(v1, v2):
    return sum(map(lambda x: x[0] * x[1], zip(v1, v2)))

def cosine_measure(v1, v2):
    prod = dot_product(v1, v2)
    len1 = math.sqrt(dot_product(v1, v1))
    len2 = math.sqrt(dot_product(v2, v2))
    return prod / (len1 * len2)

def get_similarity_noidf(model, X):
  n = []
  for i in X:
    s1 = i[0]
    s2 = i[1]
    s1 = tokenize(s1)
    s2 = tokenize(s2)
    s1_vec = np.asarray([model.wv[j] for j in s1])
    s2_vec = np.asarray([model.wv[j] for j in s2])
    s1_vec = np.mean(s1_vec, axis = 0)
    s2_vec = np.mean(s2_vec, axis = 0)
    n.append(cosine_measure(s1_vec, s2_vec))
  return np.asarray(n)

def get_common_words_percent(X):
  n = []
  for i in X:
    s1 = set(tokenize(i[0]))
    s2 = set(tokenize(i[1]))
    inters = s1.intersection(s2)
    n.append(len(inters)/(len(s1)+len(s2)))
  return np.asarray(n)

def get_len_diff_percent(X):
  n = []
  for i in X:
    s1 = tokenize(i[0])
    s2 = tokenize(i[1])
    n.append(abs(len(s1)-len(s2))/(len(s1)+len(s2)))
  return np.asarray(n)
  
ifile  = open('train.csv', "r")
read = csv.reader(ifile)
dataset = []
for row in read :
    l = row
    dataset.append(l)
print(len(dataset))
dataset = dataset[1:]
dataset = np.asarray(dataset)
X = dataset[:,[3,4]]
y = dataset[:,[5]]

sentences = []
for i in X:
  for j in i:
    sentences.append(tokenize(j))
#logging.basicConfig(format='%(asctime)s : %(levelname)s : %(message)s', level=logging.INFO)
model = word2vec.Word2Vec(sentences, min_count=1, size=300)

similarity_noidf = get_similarity_noidf(model, X)
#print(similarity_noidf[:100])

similarity_idf = get_similarity_idf(model, X)
#print(similarity_idf)

common_words_percent = get_common_words_percent(X)
len_diff_percent = get_len_diff_percent(X)

dataset_ml = np.asarray([[similarity_noidf[i], similarity_idf[i], common_words_percent[i], len_diff_percent[i]] for i in range(len(X))])

y = np.asarray([i[0] for i in y])
y = y.astype(np.int32)

X_train, X_test, y_train, y_test = train_test_split( dataset_ml, y, test_size=0.20, random_state=20)

print(X_train)
print(X_test)
print(y_train)
print(y_test)
'''
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import cross_val_score
from sklearn.tree import DecisionTreeClassifier

clf = DecisionTreeClassifier(random_state=0)
print(cross_val_score(clf, X_train, y_train, cv=5))

clf = RandomForestClassifier(max_depth=2, random_state=0)
print(cross_val_score(clf, X_train, y_train, cv=5))

from sklearn.neighbors import KNeighborsClassifier
neigh = KNeighborsClassifier(n_neighbors=3)
print(cross_val_score(neigh, X_train, y_train, cv=5))

neigh = KNeighborsClassifier(n_neighbors=4)
print(cross_val_score(neigh, X_train, y_train, cv=5))

neigh = KNeighborsClassifier(n_neighbors=5)
print(cross_val_score(neigh, X_train, y_train, cv=5))


neigh = KNeighborsClassifier(n_neighbors=6)
print(cross_val_score(neigh, X_train, y_train, cv=5))

neigh = KNeighborsClassifier(n_neighbors=7)
print(cross_val_score(neigh, X_train, y_train, cv=5))

neigh = KNeighborsClassifier(n_neighbors=8)
print(cross_val_score(neigh, X_train, y_train, cv=5))

from sklearn import svm
clf = svm.SVC()
print(cross_val_score(clf, X_train, y_train, cv=5))


from sklearn.ensemble import AdaBoostClassifier
from sklearn.tree import DecisionTreeClassifier
bdt = AdaBoostClassifier(DecisionTreeClassifier(max_depth=1),
                         algorithm="SAMME",
                         n_estimators=200)
                         
print(cross_val_score(bdt, X_train, y_train, cv=5))
'''

from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import cross_val_score
from sklearn.tree import DecisionTreeClassifier

clf = DecisionTreeClassifier(random_state=0)
#print(cross_val_score(clf, X_train, y_train, cv=4))

clf = RandomForestClassifier(max_depth=2, random_state=0)
print(cross_val_score(clf, X_train, y_train, cv=5))

from sklearn.neighbors import KNeighborsClassifier
neigh = KNeighborsClassifier(n_neighbors=3)
#print(cross_val_score(neigh, X_train, y_train, cv=4))

#neigh = KNeighborsClassifier(n_neighbors=4)
#print(cross_val_score(neigh, X_train, y_train, cv=4))

neigh = KNeighborsClassifier(n_neighbors=5)
#print(cross_val_score(neigh, X_train, y_train, cv=5))


#neigh = KNeighborsClassifier(n_neighbors=6)
#print(cross_val_score(neigh, X_train, y_train, cv=4))

#neigh = KNeighborsClassifier(n_neighbors=7)
#print(cross_val_score(neigh, X_train, y_train, cv=4))

#neigh = KNeighborsClassifier(n_neighbors=8)
#print(cross_val_score(neigh, X_train, y_train, cv=4))

from sklearn import svm
#clf = svm.SVC()
#print(cross_val_score(clf, X_train, y_train, cv=4))


from sklearn.ensemble import AdaBoostClassifier
from sklearn.tree import DecisionTreeClassifier
bdt = AdaBoostClassifier(DecisionTreeClassifier(max_depth=3),
                         algorithm="SAMME",
                         n_estimators=200)
                         
#print(cross_val_score(bdt, X_train, y_train, cv=5))

