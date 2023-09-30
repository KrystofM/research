from utils import *
import random

class ScanMachine:
    def __init__(self, openHours):
        self.openHours = openHours
        self.servicingCustomer = None
        self.busyTimeInOpenHours = 0
        self.busyTimeOutsideOpenHours = 0
        self.openTimeClosed = 0
        self.customersServed = 0

    def isOpen(self, time):
        for (start, end) in self.openHours[Time(time).dayNameOfCurrentWeek()]:
            if start.hour() <= Time(time).hour() < end.hour():
                return True
            if start.hour() == 0 and end.hour() == 0:
                return True
        return False
    
    def startService(self, time, customer):
        assert self.canServe(time)
        scanningTime = self.scanningTime()        
        self.servicingCustomer = customer        
        self.servicingCustomer.serviceStartTime = time        
        self.servicingCustomer.serviceEndTime = time + scanningTime
        self.servicingCustomer.scanMachine = self

        startWithinOpeningHours = checkTimeWithinOpeningHours(Time(time), WEEKDAY_OPEN)
        endWithinOpeningHours = checkTimeWithinOpeningHours(Time(time + scanningTime), WEEKDAY_OPEN)
        # both start and end are within opening hours
        if startWithinOpeningHours and endWithinOpeningHours:
            self.busyTimeInOpenHours += scanningTime
        # start is within opening hours but end is outside opening hours
        elif startWithinOpeningHours and not endWithinOpeningHours:            
            tillClosing = timeTillClosing(Time(time), WEEKDAY_OPEN)
            assert scanningTime > tillClosing            
            self.busyTimeInOpenHours += tillClosing
            self.busyTimeOutsideOpenHours += scanningTime - tillClosing
        # start is outside opening hours but end is within opening hours
        elif not startWithinOpeningHours and endWithinOpeningHours:
            tillOpening = nextOpeningTime(Time(time), WEEKDAY_OPEN)
            assert scanningTime > tillOpening
            self.busyTimeOutsideOpenHours += tillOpening
            self.busyTimeInOpenHours += scanningTime - tillOpening
        # both start and end are outside opening hours
        elif not startWithinOpeningHours and not endWithinOpeningHours:
            self.busyTimeOutsideOpenHours += scanningTime

    def endService(self):
        assert self.servicingCustomer != None
        self.servicingCustomer = None
        self.customersServed += 1
    
    def canServe(self, time):
        # print("Time: ", Time(time))
        # print("isOpen: ", self.isOpen(time))
        return self.servicingCustomer == None and self.isOpen(time)

    # Scanning time is uniformly distributed between 10 and 19 minutes
    def scanningTime(self):
        return 10 + 9 * random.random()