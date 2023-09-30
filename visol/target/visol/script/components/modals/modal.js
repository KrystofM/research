import {div, el, elCC} from '../../utils/elements.js';

class Modal extends HTMLElement {
  parentModal;
  loader = div('w-100 d-flex justify-content-center', [
      div('spinner-grow text-primary', [
          elCC('span', {}, 'src-only', [])
      ])
  ]);

  constructor(title, submitBtnText, parentModal = undefined) {
    super();
    this.title = title;
    this.parentModal = parentModal;

    this.submitBtn = document.createElement('button');
    this.submitBtn.className = 'btn btn-primary';
    this.submitBtn.textContent = submitBtnText;
    this.submitBtn.type = 'submit';
  }

  async connectedCallback() {
    this.className = 'modal fade';
    this.setAttribute('tabindex', '-1');

    const dialog = document.createElement('div');
    dialog.className = 'modal-dialog';
    this.appendChild(dialog);

    const contentForm = document.createElement('form');
    contentForm.className = 'modal-content';
    contentForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      this.startLoading();
      await this.submit();
      await this.stopLoading();
      await this.hide();
    });
    dialog.appendChild(contentForm);

    const header = document.createElement('div');
    header.className = 'modal-header';
    contentForm.appendChild(header);

    const title = document.createElement('h5');
    title.className = 'modal-title';
    title.innerText = this.title;
    header.appendChild(title);

    const close = document.createElement('button');
    close.className = 'btn-close';
    close.type = 'button';
    close.onclick = () => this.hide();
    header.appendChild(close);

    this.body = div('modal-body', [await this.buildBody()]);
    contentForm.appendChild(this.body);

    this.footer = div('modal-footer', [await this.buildFooter()]);
    contentForm.appendChild(this.footer);

    this.bootsrapModal = new bootstrap.Modal(this);
  }

  show() {
    if(this.parentModal !== undefined) {
      this.parentModal.hide();
    }
    this.bootsrapModal.show();
  }

  async hide() {
    if(this.parentModal !== undefined) {
      await this.parentModal.refresh();
      this.bootsrapModal.hide();
      this.parentModal.show();
    } else {
      this.bootsrapModal.hide();
    }
  }

  buildBody() {
    throw new Error('Must be overwritten!');
  }

  buildFooter() {
    return this.submitBtn;
  }

  submit() {
    throw new Error('Must be overwritten!');
  }

  async refresh() {
    this.body.removeChild(this.body.firstChild);
    this.body.appendChild(await this.buildBody());
  }

  startLoading() {
    this.footer.removeChild(this.footer.firstChild);
    this.footer.appendChild(this.loader);
  }

  async stopLoading() {
    this.footer.removeChild(this.footer.firstChild);
    this.footer.appendChild(await this.buildFooter());
  }

  labeled(label, input, divClasses = '', required = false) {
    if(required) {
      input.setAttribute('required', '');
    }
    return div(divClasses, [
        required ? elCC('label', {} , 'form-label', [el('b', {textContent: label})])
            : elCC('label', {textContent: label}, 'form-label', []),
        input,
    ])
  }
}

export default Modal;
