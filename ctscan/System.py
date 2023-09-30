
# Two scanning machines
# First open hours are 8am to 4pm on weekdays
# Second open all the time


# System can have N different scanning machines each with different open hours

from Customer import *
from ScanMachine import *
from Schedule import *
from Statistics import *
from PASHelper import *

class System:
    SECRETARY_STAFF = 1
    CONSULTATION_STAFF = 3
    CPM_STAFF = 2
    ADD_STAFF = 1

    def __init__(self):
        # Waiting for inpatients, outpatients and emergencies
        self.waitingRoom = []
        self.scanningMachines = [ScanMachine(ALL_TIME_OPEN), ScanMachine(WEEKDAY_OPEN)]
        self.patientsSecretary = []
        self.patientsCPM = []
        self.patientsConsultation = []
        self.patientsAdditionalTest = []
        self.waitingInpatients = []
        self.travelingInpatients = []
        # Waiting outpatients for scheduling next day
        self.waitingOutpatients = []
        self.openHours = WEEKDAY_OPEN
        self.waitingTimes = []
        self.statistics = Statistics(self)
        self.inpatientsOfDay = []
        # PAS Waiting rooms for each department
        self.secretaryWaitingRoom = []
        self.cpmWaitingRoom = []
        self.consultationWaitingRoom = []
        self.additionalTestWaitingRoom = []
        # PAS Actualy patients in the department
        self.patientsSecretary = []
        self.patientsCPM = []
        self.patientsConsultation = []
        self.patientsAdditionalTest = []

    def canAddToSecretary(self):
        return len(self.patientsSecretary) < System.SECRETARY_STAFF
    
    def addSecretary(self, customer):
        assert self.canAddToSecretary()
        self.patientsSecretary.append(customer)        

    def removeSecretary(self, customer):
        assert len(self.patientsSecretary) > 0
        return self.patientsSecretary.remove(customer)

    def canAddToCPM(self):
        return len(self.patientsCPM) < System.CPM_STAFF

    def addCPM(self, customer):
        assert self.canAddToCPM()
        self.patientsCPM.append(customer)

    # remove the given customer from the CPM
    def removeCPM(self, customer):
        assert len(self.patientsCPM) > 0 and customer in self.patientsCPM
        self.patientsCPM.remove(customer)

    def canAddToConsultation(self):
        return len(self.patientsConsultation) < System.CONSULTATION_STAFF

    def addConsultation(self, customer):
        assert self.canAddToConsultation()
        self.patientsConsultation.append(customer)

    def removeConsultation(self, customer):
        assert len(self.patientsConsultation) > 0 and customer in self.patientsConsultation
        self.patientsConsultation.remove(customer)

    def canAddToAdditionalTest(self):
        return len(self.patientsAdditionalTest) < System.ADD_STAFF

    def addAdditionalTest(self, customer):
        assert self.canAddToAdditionalTest()
        self.patientsAdditionalTest.append(customer)     

    def removeAdditionalTest(self, customer):
        assert len(self.patientsAdditionalTest) > 0 and customer in self.patientsAdditionalTest
        self.patientsAdditionalTest.remove(customer)   

    def inpatientWaits(self, customer):
        self.waitingInpatients.append(customer)
    
    def customerStartService(self, time, customer):
        # assert customer first in waiting room
        assert self.canServe(time) and self.getNextCustomer() == customer
        # get the first machine that can serve
        machine = next(machine for machine in self.scanningMachines if machine.canServe(time))     
        # remove the customer from the waiting room
        self.waitingRoom.pop(0)
        # start service for the customer
        machine.startService(time, customer)

        if isinstance(customer, EmergencyCustomer):
            self.statistics.emergencyCustomersServed += 1
            self.statistics.waitingTimeEmergencySum += customer.waitingTime()
        elif isinstance(customer, OutpatientCustomer):
            self.statistics.outpatientsServed += 1
            self.statistics.waitingTimeOutpatientsSum += customer.waitingTime()

    def customerEndService(self, time, customer):
        # remove the customer from the machine
        customer.scanMachine.endService()

        self.statistics.customersServed += 1

    def getNextCustomer(self):
        # if there is no one in the waiting room return None
        if len(self.waitingRoom) == 0:
            return None

        # if there is someone in the waiting room pop the first customer
        return self.waitingRoom[0]
    
    def openingHoursEnded(self):
        # get all inpatients with request after opening time
        inpatientsInOpen = [inpatient for inpatient in self.inpatientsOfDay if Time(inpatient.requestTime).hour() >= 8]
        self.statistics.inpatientsInOpen += len(inpatientsInOpen)
        # get all inpatients without service start time
        self.statistics.inpatientsWithoutServiceStartTime += len([inpatient for inpatient in inpatientsInOpen if inpatient.serviceStartTime == math.inf]) 
    
    def dayStarted(self, time):
        # delete all inpatients of the day
        self.inpatientsOfDay = []

        self.statistics.dayStarted(time)


    # Add the customer to the first place of the same priority in the waiting room
    def addCustomerToWaitingRoom(self, customer):
        self.waitingRoom.append(customer)

        i = len(self.waitingRoom) - 1
        while i > 0 and self.waitingRoom[i].priority < self.waitingRoom[i - 1].priority:
            self.waitingRoom[i], self.waitingRoom[i - 1] = self.waitingRoom[i - 1], self.waitingRoom[i]
            i -= 1

        self.statistics.customerArrivedToWaitingRoom()

        if len(self.waitingRoom) > 8:
            self.statistics.customersOutside += 1

    def canServe(self, time):        
        for machine in self.scanningMachines:
            if machine.canServe(time) and not self.isWaitingRoomEmpty():
                return True
        return False
    
    def isWaitingRoomEmpty(self):
        return len(self.waitingRoom) == 0
    
    def canInpatientTravel(self):
        return self.isWaitingRoomEmpty() and len(self.travelingInpatients) <= 1
    
    # to str print statistics
    def __str__(self):
        return str(self.statistics)
    