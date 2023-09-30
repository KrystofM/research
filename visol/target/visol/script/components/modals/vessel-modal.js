import VisolApi from '../../api.js';
import Modal from './modal.js'
import {div} from '../../utils/elements.js';
import sanitize from '../../utils/sanitize.js';

// TODO Low default arrival the day that is currently viewed
export default class VesselModal extends Modal {
  schedule_type;
  // ID prefix
  vesselApiEndpoint;
  getId;
  name;

  constructor(name, submitText) {
    super(`${name.charAt(0).toUpperCase() + name.substring(1)} vessel`, submitText);
    this.name = name;
  }

  hide() {
    this.closeCallback();
    super.hide();
  }

  get modalId() {
    return this.name;
  }

  setScheduleType(type) {
    this.schedule_type = type;
    if (type === 'unscheduled') {
      this.getElement(`schedule-edit`).setAttribute('hidden', '');
    } else {
      this.getElement(`schedule-edit`).removeAttribute('hidden');
      for (const item of document.getElementsByClassName(`${this.modalId}-disabled-if-auto`)) {
        if (type === 'manual') {
          item.removeAttribute('disabled');
        } else {
          item.setAttribute('disabled', '');
        }
      }
    }
  }

  async connectedCallback() {
    await super.connectedCallback();
    this.setScheduleType('auto');
    this.attachListeners();
  }

  buildBody() {
    const d = document.createElement('div');

   d.innerHTML = `
        <div>
          <div class="mb-3">
            <label class="form-label" for="form-name">Name:</label>
            <input
              class="form-control form-control-sm"
              id="${this.modalId}-form-name">
          </div>

          <div class="row mb-3">
            <div class="col">
              <label class="form-label" for="${this.modalId}-form-arrival">Arrival:</label>
              <input id="${this.modalId}-form-arrival"
                     is="datetime-input"
                     class="form-control form-control-sm"
                     now no-seconds
                     required>
            </div>
            <div class="col">
              <label class="form-label" for="${this.modalId}-form-deadline">Deadline:</label>
              <input is="datetime-input"
                     class="form-control form-control-sm"
                     id="${this.modalId}-form-deadline" no-seconds>
            </div>
          </div>

          <div class="row mb-3">
            <div class="col">
              <label class="form-label" for="form-containers">Container amount:</label>
              <input class="form-control form-control-sm"
                     id="${this.modalId}-form-containers"
                     min="0"
                     required
                     type="number">
            </div>
<!--            TODO Miki min-->
            <div class="col">
              <label class="form-label" for="${this.modalId}-form-cost_per_hour">Cost:</label>
              <input
                class="form-control form-control-sm"
                id="${this.modalId}-form-cost_per_hour"
                type="number" step="0.01" min="0"
              >
            </div>
          </div>

          <div class="mb-3 row">
            <div class="col">
              <label class="form-label" for="${this.modalId}-form-width"><b>Terminal:</b></label>
                <select is="select-terminal"
                      class="form-select form-select-sm"
                      id="${this.modalId}-form-destination">
                </select>
            </div>
            <div class="col">
              <label class="form-label" for="form-length">Length:</label>
              <input class="form-control form-control-sm"
                     id="${this.modalId}-form-length"
                     required
                     type="number"
                     value="1" min="1">
            </div>
          </div>

          <div class="row mb-3">
            <div class="col">
              <label class="form-label" for="form-width">Width:</label>
              <input class="form-control form-control-sm"
                     id="${this.modalId}-form-width"
                     required
                     type="number"
                     value="1" min="1">
            </div>
            <div class="col">
              <label class="form-label" for="${this.modalId}-form-depth">Depth:</label>
              <input class="form-control form-control-sm"
                     id="${this.modalId}-form-depth"
                     required
                     type="number"
                     value="1" min="1">
            </div>
          </div>

          <div class="mb-3 row mt-2 flex-nowrap">
            <label class="label me-3 col" for="label"><b>Schedule:</b></label>
            <div class="form-check form-check-inline col">
              <input checked
                     class="form-check-input"
                     id="${this.modalId}-radio-auto"
                     name="${this.modalId}-radio"
                     value="false"
                     type="radio">
              <label class="form-check-label" for="${this.modalId}-radio-auto">
                Automatic
              </label>
            </div>
            <div class="form-check form-check-inline col">
              <input class="form-check-input"
                     id="${this.modalId}-radio-manual"
                     name="${this.modalId}-radio"
                     value="true"
                     type="radio">
              <label class="form-check-label" for="${this.modalId}-radio-manual">
                Manual
              </label>
            </div>
            <div class="form-check form-check-inline col">
              <input class="form-check-input"
                     id="${this.modalId}-radio-unscheduled"
                     name="${this.modalId}-radio"
                     value="true"
                     type="radio">
              <label class="form-check-label" for="${this.modalId}-radio-unscheduled">
                Unscheduled
              </label>
            </div>
          </div>
          
          <div class="row mb-3" id="${this.modalId}-schedule-edit">
            <div class="col d-grid">
              <label class="form-label" for="${this.modalId}-form-berth">Berth:</label>
              <select is="select-berth" 
                      id="${this.modalId}-form-berth"
                      class="form-select form-select-sm ${this.modalId}-disabled-if-auto"
              > </select>
            </div>
            <div class="col">
              <label class="form-label" for="form-start">Handle time:</label>
              <input id="${this.modalId}-form-start"
                     is="datetime-input"
                      class="form-control form-control-sm ${this.modalId}-disabled-if-auto"
                     disabled
                     required
                     no-seconds
                     now
                   >
            </div>
          </div>
        </div>`;
    return d;
  }

