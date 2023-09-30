# -*- coding: utf-8 -*-
"""
Created on Sun May  3 10:01:05 2020

@author: Jan-Kees
"""

from System import System
import DiscreteEventSimulation as DES
from Customer import *
from utils import *
from Time import Time

def StartService(Time, customer):
    system.customerStartService(Time, customer)
    DES.insertEvent(EndService(customer.serviceEndTime, customer))

# Request event generates other request events with the same customer type based on the request rates of the different customer types
# It calls a system function to get the time of arrival for the customer type with the given request and schedules an Arrival event
class Request(DES.Event):
    def __init__(self, tm, customer):
        self.Time = tm
        self.customer = customer

    def description(self):
        return 'request: ' + str(self.customer)

    def execute(self):          
        self.customer.requestTime = self.Time

        if isinstance(self.customer, InpatientCustomer):
            system.inpatientsOfDay.append(self.customer)
            if system.canInpatientTravel():
                DES.insertEvent(TravelInpatient(DES.currSimTime, self.customer))
                return
            else:
                system.inpatientWaits(self.customer)
                return        

        # Schedule next request event for the customer of the same type
        DES.insertEvent(Request(self.Time + self.customer.nextRequestTime(self.Time), createCustomer(self.customer.type, self.customer.index + 1)))
        
        if isinstance(self.customer, EmergencyCustomer) or isinstance(self.customer, PASCustomer):
            DES.insertEvent(Arrival(self.customer.requestTime, self.customer))
            return
        
        if isinstance(self.customer, OutpatientCustomer):
            system.waitingOutpatients.append(self.customer)
            return
        

class FakeRequest(DES.Event):
    def __init__(self, tm, customer):
        self.Time = tm
        self.customer = customer
    
    def description(self):
        return 'fake request: ' + str(self.customer)
    
    def execute(self):
        # The Phony arrival turns real probabilistically
        if inpatient_time_to_next_event(self.Time, InhomogeneousPoisson.lambda_I_opt, InhomogeneousPoisson.peak_opt):
            DES.insertEvent(Request(self.Time, self.customer))

        # Schedule next request event for the customer of the same type
        DES.insertEvent(FakeRequest(self.Time + self.customer.nextRequestTime(self.Time), createCustomer(self.customer.type, self.customer.index + 1)))
    
class Arrival(DES.Event):
    def __init__(self, tm, customer):
        self.Time = tm
        self.customer = customer

    def description(self):
        return 'arrival: ' + str(self.customer)

    def execute(self):        
        self.customer.arrivalTime = self.Time
        # if its outpatient there is p probability they show up
        if isinstance(self.customer, OutpatientCustomer):
            system.statistics.outpatientsRequested += 1
            system.statistics.accessTimeOutpatientsSum += self.customer.arrivalTime - self.customer.requestTime    
            if random.random() > OutpatientCustomer.P_SHOW_UP:
                return                     
        
        if isinstance(self.customer, InpatientCustomer):
            system.travelingInpatients.remove(self.customer)
        
        if isinstance(self.customer, PASCustomer):            
            startSecretaryDesk(self.Time, self.customer)
            return

        system.addCustomerToWaitingRoom(self.customer)

        # When nobody is being serviced we start service immediately
        if system.canServe(self.Time):
            nextCustomer = system.getNextCustomer()
            StartService(self.Time, nextCustomer) 

class EndService(DES.Event):
    def __init__(self, tm, customer):
        self.Time = tm
        self.customer = customer

    def description(self):
        return 'end service: ' + str(self.customer)

    def execute(self):
        system.customerEndService(self.Time, self.customer)

        # After a customer's scan has ended, check if there are any inpatients waiting
        # Check if the conditions to call in an inpatient are met (waiting room is empty and at most one inpatient traveling)
        if len(system.waitingInpatients) > 0 and system.canInpatientTravel():            
            # Call the first inpatient from the waiting list
            inpatient_to_call = system.waitingInpatients.pop(0)
            # Add an InpatientCall event to the event queue
            DES.insertEvent(TravelInpatient(DES.currSimTime, inpatient_to_call))

        # Can serve more, start service        
        if system.canServe(self.Time):            
            nextCustomer = system.getNextCustomer()
            StartService(self.Time, nextCustomer)   

        stopC.stop()

def startSecretaryDesk(time, customer):
    system.statistics.customerArrivedToWaitingRoom()
    if system.canAddToSecretary():
        system.addSecretary(customer)
        DES.insertEvent(SecretaryDeskEnd(time + SecretaryDeskEnd.serviceTime(), customer))
    else:
        system.secretaryWaitingRoom.append(customer)

