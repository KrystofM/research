// eslint-disable-next-line no-unused-vars
import Time from '../utils/time.js';
import {div, p} from '../utils/elements.js';

class CurrentTime extends HTMLElement {
  view;

  static VIEW = {
    daily: 'daily',
    weekly: 'weekly',
  };

  constructor() {
    super();
  }

  connectedCallback() {
    this.classList.add('planner-current-wrap');
    this.currentTime = p('planner-current-time');
    this.current = div('planner-current', [
      div('planner-current-in', [
        this.currentTime
      ])
    ])
    this.append(this.current)
    this.updateTime();

    const self = this;
    // Make it run at the top of every minute
    setTimeout(function() {
      self.updateTime();
      setInterval(function() {
        self.updateTime();
      }, 1000 * 60);
    }, 1000 * (60 - new Date().getSeconds()));

    this.scrollToMiddle();
  }

  updateTime() {
    const timeScale = this.getTimeScale();
    const time = new Time(new Date()).toLocal();

    if (this.view === CurrentTime.VIEW.daily) {
      this.currentTime.textContent = time.formatted('h:mm');

      const top = 60 + time.value * timeScale;
      this.current.style.top = top + 'px';
    } else if (this.view === CurrentTime.VIEW.weekly) {
      // TODO
    }
  }

  scrollToMiddle() {
    const timeScale = this.getTimeScale();
    const time = new Time(new Date()).toLocal();
    const plannerHeight = document.querySelector('.planner').offsetHeight;
    document.querySelector('.planner-schedule').scroll(0,
        (60 + time.value * timeScale) - (plannerHeight / 2));
  }

  getTimeScale() {
    if (this.view === CurrentTime.VIEW.daily) {
      return 120;
    } else if (this.view === CurrentTime.VIEW.weekly) {
      return 20; // TODO
    }
  }
}

customElements.define('current-time', CurrentTime);

export default CurrentTime;
