import Timestamp from '../utils/timestamp.js';

export default class DatetimeInput extends HTMLInputElement {
  constructor() {
    super();
    this.setAttribute('type', 'datetime-local');

    // Format
    this._format = 'YYYY-MM-DDThh:mm:ss';
    if (this.hasAttribute('no-seconds')) {
      this.setNoSeconds();
    }

    // Other attributes
    if (this.hasAttribute('now')) {
      this.value = new Date();
    }

    this.classList.add('form-control');
    this.classList.add('form-control-sm');
  }

  setNoSeconds () {
    this.setAttribute('step', '60');
    this._format = 'YYYY-MM-DDThh:mm';
  }

  get value() {
    try {
      return new Timestamp(super.value).toUTC().formatted();
    } catch (error) {
      return undefined;
    }
  }

  set value(timestamp) {
    super.value = new Timestamp(timestamp).toLocal().formatted(this._format);
  }
}

customElements.define('datetime-input', DatetimeInput, {extends: 'input'});
