import VisolApi from '../api.js';
import TokenManager from './token-manager.js';

class Employee {
  static ROLE = {
    vessel_planner: 'vessel planner',
    terminal_manager: 'terminal manager',
    port_authority: 'port authority',
    researcher: 'researcher'
  };

  static async getEmployee() {
    const token = TokenManager.parseToken();
    if (token == null) {
      // TODO Redirect to login page
      return null;
    } else {
      if (token?.employee && token.employee !== JSON.parse(sessionStorage.getItem("employeeCached"))?.email) {
        sessionStorage.setItem("employeeCached", JSON.stringify(await VisolApi.getEmployee(token.employee)));
      }
      return JSON.parse(sessionStorage.getItem("employeeCached"));
    }
  }
}

export default Employee;
