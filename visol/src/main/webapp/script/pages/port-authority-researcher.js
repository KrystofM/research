import PlannerSchedule from '../components/planner-schedule.js';
import TokenManager from '../auth/token-manager.js';
import Employee from '../auth/employee.js';
import {div, el, elCC} from '../utils/elements.js';
import VisolApi from '../api.js';
import State from '../auth/state.js';
import FullButton from '../components/full-button.js';
import PortModal from '../components/modals/port-modal.js';

class PortAuthorityResearcher extends HTMLElement {
  static terminalDropdownTag = 'chooseTerminal';
  schedules;
  role;

  constructor() {
    super();
  }

  async connectedCallback() {
    this.role = (await Employee.getEmployee()).role
    if (this.isResearcher()) {
      await TokenManager.authorizeUser(Employee.ROLE.researcher);
    } else {
      await TokenManager.authorizeUser(Employee.ROLE.port_authority);
    }

    this.schedules = div('schedules');

    const port = await State.getPort();
    await this.loadTerminals();
    const self = this;

    this.portChangeBtn = elCC('full-button', {
      id: 'btn-change-port',
      icon: 'anchor',
      view: FullButton.VIEW.secondary,
      size: FullButton.SIZE.large,
      textContent: port.name
    });

    this.append(
        elCC('nav-bar', {
          role: this.isResearcher() ? 'researcher' : 'authority',
          mid: [
            el('drop-down', {
              name: 'chooseTerminal',
              data: this.terminalData,
              active: this.terminalActiveId,
              size: FullButton.SIZE.large,
              callBack: (id) => self.changeTerminal(id)
            }),
            this.isResearcher() ? this.portChangeBtn : '',
          ],
          right: [
            el('export-button')
          ]
        }),
        this.schedules,
        el('error-toast')
    )

    if(this.isResearcher()) {
      const portModal = new PortModal();
      portModal.onFinished = () => {
        this.innerHTML = '';
        this.connectedCallback();
      }
      this.appendChild(portModal);
      document.getElementById('btn-change-port').onclick = () => portModal.show();
    }
  }

  isResearcher() {
    return this.role === Employee.ROLE.researcher;
  }

  async loadTerminals() {
    const portId = await State.getPortId();
    const terminals = await VisolApi.getTerminalsPerPort(portId);
    this.terminalData = {};
    this.terminalActiveId = undefined;
    Object.entries(terminals).forEach(([id, terminal]) => {
      this.terminalData[id] = terminal.name;
      if(!this.terminalActiveId) this.terminalActiveId = id;
    })
    await this.changeTerminal(this.terminalActiveId);
  }

  async changeTerminal(newTerminalId) {
    const schedules = this.schedules.children
    Object.values(schedules).forEach((schedule) => {
      schedule.removeAll();
    })
    await State.setTerminalId(newTerminalId);
    this.schedules.append(el('planner-schedule', {
      view: PlannerSchedule.VIEW.daily
    }))
  }
}


customElements.define('port-authority-researcher', PortAuthorityResearcher);
