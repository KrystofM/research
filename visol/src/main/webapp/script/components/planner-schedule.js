import IconCircle from './icon-circle.js';
import CurrentTime from './current-time.js';
import VisolApi from '../api.js';
import {div, el, p, span} from '../utils/elements.js';
import DragNDrop from './drag-n-drop.js';
import VesselCard from './vessel-card.js';
import State from '../auth/state.js';
import VesselModalCreate from './modals/vessel-modal-create.js';
import VesselModalUpdate from './modals/vessel-modal-update.js';

class PlannerSchedule extends HTMLElement {
  vessels;
  timeFrom;
  timeTo;
  view = PlannerSchedule.VIEW.daily;
  berths;
  addVessel;
  dnd;
  vesselModal;
  updateModal;

  static VIEW = {
    daily: 'daily',
    weekly: 'weekly',
  };

  constructor() {
    super();
  }

  async connectedCallback() {
    this.vesselModal = new VesselModalCreate(
      () => self.reloadSchedule(true, true),
    );

    this.updateModal = new VesselModalUpdate(
      () => self.reloadSchedule(true, true),
    );
    this.appendChild(this.vesselModal);
    this.appendChild(this.updateModal);

    this.berths = div('planner-berths', [
      this.dividers(),
    ]);
    this.addVessel = el('icon-circle', {
      name: 'ship',
      view: IconCircle.VIEW.addVessel,
    });
    this.addVessel.onclick = () => this.vesselModal.show();

    const self = this;
    this.current = el('current-time', {
      view: CurrentTime.VIEW.daily,
    });
    this.header = el('planner-header', {
      optimiseAutoCallback: () => self.optimiseSchedules(false, false),
      optimiseManualCallback: () => self.optimiseSchedules(false, true),
      optimiseAllCallback: () => self.optimiseSchedules(true, true),
      dateCallback: (from, to) => self.updatePeriod(from, to),
    });
    this.unscheduled = el('unscheduled-vessels', {});
    this.actions = div('planner-schedule-actions', [
      el('icon-circle', {
        name: 'undo',
        onclick: () => self.undoChange(),
      }),
      el('icon-circle', {
        name: 'redo',
        onclick: () => self.redoChange(),
      }),
      this.addVessel,
    ]);
    this.sidebar = el('side-bar', {
      vessels: this.vessels,
      fromTime: this.timeFrom,
      toTime: this.timeTo,
      actions: this.actions,
      newChangesCallback: this.reloadScheduleFromSidebar.bind(this),
    });
    this.append(
        div('planner', [
          this.header,
          div('planner-schedule view-day', [
            this.actions,
            this.sidebar,
            div('planner-schedule-in', [
              this.current,
              this.timeline(),
              this.berths,
            ]),
          ]),
        ]),
        this.unscheduled
    );

    await this.loadBerths();
    await this.loadVessels();
    this.dnd = new DragNDrop(
            this,
  'vessel-card',
  'planner-berths-berth-ships',
  'planner-berths-berth-ships-mark',
  () => self.timeFrom.clone(),
  async () => self.reloadSchedule(false, true),
    );
    // TODO update automatically if past midnight
    this.header.changeDateToday();
  }

  removeAll() {
    console.log('calling remove');
    this.remove();
    this.vesselModal.remove();
    this.updateModal.remove();
  }

  timeline() {
    const timeline = div('planner-timeline');
    Array.from(Array(48).keys()).forEach((val) => {
      const hour = Math.floor(val / 2);
      const minutes = val % 2 === 0 ? '00' : '30';
      timeline.append(div('planner-timeline-divider', [
        p('planner-timeline-divider-time',
            hour + ':' + minutes),
      ]));
    });
    return timeline;
  }

  dividers() {
    const dividers = div('planner-berths-dividers');
    Array.from(Array(48).keys()).forEach((val) => {
      dividers.append(span('planner-berths-dividers-item'));
    });
    return dividers;
  }

  async redoChange() {
    await VisolApi.redoChange();
    await this.reloadSchedule(true, true)
  }

  async undoChange() {
    await VisolApi.undoChange();
    await this.reloadSchedule(true, true);
  }

  async loadBerths() {
    const berths = await VisolApi.getBerthsPerTerminal(await State.getTerminalId());
    Object.entries(berths).forEach(([id, berth]) => {
      this.berths.appendChild(el('planner-berth', {
        id: id,
        data: berth,
        plannerView: this.view,
      }));
    });
  }

  async loadVessels(vessels) {
    this.vessels = vessels || await VisolApi.getVesselsPerTerminal(await State.getTerminalId());
    this.sidebar.vessels = this.vessels;
  }

