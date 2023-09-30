import VisolApi from '../api.js';
import {div, el, elCC, p, span} from '../utils/elements.js';
import VesselCard from './vessel-card.js';
import Timestamp from '../utils/timestamp.js';

class PrintSchedule extends HTMLElement {
  static VIEW = {
    daily: 'daily',
    weekly: 'weekly',
  };
  vessels;
  terminal;
  date;
  timeFrom;
  timeTo;
  view = PrintSchedule.VIEW.daily;
  berths;

  constructor() {
    super();
  }

  async connectedCallback() {
    this.timeFrom = new Timestamp(this.date.year, this.date.month, this.date.day, 0, 0, 0);
    this.timeTo = this.timeFrom.add({days: 1});

    this.berths = [];

    this.append(
      div('print-planner', [
        div('print-planner-header', [
          elCC('div', {
            textContent: this.date.formatted('YYYY-MM-DD')
          }, 'print-planner-header-sub print-planner-header-date')
        ])
      ])
    );

    await this.loadBerths();
    await this.loadVessels();
    await this.loadSchedules();
  }

  removeAll() {
    this.remove();
  }

  timeline() {
    const timeline = div('print-planner-timeline');
    Array.from(Array(48).keys()).forEach((val) => {
      const hour = Math.floor(val / 2);
      const minutes = val % 2 === 0 ? '00' : '30';
      timeline.append(div('print-planner-timeline-divider', [
        p('print-planner-timeline-divider-time',
            hour + ':' + minutes),
      ]));
    });
    return timeline;
  }

  dividers() {
    const dividers = div('print-planner-berths-dividers');
    Array.from(Array(48).keys()).forEach((val) => {
      dividers.append(span('print-planner-berths-dividers-item'));
    });
    return dividers;
  }

  async loadBerths() {
    const berths = await VisolApi.getBerthsPerTerminal(this.terminal);
    Object.entries(berths).forEach(([id, berth]) => {
      this.berths.push(el('planner-berth', {
        id: id,
        data: berth,
        plannerView: this.view,
      }));
    });

    for (let i = 0; i < this.berths.length; i += 5) {
      this.getElementsByClassName('print-planner')[0].append(
          div('print-planner-schedule view-day mb-5', [
            div('print-planner-schedule-in', [
              this.timeline(),
              this.buildBerths(i),
            ]),
          ])
      )
    }
  }

  buildBerths(startIndex) {
    let berths = div('print-planner-berths', [
      this.dividers(),
    ]);

    for (let i = startIndex; i < this.berths.length; i++) {
      berths.appendChild(this.berths[i]);
    }

    return berths;
  }

  async loadVessels(vessels) {
    this.vessels = vessels || await VisolApi.getVesselsPerTerminal(this.terminal);
  }

  async loadSchedules() {
    const berthsSchedules =
        await VisolApi.getSchedulesPerTerminal(
            this.terminal,
            this.timeFrom.formatted(),
            this.timeTo.formatted(),
        );

    const infeasibleVessels =
        await VisolApi.getInfeasibilityPerTerminal(
            this.terminal,
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
    });
    res = res
        .sort((a, b) => a[1] - b[1]) // sorting based on arrival time
        .map(e => {
          return e[0];
        });

    const sequences = [];
    sequences.push(res);
    let numOfSequences = 1;

    for (let i = 2; i < res.length; i++) { // only check for the 3th vessel
      const prevSchedule = berthSchedules.find(schedule => schedule.vessel === res[i - 1]);
      const thisSchedule = berthSchedules.find(schedule => schedule.vessel === res[i]);
      const nextSchedule = berthSchedules.find(schedule => schedule.vessel === res[i + 1]);

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

  deleteSchedule() {
    Array.from(this.getElementsByTagName('vessel-card')).forEach((vesselEl) => {
      vesselEl.remove();
    });
  }
}

customElements.define('print-schedule', PrintSchedule);

export default PrintSchedule;
