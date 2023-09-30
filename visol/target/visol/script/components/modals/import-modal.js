import Modal from './modal.js'
import VisolApi from '../../api.js';
import {div} from '../../utils/elements.js'

class ImportModal extends Modal {
  constructor(parent) {
    super('Import a port', 'Import', parent)
  }

  buildBody() {
    const label = document.createElement('label');
    label.className = 'form-label';
    label.innerText = 'Exported port file (.ViSOL)';

    this.fileInput = document.createElement('input');
    this.fileInput.className = 'form-control form-control-sm'
    this.fileInput.type = 'file';
    this.fileInput.required = true;

    return div('', [label, this.fileInput] );
  }

  buildFooter() {
    return div('', [this.submitBtn]);
  }

  async submit() {
    const file = this.fileInput.files[0];
    const port = JSON.parse(await file.text());
    await VisolApi.importPort(port);
    await this.hide();
  }
}

customElements.define('import-modal', ImportModal);

export default ImportModal;
