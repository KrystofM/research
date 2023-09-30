import FullButton from './full-button.js';
import State from '../auth/state.js';

class PrintButton extends HTMLElement {
  date;

  constructor() {
    super();
    this.onclick = async () => window.location.assign(
        `/print.html?terminal=${await State.getTerminalId()}&date=${this.date().formatted('YYYY-MM-DD')}`
    );
  }

  connectedCallback() {
    this.innerHTML = `
      <full-button
        id="btn-change-port"
        icon="print"
        view="${FullButton.VIEW.secondary}"
        size="${FullButton.SIZE.large}">
        Print
      </full-button>
    `;
  }
}

customElements.define('print-button', PrintButton);

export default PrintButton;
