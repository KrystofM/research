import math
import random
import numpy as np
from utils import *
from InhomogeneousPoisson import *

class Customer:
    def __init__(self):
        self.priority = -1
        self.requestTime = math.inf
        self.arrivalTime = math.inf
        self.serviceStartTime = math.inf
        self.serviceEndTime = math.inf
        self.scanMachine = None

    def serviceTime(self):
        return self.serviceEndTime - self.serviceStartTime

    def waitingTime(self):
        return self.serviceStartTime - self.arrivalTime
    
    def __str__(self):
        return "C(" + str(self.index) + "," + str(self.arrivalTime) + "," + str(self.serviceStartTime) + "," + str(self.waitingTime()) + ")"
    
    def __repr__(self):
        return self.__str__()


class EmergencyCustomer(Customer):
    ARRIVAL_RATE = 24 / (24 * 60)

    def __init__(self, index):
        super().__init__()
        self.index = index
        self.priority = 1
        self.type = "Emergency"

    def nextRequestTime(self, time):
        return np.random.exponential(1 / EmergencyCustomer.ARRIVAL_RATE)
    
    def __str__(self):
        return "EC(" + str(self.index) + "," + str(self.arrivalTime) + "," + str(self.serviceStartTime) + "," + str(self.waitingTime()) + ")"
    
    def __repr__(self):
        return self.__str__()
    
class InpatientCustomer(Customer):
    CALL_RATE = (6+6/16)/60 # 3*pi/(2*60) # in minutes

    def __init__(self, index):
        super().__init__()
        self.index = index
        self.ctCallTime = math.inf
        self.priority = 2
        self.type = "Inpatient"

    def nextRequestTime(self, time):
        addedTime = np.random.exponential(1 / InpatientCustomer.CALL_RATE)
        return addedTime

    # Uniformly distributed between [9, 15]
    def transferTimeFunction(self):
        return 9 + 6 * random.random()       

    def __str__(self):
        return "IC(" + str(self.index) + "," + str(Time(self.arrivalTime)) + "," + str(Time(self.serviceStartTime)) + "," + str(self.waitingTime()) + "," + str(self.requestTime) + "," + str(self.ctCallTime) + ")"
    
    def __repr__(self):
        return self.__str__()

class OutpatientCustomer(Customer):
    CALL_RATE = 23 / (8 * 60) # per open minute
    P_SHOW_UP = 0.84

    def __init__(self, index):
        super().__init__()
        self.index = index
        self.scheduledTime = math.inf        
        self.priority = 2
        self.type = "Outpatient" 

    def nextRequestTime(self, time):
        # if the time is within the open hours otherwise schedule for the next opening time
        addedTime = np.random.exponential(1 / OutpatientCustomer.CALL_RATE)
        nextOpenTime = nextOpeningTime(Time(time + addedTime), WEEKDAY_OPEN)
        if nextOpenTime == 0:
            return addedTime
        else:
            return addedTime + nextOpenTime + np.random.exponential(1 / OutpatientCustomer.CALL_RATE)

    def __str__(self):
        return "OC(" + str(self.index) + "," + str(Time(self.requestTime)) + "," + str(Time(self.arrivalTime)) + "," + str(Time(self.serviceStartTime)) + "," + str(self.waitingTime()) + ")"
    
    def __repr__(self):
        return self.__str__()

class PASCustomer(Customer):
    CALL_RATE = 8.3 / 60 # per minute

    def __init__(self, index):
        super().__init__()
        self.index = index
        self.priority = 2
        self.type = "PAS"
        self.serviceDeskStartTime = math.inf
        self.didCPMFirst = None
        self.doesBothCPMAndAdditionalTest = None
    
    def nextRequestTime(self, time):
        return np.random.exponential(1 / PASCustomer.CALL_RATE)
    
    def __str__(self):
        return "PASC(" + str(self.index) + "," + str(Time(self.arrivalTime)) + ", CPMFirst: " + str(self.didCPMFirst) + ", BothCPMAndAdditional: " + str(self.doesBothCPMAndAdditionalTest) + ")"

    def __repr__(self):
        return self.__str__()

def createCustomer(customerType, index):
    if customerType == "Emergency":
        return EmergencyCustomer(index)
    elif customerType == "Inpatient":
        return InpatientCustomer(index)
    elif customerType == "Outpatient":
        return OutpatientCustomer(index)
    elif customerType == "PAS":
        return PASCustomer(index)
    else:
        raise ValueError("Unknown customer type: " + customerType)