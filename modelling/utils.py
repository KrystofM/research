import numpy as np
from sklearn.metrics import confusion_matrix

def smse(actual, predicted):
    return np.sqrt(np.mean((actual - predicted) ** 2))

def me(actual, predicted):
    return np.mean(np.abs(actual - predicted))

def fpr(actual, predicted):
    fp = np.sum(((actual < 1.5) & (predicted >= 1.5)))
    n = np.sum((actual < 1.5))
    return 100 * fp / n

def fnr(actual, predicted):
    fn = np.sum(((actual >= 1.5) & (predicted < 1.5)))
    p = np.sum(((actual >= 1.5)))
    return 100*fn / p

def confusion(actual, predicted):
    print(actual, predicted)
    y_true = actual >= 1.5
    y_pred = predicted >= 1.5
    return 100*np.array(confusion_matrix(y_true, y_pred))/ len(actual)

def average_slope(signal, Ts):
    return np.mean(np.abs(np.diff(signal)))/Ts

def time_difference_estimate(actual, predicted, Ts):
    return 24*60*me(actual, predicted) / average_slope(actual, Ts)