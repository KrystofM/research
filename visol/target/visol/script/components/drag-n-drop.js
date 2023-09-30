import {div} from '../utils/elements.js';
import VisolApi from '../api.js';

class DragNDrop {
  parent;
  dragClass;
  targetClass;
  markClass;
  markCurrent;
  draggedEl;
  draggedCallback;
  static DRAG_ENTER_CLASS = 'drag-enter';

  constructor(parent, dragClass, targetClass, markClass, getTimeFrom, draggedCallback) {
    this.parent = parent;
    this.dragClass = dragClass;
    this.targetClass = targetClass;
    this.markClass = markClass;
    this.getTimeFrom = getTimeFrom;
    this.draggedCallback = draggedCallback;
    this.start();
  }

  start() {
    const self = this;
    this.loadDragEls();
    const targetEls = document.querySelectorAll('.' + this.targetClass);
    targetEls.forEach((targetEl) => {
      targetEl.addEventListener('dragover', (e) => self.dragOver(e));
      targetEl.addEventListener('drop', (e) => self.drop(e));
    });
    this.parent.addEventListener('dragenter', (e) => self.dragEnter(e));
    this.parent.addEventListener('dragleave', (e) => self.dragLeave(e));
  }

  loadDragEls() {
    const self = this;
    const dragEls = document.querySelectorAll('.' + this.dragClass);
    dragEls.forEach((dragEl) => {
      dragEl.setAttribute('draggable', 'true');
      dragEl.addEventListener('dragstart', (e) => self.dragStart(e));
    });
  }

  dragStart(e) {
    this.draggedEl = e.target;
  }

  dragOver(e) {
    e.preventDefault();
    if (this.isTarget(e)) {
      const top = this.getTop(e);
      this.markCurrent.style.transform = `translateY(${top}px)`;
      const [hours, minutes] = this.getTimeFromTop(top);
      this.markCurrent.textContent = hours + ':' + minutes.toString().padStart(2, '0');
    }
  }

  async drop(e) {
    e.preventDefault();
    if (this.isTarget(e)) {
      e.target.classList.remove(DragNDrop.DRAG_ENTER_CLASS);
      this.removeMark(e);
      await this.putSchedule(e);
    }
  }

  dragEnter(e) {
    if (this.isTarget(e)) {
      e.target.classList.add(DragNDrop.DRAG_ENTER_CLASS);
      this.markCurrent = div(this.markClass);
      e.target.append(this.markCurrent);
    }
  }

  dragLeave(e) {
    if (this.isTarget(e)) {
      e.target.classList.remove(DragNDrop.DRAG_ENTER_CLASS);
      this.removeMark(e);
    }
  }

  async putSchedule(e) {
    const schedule = {
      manual: true,
      start: this.getTimestamp(this.getTimeFrom(), this.getTop(e)).formatted(),
      berth: e.target.parentNode.id,
    };
    const vesselId = this.draggedEl.id;

    await VisolApi.putSchedule(vesselId, schedule);
    await this.draggedCallback();
    this.loadDragEls();
  }

  getTimestamp(timeFrom, top) {
    const [hours, minutes] = this.getTimeFromTop(top);
    timeFrom.hours = hours;
    timeFrom.minutes = minutes;
    return timeFrom.toUTC();
  }

  getTimeFromTop(top) {
    const minuteHeight = 2;
    const hourHeight = minuteHeight*60;
    const minutes = Math.floor((top % hourHeight) / minuteHeight);
    const hours = Math.floor((top - minutes*minuteHeight) / hourHeight);

    return [hours, minutes];
  }

  getTop(e) {
    return e.layerY - 80;
  }

  removeMark(e) {
    const mark = e.target.getElementsByClassName(this.markClass);
    for (const markElement of mark) {
      markElement.remove();
    }
  }

  isTarget(e) {
    return e.target.classList.contains(this.targetClass);
  }
}

export default DragNDrop;
