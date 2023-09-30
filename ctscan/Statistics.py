from Time import Time
from utils import *

# This class is used to calculate the required statistics of the system
class Statistics:
    def __init__(self, system):
        self.system = system
        self.customersServed = 0
        self.outpatientsRequested = 0 
        self.accessTimeOutpatientsSum = 0
        self.outpatientsServed = 0
        self.waitingTimeOutpatientsSum = 0
        self.emergencyCustomersServed = 0
        self.waitingTimeEmergencySum = 0
        self.customersOutside = 0
        self.inpatientsInOpen = 0
        self.inpatientsWithoutServiceStartTime = 0
        self.openTimeSum = 0
        self.closeTimeSum = 0
        self.utilizationsRegularPerWeek = []

        self.utilizationsRegularPerDay = []
        self.utilizationsOutsidePerDay = []
        self.averageAccessTimeOutpatientsPerDay = []
        self.inpatientsDidNotScanPerDay = []
        self.averageWaitingTimeEmergencyPerDay = []
        self.averageWaitingTimeOutpatientsPerDay = []
        self.fractionCustomersOutsidePerDay = []
        self.getWaitingRoomSizeDistributionPerDay = []
        self.averageWaitingRoomSizePerDay = []

        self.arrivedToWaitingRoom = 0
        self.discreteWaitingMap = {}


    def __str__(self):
        string = ""
        overallCost = 0
        (string, overallCost) = self.addMachinesString(string, overallCost)
        print("Overall cost: ", overallCost)
        string += "\n\nOverall\n"
        string += "Utilization in open hours: " + str(self.get_utilization_regular_hours()) + "\n"
        string += "Utilization outside open hours: " + str(self.get_utilization_outside_hours()) + "\n"
        averageAccessTimeOutpatients = self.get_average_access_time_outpatients()
        overallCost += averageAccessTimeOutpatients * 200
        string += "Average access time outpatients: " + str(averageAccessTimeOutpatients) + "\n"
        averageWaitingTimeOutpatients = self.get_average_waiting_time_outpatients()
        overallCost += averageWaitingTimeOutpatients * 50
        string += "Average waiting time outpatients: " + str(averageWaitingTimeOutpatients) + "\n"
        averageWaitingTimeEmergency = self.get_average_waiting_time_emergency()
        overallCost += averageWaitingTimeEmergency * 70
        string += "Average waiting time emergency: " + str(averageWaitingTimeEmergency) + "\n"
        fractionCustomersOutside = self.get_fraction_customers_outside()
        string += "Fraction customers outside: " + str(fractionCustomersOutside) + "\n"
        percetageInpatientsDidNotScan = self.get_percentage_inpatients_did_not_scan()
        overallCost += percetageInpatientsDidNotScan * 100 * 100
        string += "Percentage inpatients did not scan: " + str(percetageInpatientsDidNotScan) + "\n\n"
        string += "Overall cost: " + str(overallCost) + "\n\n"
        string += "Waiting room sizes amounts: " + str(self.discreteWaitingMap) + "\n"
        string += "Waiting room size distribution: " + str(self.getWaitingRoomSizeDistribution()) + "\n"
        string += "Average waiting room size: " + str(self.averageWaitingRoomSize()) + "\n"
        string += "Waiting room size: " + str(self.getWaitingRoomSize()) + "\n"
        string += "Waiting room secretaries size: " + str(len(self.system.secretaryWaitingRoom)) + "\n"
        string += "Waiting room CPM size: " + str(len(self.system.cpmWaitingRoom)) + "\n"
        string += "Waiting room consultation size: " + str(len(self.system.consultationWaitingRoom)) + "\n"
        string += "Waiting room additional test size: " + str(len(self.system.additionalTestWaitingRoom)) + "\n"
        return string
    
    def addMachinesString(self, string, overallCost):
        for i, machine in enumerate(self.system.scanningMachines):
            string += "\n\nMachine " + str(i) + ": \n"
            # string += "Busy time in open hours: " + str(machine.busyTimeInOpenHours) + "\n"
            # string += "Busy time outside open hours: " + str(machine.busyTimeOutsideOpenHours) + "\n"
            # string += "Customers served: " + str(machine.customersServed) + "\n"
            string += "Utilization in open hours: " + str(machine.busyTimeInOpenHours / self.openTimeSum) + "\n"
            overallCost += (1 - machine.busyTimeInOpenHours / self.openTimeSum) * 500
            if machine.openHours == ALL_TIME_OPEN:
                string += "Utilization outside open hours: " + str(machine.busyTimeOutsideOpenHours / self.closeTimeSum) + "\n"
                overallCost += (1 - machine.busyTimeOutsideOpenHours / self.closeTimeSum) * 300
        return (string, overallCost)
    
    def get_percentage_inpatients_did_not_scan(self):
        if self.inpatientsInOpen == 0:
            return 0
        return self.inpatientsWithoutServiceStartTime / self.inpatientsInOpen
    
    def get_average_access_time_outpatients(self):
        if self.outpatientsRequested == 0:
            return 0
        return Time(self.accessTimeOutpatientsSum / self.outpatientsRequested).dayExact()
    
    def get_average_waiting_time_outpatients(self):
        if self.outpatientsServed == 0:
            return 0
        return Time(self.waitingTimeOutpatientsSum / self.outpatientsServed).time()
    
    def get_average_waiting_time_emergency(self):
        if self.emergencyCustomersServed == 0:
            return 0
        return Time(self.waitingTimeEmergencySum / self.emergencyCustomersServed).time()
    
    def get_fraction_customers_outside(self):
        if self.customersServed == 0:
            return 0
        return self.customersOutside / self.customersServed

    def get_utilization_regular_hours(self):
        # for all cts add their busy time in open hours
        total_busy_time_in_open_hours = 0
        for ct in self.system.scanningMachines:
            total_busy_time_in_open_hours += ct.busyTimeInOpenHours
        # return the utilization
        return total_busy_time_in_open_hours / (self.openTimeSum * len(self.system.scanningMachines))
        
    def get_utilization_outside_hours(self):
        # for all cts add their busy time outside open hours
        total_busy_time_outside_open_hours = 0
        for ct in self.system.scanningMachines:
            total_busy_time_outside_open_hours += ct.busyTimeOutsideOpenHours
        # return the utilization
        return total_busy_time_outside_open_hours / self.closeTimeSum
    
    def collectStatisticsDay(self):
        self.utilizationsRegularPerDay.append(self.get_utilization_regular_hours())
        self.utilizationsOutsidePerDay.append(self.get_utilization_outside_hours())
        self.averageAccessTimeOutpatientsPerDay.append(self.get_average_access_time_outpatients())
        self.inpatientsDidNotScanPerDay.append(self.get_percentage_inpatients_did_not_scan())
        self.averageWaitingTimeEmergencyPerDay.append(self.get_average_waiting_time_emergency())
        self.averageWaitingTimeOutpatientsPerDay.append(self.get_average_waiting_time_outpatients())
        self.fractionCustomersOutsidePerDay.append(self.get_fraction_customers_outside())  
        self.getWaitingRoomSizeDistributionPerDay.append(self.getWaitingRoomSizeDistribution())      
        self.averageWaitingRoomSizePerDay.append(self.averageWaitingRoomSize())
    
    def collectStatisticsWeek(self):
        self.utilizationsRegularPerWeek.append(self.get_utilization_regular_hours())

    def dayStarted(self, time):
        if Time(time).dayNameOfCurrentWeek() in ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday"] and Time(time).time() != 0:
            self.openTimeSum += 8 * 60
            self.closeTimeSum += 16 * 60
        else:
            self.closeTimeSum += 24 * 60
            
       # if not the first day
        if Time(time).time() != 0:
            self.collectStatisticsDay()
        # if its sunday
        if Time(time).dayNameOfCurrentWeek() == "Sunday":
            self.collectStatisticsWeek()

    def averageWaitingRoomSize(self):
        totalPatientsInWaitingRoom = 0
        for key in self.discreteWaitingMap:
            totalPatientsInWaitingRoom += key * self.discreteWaitingMap[key]
        return totalPatientsInWaitingRoom / self.arrivedToWaitingRoom

    def getWaitingRoomSizeDistribution(self):
        # deep copy the discreteWaitingMap
        discreteWaitingMapCopy = self.discreteWaitingMap.copy()
        # for each item of discreteWaitingMap divide by the total number of arriveToWaitingRoom
        for key in discreteWaitingMapCopy:
            discreteWaitingMapCopy[key] /= self.arrivedToWaitingRoom
        
        # sort based on key
        discreteWaitingMapCopy = dict(sorted(discreteWaitingMapCopy.items()))

        return discreteWaitingMapCopy
    
    def getWaitingRoomSize(self):
        totalPatientsInWaitingRoom = len(self.system.waitingRoom)
        totalPatientsInWaitingRoom += len(self.system.secretaryWaitingRoom)
        totalPatientsInWaitingRoom += len(self.system.cpmWaitingRoom)
        totalPatientsInWaitingRoom += len(self.system.consultationWaitingRoom)
        totalPatientsInWaitingRoom += len(self.system.additionalTestWaitingRoom)

        return totalPatientsInWaitingRoom

    def customerArrivedToWaitingRoom(self):
        self.arrivedToWaitingRoom += 1

        # sum all patients in waiting rooms
        totalPatientsInWaitingRoom = self.getWaitingRoomSize()        

        if self.discreteWaitingMap.get(totalPatientsInWaitingRoom) == None:
            self.discreteWaitingMap[totalPatientsInWaitingRoom] = 0
        self.discreteWaitingMap[totalPatientsInWaitingRoom] += 1


