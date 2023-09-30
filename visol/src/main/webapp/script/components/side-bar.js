import IconPlain from './icon-plain.js';
import VisolApi from '../api.js';
import Timestamp from '../utils/timestamp.js';
import ErrorToast from './error-toast.js';
import State from '../auth/state.js';

class SideBar extends HTMLElement {
  authorGravatars = {};
  authorProfiles = {};

  constructor() {
    super();
  }

  set newChangesCallback(val) {
    this._newChangesCallback = val;
  }

  get newChangesCallback() {
    return this._newChangesCallback;
  }

  set vessels(val) {
    this._vessels = val;
  }

  get vessels() {
    return this._vessels;
  }

  set actions(val) {
    this._actions = val;
  }

  get actions() {
    return this._actions;
  }

  set fromTime(val) {
    this._fromTime = val;
  }

  get fromTime() {
    return this._fromTime;
  }

  set toTime(val) {
    this._toTime = val;
  }

  get toTime() {
    return this._toTime;
  }

  get newestChangeTime() {
    return this._newestChangeTime;
  }

  set newestChangeTime(val) {
    this._newestChangeTime = val;
  }

  get oldestChangeTime() {
    return this._oldestChangeTime;
  }

  set oldestChangeTime(val) {
    this._oldestChangeTime = val;
  }

  static compareValues(oldObject, newObject, key, format = (value) => value) {
    if (newObject && oldObject) {
      if (Array.isArray(key)) {
        const oldValues = Object.fromEntries(key.map(k => [k, oldObject[k]]));
        const newValues = Object.fromEntries(key.map(k => [k, newObject[k]]));
        for (const k of key) {
          if (newObject[k] !== oldObject[k]) {
            return `<dd class="old">${format(oldValues)}</dd><dd class="new">${format(newValues)}</dd>`;
          }
        }
        return `<dd>${format(newValues)}</dd>`;
      } else {
        return newObject[key] === oldObject[key]
            ? `<dd>${format(newObject[key])}</dd>`
            : `<dd class="old">${format(oldObject[key])}</dd><dd class="new">${format(newObject[key])}</dd>`;
      }
    } else if (newObject) {
      if (Array.isArray(key)) {
        const newValues = Object.fromEntries(key.map(k => [k, newObject[k]]));
        return `<dd class="new">${format(newValues)}</dd>`;
      } else {
        return `<dd class="new">${format(newObject[key])}</dd>`;
      }
    } else if (oldObject) {
      if (Array.isArray(key)) {
        const oldValues = Object.fromEntries(key.map(k => [k, oldObject[k]]));
        return `<dd class="old">${format(oldValues)}</dd>`;
      } else {
        return `<dd class="old">${format(oldObject[key])}</dd>`;
      }
    } else {
      throw new Error('No object to compare');
    }
  }

  async connectedCallback() {

    this.classList.add('sidebar');

    this.innerHTML = `
    <div class="sidebar-body">
      <div class="sidebar-body-item sidebar-performance-toggle" id="sidebar-performance-button">
        <a>
          <icon-plain
          name = "fire"
          view = "${IconPlain.VIEW.sidebar}"
          ></icon-plain>
        </a>
      </div>
      
      <div class="sidebar-body-item sidebar-recent-changes-toggle" id="sidebar-recent-changes-button">
        <a>   
          <icon-plain
          name = "calculator"
          view = "${IconPlain.VIEW.sidebar}"
          ></icon-plain>
        </a>
      </div>
      
      <div class="sidebar-body-content" id="sidebar-performance">
        
      </div>
      
      <div class="sidebar-body-content" id="sidebar-recent-changes">
        
      </div>
    </div>
    `;

    await this.loadPerformance();
    await this.loadRecentChanges();
    await this.addEventListeners();
  }

  async addEventListeners() {
    const performanceToggles = document.getElementsByClassName('sidebar-performance-toggle');
    const recentChangesToggle = document.getElementsByClassName('sidebar-recent-changes-toggle');

    for (const el of performanceToggles) {
      el.addEventListener('click', () => {
        this.sidebarToggle('sidebar-performance', 'sidebar-performance-button');
      });
    }

    for (const el of recentChangesToggle) {
      el.addEventListener('click', () => {
        this.sidebarToggle('sidebar-recent-changes', 'sidebar-recent-changes-button');
      });
    }

    setInterval(async () => {
      await this.loadNewerChanges();
    }, 1000 * 1);

    document.querySelector('#sidebar-recent-changes .sidebar-body-content-body').addEventListener('scroll', async () => {
      await this.fillChangesCards();
    });

    await this.fillChangesCards();

    // TODO detect deleted changes (redo)
  }

