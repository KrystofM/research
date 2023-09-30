# class that represents time and base is in minutes
class Time:
    WEEKDAYS = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]

    # from minutes
    def __init__(self, minute):
        self.minutes = minute

    # from hours
    def fromHours(hour):
        return Time(hour * 60)
    
    def fromDaysHours(days, hours):
        return Time(days * 24 * 60 + hours * 60)
    
    def fromDays(days):
        return Time(days * 24 * 60)
    
    def fromWeeks(weeks):
        return Time(weeks * 7 * 24 * 60)
    
    def time(self):
        return self.minutes
    
    def minutesToday(self):
        return self.minutes % (24 * 60)

    def minute(self):
        return self.minutes % 60

    def hour(self):
        return self.minutes // 60 % 24
    
    def day(self):
        return self.minutes // (24 * 60)
    
    def dayExact(self):
        return self.minutes / (24 * 60)
    
    def dayOfCurrentWeek(self):
        return int(self.day() % 7)
    
    def dayNameOfCurrentWeek(self):
        return Time.WEEKDAYS[self.dayOfCurrentWeek()]
    
    def minutesLeftInDay(self):
        return 24 * 60 - self.minutesToday()
    
    def minutesLeftInWeek(self):
        return 7 * 24 * 60 - self.minutes
    
    # override the + operator
    def __add__(self, other):
        return Time(self.minutes + other.minutes)
    
    # override the - operator
    def __sub__(self, other):
        return Time(self.minutes - other.minutes)
    
    # override the < operator
    def __lt__(self, other):
        return self.minutes < other.minutes
    
    # override the <= operator
    def __le__(self, other):
        return self.minutes <= other.minutes
    
    # override the > operator
    def __gt__(self, other):
        return self.minutes > other.minutes
    
    # override the >= operator
    def __ge__(self, other):
        return self.minutes >= other.minutes
    
    # override the == operator
    def __eq__(self, other):
        return self.minutes == other.minutes
    
    # override the != operator
    def __ne__(self, other):
        return self.minutes != other.minutes    
    
    def __str__(self):
        if self.minutes != float("inf"):
            return str(int(self.day())) + ":" + str(int(self.hour())) + ":" + str(int(self.minute()))
        else:
            return "inf"

    
