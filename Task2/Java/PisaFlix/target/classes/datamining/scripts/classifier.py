import nltk
import pandas
import os
import warnings
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression


# Gestisce i path relativi in quanto lo script è chiamato dalla classe java
def relative_path(path):
    dirname = os.path.dirname(__file__)
    return os.path.join(dirname, path)


def prepareStopWords():
    # nltk.download('stopwords') # la prima volta va scaricato
    # nltk.download('names')
    stopwords = nltk.corpus.stopwords.words('english')
    stopwords += nltk.corpus.names.words('male.txt') + nltk.corpus.names.words('female.txt')
    stopwords += ['although', 'along', 'also', 'abov', 'afterward', 'alon', 'alreadi', 'alway', 'ani', 'anoth', 'anyon',
                  'anyth', 'anywher', 'becam',
                  'becaus', 'becom', 'befor', 'besid', 'cri', 'describ', 'dure', 'els', 'elsewher', 'empti', 'everi',
                  'everyon', 'everyth', 'everywher', 'fifti', 'forti', 'henc', 'hereaft', 'herebi', 'howev', 'hundr',
                  'inde', 'mani', 'meanwhil', 'moreov', 'nobodi', 'noon', 'noth', 'nowher', 'onc', 'onli', 'otherwis',
                  'ourselv', 'perhap', 'pleas', 'sever', 'sinc', 'sincer', 'sixti', 'someon', 'someth', 'sometim',
                  'somewher', 'themselv', 'thenc', 'thereaft', 'therebi', 'therefor', 'togeth', 'twelv', 'twenti',
                  'veri', 'whatev', 'whenc', 'whenev', 'wherea', 'whereaft', 'wherebi', 'wherev', 'whi', 'yourselv']
    stopwords += ['a.', "'d", "'s", 'anywh', 'could', 'doe', 'el', 'elsewh', 'everywh', 'ind', 'might', 'must', "n't",
                  'need', 'otherwi', 'plea', 'sha', 'somewh', 'wo', 'would']
    return map(lambda x: x.lower(), stopwords)


def tokenize_and_stem(text):
    # nltk.download('punkt')  # la prima volta va scaricato
    stemmer = nltk.SnowballStemmer("english")
    tokens = [word for sent in nltk.sent_tokenize(text) for word in nltk.word_tokenize(sent)]
    filtered_tokens = []
    for token in tokens:
        if nltk.re.search('[a-zA-Z]', token):
            filtered_tokens.append(token)
    stems = [stemmer.stem(t) for t in filtered_tokens]
    return stems


#  NOTA:
#  Il preprocessing è identico a quello nello script "preprocessing.py" è stato copiato per semplicità, in quanto la
#  gestione delle importazioni nel caso in cui lo script venga chiamato dalla classe java riultava molto complicato
#  in altro modo

def preprocessing(dataset, min_df=0.1, max_df=0.9, max_features=None):
    dataset.drop('Title', axis=1, inplace=True)
    dataset.drop('Year', axis=1, inplace=True)
    class_ADULTS = dataset[dataset["MPAA"] == "ADULTS"]
    class_CHILDREN = dataset[dataset["MPAA"] == "CHILDREN"]
    dataset = class_ADULTS.append(class_CHILDREN, ignore_index=True)

    to_be_classified = pandas.read_csv(relative_path("../resources/datasets/to_be_classified.csv"))
    dataset = dataset.append(to_be_classified, ignore_index=True)

    plots = dataset.__getattr__("Plot")

    stopwords = prepareStopWords()

    tfidf_vectorizer = TfidfVectorizer(min_df=min_df, max_df=max_df, max_features=max_features,
                                       stop_words=stopwords,
                                       use_idf=True, tokenizer=tokenize_and_stem, ngram_range=(1, 3))
    tfidf_matrix = tfidf_vectorizer.fit_transform(plots)  # esegue la vettorizzazzione
    tfidf_vector = tfidf_matrix.toarray()
    terms = tfidf_vectorizer.get_feature_names()  # lista dei termini presi in considerazione

    result_dataset = dataset
    result_dataset.drop('Plot', axis=1, inplace=True)

    for j in range(0, len(terms)):
        values = []

        for i in range(0, len(tfidf_vector)):
            values.insert(len(values), tfidf_vector[i][j])

        result_dataset[terms[j]] = values
    return result_dataset


if __name__ == '__main__':
    warnings.filterwarnings("ignore")

    # Prelievo del dataset etichettato
    dataset = pandas.read_csv(relative_path("../resources/datasets/labelledData.csv"), ";")
    data = preprocessing(dataset=dataset, min_df=0.03, max_df=0.82, max_features=1196)

    # Si dividono i film del modello da quelli da classificare
    class_ADULTS = data[data["MPAA"] == "ADULTS"]
    class_CHILDREN = data[data["MPAA"] == "CHILDREN"]
    model_tuples = class_ADULTS.append(class_CHILDREN, ignore_index=True)
    to_be_classified_tuples = data[data["MPAA"] == "to_be_classified"]

    # Si trasformano i dati nel modello necessario alla classificazione
    X = model_tuples.iloc[:, 1:].values
    y = model_tuples['MPAA']
    C = to_be_classified_tuples.iloc[:, 1:].values

    # Modello di regresione logistica
    model = LogisticRegression().fit(X, y)

    # Stampa dei risultati delle predizioni su standard output
    for row in model.predict_proba(C):
        print(str(row[0]))
