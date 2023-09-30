import {div, elCC} from '../utils/elements.js';
import TokenManager from '../auth/token-manager.js';
import Employee from '../auth/employee.js';

class LoginPage extends HTMLElement {
  constructor() {
    super();
  }

  connectedCallback() {
    localStorage.clear();
    this.append(
        div('login-page', [
            div('login-page-main', [
                div('login-page-main-tittle',
                    ['Login into your account']),
                div('login-page-main-inputs', [
                    div('login-page-main-inputs-item',[
                        elCC('label', {
                          for: 'email',
                          name: 'email'
                        }, 'login-page-main-inputs-item-label',
                            ['Email']),
                        div('login-page-main-inputs-item-field', [
                          elCC('input', {
                            id: 'email',
                            type: 'email',
                            required: true,
                            placeholder: 'Type your email address',
                          }, 'login-page-main-inputs-item-field-input')
                        ])
                    ]),
                    div('login-page-main-inputs-item',[
                      elCC('label', {
                        for: 'password'
                      }, 'login-page-main-inputs-item-label',
                          ['Password']),
                      div('login-page-main-inputs-item-field', [
                        elCC('span', {
                          id: 'password-visible'
                        }, 'fa-solid fa-eye fa-lg'),
                        elCC('span', {
                          id: 'password-invisible'
                        }, 'fa-solid fa-eye-slash fa-lg hidden'),
                        elCC('input', {
                          id: 'password',
                          type: 'password',
                          name: 'password',
                          minlength: '10',
                          placeholder: 'Type your password',
                          required: true
                        }, 'login-page-main-inputs-item-field-input')
                      ])
                    ])
                ]),
                div('login-page-main-buttons' ,[
                    elCC('button', {
                      type: 'submit',
                      id: 'signin-button'
                    }, 'login-page-main-buttons-login',
                        ['Sign In']),
                    div('login-page-main-buttons-link',
                        ['Forgot your password?'])
                ])
            ]),
            div('login-page-poster', [
              elCC('img', {
                src: './assets/logo.svg',
                alt: 'logo'
              }, 'login-page-poster-logo'),
              div('login-page-poster-moto',
                  ['Berth control? You should not neglect it.']),
              div('login-page-poster-credits', [
                div('login-page-poster-credits-item', ['Made by']),
                div('login-page-poster-credits-item', ['Thai Ha Bui']),
                div('login-page-poster-credits-item', ['Hessel Stokman']),
                div('login-page-poster-credits-item', ['Mikulas Vanousek']),
                div('login-page-poster-credits-item', ['Niels van Duijl']),
                div('login-page-poster-credits-item', ['Krystof Mitka']),
                div('login-page-poster-credits-item', ['Luka Leer']),
              ])
            ])
        ])
    )

    this.addEventListeners();
  }

  addEventListeners() {
    const signinButton = document.getElementById('signin-button');
    const passwordVisible = document.getElementById('password-visible');
    const passwordInvisible = document.getElementById('password-invisible');
    const emailEl = document.getElementById('email');

    document.addEventListener('keydown', (e) => {
      if (e.code === 'Enter' || e.code === 'NumpadEnter') {
        e.preventDefault();
        this.signInCheck();
      }
    })
    signinButton.addEventListener('click', () =>
        this.signInCheck())
    passwordVisible.addEventListener('click', () =>
        this.togglePasswordShow(passwordVisible, passwordInvisible))
    passwordInvisible.addEventListener('click', () =>
        this.togglePasswordShow(passwordInvisible, passwordVisible))
    emailEl.addEventListener('input', () =>
        this.validateEmail())
  }

  togglePasswordShow() {
    const passwordVisible = document.getElementById('password-visible');
    const passwordInvisible = document.getElementById('password-invisible');
    const passwordField = document.getElementById('password');

    passwordField.type = passwordField.type === 'password' ? 'text' : 'password';
    passwordVisible.classList.toggle('hidden');
    passwordInvisible.classList.toggle('hidden');
  }

  async signInCheck() {
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;

    if (!this.validateEmail()) {
      window.alert("You have to fill in all fields.");
    } else {
      const obtainToken = await TokenManager.obtainToken(email, password);

      if (obtainToken === 200) {
        await this.redirectTo();
      } else if (obtainToken === 404) {
        window.alert("Your email is not connected to any user.");
      } else if (obtainToken === 401) {
        window.alert("Your password is incorrect.")
      } else {
        window.alert("We are probably having trouble with the server, please contact for help. Good luck.");
      }
    }
  }

  async redirectTo() {
    const employee = await Employee.getEmployee();
    if (employee.role === Employee.ROLE.vessel_planner) {
      window.location.replace('./vessel_planner.html');
    } else if (employee.role === Employee.ROLE.terminal_manager) {
      window.location.replace('./terminal_manager.html');
    } else if (employee.role === Employee.ROLE.port_authority) {
      window.location.replace('./port_authority.html');
    } else if (employee.role === Employee.ROLE.researcher) {
      window.location.replace('./researcher.html');
    }
  }

  validateEmail() {
    const emailEL = document.getElementById('email');
    const password = document.getElementById('password').value;
    const validRegex = /^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\.[a-zA-Z0-9-]+)*$/;

    if (emailEL.value.match(validRegex)) {
      emailEL.classList.toggle('invalid', false);
      return (password !== '');
    } else {
      emailEL.classList.toggle('invalid', true);
      return false;
    }
  }
}

customElements.define('login-page', LoginPage);
