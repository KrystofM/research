import sys
from utils import *
from Time import Time


# --------------- basic event -----------------
class Event(object):
    """
    This basic class is used for all other event types
    --- redefine execute for every type of event
    --- add extra
    """

    def description(self):
        return 'do nothing'

    def execute(self):
        pass

    def __init__(self,tm):
        self.Time = tm

    # time in days hours:minutes
    def __str__(self):
        # time in days hours:minutes rounded to 0 de
        if self.Time != float("inf"):
            return '@' + str(Time(self.Time)) + ' ' + self.description()
        else:
            return self.description()


# --------------- standard events ----------------
class EndOfTime(Event):
    """ Error: the simulation did not end"""

    def description(self):
        return 'end of eventList'

    def execute(self):
        global stopSimulation
        print(' EMPTY EVENTLIST ')
        stopSimulation = True


class EndOfSimulationRun(Event):
    """ Time to collect statistics """

    def description(self):
        return 'end of simulation run'

    def execute(self):
        global stopSimulation
        stopSimulation = True

# --------------- standard variables ----------------
eventList = [EndOfTime(float("inf"))]

stopSimulation =  False
prevSimTime = -float("inf")
currSimTime= prevSimTime
currEvent= Event(currSimTime)

def showEventList(GetInput= True):
    """ default is to wait for input, give False as parameter to continue without waiting """
    print( '\n>>>show list ')
    for n in range(len(eventList)):
        print(eventList[n])
    if GetInput:  #to interrupt the program every time the list is shown
        inString= input('')
        if len(inString)>0 and (inString[0]=='s' or inString[0]=='S'):
            sys.exit(0)

def insertEvent(ev):
    up = len(eventList)-1
    dn = 0
    while dn<up:
        md = (dn+up)//2
        if eventList[md].Time <= ev.Time:
            dn = md+1
        else:
            up = md
    eventList.insert(dn,ev)
    
def executeBeforeEveryEvent():
    pass

def executeAfterEveryEvent():
    pass

def stopCriterium():
    return stopSimulation

def clearDES():
    global eventList, stopSimulation, prevSimTime, currSimTime, currEvent
    eventList = [EndOfTime(float("inf"))]
    stopSimulation =  False
    prevSimTime = -float("inf")
    currSimTime= prevSimTime
    currEvent= Event(currSimTime)

def runSimulation(StopCriterium= stopCriterium,
                  ExecuteBeforeEveryEvent=executeBeforeEveryEvent, 
                  ExecuteAfterEveryEvent=executeAfterEveryEvent):
    global eventList,stopSimulation, prevSimTime, currSimTime, currEvent
    prevSimTime = 0
    stopSimulation = False
    while not ( StopCriterium() ):
        currEvent= eventList.pop(0)
        currSimTime = currEvent.Time
        ExecuteBeforeEveryEvent()
        currEvent.execute()       
        ExecuteAfterEveryEvent()
        prevSimTime = currSimTime
# ----------------------- end general structure ----------------

