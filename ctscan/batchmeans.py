from Events import *
from Time import Time
import scipy.stats as stats

def isWarmUp(pointEstimatersWk, D):
    assert(len(pointEstimatersWk) > 2*D)
    
    sumD = 0
    sum2D = 0
    for i in range(0, D):
        sumD += pointEstimatersWk[i]
    for i in range(0, 2*D):
        sum2D += pointEstimatersWk[i]

    avgD  = sumD / D
    avg2D = sum2D / (2*D)
    abs = np.abs((avgD / avg2D) - 1)
    print("abs: " + str(abs))
    return abs <= 0.05

# utilization of inside of opening hours
def findWarmUpPeriodWithRegularUtilizationWeeks():
    Nw = 10
    Kw = 200
    allWk = []

    for i in range(0, Nw):
        simulation = runSimulation(stopCriterium=StopAfterTime(Time.fromWeeks(Kw).time())).statistics.utilizationsRegularPerWeek
        print("Length of simulation:" + str(len(simulation)))
        allWk.append(simulation)

    pointEstimatersWk = []
    for i in range(0, Kw):
        sum = 0        
        for wk in allWk:        
            sum += wk[i]
        pointEstimatersWk.append(sum / Nw)
    
    D = 1
    while not isWarmUp(pointEstimatersWk, D):
        print("D: " + str(D))
        D += 1
    print("Warm up period: " + str(D))

def findWarmUpPeriodWithRegularUtilizationDays():
    Nw = 50
    Kw = 100
    allWk = []

    for i in range(0, Nw):
        simulation = runSimulation(stopCriterium=StopAfterTime(Time.fromDays(Kw).time())).statistics.utilizationsRegularPerDay
        print("Length of simulation:" + str(len(simulation)))
        allWk.append(simulation)

    pointEstimatersWk = []
    for i in range(0, Kw):
        sum = 0        
        for wk in allWk:        
            sum += wk[i]
        pointEstimatersWk.append(sum / Nw)
    
    D = 1
    while not isWarmUp(pointEstimatersWk, D):
        print("D: " + str(D))
        D += 1
    print("Warm up period: " + str(D))

# findWarmUpPeriodWithRegularUtilizationDays()

def runStatsOf(array):
    (mean, halfwidth) = runStats(array)
    print("Mean: " + str(mean))
    print("Confidence interval: " + str(mean - halfwidth) + " - " + str(mean + halfwidth))
    print("Relative precision " + str(relativePrecision(halfwidth, mean)) + "%")

    # estimate cov(X1, X2)
    """ cov = 0
    for i in range(0, N - 1):
        cov += (array[i] - mean) * (array[i + 1] - mean)
        cov /= (N - 1)
    print("Covariance: " + str(cov)) """

def runStats(array):
    mean = np.mean(array)
    std = np.std(array)
    N = len(array)
    c = np.abs(stats.t.ppf(1 - 0.05 / 2, N - 1))
    halfwidth = c * std / math.sqrt(N)

    return (mean, halfwidth)

def relativePrecision(halfWidth, mean):
    if mean == 0:
        return 0
    return halfWidth / (mean - halfWidth) * 100

def relativePrecisionOfHalfWidthCI(halfWidth, mean):
    return halfWidth < 0.1 * mean / 1.1


def runGraphStatsOf(array):
    # for each distribution and each key in distribution 
    grouped = {}
    for distribution in array:
        for key in distribution:
            if grouped.get(key) == None:
                grouped[key] = []
            grouped[key].append(distribution[key])

    # for each key in grouped runStats
    cis = []
    means = []
    for key in grouped:
        print("\nKey: " + str(key))
        (mean, halfwidth) = runStats(grouped[key])
        cis.append(2*halfwidth)
        means.append(mean)
        print("Mean: " + str(mean))
        print("Confidence interval: " + str(mean - halfwidth) + " - " + str(mean + halfwidth))
        print("Relative precision " + str(relativePrecision(halfwidth, mean)) + "%")
        print("Relative precision of half width CI: " + str(relativePrecisionOfHalfWidthCI(halfwidth, mean)))
        # create the graph

    meansAdjusted = []
    # for each item in means sum all the items after it
    for i in range(0, len(means)):
        sum = 0
        for j in range(i, len(means)):
            sum += means[j]
        meansAdjusted.append(sum)


    meansAdjusted = []
    # for each item in means sum all the items after it
    for i in range(0, len(means)):
        sum = 0
        for j in range(i, len(means)):
            sum += means[j]
        meansAdjusted.append(sum)

    means = means[:50]
    cis = cis[:50]
    grouped = dict(list(grouped.items())[:50])
    meansAdjusted = meansAdjusted[:50]

    plt.bar(grouped.keys(), means, yerr=cis, align='center', alpha=0.5, ecolor='black', capsize=10)
    plt.ylabel('Fraction of Patients')
    plt.xlabel('Waiting Room Size upon Patients arrival')
    plt.title('Fraction of Patients by Waiting Room Size')
    plt.show()

    # plot meansAdjusted
    plt.bar(grouped.keys(), meansAdjusted, align='center', alpha=0.5, ecolor='black', capsize=10)
    plt.ylabel('Fraction of Patients without chair')
    plt.xlabel('Number of chairs in waiting room')
    plt.title('Fraction of Patients without chair by Number of chairs in waiting room')
    plt.show()

    # get elements 10 - 20 of means, cis and grouped.keys()
    means = means[9:21]
    cis = cis[9:21]
    grouped = dict(list(grouped.items())[9:21])
    meansAdjusted = meansAdjusted[9:21]

    plt.bar(grouped.keys(), means, yerr=cis, align='center', alpha=0.5, ecolor='black', capsize=10)
    plt.ylabel('Fraction of Patients')
    plt.xlabel('Waiting Room Size upon Patients arrival')
    plt.title('Fraction of Patients by Waiting Room Size')
    plt.show()

    plt.bar(grouped.keys(), meansAdjusted, align='center', alpha=0.5, ecolor='black', capsize=10)
    plt.ylabel('Fraction of Patients without chair')
    plt.xlabel('Number of chairs in waiting room')
    plt.title('Fraction of Patients without chair by Number of chairs in waiting room')
    plt.show()


    return (means, cis, grouped.keys())