class SecretaryDeskEnd(DES.Event):
    SERVICE_TIME = 2.72 # in minutes

    def __init__(self, tm, customer):
        self.Time = tm
        self.customer = customer
    
    def description(self):
        return 'end secretary desk: ' + str(self.customer)
    
    # exponentially distributed
    def serviceTime():
        return np.random.exponential(SecretaryDeskEnd.SERVICE_TIME)
    
    def execute(self):
        # remove the customer from the secretary desk
        system.removeSecretary(self.customer)

        # with 25% chance schedule a CPM End event and with 75% chance schedule a Consultation End event
        if random.random() < 0.25:
            self.customer.didCPMFirst = True
            startCPM(self.Time, self.customer)
        else:
            self.customer.didCPMFirst = False
            startConsultation(self.Time, self.customer)

        # somebody in queue start serving them
        if system.canAddToSecretary() and len(system.secretaryWaitingRoom) > 0:
            nextCustomer = system.secretaryWaitingRoom.pop(0)
            startSecretaryDesk(self.Time, nextCustomer)

def startCPM(time, customer):
    system.statistics.customerArrivedToWaitingRoom()
    if system.canAddToCPM():
        system.addCPM(customer)
        DES.insertEvent(CPMEnd(time + CPMEnd.serviceTime(), customer))
    else:
        system.cpmWaitingRoom.append(customer)

class CPMEnd(DES.Event):
    SERVICE_TIME = 25.32 # in minutes

    def __init__(self, tm, customer):
        self.Time = tm
        self.customer = customer

    def description(self):
        return 'end CPM: ' + str(self.customer)
    
    def serviceTime():
        return np.random.exponential(CPMEnd.SERVICE_TIME)
    
    def execute(self):
        system.removeCPM(self.customer)

        if self.customer.didCPMFirst:
            startConsultation(self.Time, self.customer)
        
        if self.customer.doesBothCPMAndAdditionalTest:    
            startAdditionalTest(self.Time, self.customer)

        # somebody in queue start serving them
        if system.canAddToCPM() and len(system.cpmWaitingRoom) > 0:
            nextCustomer = system.cpmWaitingRoom.pop(0)
            startCPM(self.Time, nextCustomer)     

def startConsultation(time, customer):
    system.statistics.customerArrivedToWaitingRoom()
    if system.canAddToConsultation():
        system.addConsultation(customer)
        DES.insertEvent(ConsultationEnd(time + ConsultationEnd.serviceTime(), customer))
    else:
        system.consultationWaitingRoom.append(customer)

class ConsultationEnd(DES.Event):
    SERVICE_TIME = 19.31     # in minutes

    def __init__(self, tm, customer):
        self.Time = tm
        self.customer = customer

    def description(self):
        return 'end Consultation: ' + str(self.customer)
    
    def serviceTime():
        return np.random.exponential(ConsultationEnd.SERVICE_TIME)
    
    def execute(self):
        system.removeConsultation(self.customer)

        # somebody in queue start serving them
        if system.canAddToConsultation() and len(system.consultationWaitingRoom) > 0:
            nextCustomer = system.consultationWaitingRoom.pop(0)
            startConsultation(self.Time, nextCustomer)

        if self.customer.didCPMFirst:
            if random.random() < 0.25:
                startAdditionalTest(self.Time, self.customer)
                return
            else:                
                return
        else:
            # 72% will leave, 7% will do CPM, 17% will do additional test, 4% will do both
            decision = random.random()
            if decision < 0.72:
                return
            elif decision < 0.72 + 0.07:
                startCPM(self.Time, self.customer)
                return
            elif decision < 0.72 + 0.07 + 0.17:
                startAdditionalTest(self.Time, self.customer)
                return
            else:
                self.customer.doesBothCPMAndAdditionalTest = True
                startCPM(self.Time, self.customer)                
                return


def startAdditionalTest(time, customer):
    system.statistics.customerArrivedToWaitingRoom()
    if system.canAddToAdditionalTest():
        system.addAdditionalTest(customer)
        DES.insertEvent(AdditionalTestEnd(time + AdditionalTestEnd.serviceTime(), customer))
    else:
        system.additionalTestWaitingRoom.append(customer)

class AdditionalTestEnd(DES.Event):
    SERVICE_TIME = 10.24 # in minutes

    def __init__(self, tm, customer):
        self.Time = tm
        self.customer = customer

    def description(self):
        return 'end Additional test: ' + str(self.customer)
    
    def serviceTime():
        return np.random.exponential(AdditionalTestEnd.SERVICE_TIME)
    
    def execute(self):
        system.removeAdditionalTest(self.customer)

        # somebody in queue start serving them
        if system.canAddToAdditionalTest() and len(system.additionalTestWaitingRoom) > 0:
            nextCustomer = system.additionalTestWaitingRoom.pop(0)
            startAdditionalTest(self.Time, nextCustomer)
        

