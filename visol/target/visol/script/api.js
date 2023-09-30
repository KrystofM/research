import Requests from './requests.js';

class VisolApi {
  static getTerminalsPerPort = (portId) =>
    Requests.getData(`/ports/${portId}/terminals`);

  static getBerthsPerTerminal = (terminalId) =>
    Requests.getData(`/terminals/${terminalId}/berths`);

  static getSchedulesPerTerminal = (terminalId, from, to) =>
    Requests.getData(
        `/terminals/${terminalId}/schedules`,
        {},
        'from', from, 'to', to);

  static getInfeasibilityPerTerminal = (terminalId, from, to) =>
      Requests.getData(
          `/terminals/${terminalId}/schedules/valid`,
          {},
          'from', from, 'to', to,
      );

  static getVesselsPerTerminal = (terminalId) =>
    Requests.getData(`/terminals/${terminalId}/vessels`);

  static getUnscheduledPerTerminal = (terminalId) =>
    Requests.getData(`/terminals/${terminalId}/unscheduled`);

  static getVessel = (vesselId) => Requests.getData(`/vessels/${vesselId}`);

  static checkVesselExists = (vesselId) => Requests.getDataRaw(`/vessels/${vesselId}`).then((response) => {
    return response.ok;
  });

  static postVessel = (vessel, reason) =>
    Requests.postData('/vessels', vessel, {'Reason': reason});

  static getPerformancePerTerminal = (terminalId, from, to) => {
    const params = [];
    if (from !== undefined && from != null && from !== '') {
      params.push('from', from);
    }
    if (to !== undefined && to != null && to !== '') {
      params.push('to', to);
    }

    return Requests.getData(`/terminals/${terminalId}/performance`, {}, ...params);
  };

  static getPerformancePerPort = (portId, from, to) => {
    const params = [];
    if (from !== undefined && from != null && from !== '') {
      params.push('from', from);
    }
    if (to !== undefined && to != null && to !== '') {
      params.push('to', to);
    }

    return Requests.getData(`/ports/${portId}/performance`, {}, ...params);
  };

  static getChangesPerTerminal = (terminalId, from, to) => {
    const params = [];
    if (from) {
      params.push('from', from);
    }
    if (to) {
      params.push('to', to);
    }

    return Requests.getData(`/terminals/${terminalId}/changes`, {}, ...params);
  };

  static redoChange = () => Requests.postData('/changes/redo', {})

  static undoChange = () => Requests.postData('/changes/undo', {})

  static putVessel = (vesselId, vessel, reason) =>
    Requests.putData(`/vessels/${vesselId}`, vessel, {'Reason': reason});

  static deleteVessel = (vesselId) => Requests.deleteData(`/vessels/${vesselId}`);

  static putSchedule = (vesselId, schedule, reason) => {
    if (reason) {
      return Requests.putData(`/vessels/${vesselId}/schedule`, schedule, {'Reason': reason});
    } else {
      return Requests.putData(`/vessels/${vesselId}/schedule`, schedule);
    }
  }

  static optimiseSchedule = (terminalId, from, to, unscheduled, manual) => {
    const params = [];
    if (from) {
      params.push('from', from);
    }
    if (to) {
      params.push('to', to);
    }
    if (unscheduled) {
      params.push('unscheduled', unscheduled);
    }
    if (manual) {
      params.push('manual', manual);
    }

    return Requests.postData(`/terminals/${terminalId}/schedules/optimise`,
        {}, {}, false, ...params);
  }

  static getPorts = () =>
    Requests.getData(`/ports`);

  static exportPort (portId, from, to) {
    const params = [];
    if (from && from !== '') {
      params.push('from', from);
    }
    if (to && to !== '') {
      params.push('to', to);
    }
    return Requests.getData(`/ports/${portId}/export`, {}, ...params);
  }

  static getPort = (portId) => Requests.getData(`/ports/${portId}`)

  static importPort = (data) =>
    Requests.postData('/ports/import', data);

  static deletePort = (portId) =>
    Requests.deleteData(`/ports/${portId}`);

  static obtainToken = (email, password) => Requests.postData('/token/obtain', {
    'email': email,
    'password': password,
  }, {}, true);

  static verifyToken = () => Requests.getData('/token/verify');

  static getEmployee = (email) => Requests.getData(`/employees/${email}`);

  static getEmployeeGravatar = (email) => Requests.getData(`/employees/${email}/gravatar`);

  static getTerminal = (terminalId) => Requests.getData(`/terminals/${terminalId}`);

  static deleteSchedule = (vesselId) => Requests.deleteData(`/vessels/${vesselId}/schedule`);
}

export default VisolApi;
