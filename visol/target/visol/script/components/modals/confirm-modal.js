import VisolApi from '../../api.js';
import Modal from './modal.js'
import {div} from '../../utils/elements.js';
import State from '../../auth/state.js';

export default class ConfirmPortDeleteModal extends Modal {
  constructor(parent) {
    super('Are you sure you want to delete this port?', 'Yes', parent);
  }

  buildBody() {
    const d = div('', []);
    d.innerHTML = `This will permanently delete all vessels and schedules in the port. 
    <b>It can not be undone!</b>`
    return d;
  }

  buildFooter() {
    const close = document.createElement('button');
    close.className = 'btn btn-secondary me-2';
    close.type = 'button';
    close.onclick = () => this.hide();
    close.innerText = 'Cancel';

    return div('d-inline-flex', [close, this.submitBtn])
  }

  async submit() {
    if (this.portId === undefined) {
      throw new Error('You have to set and id before opening the confirm modal.')
    }
    await VisolApi.deletePort(this.portId);
    if(this.portId === await State.getPortId()) {
      await State.setAnyPort();
    }
  }
}

customElements.define('confirm-modal', ConfirmPortDeleteModal);