class StartOfDay(DES.Event):
    def __init__(self, tm):
        self.Time = tm

    def description(self):
        return 'start of day: ' + str(Time(self.Time).day())
    
    def execute(self):
        # system.startOfDay(self.Time)
        DES.insertEvent(StartOfDay(self.Time + 24 * 60))
        
        system.dayStarted(self.Time)

        # look at the day and based on opening hours schedule start and end of opening hours
        for (start, end) in system.openHours[Time(self.Time).dayNameOfCurrentWeek()]:
            DES.insertEvent(StartOfOpeningHours(self.Time + start.time()))
            DES.insertEvent(EndOfOpeningHours(self.Time + end.time()))

class StartOfOpeningHours(DES.Event):
    def __init__(self, tm):
        self.Time = tm

    def description(self):
        return 'start of opening hours: ' + str(Time(self.Time).day())
    
    def execute(self):
        # if we can serve a customer start service
        if system.canServe(self.Time):
            nextCustomer = system.getNextCustomer()
            StartService(self.Time, nextCustomer)
        
        # schedule outpatients that are waiting
        for i in range(len(system.scanningMachines) - 1):
            length = min(32, len(system.waitingOutpatients))
            for i in range(length):            
                scheduleTime = Time(self.Time + 15*i)
                if scheduleTime.hour() >= 12 and scheduleTime.minute() == 45:
                    continue
                customer = system.waitingOutpatients.pop(0)
                DES.insertEvent(Arrival(self.Time + 15*i, customer))
        

class EndOfOpeningHours(DES.Event):
    def __init__(self, tm):
        self.Time = tm

    def description(self):  
        return 'end of opening hours: ' + str(Time(self.Time).day())
    
    def execute(self):        
        system.openingHoursEnded()
        return

class TravelInpatient(DES.Event):
    def __init__(self, time, customer):
        self.Time = time
        self.customer = customer

    def description(self):
        return 'travelling ' + str(self.customer)

    def execute(self):
        system.travelingInpatients.append(self.customer)
        DES.insertEvent(Arrival(self.Time + self.customer.transferTimeFunction(), self.customer))

def BeforeEvent():
    return

def AfterEvent():    
    # print("Time: " + str(Time(DES.currSimTime)))
    # print(repr(system))
    # print if the first machine is busy
    # print("Machine 1 is busy: ", system.scanningMachines[0].servicingCustomer != None)
    # print if the second machine is busy
    # print("Machine 2 is busy: ", system.scanningMachines[1].servicingCustomer != None)   
    # print the waiting room
    # print("Waiting room: ", system.waitingRoom) 
    """ print("Waiting room secretaries size: " + str(len(system.secretaryWaitingRoom)))
    print("Patients in secretary: " + str(system.patientsSecretary))
    print("Waiting room CPM size: " + str(len(system.cpmWaitingRoom)))
    print("Patients in CPM: " + str(system.patientsCPM))
    print("Waiting room consultation size: " + str(len(system.consultationWaitingRoom)))
    print("Patients in consultation: " + str(system.patientsConsultation))
    print("Waiting room additional test size: " + str(len(system.additionalTestWaitingRoom)))
    print("Patients in additional test: " + str(system.patientsAdditionalTest)) """
    # DES.showEventList()
    return


class StopAfterTime:
    def __init__(self, time):
        self.time = time

    def stop(self):
        DES.stopSimulation = DES.currSimTime >= self.time


def runSimulation(stopCriterium=StopAfterTime(Time.fromWeeks(250).time())):
    global stopC, system, lastTime
    lastTime = 0
    stopC=stopCriterium
    system=System()

    # We start the simulation at midnight Monday morning and run simulation in minutes
    # First emergency request is exacly at midnight
    DES.insertEvent(Request(np.random.exponential(1 / EmergencyCustomer.ARRIVAL_RATE), createCustomer("Emergency", 0)))
    # First inpatient request is exactly at the start of open hours 8am on Monday
    DES.insertEvent(FakeRequest(0, createCustomer("Inpatient", 0)))
    # First outpatient request is exactly at the start of open hours 8am on Monday
    DES.insertEvent(Request(8 * 60, createCustomer("Outpatient", 0)))

    #First pas patient request is exactly at the start of the day
    DES.insertEvent(Request(np.random.exponential(1 / PASCustomer.CALL_RATE), createCustomer("PAS", 0)))

    DES.insertEvent(StartOfDay(0))
    DES.runSimulation(ExecuteAfterEveryEvent= AfterEvent, ExecuteBeforeEveryEvent= BeforeEvent)
    DES.clearDES()
    print(system)
    return system

runSimulation()
