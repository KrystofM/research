import FullButton from './full-button.js';
import {div} from '../utils/elements.js';

class DropDown extends HTMLElement {
  data;
  active;
  callBack;
  name;

  constructor() {
    super();
  }

  connectedCallback() {
    this.render();
  }

  render() {
    if (this.data) {
      this.innerHTML = `
      <div class="dropdown">
        <div 
            data-bs-toggle="dropdown" 
            aria-expanded="false" 
            id="${this.name}">
          <full-button 
            icon="caret-down" 
            view="${FullButton.VIEW.secondary}" 
            size="${this.size ?? FullButton.SIZE.medium}">
            ${this.data[this.active]}
          </full-button>
        </div>
        
        <ul 
            aria-labelledby="dropdownMenu" 
            class="dropdown-menu view-${this.name}">
          ${this.buildList()}
        </ul>
      </div>    
    `;
      this.addEventListeners();
    }
  }

  buildList() {
    let res = '';
    Object.entries(this.data).forEach(([id, val]) => {
      const active = this.active === id ? 'active' : '';
      res += `<li>
                <button 
                    class="dropdown-item ${active}" 
                    type="button"
                    id="${id}">${val}</button>
              </li>`;
    });
    return res;
  }

  addEventListeners() {
    const listItems = document
        .querySelectorAll('.dropdown-menu.view-' +
          this.name + ' .dropdown-item');
    listItems.forEach((el) => {
      el.addEventListener('click', () => {
        this.active = el.id;
        this.render();
        this.callBack(el.id);
      });
    });
  }
}

customElements.define('drop-down', DropDown);
