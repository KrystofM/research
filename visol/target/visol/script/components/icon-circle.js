class IconCircle extends HTMLElement {
  name;
  view = IconCircle.VIEW.default;

  static VIEW = {
    default: 'default',
    primary: 'primary',
    berth: 'berth',
    closed: 'closed',
    addVessel: 'add-vessel',
    infeasible: 'infeasible',
  };

  constructor() {
    super();
  }

  connectedCallback() {
    const view =
      this.hasAttribute('view') ?
        this.getAttribute('view') : this.view;
    const name = this.hasAttribute('name') ?
        this.getAttribute('name') : this.name;
    this.innerHTML = `
       <div class="icon-circle view-${view}">
        <div class="icon-circle-in">
          <icon-plain name="${name}"></icon-plain>
        </div>
       </div>
    `;
  }
}

customElements.define('icon-circle', IconCircle);

export default IconCircle;
