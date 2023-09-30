from Time import Time

class Schedule:
    def __init__(self):
        self.schedule = [[False] * 4 * 8, [False] * 4 * 8, [False] * 4 * 8, [False] * 4 * 8, [False] * 4 * 8]
        self.startTime = Time.fromHours(8)

    def scheduleOutpatient(self, fromTime):
        # get the time to the earliest schedule spot
        (timeToEarliestScheduleSpot, day, timeSlot) = self.getTimeToEarliestScheduleSpot(fromTime)
        if timeToEarliestScheduleSpot == None:
            return None
        # schedule the customer for the time slot
        self.scheduleOutpatientForTimeSlot(day, timeSlot)
        # set the scheduled time of the customer
        return timeToEarliestScheduleSpot

     # Schedule an outpatient customer at 15-minute intervals in hours 8 to 16 (i.e., 8:00, 8:15, 8:30, 8:45, 9:00, 9:15, etc.)
    # Schedule has to be after the request time of the customer
    def getTimeToEarliestScheduleSpot(self, fromTime):
        # get the day of the week
        requestTime = Time(fromTime)
        day = requestTime.dayOfCurrentWeek()
        if fromTime == 0:
            day = -1
        # loop throught all days following day in the schedule and find the first available time
        for curDay in range(day + 1, len(self)):
            for i in range(len(self[curDay])):
                # if the time is available
                if not self[curDay][i]:
                    # return the overall time                    
                    return (self.computeTimeToSchedule(requestTime, curDay, i), curDay, i)
        
        # if no time is available return None
        return (None, None, None)
    
    def computeTimeToSchedule(self, requestTime, scheduleDay, timeSlotIndex):
        return (scheduleDay - requestTime.dayOfCurrentWeek() - 1) * 24 * 60 + timeSlotIndex * 15 + requestTime.minutesLeftInDay() + self.startTime.time()
    
    def scheduleOutpatientForTimeSlot(self, day, timeSlot):        
        # schedule the time slot
        self[day][timeSlot] = True        
        # get index of slot within the hour
        slotIndex = timeSlot % 4
        # get the indexes of available slots within the same hour
        availableSlots = [i for i in range(4) if not self[day][timeSlot - slotIndex + i]]
        # if time between 12 and 16 and only 1 more slot available
        # print("Time of schedule in hours: ", ((timeSlot * 15 + self.startTime.time()) / 60))
        if 12 <= ((timeSlot * 15 + self.startTime.time()) / 60) < 16 and len(availableSlots) == 1:
            # make the last slot unavailable
            # print("\n\n!Making last slot unavailable!\n\n")
            self[day][timeSlot + 1] = True

        # print(str(self))

    # create getitem by taking index of self.schedule
    def __getitem__(self, index):
        return self.schedule[index]
    
    # create setitem by taking index of self.schedule
    def __setitem__(self, index, value):
        self.schedule[index] = value

    # length
    def __len__(self):
        return len(self.schedule)
    
    def __str__(self):        
        string = "Outpatient Schedule"
        for day in range(len(self.schedule)):
            string += "\n" + Time.WEEKDAYS[day] + ": "
            for timeSlot in range(len(self.schedule[day])):
                if self.schedule[day][timeSlot]:
                    string += "X"
                else:
                    string += "_"
        return string