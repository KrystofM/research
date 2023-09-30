// eslint-disable-next-line no-unused-vars
import IconCircle from './icon-circle.js';
import IconPlain from './icon-plain.js';
import PlannerSchedule from './planner-schedule.js';
import Time from '../utils/time.js';
import Timestamp from '../utils/timestamp.js';

class VesselCard extends HTMLElement {
  isInfeasible;
  id;
  view;
  data;
  plannerView;
  scheduled;

  static VIEW = {
    automatic: 'automatic',
    manual: 'manual',
    unscheduled: 'unscheduled',
    infeasible: 'infeasible',
  };

  constructor() {
    super();
  }

  get arrival() {
    return new Timestamp(this.schedule.start).toLocal();
  }

  get departure() {
    return new Timestamp(this.schedule.expected_end).toLocal();
  }

  connectedCallback() {
    this.classList.add('vessel-card', `view-${this.view}`);
    this.innerHTML = `
      <div class="vessel-card-info">
        <div class="vessel-card-info-left">
          <icon-circle
            name="ship"
            view="${this.view === VesselCard.VIEW.infeasible ?
          IconCircle.VIEW.infeasible : IconCircle.VIEW.primary}">
          </icon-circle>
          <div class="vessel-card-info-in">
            <h6 class="vessel-card-info-heading">${this.data.name}</h6>
            ${this.view !== VesselCard.VIEW.unscheduled ?
          this.getDescription() : ''}
          </div>
        </div>
        <div class="vessel-card-info-right">
          <div class="vessel-card-info-description">
            ${this.data.width}x${this.data.length}x${this.data.depth}</div>
          <div class="vessel-card-info-description">
            ${this.data.cost_per_hour} $/h • ${this.data.containers} con</div>
        </div>
        ${(this.reason) ? this.getReason() : ''}
      </div>
      ${this.view !== VesselCard.VIEW.automatic &&
        this.view !== VesselCard.VIEW.unscheduled ?
        this.getIcon() : ''}`;

    this.generateStyle();

    this.addEventListener('mouseenter', e => {
      this.style.zIndex = this.sequenceLength + 1;
    });

    this.addEventListener('mouseleave', e => {
      this.style.zIndex = this.overlappingPos;
    });

    this.addEventListener('click', () => {
      const updateModal = document.getElementsByTagName('vessel-modal-update')[0];
      updateModal.setVessel(this.data, this.id);
      updateModal.setSchedule(this.schedule);
      updateModal.show();
    });
  }

  getReason() {
    return `
      <div class="vessel-card-info-message">
        ${this.reason}
      </div>
    `
  }

  generateStyle() {
    if (this.view !== VesselCard.VIEW.unscheduled) {
      this.style.height = this.calculateHeight();
      this.style.top = this.calculateTop();

      if (this.view === VesselCard.VIEW.infeasible) {
        this.style.left = this.calculateLeft();
        this.style.zIndex = this.calculateZIndex();
      }
    }
  }

  calculateHeight() {
    const timeScale = this.getTimeScale();

    const startAt = Math.max(this.arrival.toDate().getTime(), this.plannerTimeFrom.toDate().getTime());
    const endAt = Math.min(this.departure.toDate().getTime(), this.plannerTimeTo.toDate().getTime());

    return ((endAt - startAt) / 1000 / 60 / 60) * timeScale + 'px';
  }

  calculateTop() {
    const timeScale = this.getTimeScale();

    const startAt = new Timestamp(Math.max(this.arrival.toDate().getTime(), this.plannerTimeFrom.toDate().getTime()));

    let top = new Time(startAt).value * timeScale;

    if (this.plannerView === PlannerSchedule.VIEW.weekly) {
      // Add day to top
      // TODO
      top += 0;
    }

    return top + 'px';
  }

  getTimeScale() {
    if (this.plannerView === PlannerSchedule.VIEW.daily) {
      return 120;
    } else if (this.plannerView === PlannerSchedule.VIEW.weekly) {
      return 20; // TODO
    }
  }

  calculateZIndex() {
      return this.overlappingPos;
  }

  calculateLeft() {
    if (this.overlappingPos === 0) {
      return "6px";
    }
    return (10 * this.overlappingPos).toString() + "%";
  }

  formatDate(date) {
    if (this.plannerView === PlannerSchedule.VIEW.daily) {
      return date.formatted('hh:mm');
    } else if (this.plannerView === PlannerSchedule.VIEW.weekly) {
      return date.formatted('YYYY-MM-DD hh:mm');
    }
  }

  getDescription() {
    return `<div class="vessel-card-info-description">
    ${this.formatDate(this.arrival) +
    ' – ' + this.formatDate(this.departure) +
    ' • ' + this.view.capitalizeFirstLetter()}
    </div>`;
  }

  getIcon() {
    return `<div class="vessel-card-footer">
              <icon-plain
                  name="hand-paper"
                  view="${IconPlain.VIEW.secondary}"
              ></icon-plain>
           </div>`;
  }
}

customElements.define('vessel-card', VesselCard);

export default VesselCard;
