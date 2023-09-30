import VisolApi from '../../api.js';
import VesselModal from './vessel-modal.js';
import {elCC} from '../../utils/elements.js';

export default class VesselModalUpdate extends VesselModal {
  vesselId;

  constructor(closeCallback) {
    super('update', 'Update vessel')

    this.closeCallback = closeCallback;
    const ic = document.createElement('icon-plain');
    ic.setAttribute('name', 'trash');
    this.footerBtns = [
      elCC('button', {type: 'button', style: 'width: 38px; height: 38px', onclick: () => this.deleteVessel()}, 'btn btn-danger me-3', [
        ic
      ]),
      this.submitBtn
    ];
  }

  async deleteVessel () {
    this.startLoading();
    await VisolApi.deleteVessel(this.vesselId);
    await this.stopLoading();
    await this.hide();
  }

  vesselApiEndpoint = (vessel, reason) => {
    if (this.vesselId === null) {
      throw new Error('You have to set a vesselId when opening an update form.');
    }
    return VisolApi.putVessel(this.vesselId, vessel, reason);
  };

  getId = () => this.vesselId;

  fillIn(object) {
    // eslint-disable-next-line guard-for-in
    for (const key in object) {
      try {
        this.getElement(`form-${key}`).value = object[key];
      } catch (_) {
        console.log(`Warning: ${key} has no value in the form.`);
      }
    }
  }

  setVessel(vessel, vesselId) {
    this.fillIn(vessel);
    this.vesselId = vesselId;
  }

  setSchedule(schedule) {
    if (!schedule || !('manual' in schedule)) {
      this.getElement('radio-unscheduled').click();
      this.hasSchedule = false;
    } else {
      this.hasSchedule = true;
      this.getElement(`form-berth`).value = schedule['berth'];
      this.getElement('form-start').value = schedule['start'];
      this.getElement(`radio-${schedule['manual'] ? 'manual' : 'auto'}`).click();
    }
  }
}

customElements.define('vessel-modal-update', VesselModalUpdate);
