// eslint-disable-next-line no-unused-vars
import IconCircle from './icon-circle.js';
import Time from '../utils/time.js';
import PlannerSchedule from './planner-schedule.js';

class BerthClosed extends HTMLElement {
  constructor() {
    super();
  }

  connectedCallback() {
    this.classList.add('planner-berths-berth-closed');
    this.plannerView =
        this.hasAttribute('plannerView') ?
            this.getAttribute('plannerView') : PlannerSchedule.VIEW.daily;
    this.innerHTML = this.generateBlocks();
  }

  generateBlocks() {
    const open = new Time(this.getAttribute('open')).toLocal();
    const close = new Time(this.getAttribute('close')).toLocal();
    let blocks = '';

    if (this.plannerView === PlannerSchedule.VIEW.daily) {
      if (open.value < close.value || close.value === 0) {
        // Two blocks, before open and after close
        if (open.value > 0) {
          // Block before open
          blocks += this.generateBlock(0, 0, open.value);
        }
        if (close.value > 0) {
          // Block after close
          blocks += this.generateBlock(0, close.value, 24);
        }
      } else {
        // One block, between close and open
        blocks += this.generateBlock(0, close.value, open.value);
      }
    } else if (this.plannerView === PlannerSchedule.VIEW.weekly) {
      // TODO
    }
    return blocks;
  }

  get plannerView() {
    return this._plannerView;
  }

  set plannerView(newVal) {
    this._plannerView = newVal;
  }

  generateBlock(top, start, end) {
    // `start` and `end` must both be between 0 (inclusive) and 24 (exclusive)
    const timeScale = this.getTimeScale();
    return `
    <div class="planner-berths-berth-closed-in" 
         style="height: ${(end === 0 ? 24 - start : end - start) * timeScale}px;
                top: ${(top + start) * timeScale}px">
      <icon-circle name="cloud-moon" view="${IconCircle.VIEW.closed}">
      </icon-circle>
    </div>
    `;
  }

  getTimeScale() {
    if (this.plannerView === PlannerSchedule.VIEW.daily) {
      return 120;
    } else if (this.plannerView === PlannerSchedule.VIEW.weekly) {
      return 20; // TODO
    }
  }
}

customElements.define('berth-closed', BerthClosed);
