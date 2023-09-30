import {div, elCC} from '../utils/elements.js';

class Navbar extends HTMLElement {
	role;
	mid;
	right;

  constructor() {
    super();
  }

  connectedCallback() {
    this.className = 'navigation';
	this.prepend(
		div('navigation-left', [
			div('navigation-left-in', [
				elCC('a', {
					href: './'
				}, 'navigation-brand', [
					elCC('img', {
						src: `./assets/logo-${this.role}.svg`,
						alt: "logo"
					}, 'navigation-brand-logo')
				])
			])
		]),
		div('navigation-mid', this.mid),
		div('navigation-right', [
			div('navigation-right-in', this.right)
		])
	)
  }
}

customElements.define('nav-bar', Navbar);
