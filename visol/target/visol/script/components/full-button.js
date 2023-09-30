import IconPlain from './icon-plain.js';

class FullButton extends HTMLElement {
  _view;
  size = FullButton.SIZE.medium;
  children;
  icon;

  static VIEW = {
    primary: 'primary',
    secondary: 'secondary',
    disabled: 'disabled',
  };

  static SIZE = {
    medium: 'medium',
    large: 'large',
  };

  constructor() {
    super();
  }

  connectedCallback() {
    this.children = this.textContent;
    this.icon = this.hasAttribute('icon') ?
        this.getAttribute('icon') : this.icon;
    this.size = this.hasAttribute('size') ?
      this.getAttribute('size') : this.size;
    this.view = this.hasAttribute('view') ?
        this.getAttribute('view') : this.view;
  }

  set view(newVal) {
    this._view = newVal;
    this.render();
  }

  get view() {
    return this._view;
  }

  render() {
    this.style.pointerEvents = this.view === FullButton.VIEW.disabled ?
        'none' : 'all';
    const iconEl = this.icon ?
        `<icon-plain
            name="${this.icon}" 
            view="${IconPlain.VIEW.button}"></icon-plain>` : ``;
    this.innerHTML = `
        <button 
            class="full-button view-${this.view} size-${this.size}"
            type="button">
        ${this.textContent} ${iconEl}</button>
    `;
  }

}

customElements.define('full-button', FullButton);

export default FullButton;
