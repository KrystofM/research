import PrintSchedule from '../components/print-schedule.js';
import {el} from '../utils/elements.js';
import TokenManager from '../auth/token-manager.js';
import Employee from '../auth/employee.js';
import Timestamp from '../utils/timestamp.js';

class PrintPage extends HTMLElement {
  terminal;
  date;

  constructor() {
    super();
    const queryParameters = new URLSearchParams(window.location.search);
    this.terminal = queryParameters.get('terminal');
    this.date = queryParameters.get('date');
    if (!this.terminal || !this.date) {
      window.location.assign('/error/406.html');
    }
  }

  async connectedCallback() {
    // TODO proper authorization, so not only one specific role but also all roles of a higher level
    //  await TokenManager.authorizeUser(Employee.ROLE.vessel_planner);

    this.append(
        el('print-schedule', {
          view: PrintSchedule.VIEW.daily,
          terminal: this.terminal,
          date: new Timestamp(this.date)
        })
    );

    setTimeout(() => {
      window.print();
    }, 3 * 1000);
  }
}


customElements.define('print-page', PrintPage);
