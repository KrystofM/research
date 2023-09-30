import {div, el, elCC} from '../utils/elements.js';
import FullButton from './full-button.js';
import IconCircle from './icon-circle.js';
import Timestamp from '../utils/timestamp.js';

class PlannerHeader extends HTMLElement {
  optimiseCallback;
  dateCallback;
  timeFrom;
  timeTo;
  view;
  datePicker;
  todayBtn;

  connectedCallback() {
    const self = this;
    this.datePicker = elCC('label', {
      htmlFor: 'planner-date',
      textContent: ' ',
    }, 'planner-header-date', [
      elCC('input', {
        type: 'date',
        id: 'planner-date',
        onchange: (c) => self.changeDate(c.currentTarget.valueAsDate),
      }, 'planner-header-date-input'),
    ]);
    this.todayBtn = el('full-button', {
      textContent: 'Today',
      view: FullButton.VIEW.disabled,
      onclick: () => self.changeDateToday()
    });

    this.append(
      div('planner-header', [
        div('planner-header-sub', [
          div('planner-header-sub-in', [
            this.todayBtn
          ])
        ]),
        div('planner-header-sub', [
          el('icon-circle', {
            name: 'arrow-left',
            view: IconCircle.VIEW.default,
            onclick: () => self.changeDateBack()
          }),
          this.datePicker,
          el('icon-circle', {
            name: 'arrow-right',
            view: IconCircle.VIEW.default,
            onclick: () => self.changeDateForward()
          })
        ]),
        div('planner-header-sub', [
          div('planner-header-sub-in', [
            el('optimise-dropdown', {
              name: 'optimise',
              callBack: this.optimiseCallback
            })
          ])
        ])
      ])
    );
  }

  changeDateToday() {
    this.changeDate(new Date());
  }

  changeDateBack() {
    this.changeDate(this.timeFrom.previousDate());
  }

  changeDateForward() {
    this.changeDate(this.timeFrom.nextDate());
  }

  changeDate(date) {
    this.setDayBorders(date);
    this.todayBtn.view = this.timeFrom.isToday() ?
        FullButton.VIEW.disabled : FullButton.VIEW.secondary;
    this.datePicker.firstChild.data = this.timeFrom.dateFormat();
    this.dateCallback(this.timeFrom, this.timeTo);
  }

  setDayBorders(date) {
    this.timeFrom = new Timestamp(
        date.getFullYear(),
        date.getMonth() + 1,
        date.getDate());
    let nextDate = this.timeFrom.nextDate();
    this.timeTo = new Timestamp(
        nextDate.getFullYear(),
        nextDate.getMonth() + 1,
        nextDate.getDate());
  }
}

customElements.define('planner-header', PlannerHeader);
