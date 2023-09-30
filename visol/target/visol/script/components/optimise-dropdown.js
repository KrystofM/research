import FullButton from './full-button.js';

class OptimiseDropdown extends HTMLElement {
  callBack;
  name;

  constructor() {
    super();
  }

  connectedCallback() {
    this.innerHTML = `
      <div class="dropdown">
        <div
            data-bs-toggle="dropdown"
            aria-expanded="false"
            id="${this.name}">
          <full-button
              icon="fire"
              view="${FullButton.VIEW.secondary}"
              size="${FullButton.SIZE.medium}">
            Optimise
          </full-button>
        </div>
  
        <div
            aria-labelledby="dropdownMenu"
            class="dropdown-menu dropdown-menu-end view-${this.name} p-0 table-responsive">
          <table class="table align-middle text-nowrap text-center mb-0">
            <thead>
              <tr>
                <th scope="col">Automatic</th>
                <th scope="col">+ Manual</th>
                <th scope="col">
                  <span class="d-inline-block" style="width: 2ch;">
                    <!-- Empty block to preserve column width -->
                  </span>
                </th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td unscheduled="false" manual="false">
                  <icon-plain name="fire-flame-simple"></icon-plain>
                </td>
                <td unscheduled="false" manual="true">
                  <icon-plain name="fire-flame-curved"></icon-plain>
                </td>
                <th scope="row" class="">Scheduled</th>
              </tr>
              <tr>
                <td unscheduled="true" manual="false">
                  <icon-plain name="fire-burner"></icon-plain>
                </td>
                <td unscheduled="true" manual="true">
                  <icon-plain name="dumpster-fire"></icon-plain>
                </td>
                <th scope="row">+ Unscheduled</th>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    `

    Array.from(this.getElementsByTagName('td')).forEach(el => {
      el.addEventListener('click', () => {
        this.callBack(el.getAttribute('unscheduled') === 'true', el.getAttribute('manual') === 'true');
      });
    });
  }
}

customElements.define('optimise-dropdown', OptimiseDropdown);

export default OptimiseDropdown;
