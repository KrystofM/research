import TokenManager from '../auth/token-manager.js';

import VisolApi from '../api.js';
import PortModal from '../components/modals/port-modal.js';
import ImportModal from '../components/modals/import-modal.js';

class LandingPage extends HTMLElement {
  constructor() {
    super();
  }

  async connectedCallback() {
    this.innerHTML = `
<nav-bar></nav-bar>
    <div class="d-flex flex-column justify-content-center align-items-center">
      <h1 class="text-center">
        Welcome to the ViSOL Project!
      </h1>

      <p class="text-center">
        Berth control? You should not neglect it. </br>
        Please choose your role:
      </p>

      <div class="d-grid gap-2 col-5">
        <a class="btn btn-primary btn-dark" href="./vessel_planner.html" type="button">Vessel Planner</a>
        <a class="btn btn-primary btn-dark" href="./terminal_manager.html" type="button">Terminal Manager</a>
        <a class="btn btn-primary btn-dark" href="./port_authority.html" type="button">Port Authority</a>
        <a class="btn btn-primary btn-dark" href="./researcher.html" type="button">Researcher</a>
      </div>
    </div>
    <error-toast></error-toast>`;

    // TODO in a proper place
    TokenManager.obtainToken('jdoe@tormails.com', 'R2VVCYYW-jghZ:e').then((success) => {
      if (success) alert('Login successful! ');
      else alert('Login failed!');
    });

    const importModal = new ImportModal();
    const portModal = new PortModal(importModal);
    importModal.parentModal = portModal;
    this.appendChild(portModal);
    this.appendChild(importModal);

    document.getElementById('btn-change-port').onclick = () => portModal.show();
  }
}

customElements.define('landing-page', LandingPage);
