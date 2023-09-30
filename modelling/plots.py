import matplotlib.pyplot as plt
import matplotlib.dates as mdates
import datetime
import numpy as np
import pandas as pd

GRAPH_SIZE_X = 12
GRAPH_SIZE_Y = 7

def plot_freq_amplitude(amplitude, frequency):
    plt.plot(frequency, amplitude)
    plt.title('Frequency spectrum')
    plt.xlabel(r"$f$ (1/days)")
    plt.ylabel(r"$\hat{x}(f)$", rotation=0, labelpad=20)
    plt.gcf().set_size_inches(GRAPH_SIZE_X, GRAPH_SIZE_Y)
    plt.show()


def plot_raw_data(t, signal, resolution = 1):
    plt.plot(t[::resolution], signal[::resolution])
    plt.title("Signal")
    plt.xlabel(r't (days)')
    plt.ylabel(r"$x(t)$ (cm)")
    plt.gcf().set_size_inches(GRAPH_SIZE_X, GRAPH_SIZE_Y)
    plt.show()


def plot_level_approximation(t, levels, approximation, resolution = 1, title = 'Level Prediction'):
    plt.plot(t[::resolution], levels[::resolution], label="actual_level")
    plt.plot(t[::resolution], approximation[::resolution], label="predicted_level")
    plt.title(title)
    plt.ylabel(r"$x_{p}(t)$ (cm)")
    plt.xlabel(r't (days)')
    plt.gcf().set_size_inches(GRAPH_SIZE_X, GRAPH_SIZE_Y)
    plt.legend()
    plt.show()

def plot_chosen_frequencies(coeficcients_indices, freq:np.ndarray, amp:np.ndarray):
    plt.scatter(freq[coeficcients_indices], amp[coeficcients_indices], color='red', s=25)
    plt.gcf().set_size_inches(GRAPH_SIZE_X, GRAPH_SIZE_Y)
    plot_freq_amplitude(amp, freq )


def plot_original_predicted_difference(t, original, predicted, resolution = 1):
    plt.plot(t[::resolution], original[::resolution], label="actual_level")
    plt.plot(t[::resolution], predicted[::resolution], label="predicted_level")
    plt.plot(t[::resolution], original[::resolution] - predicted[::resolution], label="difference_level")
    plt.title('Difference between original and predicted')
    plt.xlabel(r't (days)')
    plt.ylabel(r"$x(t)$ (cm)")
    plt.gcf().set_size_inches(GRAPH_SIZE_X, GRAPH_SIZE_Y)
    plt.legend()
    plt.show()

# plot smse of all signal and smse of last n days of signal prediction
def plot_smse(smse_train, smse_pred):
    plt.plot(smse_train, label=r'$RMSE_{train}$ Training RMSE')
    plt.plot(smse_pred, label=r'$RMSE_{pred}$ Predicting RMSE')
    # mark point 8 on top of plot size bigger
    plt.scatter(8, smse_train[8], s=100, color='red')
    plt.title('Training vs Predicting RMSE')
    plt.xlabel(r'$n_{itr}$ Number of iterations')
    plt.ylabel(r"RMSE")
    plt.gcf().set_size_inches(GRAPH_SIZE_X, GRAPH_SIZE_Y)
    plt.legend()
    plt.show()


# plot smse of all signal and smse of last n days of signal prediction
def plot_td(TD_train, TD_pred):
    plt.plot(TD_train, label=r'$\Delta t{_{train}}$ Training $\Delta t$')
    plt.plot(TD_pred, label=r'$\Delta t{_{pred}}$ Predicting $\Delta t$')
    # mark point 8 on top of plot size bigger
    plt.scatter(8, TD_train[8], s=100, color='red')
    plt.title(r'Training vs Predicting $\Delta t$')
    plt.xlabel(r'$n_{itr}$ Number of iterations')
    plt.ylabel(r'$\Delta t$ (minutes)')
    plt.gcf().set_size_inches(GRAPH_SIZE_X, GRAPH_SIZE_Y)
    plt.legend()
    plt.show()

def plot_level_approximation_2(t, levels, approximation, resolution = 1, title = ''):
    start_date = datetime.datetime.strptime("01-10-2012", "%d-%m-%Y")
    plt.plot([start_date + datetime.timedelta(days=d) for d in t[::resolution]], levels[::resolution], label="actual_level")
    plt.plot([start_date + datetime.timedelta(days=d) for d in t[::resolution]], approximation[::resolution], label="predicted_level")
    plt.xlabel('Time (dates)')
    plt.ylabel('Water level (cm)')
    plt.gcf().set_size_inches(GRAPH_SIZE_X, GRAPH_SIZE_Y)
    plt.axhline(y=150, color='r', linestyle='-')
    plt.gca().xaxis.set_major_formatter(mdates.DateFormatter('%d.%m.%Y'))
    plt.xticks(rotation=45)
    plt.legend()

def main():
    signal = np.array(pd.read_csv('data/data_10_cleaned4.csv')['NUMERIEKEWAARDE'], dtype=np.float64)
