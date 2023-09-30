import VisolApi from '../api.js';
import Employee from './employee.js';

class State {
  static async getPort() {
    let portId = await State.getPortId();
    return portId != null ? VisolApi.getPort(portId) : null;
  }

  static async getPortId() {
    let employee = await Employee.getEmployee();
    if (!employee) {
      return null;
    }
    let portId;
    if (employee.role === Employee.ROLE.vessel_planner || employee.role === Employee.ROLE.terminal_manager) {
      portId = null;
    } else if (employee.role === Employee.ROLE.port_authority) {
      portId = employee?.port;
    } else if (employee.role === Employee.ROLE.researcher) {
      portId = localStorage.getItem('portId');
      if (portId === null) { // set the first port as the selected one
        await State.setAnyPort();
        portId = localStorage.getItem('portId');
      }
    }
    return portId;
  }

  static async setAnyPort() {
    // TODO just show a selector
    const portIds = Object.keys(await VisolApi.getPorts());
    if (portIds.length === 0) {
      throw new Error('There are not ports in the DB!');
    }
    await this.setPortId(portIds[0]);
  }

  static async setPortId(portId) {
    let employee = await Employee.getEmployee();
    if (employee == null) {
      return;
    }
    if (employee.role === Employee.ROLE.vessel_planner || employee.role === Employee.ROLE.terminal_manager || employee.role === Employee.ROLE.port_authority) {
      throw new Error('Cannot set port ID for vessel planner, terminal manager, or port authority');
    } else if (employee.role === Employee.ROLE.researcher) {
      localStorage.setItem('portId', portId);
    }
  }

  static async getTerminal() {
    let terminalId = await State.getTerminalId();
    return terminalId != null ? VisolApi.getTerminal(terminalId) : null;
  }

  static async getTerminalId() {
    let employee = await Employee.getEmployee();
    if (!employee) {
      return null;
    }
    let terminalId;
    if (employee.role === Employee.ROLE.vessel_planner || employee.role === Employee.ROLE.terminal_manager) {
      terminalId = employee?.terminal;
    } else if (employee.role === Employee.ROLE.port_authority || employee.role === Employee.ROLE.researcher) {
      terminalId = localStorage.getItem('terminalId');
      if (terminalId === null) { // set the first terminal as the selected one
        await State.setAnyTerminal();
        terminalId = localStorage.getItem('terminalId');
      }
    }
    return terminalId;
  }

  static async setAnyTerminal() {
    // TODO just show a selector
    const terminalIds = Object.keys(await VisolApi.getTerminalsPerPort(await State.getPortId()));
    if (terminalIds.length === 0) {
      throw new Error('There aren\'t any terminals for your port in the DB!');
    }
    await this.setTerminalId(terminalIds[0]);
  }

  static async setTerminalId(terminalId) {
    let employee = await Employee.getEmployee();
    if (employee == null) {
      return;
    }
    if (employee.role === Employee.ROLE.vessel_planner || employee.role === Employee.ROLE.terminal_manager) {
      throw new Error('Cannot set terminal ID for vessel planner or terminal manager');
    } else if (employee.role === Employee.ROLE.port_authority || employee.role === Employee.ROLE.researcher) {
      localStorage.setItem('terminalId', terminalId);
    }
  }
}

export default State;
