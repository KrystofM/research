import PlannerSchedule from '../components/planner-schedule.js';
import {el, elCC} from '../utils/elements.js';
import TokenManager from '../auth/token-manager.js';
import Employee from '../auth/employee.js';

class VesselPlanner extends HTMLElement {
  constructor() {
    super();
  }

  async connectedCallback() {
    await TokenManager.authorizeUser(Employee.ROLE.vessel_planner);

    this.append(
      elCC('nav-bar', {
        role: 'planner',
        right: [
          el('export-button')
        ]
      }),
      el('planner-schedule', {
        view: PlannerSchedule.VIEW.daily
      }),
      el('error-toast')
    )
  }
}


customElements.define('vessel-planner', VesselPlanner);
