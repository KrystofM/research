import VisolApi from '../api.js';
import Employee from './employee.js';

class TokenManager {
  static TOKEN_KEY = 'token';

  static async obtainToken(email, password) {
    return VisolApi.obtainToken(email, password).then(async (response) => {
      if (response.ok) {
        await response.json().then(data => {
          localStorage.setItem(this.TOKEN_KEY, data.token);
        });
      }
      return response.status;
    });
  }

  static hasToken() {
    return this.getToken() != null;
  }

  static async verifyToken() {
    try {
      await VisolApi.verifyToken();
      return true;
    } catch (error) {
      return false;
    }
  }

  static async authorizeUser(role) {
    const hasToken = TokenManager.hasToken();
    const tokenVerification = await TokenManager.verifyToken();
    const employee = await Employee.getEmployee();

    if (!hasToken ||
        !tokenVerification ||
        employee.role !== role) {
      window.location.assign('./');
    }
  }

  static getToken() {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  static parseToken() {
    if (this.hasToken()) {
      const base64urlPayload = this.getToken().split('.')[1];
      const base64Payload = base64urlPayload.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = window.atob(base64Payload);
      return JSON.parse(jsonPayload);
    } else {
      return null;
    }
  }
}

export default TokenManager;
