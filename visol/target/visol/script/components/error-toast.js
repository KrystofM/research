// TODO Miki customize text
class ErrorToast extends HTMLElement {
  static id = 'error-toast';

  static show(content = '<b> A request failed! </b> Try reloading the page!') {
    document.getElementById('toast-body').innerHTML = content;
    bootstrap.Toast.getOrCreateInstance(document.getElementById(ErrorToast.id)).show();
  }

  connectedCallback() {
    this.innerHTML = `
<div class="position-fixed bottom-0 end-0 p-3" style="z-index: 11">
  <div class="toast align-items-center text-white bg-danger border-0" role="alert" 
    aria-live="assertive" aria-atomic="true" id="${ErrorToast.id}">
    <div class="d-flex">
      <div class="toast-body" id="toast-body">
        <b> A request failed! </b> Try reloading the page!
      </div>
      <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" 
        aria-label="Close"></button>
    </div>
  </div>
</div>`;
  }
}

customElements.define('error-toast', ErrorToast);

export default ErrorToast;
