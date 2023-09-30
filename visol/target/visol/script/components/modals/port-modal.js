import Modal from './modal.js'
import VisolApi from '../../api.js'
import {div} from '../../utils/elements.js';
import ImportModal from './import-modal.js';
import ConfirmPortDeleteModal from './confirm-modal.js';
import State from '../../auth/state.js';

class PortModal extends Modal {
  constructor(onFinished) {
    super('Select port', '');
    this.onFinished = onFinished;

    this.importModal = new ImportModal(this);
    document.body.appendChild(this.importModal);

    this.confirmModal = new ConfirmPortDeleteModal(this);
    document.body.appendChild(this.confirmModal);
  }

  async buildBody() {
    this.ports = await VisolApi.getPorts();
    const selected = await State.getPortId();
    const rows = Object.keys(this.ports).map((id) => {
      const name = document.createElement('div');
      if(selected === id) {
        name.innerHTML = `<b>${this.ports[id].name}</b>`;
      } else {
        name.textContent = this.ports[id].name;
      }
      const btns = document.createElement('div');

      const btnDel = document.createElement('button');
      btnDel.className = 'btn btn-danger me-2';
      btnDel.type = 'button';
      btnDel.style = 'width: 38px; height: 38px';
      btnDel.onclick = () => {
        this.hide();
        this.confirmModal.portId = id;
        this.confirmModal.show();
      };
      btns.appendChild(btnDel);

      const ic = document.createElement('icon-plain');
      ic.setAttribute('name', 'trash');
      btnDel.appendChild(ic);

      const btnSelect = document.createElement('button');
      btnSelect.className = 'btn btn-primary';
      btnSelect.type = 'button';
      btnSelect.textContent = 'Select';
      btnSelect.onclick = async () => {
        await State.setPortId(id);
        this.onFinished()
        await this.hide();
        await this.refresh();
      }
      btns.appendChild(btnSelect);

      return div('d-flex justify-content-between mt-3', [
          name,
          btns,
          ]
      )
    });

    return div('', rows);
  }

  buildFooter() {
    const importBtn = document.createElement('button');
    importBtn.className = 'btn btn-primary';
    importBtn.innerText = 'ImPORT';
    importBtn.type = 'button';
    importBtn.onclick = async () => {
      await this.hide();
      await this.importModal.show();
    };
    return div('', [importBtn]);
  }

}

customElements.define('port-modal', PortModal);

export default PortModal;