  sidebarToggle(id, activeEl) {
    this.closeAll(id, activeEl);

    const el = document.getElementById(id);
    const activeElem = document.getElementById(activeEl);
    const actionsButtonToggle = this.actions;

    el.classList.toggle('content-reveal');
    activeElem.classList.toggle('sidebar-active');
    if (document.getElementsByClassName('content-reveal').length > 0) actionsButtonToggle.classList.add('actions-replaced');
  }

  closeAll(id, activeEl) {
    const allElements = document.getElementsByClassName('content-reveal');
    const allActives = document.getElementsByClassName('sidebar-active');
    const actionsButtonToggle = this.actions;

    for (const el of allElements) {
      if (el.id !== id) {
        el.classList.remove('content-reveal');
      }
    }
    for (const el of allActives) {
      if (el.id !== activeEl) {
        el.classList.remove('sidebar-active');
      }
    }
    actionsButtonToggle.classList.remove('actions-replaced');
  }

  async loadPerformance() {
    const performanceElement = document.getElementById('sidebar-performance');

    performanceElement.innerHTML += `
      <div class="sidebar-body-content-header">
        <div>Performance</div>
        <icon-plain class="sidebar-body-content-close sidebar-performance-toggle" name="times" view="default"></icon-plain>
      </div>
      <div class="sidebar-body-content-body">
      
      </div>
    `;

    await this.reloadPerformance();
  }

  async reloadPerformance() {
    let performanceHTML = '';

    await State.getPortId().then(async (portId) => {
      if (portId) {
        const portPerformance = (this.fromTime != null || this.toTime != null) ?
            await VisolApi.getPerformancePerPort(portId) :
            await VisolApi.getPerformancePerPort(portId, this.fromTime, this.toTime);

        performanceHTML += `
          <h6 class="fw-bold my-1">Port</h6>
          ${this.generatePerformanceList(portPerformance)}
        `
      }
    });

    await State.getTerminalId().then(async (terminalId) => {
      const terminalPerformance = (this.fromTime != null || this.toTime != null) ?
          await VisolApi.getPerformancePerTerminal(terminalId) :
          await VisolApi.getPerformancePerTerminal(terminalId, this.fromTime, this.toTime);


      performanceHTML += `
        <h6 class="fw-bold my-1">Terminal</h6>
        ${this.generatePerformanceList(terminalPerformance)}
      `
    });

    // Use this selector everywhere
    document.getElementById('sidebar-performance').getElementsByClassName('sidebar-body-content-body')[0].innerHTML = performanceHTML;
  }

  generatePerformanceList(performance) {
    return `
      <div class="sidebar-body-content-list">
        <div class="sidebar-body-content-list-item">
            <div>Cost:</div>
            <div>${(Math.round(performance['total_cost'] * 100) / 100).toLocaleString('en')}</div>
        </div>
        
        <div class="sidebar-body-content-list-item">
            <div>Unscheduled vessels:</div>
            <div>${parseInt(performance['unscheduled_vessels']).toLocaleString('en')}</div>
        </div>
        
        <div class="sidebar-body-content-list-item">
            <div>Scheduled vessels:</div>
            <div>${parseInt(performance['scheduled_vessels']).toLocaleString('en')}</div>
        </div>
        
        <div class="sidebar-body-content-list-item">
            <div>Total amount of vessels:</div>
            <div>${(performance['scheduled_vessels'] + performance['unscheduled_vessels']).toLocaleString('en')}</div>
        </div>
      </div>
    `;
  }

