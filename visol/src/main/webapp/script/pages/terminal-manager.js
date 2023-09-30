import TokenManager from '../auth/token-manager.js';
import Employee from '../auth/employee.js';
import {el, elCC} from '../utils/elements.js';
import PlannerSchedule from '../components/planner-schedule.js';

class TerminalManager extends HTMLElement {
  constructor() {
    super();
  }

  async connectedCallback() {
    await TokenManager.authorizeUser(Employee.ROLE.terminal_manager);

    this.append(
        elCC('nav-bar', {
          role: 'manager',
          right: [
            el('export-button')
          ]
        }),
        el('planner-schedule', {
          terminalId: 1,
          view: PlannerSchedule.VIEW.daily
        }),
        el('error-toast')
    )
  }

}

customElements.define('terminal-manager', TerminalManager);
