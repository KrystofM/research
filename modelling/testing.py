import seaborn as sns

from fourier import *
from plots import *
from utils import *

def fourier_predict_last_n_days(predict_time_end, ls_start_time, iterations, fft_start=0, m_power=2, thresh = 2000 / 275000, min_dist = 3000):
    predict_time_end *= 144
    Ts = 1 / 144
    signal_whole = np.array(pd.read_csv('data/data_10_cleaned4.csv')['NUMERIEKEWAARDE'], dtype=np.float64)
    signal = np.array(signal_whole[:-predict_time_end])
    fft_end =   -1 # Time at which we cut the end of the signal for fft
    ls_start = ls_start_time # Time at which we cut the start of the signal for least squares
    ls_end =   -1 # Time at which we cut the end of the signal for least squares

    han_window = False
    signal_left_over = np.array(signal)
    signal_combo = np.zeros(len(signal))
    coefficents = []
    prediction_time = np.array([np.float64(i * Ts) for i in range(len(signal), len(signal_whole))])
    actual = signal_whole[len(signal):len(signal_whole)]
    prediction = np.zeros(len(prediction_time))
    days = predict_time_end / 144
    all_signal_td = []
    predict_last_td = []
    for i in range(iterations):
        fourier = FourierApproximation(signal_left_over, Ts, 0, fft_start, fft_end, ls_start, ls_end, m_power, thresh, min_dist, han_window, True)
        thresh *= 1.15
        signal_left_over = signal_left_over - fourier.predicted_signal
        signal_combo += fourier.predicted_signal
        coefficents.extend(fourier.coefficients)        
        print("smse all signal =", smse(signal, signal_combo))
        # print("me all signal =", me(signal, signal_combo))
        all_signal_td.append(smse(signal, signal_combo))
        prediction = predict_function(coefficents, prediction_time)
        # plot_level_approximation(prediction_time, actual, prediction, 1, "Level prediction: Last " + str(days) + " days. " + r'$n_{itr}=$' + str(i + 1))
        print(len(fourier.coefficients))
        print("smse predict last", days, "days = ", smse(actual, prediction))        
        # print("me predict last", days, "days = ", me(actual, prediction))
        predict_last_td.append(smse(actual, prediction))
        # print("time difference estimate:", time_difference_estimate(actual, prediction, Ts))
        # print("fpr:", f"{fpr(actual, prediction)}%", "fnr:", f"{fnr(actual, prediction)}%")
        # print("confusion matrix:", confusion(actual, prediction))
        print("Iteration round: ", i + 1, "\n")

    # plot_smse(all_signal_td, predict_last_td)
    plot_level_approximation(prediction_time, actual, prediction, 250, "Level prediction: last " + str(days) + " days")
    print(len(coefficents))
    return coefficents, len(signal_whole)

def predict_april():
    plt.style.use("seaborn")
    plt.rcParams['font.family'] = 'serif'
    plt.rcParams['font.size'] = 15

    ls_start = 100000
    fft_start= 000000
    m_power = 4
    thresh = 3000 / 275000
    min_dist = 5000
    prediction_coefs, signal_len = fourier_predict_last_n_days(365, ls_start, 8, fft_start, m_power=m_power, thresh=thresh, min_dist=min_dist)
    aprilsignal = np.array(pd.read_csv('data/data_april.csv')['NUMERIEKEWAARDE'], dtype=np.float64)
    Ts = 1 / 144
    apriltime = np.array([np.float64(i * Ts) for i in range(len(aprilsignal))])
    aprilprediction = predict_function(prediction_coefs, np.array([np.float64(i * Ts) for i in range(signal_len, signal_len + len(aprilsignal))]))
    print("smse predict april = ", smse(aprilsignal, aprilprediction))
    print("time difference estimate:", time_difference_estimate(aprilsignal, aprilprediction, Ts))
    plot_level_approximation(apriltime, aprilsignal, aprilprediction, 1, "Level prediction: april")
    
def main():
    #ls str fft_st
    #100000 0 great over all time
    #20000 100000 very good over all time (the best)
    #450000 100000 good over last period
    #plt.style.use("ggplot")
    plt.style.use("seaborn")
    # sns.set(font_scale=1.8)
    plt.rcParams['font.family'] = 'serif'

    ls_start = 100000
    fft_start= 000000
    m_power = 4
    thresh = 0.1
    min_dist = 5000
    fourier_predict_last_n_days(365, ls_start, 8, fft_start, m_power=m_power, thresh=thresh, min_dist=min_dist)

main()