  buildFooter() {
    this.reason = document.createElement('input');
    this.reason.type = 'text';
    this.reason.className = 'form-control form-control-sm'
    this.reason.placeholder = 'Reason';

    return div('d-inline-flex align-items-center w-100', [
        div('me-3 w-100', [
           this.reason
        ]),
        div('d-inline-flex justify-content-end h-auto w-100', this.footerBtns)
      ]);
  }

  attachListeners() {

    const radioIds = ['auto', 'manual', 'unscheduled'];
    radioIds.forEach((id) => {
      this.getElement(`radio-${id}`).addEventListener('click', () => {
        this.setScheduleType(id);
      });
    });

    const destinationSelect = this.getElement('form-destination');
    destinationSelect.addEventListener('change', async (_event) => {
      await this.getElement('form-berth').setTerminal(destinationSelect.value);
    });
  }

  serialize(keys) {
    const res = {};
    keys.forEach((key, _) => {
      const el = this.getElement(`form-${key}`);
      if (!el.disabled) {
        res[key] = sanitize(el.value);
      }
    });
    return res;
  }

  getVessel() {
    // TODO integrate this with fill-in method?
    const vesselKeys = ['name', 'arrival', 'deadline', 'containers', 'cost_per_hour',
      'destination', 'length', 'width', 'depth'];
    return this.serialize(vesselKeys);
  }

  getSchedule() {
    const scheduleKeys = ['berth', 'start'];
    const schedule = this.serialize(scheduleKeys);
    schedule['manual'] = this.schedule_type === 'manual';
    return schedule;
  }

  getElement(id) {
    const el = document.getElementById(this.modalId + '-' + id);
    if (el === null) throw new Error(`Failed to get element with id ${this.modalId}-${id}`);
    return el;
  }

  async submit() {
    const vessel = this.getVessel();
    const reasonValue = sanitize(this.reason.value);
    await this.vesselApiEndpoint(vessel, reasonValue).then(async (response) => {
      const vesselId = this.getId(response);
      if (this.schedule_type === 'unscheduled') {
        if (this.hasSchedule)
          await VisolApi.deleteSchedule(vesselId);
      } else {
        const schedule = this.getSchedule();
        await VisolApi.putSchedule(vesselId, schedule, reasonValue).then(() => {
          this.hide();
        }).catch((e) => {
          console.log('Failed to create schedule: ', e);
        });
      }
    }).catch((e) => {
      console.log('Failed to create vessel: ', e);
    });
  }
}