  async reloadSchedule(loadVessels = true, loadUnscheduled = false) {
    if (loadVessels) {
      await this.loadVessels();
    }
    if (loadUnscheduled) {
      await this.unscheduled.reloadUnscheduled();
    }
    await this.loadSchedules();
    await this.sidebar.update();
    this.dnd.loadDragEls();
  }

  async reloadScheduleFromSidebar(vessels) {
    await this.loadVessels(vessels);
    await this.unscheduled.reloadUnscheduled(vessels);
    await this.loadSchedules();
    this.dnd.loadDragEls();
  }

  async loadSchedules() {
    const berthsSchedules =
        await VisolApi.getSchedulesPerTerminal(
            await State.getTerminalId(),
            this.timeFrom.formatted(),
            this.timeTo.formatted(),
        );

    const infeasibleVessels =
        await VisolApi.getInfeasibilityPerTerminal(
            await State.getTerminalId(),
            this.timeFrom.formatted(),
            this.timeTo.formatted(),
        );
    this.deleteSchedule();
    Object.entries(berthsSchedules).forEach(([id, berthSchedules]) => {
      const berthSchedulesEl = document.getElementById(`berth-ships-${id}`);
      const berthInfeasibleSequences = this.getInfeasibleSequences(infeasibleVessels, berthSchedules);
      Object.values(berthSchedules).forEach((schedule) => {
        const infeasiblePos = this.getPositionInSequence(berthInfeasibleSequences, schedule.vessel);
        if (infeasiblePos !== null) {
          berthSchedulesEl.appendChild(el('vessel-card', {
            id: schedule.vessel,
            data: this.vessels[schedule.vessel],
            schedule: schedule,
            view: VesselCard.VIEW.infeasible,
            overlappingPos: infeasiblePos[0],
            sequenceLength: infeasiblePos[1],
            reason: infeasibleVessels[schedule.vessel].reason,
            plannerView: this.view,
            plannerTimeFrom: this.timeFrom,
            plannerTimeTo: this.timeTo,
          }));
        } else {
          berthSchedulesEl.appendChild(el('vessel-card', {
            id: schedule.vessel,
            data: this.vessels[schedule.vessel],
            schedule: schedule,
            view: (schedule.manual) ? VesselCard.VIEW.manual : VesselCard.VIEW.automatic,
            plannerView: this.view,
            plannerTimeFrom: this.timeFrom,
            plannerTimeTo: this.timeTo,
          }));
        }
      });
    });
  }

  getInfeasibleSequences(infeasibleVessels, berthSchedules) {
    const infeasibleVesselsIds = Object.keys(infeasibleVessels);

    let res = [];
    Object.values(berthSchedules).forEach((schedule) => {
      if (infeasibleVesselsIds.includes(schedule.vessel.toString())) {
        res.push([schedule.vessel, Date.parse(schedule.start)]); // creating an array of overlapped vessels and their start time in schedule
      }
    })
    res = res
        .sort((a,b) => a[1] - b[1]) // sorting based on arrival time
        .map(e => {return e[0]})

    const sequences = []
    sequences.push(res);
    let numOfSequences = 1;

    for (let i = 2; i < res.length; i++) { // only check for the 3th vessel
      const prevSchedule = berthSchedules.find(schedule => schedule.vessel === res[i-1]);
      const thisSchedule = berthSchedules.find(schedule => schedule.vessel === res[i]);
      const nextSchedule = berthSchedules.find(schedule => schedule.vessel === res[i+1]);

      let currentSequence = sequences.at(numOfSequences - 1);

      if (thisSchedule.start > prevSchedule.expected_end) {
        sequences
            .push(currentSequence
              .splice(i, currentSequence.length - i));
        numOfSequences++;
      }
    }
    return sequences;
  }

  getPositionInSequence(sequences, vessel) {
    for (let seq of sequences) {
      if (seq.includes(vessel)) return [seq.indexOf(vessel), seq.length];
    }
    return null;
  }

  async optimiseSchedules(unscheduled, manual) {
    await VisolApi.optimiseSchedule(await State.getTerminalId(), this.timeFrom.formatted(), this.timeTo.formatted(), unscheduled, manual);
    await this.reloadSchedule(false, unscheduled);
  }

  deleteSchedule() {
    Array.from(this.berths.getElementsByTagName('vessel-card')).forEach((vesselEl) => {
      vesselEl.remove();
    });
  }

  async updatePeriod(from, to) {
    this.current.style.display = from.isToday() ? 'block' : 'none';
    this.timeFrom = from;
    this.timeTo = to;
    await this.reloadSchedule(false);
  }
}

customElements.define('planner-schedule', PlannerSchedule);

export default PlannerSchedule;
