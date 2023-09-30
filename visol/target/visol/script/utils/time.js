import Timestamp from './timestamp.js';

class Time {
  constructor(value1, value2, value3) {
    if (value1 === undefined) {
      value1 = new Date();
    }

    if (typeof value1 === 'number') {
      this._hours = value1;
      this._minutes = value2 || 0;
      this._seconds = value3 || 0;
    } else if (typeof value1 === 'string') {
      const numbers = value1.split(':').map(Number);
      if (numbers.length < 1 || numbers.length > 3) {
        throw new Error('Invalid time string');
      }
      this._hours = numbers[0];
      this._minutes = numbers[1] || 0;
      this._seconds = numbers[2] || 0;
    } else if (value1 instanceof Date) {
      this._hours = value1.getUTCHours();
      this._minutes = value1.getUTCMinutes();
      this._seconds = value1.getUTCSeconds();
    } else if (value1 instanceof Timestamp) {
      this._hours = value1.hours;
      this._minutes = value1.minutes;
      this._seconds = value1.seconds;
    } else {
      throw new Error('Invalid time argument');
    }

    // Note that in PostgreSQL, 24:00:00 is considered to be 00:00:00.
    if (this.hours === 24) {
      // TODO make sure this is the correct solution
      this._hours = 0;
    }

    if (this._hours < 0 || this._hours > 23 ||
        this._minutes < 0 || this._minutes > 59 ||
        this._seconds < 0 || this._seconds > 59) {
      throw new Error(`Invalid time values: ${this._hours}:${this._minutes}:${this._seconds}`);
    }
  }

  get hours() {
    return this._hours;
  }

  get minutes() {
    return this._minutes;
  }

  get seconds() {
    return this._seconds;
  }

  get value() {
    return this.hours + this.minutes / 60 + this.seconds / 3600;
  }

  formatted(format = 'hh:mm:ss') {
    let result = format;
    result = result.replace('hh', this.hours.toString().padStart(2, '0'));
    result = result.replace('h', this.hours.toString());
    result = result.replace('mm', this.minutes.toString().padStart(2, '0'));
    result = result.replace('m', this.minutes.toString());
    result = result.replace('ss', this.seconds.toString().padStart(2, '0'));
    result = result.replace('s', this.seconds.toString());
    return result;
  }

  toLocal() {
    // Subtract the timezone offset
    const offset = new Date().getTimezoneOffset();
    const hours = offset / 60;
    const minutes = offset % 60;
    return new Time((this.hours - hours).mod(24), this.minutes - minutes, this.seconds);
  }

  toUTC() {
    // Add the timezone offset
    const offset = new Date().getTimezoneOffset();
    const hours = offset / 60;
    const minutes = offset % 60;
    return new Time((this.hours + hours).mod(24), this.minutes + minutes, this.seconds);
  }
}

export default Time;
