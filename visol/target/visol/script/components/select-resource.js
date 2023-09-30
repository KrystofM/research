import VisolApi from '../api.js';
import State from '../auth/state.js';
import Employee from '../auth/employee.js';

class SelectResource extends HTMLSelectElement {
  resourcePromise;
  displayAttribute;

  constructor(displayAttribute) {
    super();
    this.displayAttribute = displayAttribute;
  }

  setResourcePromise(resourcePromise) {
    this.innerHTML = ``;
    this.resourcePromise = resourcePromise;
    this.resourcePromise.then((resource) => {
      this.innerHTML = this.buildSelect(resource);
      this.dispatchEvent(new Event('change'));
    }).catch((err) => console.log('Error fetching resource for the selector: ', err));
  }

  buildSelect(resource) {
    return Object.keys(resource).map((i) =>
      `<option value="${i}">${this.displayAttribute === undefined ? i :
            resource[i][this.displayAttribute]}</option>`).join('\n');
  }
}

class SelectTerminal extends SelectResource {
  constructor() {
    super('name', 'destination');
    this.setResourcePromise((async () => {
      const employee = await Employee.getEmployee();
      if (employee.role === 'vessel planner' || employee.role === 'terminal manager') {
        return {
          [await State.getTerminalId()]: await State.getTerminal()
        }
      } else if (employee.role === 'port authority' || employee.role === 'researcher') {
        return VisolApi.getTerminalsPerPort(await State.getPortId())
      }
    })());
  }
}

class SelectBerth extends SelectResource {
  constructor() {
    super(undefined, 'berth');
    this.setResourcePromise(State.getTerminalId().then((terminalId) => VisolApi.getBerthsPerTerminal(terminalId)));
  }

  async setTerminal(i) {
    this.setResourcePromise(VisolApi.getBerthsPerTerminal(i));
  }
}

customElements.define('select-terminal', SelectTerminal, {extends: 'select'});
customElements.define('select-berth', SelectBerth, {extends: 'select'});

export default SelectResource;
