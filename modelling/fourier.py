from datetime import datetime
import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
import peakutils as pu
from plots import *

class FourierApproximation:
    def __init__(self, signal, ts, num_peaks,fft_start, fft_end, ls_start, ls_end, m_power, thresh = 1000 / 275000, min_dist = 100, han_window=False, debug=False):
        self.num_peaks = num_peaks
        self.m_power = m_power
        self.han_window = han_window

        self.signal = signal
        self.ts = ts
        self.time = np.array([np.float64(i * self.ts) for i in range(len(self.signal))])
        if han_window:
            self.signal= np.array(signal*np.hanning(len(self.signal)))

        self.fft_signal = np.array(self.signal[fft_start:fft_end])
        self.fft_time = self.time[fft_start:fft_end]
        self.ls_signal = np.array(self.signal[ls_start:ls_end])
        self.ls_time = self.time[ls_start:ls_end]
        self.thresh = thresh
        self.min_dist = min_dist

        if debug: print("Starting FFT!")
        self.amplitude, self.frequency = self.fft()
        if debug: print("Finding peaks..")
        self.amplitude = np.concatenate(([0], self.amplitude))
        self.frequency = np.concatenate(([0], self.frequency))

        self.coefficients_indices = pu.indexes(self.amplitude, thres= self.thresh, min_dist=self.min_dist)
        self.fs = self.frequency[self.coefficients_indices]
        if debug: print("Least squares..")
        self.coefficients = self.least_squares()
        if debug: print("Predicting!")
        self.predicted_signal = predict_function(self.coefficients, self.time)
        # plot_chosen_frequencies(self.coefficients_indices, self.frequency, self.amplitude)

    def plot(self):
        plot_raw_data(self.time, self.signal)
        plot_chosen_frequencies(self.coefficients_indices, self.frequency, self.amplitude)
        # plot_freq_amplitude(self.amplitude, self.frequency)

    def fft(self):
        n = len(self.fft_signal)
        m = 2 ** (np.int64(np.ceil(np.log2(n)) + self.m_power))  # pick m power of 2 and >= n   
        freq = np.arange(0, m) * (1 / self.ts) / m
        amp = np.fft.fft(self.fft_signal, m) * self.ts
        return np.abs(amp[:len(amp) // 2]), freq[:len(freq) // 2]  # adjust for nyquist frequency

    def least_squares(self):
        cosines = np.column_stack([np.cos(2*np.pi*f*self.ls_time) for f in self.fs])
        sines = np.column_stack([np.sin(2*np.pi*f*self.ls_time) for f in self.fs])
        w = np.hstack((cosines, sines))

        coefficients = np.linalg.lstsq(w, self.ls_signal, rcond=None)[0]
        formatted_coefficients = [(self.fs[i], coefficients[i], coefficients[i + len(self.fs)]) for i in range(len(self.fs))]

        return formatted_coefficients
    
def predict_function(coefficients, t):
    ans = np.zeros(len(t))
    for (f, a, b) in coefficients:
        ans += a * np.cos(2 * np.pi * f * t) + b * np.sin(2 * np.pi * f * t)
    return ans