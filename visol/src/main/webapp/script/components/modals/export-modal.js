import Modal from './modal.js';
import VisolApi from '../../api.js';
import {div, elCC} from '../../utils/elements.js';
import DatetimeInput from '../datetime-input.js';
import sanitize from '../../utils/sanitize.js';

class ExportModal extends Modal {
  constructor() {
    super('Export port', 'Export');
  }

  buildBody() {
    this.from = new DatetimeInput();
    this.from.setNoSeconds();
    this.to = new DatetimeInput();
    this.to.setNoSeconds();

    this.name = elCC('input', {}, 'form-control form-control-sm', []);

    return div('', [
      this.labeled('Name', this.name, 'mb-3', true),
      div('d-inline-flex align-content-stretch w-100', [
        this.labeled('From', this.from, 'w-100 me-3'),
        this.labeled('To', this.to, 'w-100'),
      ]),
    ]);
  }

  buildFooter() {
    return this.submitBtn;
  }

  async submit() {
    const res = await VisolApi.exportPort(VisolApi.portId, this.from.value, this.to.value);
    res.name = sanitize(this.name.value);
    const fileName = this.name.value + '.ViSOL';
    const file = new File([JSON.stringify(res)], fileName, {type: 'text/plain',});

    const link = document.createElement('a');
    const url = URL.createObjectURL(file);
    link.href = url;
    link.download = file.name;
    document.body.appendChild(link);
    link.click();

    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  }
}

customElements.define('export-modal', ExportModal);

export default ExportModal;
