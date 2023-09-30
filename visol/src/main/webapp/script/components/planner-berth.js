import IconCircle from './icon-circle.js';

class PlannerBerth extends HTMLElement {
  constructor() {
    super();
  }

  connectedCallback() {
    this.classList.add('planner-berths-berth');
    this.innerHTML = `
        <div class="planner-berths-berth-header">
          <div class="planner-berths-berth-header-info">
            <div class="planner-berths-berth-header-info-left">
                <icon-circle 
                  view="${IconCircle.VIEW.berth}" 
                  name="anchor"></icon-circle>
                <div 
                    class="planner-berths-berth-header-info-name">
                    Berth ${this.id}</div>
              </div>
              <div class="planner-berths-berth-header-info-details">
    <p class="planner-berths-berth-header-info-details-item">${this.data.width
}<span>x</span>${this.data.length}<span>x</span>${this.data.depth}</p>
                <p class="planner-berths-berth-header-info-details-item">
                  ${this.data.unload_speed} con/h</p>
              </div>
          </div>
        </div>
        <berth-closed
          open="${this.data.open}"
          close="${this.data.close}"
          plannerView="${this.plannerView}"></berth-closed>
        <div
            class="planner-berths-berth-ships" 
            id="berth-ships-${this.id}">
        </div>`;
  }

  get data() {
    return this._data;
  }

  set data(newVal) {
    this._data = newVal;
  }

  get plannerView() {
    return this._plannerView;
  }

  set plannerView(newVal) {
    this._plannerView = newVal;
  }
}

customElements.define('planner-berth', PlannerBerth);

export default PlannerBerth;
