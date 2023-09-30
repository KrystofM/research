import VisolApi from '../api.js';
import {div, el, elCC, h, span} from '../utils/elements.js';
import VesselCard from './vessel-card.js';
import State from '../auth/state.js';

class UnscheduledVessels extends HTMLElement {
  foundNone;
  noUnscheduled;
  vesselContainer;
  searchInput;

  constructor() {
    super();
  }

  async connectedCallback() {
    this.foundNone = div('scrollable-container-empty-search', [
      'ooops ... No vessel found in unscheduled vessels...'
    ])
    this.vesselContainer = div('scrollable-container', [this.foundNone]);
    this.searchInput = elCC('input', {
      id: 'vessels-search-bar',
      placeholder: 'Search vessel',
      type: 'text'
    }, 'form-control');
    this.noUnscheduled = div('scrollable-container-empty', [
      'No unscheduled vessel'
    ])
    this.append(
        div('unscheduled-vessels', [
            div('unscheduled-vessels-body', [
              div('unscheduled-vessels-headers', [
                  h(6, 'Unscheduled vessels'),
                  elCC('form', {
                    role: 'search'
                  }, 'form-group has-icon-beginning', [
                      span('fa fa-search form-control-icon'),
                      this.searchInput
                  ])
              ]),
              this.vesselContainer
            ])
        ])
    );
    await this.loadUnscheduled();
    this.addEventListeners();
  }

  async loadUnscheduled() {
    const unscheduledVessels = Object.entries(await VisolApi.getUnscheduledPerTerminal(await State.getTerminalId()));
    this.searchInput.disabled = false;
    if(unscheduledVessels.length === 0) {
      this.vesselContainer.appendChild(this.noUnscheduled);
      this.searchInput.disabled = true;
    }
    unscheduledVessels.forEach(([id, unscheduledVessel]) => {
      this.vesselContainer.append(el('vessel-card', {
        id: id,
        data: unscheduledVessel,
        view: VesselCard.VIEW.unscheduled,
        plannerView: this.view
      }));
    });
  }

  async reloadUnscheduled() {
    this.vesselContainer.innerHTML = '';
    await this.loadUnscheduled();
  }

  addEventListeners() {
    const vessels = this.vesselContainer.getElementsByTagName('vessel-card');

    this.searchInput.addEventListener('keyup', () => {
      const search = this.searchInput.value.toLowerCase();
      let empty = true;
      this.foundNone.style.display = 'none'

      for (const vessel of vessels) {
        if (!vessel.data.name.toLowerCase().includes(search)) {
          vessel.style.display = 'none';
        } else {
          empty = false;
          vessel.style.display = 'flex';
        }
      }

      if (empty) {
        this.foundNone.style.display = 'flex';
      }
    });
  }
}

customElements.define('unscheduled-vessels', UnscheduledVessels);
