# from utils import *
# import math
# import numpy as np

# class PASHelper():
#     def __init__(self, capacity):
#         self.capacity = capacity
#         self.servicingCustomer = None
#         self.serviceStartTime = math.inf
#         self.serviceEndTime = math.inf
    
#     def canServe(self):
#         return self.servicingCustomer == None
    
#     def startService(self, time, customer):
#         assert self.canServe()
#         self.servicingCustomer = customer
#         self.serviceStartTime = time
#         self.serviceEndTime = self.nextServiceTime(time)
#         self.servicingCustomer.serviceStartTime = self.serviceStartTime
#         self.servicingCustomer.serviceEndTime = self.serviceEndTime
    
#     def endService(self):
#         assert self.servicingCustomer != None
#         self.servicingCustomer = None
#         self.serviceStartTime = math.inf
#         self.serviceEndTime = math.inf
    
#     def __str__(self):
#         return "PH(" + str(self.serviceStartTime) + "," + str(self.serviceEndTime) + ")"

# class SecretaryHelper(PASHelper):
#     SERVICE_TIME = 2.72 # in minutes

#     def __init__(self):
#         super().__init__()
#         self.type = "Secretary"

#     def nextServiceTime(self, time):
#         return time +  np.random.exponential(1 / SecretaryHelper.SERVICE_TIME)
    
#     def __str__(self):
#         return "SH(" + str(self.serviceStartTime) + "," + str(self.serviceEndTime) + ")"
    
#     def __repr__(self):
#         return self.__str__()

# class AnesthetistHelper(PASHelper):
#     SERVICE_TIME = 19.31 # in minutes

#     def __init__(self):
#         super().__init__()
#         self.type = "Anesthetist"

#     def nextServiceTime(self, time):
#         return time +  np.random.exponential(1 / SecretaryHelper.SERVICE_TIME)
    
#     def __str__(self):
#         return "AH(" + str(self.serviceStartTime) + "," + str(self.serviceEndTime) + ")"
    
#     def __repr__(self):
#         return self.__str__()

# class NurseHelper(PASHelper):
#     SERVICE_TIME = 25.32 # in minutes

#     def __init__(self):
#         super().__init__()
#         self.type = "Nurse"

#     def nextServiceTime(self, time):
#         return time +  np.random.exponential(1 / SecretaryHelper.SERVICE_TIME)
    
#     def __str__(self):
#         return "NH(" + str(self.serviceStartTime) + "," + str(self.serviceEndTime) + ")"
    
#     def __repr__(self):
#         return self.__str__()

# class AdditionalNurse(PASHelper):
#     SERVICE_TIME = 10.24 # in minutes

#     def __init__(self):
#         super().__init__()
#         self.type = "AdditionalNurse"

#     def nextServiceTime(self, time):
#         return time +  np.random.exponential(1 / SecretaryHelper.SERVICE_TIME)
    
#     def __str__(self):
#         return "AN(" + str(self.serviceStartTime) + "," + str(self.serviceEndTime) + ")"
    
#     def __repr__(self):
#         return self.__str__()

# def createHelper(PASHelperType):
#     if PASHelperType == "Secretary":
#         return SecretaryHelper()
#     elif PASHelperType == "Aneasthetist":
#         return AnesthetistHelper()
#     elif PASHelperType == "Nurse":
#         return NurseHelper()
#     elif PASHelperType == "AdditionalNurse":
#         return AdditionalNurse()
#     else:
#         raise ValueError("Unknown helper type: " + PASHelperType)