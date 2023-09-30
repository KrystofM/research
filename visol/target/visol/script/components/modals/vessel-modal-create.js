import VisolApi from '../../api.js';
import VesselModal from './vessel-modal.js';

export default class VesselModalCreate extends VesselModal {
  footerBtns = [this.submitBtn];
  hasSchedule = false;
  constructor(closeCallback) {
    super('create', 'Create vessel')
    this.closeCallback = closeCallback;
  }
  vesselApiEndpoint = VisolApi.postVessel;
  getId = (response) => response.headers.get('Location').split('/').slice(-1)[0];

}

customElements.define('vessel-modal-create', VesselModalCreate);