  async loadRecentChanges() {
    if (this.newestChangeTime == null) {
      this.newestChangeTime = new Timestamp().subtract({seconds: 1});
    }
    if (this.oldestChangeTime == null) {
      this.oldestChangeTime = this.newestChangeTime.subtract({hours: 1});
    }
    const recentChanges = await VisolApi.getChangesPerTerminal(await State.getTerminalId(), this.oldestChangeTime.formatted(), this.newestChangeTime.formatted());

    document.getElementById('sidebar-recent-changes').innerHTML = `
      <div class="sidebar-body-content-header">
        <div>Recent changes</div>
        <icon-plain class="sidebar-body-content-close sidebar-recent-changes-toggle" name="times" view="default"></icon-plain>
      </div>
      <div class="sidebar-body-content-list">
        <form class="form-group has-icon-beginning" role="search">
          <span class="fa fa-search form-control-icon"></span>
          <input 
            id="changes-search-bar"
            aria-label="Search" 
            class="form-control" 
            placeholder="Search changes" 
            type="text"
            />
        </form>
      </div>
      <div class="sidebar-body-content-body">
        ${await this.generateChangeCards(recentChanges)}
      </div>
    `;
  }

  async generateChangeCards(changes) {
    let generatedCards = '';

    const sortedChanges = changes.sort((a, b) => {
      // First sort by date descending, then by author ascending, then put schedules first and vessels second
      if (a.date !== b.date) {
        // Sort by date, where older timestamps are first
        return b.date.localeCompare(a.date, undefined, {numeric: true});
      } else if (a.author !== b.author) {
        return a.author.localeCompare(b.author);
      } else if (a.type !== b.type) {
        // -1 means a is before b, 1 means a is after b
        return a.type === 'schedule' ? -1 : 1;
      } else {
        return 0;
      }
    });

    for (const change of sortedChanges) {
      if (this.authorGravatars[change.author] == null) {
        this.authorGravatars[change.author] = await VisolApi.getEmployeeGravatar(change.author);
      }

      if (this.authorProfiles[change.author] == null) {
        const response = await fetch(this.authorGravatars[change.author].profile, {
          method: 'GET',
          'Accept': 'application/json'
        });
        if (!response.ok) {
          ErrorToast.show();
          throw new Error(`${response.status} ${response.statusText}`);
        }
        this.authorProfiles[change.author] = (await response.json())?.entry[0];
      }

      const author = {
        name: this.authorProfiles[change.author]?.name?.formatted
            || `${this.authorProfiles[change.author]?.name?.givenName} ${this.authorProfiles[change.author]?.name?.familyName}`
            || this.authorProfiles[change.author]?.displayName
            || change.author,
        image: `${this.authorGravatars[change.author]?.picture || 'https://via.placeholder.com/60'}?s=60`
      };

      const date = new Timestamp(change.date).toLocal();

      const oldObject = JSON.parse(change.old);
      const newObject = JSON.parse(change.new);


      generatedCards += `
        <div class="card mb-4" vessel="${change.vessel}">
          <div class="card-body">
            <div class="d-flex flex-start align-items-center">
              <img class="rounded shadow-1-strong me-3"
                src="${author.image}" alt="avatar" width="60"
                height="60" />
              <div>
                <h6 class="fw-bold text-primary mb-1">${author.name}</h6>
                <p class="text-muted small mb-0">${date.formatted('YYYY-MM-DD')}</p>
                <p class="text-muted small mb-0">${date.formatted('hh:mm:ss')}</p>
              </div>
            </div>
            
            <p class="small my-1 ${change.reason ? '' : 'text-muted'}">
              ${change.reason ? change.reason : 'no reason provided'}
            </p>
  
            <h6 class="fw-bold my-1">${change.vessel_name} (${change.type.capitalizeFirstLetter()})</h6>
  
            ${oldObject === null ? `
            <p>${change.type.capitalizeFirstLetter()} was created</p>
            ` : ''}
  
            ${newObject === null ? `
            <p>${change.type.capitalizeFirstLetter()} was deleted</p>
            ` : ''}
            
            <dl>
              ${change.type === 'vessel' ? `
              <dt>Name</dt>
              ${SideBar.compareValues(oldObject, newObject, 'name')}
              <dt>Arrival</dt>
              ${SideBar.compareValues(oldObject, newObject, 'arrival', (timestamp) => new Timestamp(timestamp).toLocal().formatted('YYYY-MM-DDThh:mm'))}
              <dt>Deadline</dt>
              ${SideBar.compareValues(oldObject, newObject, 'deadline', (timestamp) => timestamp ? new Timestamp(timestamp).toLocal().formatted('YYYY-MM-DDThh:mm') : 'no deadline')}
              <dt>Containers</dt>
              ${SideBar.compareValues(oldObject, newObject, 'containers')}
              <dt>Cost</dt>
              ${SideBar.compareValues(oldObject, newObject, 'cost_per_hour', (cost) => `${cost} / hour`)}
              <dt>Terminal</dt>
              ${SideBar.compareValues(oldObject, newObject, 'destination')}
              <dt>Dimensions</dt>
              ${SideBar.compareValues(oldObject, newObject, ['width', 'length', 'depth'], (dimensions) => `${dimensions.width}<span>x</span>${dimensions.length}<span>x</span>${dimensions.depth}`)}
              ` : ''}
              ${change.type === 'schedule' ? `
              <dt>Berth</dt>
              ${SideBar.compareValues(oldObject, newObject, 'berth')}
              <dt>Manual</dt>
              ${SideBar.compareValues(oldObject, newObject, 'manual')}
              <dt>Start</dt>
              ${SideBar.compareValues(oldObject, newObject, 'start', (timestamp) => new Timestamp(timestamp).toLocal().formatted('YYYY-MM-DDThh:mm'))}
              <dt>Expected end</dt>
              ${SideBar.compareValues(oldObject, newObject, 'expected_end', (timestamp) => new Timestamp(timestamp).toLocal().formatted('YYYY-MM-DDThh:mm'))}
              ` : ''}
            </dl>
          </div>
        </div>
      `;
    }
    return generatedCards;
  }

