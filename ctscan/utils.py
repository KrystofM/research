from Time import Time

ALL_TIME_OPEN = {
    "Monday": [(Time(0), Time.fromHours(24))],
    "Tuesday": [(Time(0), Time.fromHours(24))],
    "Wednesday": [(Time(0), Time.fromHours(24))],
    "Thursday": [(Time(0), Time.fromHours(24))],
    "Friday": [(Time(0), Time.fromHours(24))],
    "Saturday": [(Time(0), Time.fromHours(24))], 
    "Sunday": [(Time(0), Time.fromHours(24))]     
}
WEEKDAY_OPEN = {
    "Monday": [(Time.fromHours(8), Time.fromHours(16))],
    "Tuesday": [(Time.fromHours(8), Time.fromHours(16))],
    "Wednesday": [(Time.fromHours(8), Time.fromHours(16))],
    "Thursday": [(Time.fromHours(8), Time.fromHours(16))],
    "Friday": [(Time.fromHours(8), Time.fromHours(16))],
    "Saturday": [],
    "Sunday": []
}
# generate an array of size 4*8 of False for each day of the week

def checkTimeWithinOpeningHours(time, openingHours):
    for (start, end) in openingHours[time.dayNameOfCurrentWeek()]:
        if start.hour() <= time.hour() < end.hour():
            return True
    return False 

# time is within opening hours
def timeTillClosing(time, openingHours):
    return openingHours[time.dayNameOfCurrentWeek()][0][1].minutesToday() - time.minutesToday()


# get the closest opening time to after the given time
def nextOpeningTime(time, openingHours):
    if checkTimeWithinOpeningHours(time, openingHours):
        return 0

    # get the day of the week
    day = time.dayOfCurrentWeek()
    # find the index and time of the next opening time
    # loop through all the following days
    for curDay in range(day, len(openingHours)):
        # loop through all the opening hours
        for (start, end) in openingHours[Time.WEEKDAYS[curDay]]:            
            if curDay == day and time.hour() < start.hour():                
                return start.minutesToday() - time.minutesToday()
            
            if curDay > day:                
                return (curDay - day) * 24 * 60 + start.minutesToday() - time.minutesToday() 
    
    timeTillEndOfWeek = (7 - day) * 24 * 60 - time.minutesToday()
    # loop through all the previous days
    for curDay in range(day):
        # loop through all the opening hours
        for (start, end) in openingHours[Time.WEEKDAYS[curDay]]:            
            return timeTillEndOfWeek + curDay * 24 * 60 + start.minutesToday()
            
# function that does tests for the function nextOpeningTime
def testNextOpeningTime():
    print("Testing nextOpeningTime")
    print("Expected: ", Time.fromHours(8).time() ," Actual: ", nextOpeningTime(Time.fromHours(0), WEEKDAY_OPEN))
    print("Expected: ", Time.fromHours(2).time() ," Actual: ", nextOpeningTime(Time.fromHours(6), WEEKDAY_OPEN))  
    print("Expected: 0, Actual: ", nextOpeningTime(Time.fromHours(14), WEEKDAY_OPEN))
    print("Expected: ", Time.fromHours(14).time() ," Actual: ", nextOpeningTime(Time.fromHours(18), WEEKDAY_OPEN))
    print("Expected: ", Time.fromHours(62).time() ," Actual: ", nextOpeningTime(Time.fromDaysHours(4, 18), WEEKDAY_OPEN))