def batchMeansWaitingTime():
    D = 50
    K = 4*D
    N = 10 # number of batches

    stats = runSimulation(stopCriterium=StopAfterTime(Time.fromDays(D + (K * N)).time())).statistics
    utilizationsRegularPerDay = []
    utilizationsOutsidePerDay = []
    averageAccessTimeOutpatientsPerDay = []
    inpatientsDidNotScanPerDay = []
    averageWaitingTimeEmergencyPerDay = []
    averageWaitingTimeOutpatientsPerDay = []
    fractionCustomersOutsidePerDay = []
    waitingRoomSizeDistributionPerDay = []
    averageWaitingRoomSizePerDay = []

    for i in range(0, N):   
        sum1 = 0
        sum2 = 0
        sum3 = 0
        sum4 = 0
        sum5 = 0
        sum6 = 0
        sum7 = 0
        sum8 = 0
        sumDistribution = {}
        for j in range(0, K):
            sum1 += stats.utilizationsRegularPerDay[D + (i * K) + j]
            sum2 += stats.utilizationsOutsidePerDay[D + (i * K) + j]
            sum3 += stats.averageAccessTimeOutpatientsPerDay[D + (i * K) + j]
            sum4 += stats.inpatientsDidNotScanPerDay[D + (i * K) + j]
            sum5 += stats.averageWaitingTimeEmergencyPerDay[D + (i * K) + j]
            sum6 += stats.averageWaitingTimeOutpatientsPerDay[D + (i * K) + j]
            sum7 += stats.fractionCustomersOutsidePerDay[D + (i * K) + j]
            sum8 += stats.averageWaitingRoomSizePerDay[D + (i * K) + j]
            for key in stats.getWaitingRoomSizeDistributionPerDay[D + (i * K) + j]:
                if sumDistribution.get(key) == None:
                    sumDistribution[key] = 0
                sumDistribution[key] += stats.getWaitingRoomSizeDistributionPerDay[D + (i * K) + j][key]
        utilizationsRegularPerDay.append(sum1 / K)
        utilizationsOutsidePerDay.append(sum2 / K)
        averageAccessTimeOutpatientsPerDay.append(sum3 / K)
        inpatientsDidNotScanPerDay.append(sum4 / K)
        averageWaitingTimeEmergencyPerDay.append(sum5 / K)
        averageWaitingTimeOutpatientsPerDay.append(sum6 / K)
        fractionCustomersOutsidePerDay.append(sum7 / K)
        averageWaitingRoomSizePerDay.append(sum8 / K)
        for key in sumDistribution:
            sumDistribution[key] /= K
        waitingRoomSizeDistributionPerDay.append(sumDistribution)

    print("Utilization of regular hours:")
    runStatsOf(utilizationsRegularPerDay)
    print("\nUtilization of outside hours:")
    runStatsOf(utilizationsOutsidePerDay)
    print("\nAverage access time of outpatients:")
    runStatsOf(averageAccessTimeOutpatientsPerDay)
    print("\nPercentage of inpatients that did not scan:")
    runStatsOf(inpatientsDidNotScanPerDay)
    print("\nAverage waiting time of emergency:")
    runStatsOf(averageWaitingTimeEmergencyPerDay)
    print("\nAverage waiting time of outpatients:")
    runStatsOf(averageWaitingTimeOutpatientsPerDay)
    print("\nFraction of customers outside:")
    runStatsOf(fractionCustomersOutsidePerDay) 
    print("\nWaiting room size distribution:")
    runGraphStatsOf(waitingRoomSizeDistributionPerDay)
    print("\nAverage waiting room size:")
    runStatsOf(averageWaitingRoomSizePerDay)

batchMeansWaitingTime()
