import FullButton from './full-button.js';
import ExportModal from './modals/export-modal.js';

class ExportButton extends HTMLElement {
  constructor() {
    super();
    this.exportModal = new ExportModal();
    document.body.appendChild(this.exportModal);
    this.onclick = () => this.exportModal.show();
  }

  connectedCallback() {
    this.innerHTML = `
<full-button
  id="btn-change-port"
  icon="download"
  view="${FullButton.VIEW.secondary}"
  size="${FullButton.SIZE.large}">
  Export
</full-button>
`;
  }
}

customElements.define('export-button', ExportButton);

export default ExportButton;
