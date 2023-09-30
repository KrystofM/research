import numpy as np
import matplotlib.pyplot as plt
from scipy.optimize import minimize
from scipy.integrate import quad
from math import pi, sin
import fractions
from Time import Time
from utils import *

class InhomogeneousPoisson:
    lambda_I_opt = (6/16)/60 # 3/(8*60) # in minutes
    peak_opt = (6 + 6/16)/60 # 3*pi/(2*60) #in minutes

    # lambda_I_opt = 3*pi/(2*60) # in minutes
    # peak_opt = 3/(8*60) #in minutes

    def __init__(self):
        pass

# Define the times for the intervals
intervals = [0, 540, 630, 720, 810, 900, 1440]  # in minutes
morning_start = 540
mid_morning_peak_time = 630
noon_time = 720
mid_afternoon_peak_time = 810
end_time = 900

# Define the arrival rate function
def arrival_rate(t, params):
    # Convert time to minutes in a day
    t = t % (24 * 60)

    lambda_I, peak = params
    # Time intervals in minutes
    morning_start = 9 * 60
    mid_morning_peak_time = 10.5 * 60
    noon_time = 12 * 60
    mid_afternoon_peak_time = 13.5 * 60
    end_time = 15 * 60

    if t < morning_start or t >= end_time:
        return lambda_I
    elif morning_start <= t < mid_morning_peak_time:
        return lambda_I + (peak - lambda_I) * (np.sin((t - morning_start) / (mid_morning_peak_time - morning_start) * np.pi - np.pi / 2) + 1) / 2
    elif mid_morning_peak_time <= t < noon_time:
        return lambda_I + (peak - lambda_I) * (np.sin((noon_time - t) / (noon_time - mid_morning_peak_time) * np.pi - np.pi / 2) + 1) / 2
    elif noon_time <= t < mid_afternoon_peak_time:
        return lambda_I + (peak - lambda_I) * (np.sin((t - noon_time) / (mid_afternoon_peak_time - noon_time) * np.pi - np.pi / 2) + 1) / 2
    elif mid_afternoon_peak_time <= t < end_time:
        return lambda_I + (peak - lambda_I) * (np.sin((end_time - t) / (end_time - mid_afternoon_peak_time) * np.pi - np.pi / 2) + 1) / 2

# Define the objective function for the optimization
def objective(params):
    lambda_I, peak = params
    area1 = quad(lambda t: arrival_rate(t, params), intervals[0], intervals[1])[0]
    area2 = quad(lambda t: arrival_rate(t, params), intervals[1], intervals[5])[0]
    area3 = quad(lambda t: arrival_rate(t, params), intervals[5], intervals[6])[0]
    return abs(area1 + area3 - 6) + abs(area2 - 21)

# Initial guess for the parameters
initial_guess = [1, 5]

global lambda_I_opt, peak_opt
# Optimize the parameters
result = minimize(objective, initial_guess, bounds=((0, None), (0, None)))
lambda_I_opt, peak_opt = result.x

# print(lambda_I_opt, peak_opt)
# print(fractions.Fraction(lambda_I_opt).limit_denominator(), fractions.Fraction(peak_opt).limit_denominator())

lambda_I_opt = 3/(8*60) # in minutes
peak_opt = 1/9 #in minutes

# Reference Values
# lambda_I_opt = 3/(8*60) # in minutes
# peak_opt = 3*pi/(2*60) #in minutes
# print(lambda_I_opt, peak_opt)

# t = np.linspace(0, 24*60, 1000)
# rate = [arrival_rate(ti, [lambda_I_opt, peak_opt]) for ti in t]
# lambda_max = max(rate)

# Plotting
# plt.plot(t, rate)
# plt.xlabel('Time (minutes)')
# plt.ylabel('Arrival rate')
# plt.title('Arrival rate of inpatient requests')
# plt.grid(True)
# plt.show()


def inpatient_time_to_next_event(t, lambda_I_opt, peak_opt):
    # Calculate the rate of the inhomogeneous Poisson process at the time of the next event
    time = Time(t)
    if checkTimeWithinOpeningHours(time, WEEKDAY_OPEN) and time.hour() >= 9 and time.hour() < 15:
        # print((3 * (sin(2*pi/3*time.hour() + 2*pi/3) + 1) / 60))
        rate_inhomogeneous = lambda_I_opt + (3 * (sin(2*pi/3*time.hour() + 2*pi/3) + 1) / 60)
    else:
        rate_inhomogeneous = lambda_I_opt   

    # Accept the event with probability rate_inhomogeneous / max_rate
    if np.random.rand() < rate_inhomogeneous / peak_opt:
        return True
    else:
        return False

