import Time from './time.js';

class Timestamp {
  static monthNames = ['January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December',
  ];

  constructor(value1, value2, value3, value4, value5, value6) {
    if (value1 === undefined) {
      value1 = new Date();
    } else if (typeof value1 === 'number' && value2 === undefined) {
      value1 = new Date(value1);
    }

    if (typeof value1 === 'number') {
      if (value2 === undefined || value3 === undefined) {
        throw new Error('Invalid timestamp values, need at least 3');
      } else {
        this._year = value1;
        this._month = value2;
        this._day = value3;
        this._hours = value4 || 0;
        this._minutes = value5 || 0;
        this._seconds = value6 || 0;
      }
    } else if (typeof value1 === 'string') {
      const numbers = value1.split(/(?<!^)[-:TZ]/)
          .filter((s) => s.length > 0).map(Number);
      if (numbers.length < 3 || numbers.length > 6) {
        throw new Error('Invalid timestamp string');
      }
      this._year = numbers[0];
      this._month = numbers[1];
      this._day = numbers[2];
      this._hours = numbers[3] || 0;
      this._minutes = numbers[4] || 0;
      this._seconds = numbers[5] || 0;
    } else if (typeof value1 === 'object') {
      if (value1 instanceof Date) {
        this._year = value1.getUTCFullYear();
        this._month = value1.getUTCMonth() + 1;
        this._day = value1.getUTCDate();
        this._hours = value1.getUTCHours();
        this._minutes = value1.getUTCMinutes();
        this._seconds = value1.getUTCSeconds();
      } else if (value1.hasOwnProperty('year') && value1.hasOwnProperty('month') && value1.hasOwnProperty('day')) {
        this._year = value1.year;
        this._month = value1.month;
        this._day = value1.day;
        this._hours = value1.hours || 0;
        this._minutes = value1.minutes || 0;
        this._seconds = value1.seconds || 0;
      } else {
        throw new Error('Invalid timestamp object');
      }
    } else {
      throw new Error('Invalid timestamp argument');
    }

    // Note that in PostgreSQL, 24:00:00 is considered to be 00:00:00.
    if (this._month < 1 || this._month > 12 || this._day < 1 ||
        this._day > 31 ||
        this._hours < 0 || this._hours > 23 ||
        this._minutes < 0 || this._minutes > 59 ||
        this._seconds < 0 || this._seconds > 59) {
      throw new Error(`Invalid timestamp values: ${this._year}-${this._month}-${this._day}T${this._hours}:${this._minutes}:${this._seconds}`);
    }
  }

  get year() {
    return this._year;
  }

  get month() {
    return this._month;
  }

  get day() {
    return this._day;
  }

  set day(newVal) {
    this._day = newVal;
  }

  get hours() {
    return this._hours;
  }

  set hours(newVal) {
    this._hours = newVal;
  }

  get minutes() {
    return this._minutes;
  }

  set minutes(newVal) {
    this._minutes = newVal;
  }

  get seconds() {
    return this._seconds;
  }

  formatted(format = 'YYYY-MM-DDThh:mm:ssZ') {
    let result = format;
    result = result.replace('YYYY', this.year.toString());
    result = result.replace('MM', this.month.toString().padStart(2, '0'));
    result = result.replace('M', this.month.toString());
    result = result.replace('DD', this.day.toString().padStart(2, '0'));
    result = result.replace('D', this.day.toString());
    result = result.replace('hh', this.hours.toString().padStart(2, '0'));
    result = result.replace('h', this.hours.toString());
    result = result.replace('mm', this.minutes.toString().padStart(2, '0'));
    result = result.replace('m', this.minutes.toString());
    result = result.replace('ss', this.seconds.toString().padStart(2, '0'));
    result = result.replace('s', this.seconds.toString());
    return result;
  }

  dateFormat() {
    return this.day.toString() + ' ' + Timestamp.monthNames[this.month - 1];
  }

  toDate() {
    return new Date(Date.UTC(
        this.year,
        this.month - 1,
        this.day,
        this.hours,
        this.minutes,
        this.seconds,
    ));
  }

  clone() {
    return new Timestamp(this.toDate());
  }

  previousDate() {
    const previousDate = new Date(this.toDate());
    previousDate.setDate(this.day - 1);
    return previousDate;
  }

  nextDate() {
    const nextDate = new Date(this.toDate());
    nextDate.setDate(this.day + 1);
    return nextDate;
  }

  isToday() {
    return (new Timestamp()).sameDay(this);
  }

  sameDay(date) {
    return this.year === date.year &&
        this.month === date.month &&
        this.day === date.day;
  }

  add(timestamp) {
    if (!(timestamp instanceof Timestamp)) {
      if (timestamp instanceof Time) {
        return new Timestamp(this.toDate() + value * 60 * 60 * 1000);
      } else if (timestamp.hasOwnProperty('years') || timestamp.hasOwnProperty('months') || timestamp.hasOwnProperty('days') || timestamp.hasOwnProperty('hours') || timestamp.hasOwnProperty('minutes') || timestamp.hasOwnProperty('seconds')) {
        return new Timestamp(Date.UTC(
            this.year + (timestamp.years || 0),
            this.month + (timestamp.months || 0) - 1,
            this.day + (timestamp.days || 0),
            this.hours + (timestamp.hours || 0),
            this.minutes + (timestamp.minutes || 0),
            this.seconds + (timestamp.seconds || 0)
        ));
      } else {
        timestamp = new Timestamp(timestamp);
      }
    }
    return new Timestamp(this.toDate().getTime() + timestamp.toDate().getTime());
  }

  subtract(timestamp) {
    if (!(timestamp instanceof Timestamp)) {
      if (timestamp instanceof Time) {
        return new Timestamp(this.toDate() - value * 60 * 60 * 1000);
      } else if (timestamp.hasOwnProperty('years') || timestamp.hasOwnProperty('months') || timestamp.hasOwnProperty('days') || timestamp.hasOwnProperty('hours') || timestamp.hasOwnProperty('minutes') || timestamp.hasOwnProperty('seconds')) {
        return new Timestamp(Date.UTC(
            this.year - (timestamp.years || 0),
            this.month - (timestamp.months || 0) - 1,
            this.day - (timestamp.days || 0),
            this.hours - (timestamp.hours || 0),
            this.minutes - (timestamp.minutes || 0),
            this.seconds - (timestamp.seconds || 0)
        ));
      } else {
        timestamp = new Timestamp(timestamp);
      }
    }
    return new Timestamp(this.toDate().getTime() - timestamp.toDate().getTime());
  }

  toLocal() {
    // Subtract the timezone offset
    const date = this.toDate();
    const offset = new Date().getTimezoneOffset() * 60 * 1000;
    return new Timestamp(date.getTime() - offset);
  }

  toUTC() {
    // Add the timezone offset
    const date = this.toDate();
    const offset = new Date().getTimezoneOffset() * 60 * 1000;
    return new Timestamp(date.getTime() + offset);
  }
}

export default Timestamp;
