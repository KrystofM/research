export function div(classes, children = []) {
  return elCC('div', {}, classes, children);
}

export function span(classes, children = []) {
  return elCC('span', {}, classes, children);
}

export function h(size, text, classes = null) {
  return elCC('h' + size, {textContent: text}, classes);
}

export function p(classes, text = '') {
  return elCC('p', {textContent: text}, classes);
}

export function elCC(elem, params, classes = [], children = []) {
  const e = el(elem, params);
  e.className = classes;
  e.append(...children);
  return e;
}

export function el(elem, params) {
  let e = document.createElement(elem);
  Object.assign(e, params);
  return e;
}

export function input (classes, params) { return elCC('input', params, classes) }