  async loadNewerChanges() {
    if (!this.newestChangeTime) {
      return;
    }

    // TODO changes are loaded twice

    const newerChangeTime = new Timestamp().subtract({seconds: 1});

    const terminalId = await State.getTerminalId();
    const recentChanges = await VisolApi.getChangesPerTerminal(terminalId, this.newestChangeTime.formatted(), newerChangeTime.formatted());
    // Find the vessels that were in our local list but aren't anymore
    const currentVessels = await VisolApi.getVesselsPerTerminal(terminalId);
    // We are only interested in the keySet
    const currentVesselIds = Object.keys(currentVessels);
    const removedVessels = Object.keys(this.vessels).filter(vessel => !currentVesselIds.includes(vessel));

    if (removedVessels.length > 0 || recentChanges.length > 0) {
      this.newChangesCallback(currentVessels);
    }

    if (removedVessels.length > 0) {
      // Remove them from the existing schedules
      for (const vesselId of removedVessels) {
        let cards = document.querySelectorAll(`#sidebar-recent-changes .sidebar-body-content-body .card[vessel="${vesselId}"]`);
        if (cards) {
          // Confirm it doesn't exist with the API
          // It could be that it was just moved to a different day
          VisolApi.checkVesselExists(vesselId).then((exists) => {
            if (!exists) {
              cards.forEach(card => card.remove());
            }
          });
        }
      }
    }

    if (recentChanges.length > 0) {
      let element = document.querySelector('#sidebar-recent-changes .sidebar-body-content-body');
      element.innerHTML = await this.generateChangeCards(recentChanges) + element.innerHTML;
    }

    this.newestChangeTime = newerChangeTime;
  }

  async loadOlderChanges() {
    if (!this.oldestChangeTime) {
      return;
    }

    const olderChangeTime = this.oldestChangeTime.subtract({hours: 1});

    const olderChanges = await VisolApi.getChangesPerTerminal(await State.getTerminalId(), olderChangeTime.formatted(), this.oldestChangeTime.formatted());

    if (olderChanges.length > 0) {
      let element = document.querySelector('#sidebar-recent-changes .sidebar-body-content-body');
      element.innerHTML += await this.generateChangeCards(olderChanges);
    }

    this.oldestChangeTime = olderChangeTime;
  }

  async fillChangesCards() {
    const recentChangesCards = document.querySelector('#sidebar-recent-changes .sidebar-body-content-body');
    // TODO stop this while loop after some attempts (endpoint to get latest change on terminal)
    let attempts = 0;
    while (recentChangesCards.offsetHeight + recentChangesCards.scrollTop >= recentChangesCards.scrollHeight - 100 && attempts < 10) {
      await this.loadOlderChanges();
      attempts++;
    }
  }

  async update() {
    // Reload performance


    // Check recent changes
    await this.loadNewerChanges();
  }
}

customElements.define('side-bar', SideBar);
