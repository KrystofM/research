class FullButtonGroup extends HTMLElement {
  constructor() {
    super();
  }

  connectedCallback() {
    this.classList.add('btn-group');
    this.attributes.role = 'group';

    let nodesToRemove = []
    let nodesToAdd = [];


    this.childNodes.forEach(child => {
      if (child.tagName === 'FULL-BUTTON') {
        let innerChild = child.firstElementChild;
        innerChild.classList.add('btn');
        innerChild.onclick = child.onclick;
        nodesToRemove.push(child);
        nodesToAdd.push(innerChild);
      }
    });

    // Replace existing child nodes with new child nodes
    console.log(nodesToRemove);
    nodesToRemove.forEach(child => {
      this.removeChild(child);
    });

    console.log(nodesToAdd);
    nodesToAdd.forEach(child => {
      this.appendChild(child);
    });
  }
}

customElements.define('full-button-group', FullButtonGroup);

export default FullButtonGroup;
