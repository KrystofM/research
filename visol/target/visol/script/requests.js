import ErrorToast from './components/error-toast.js';
import TokenManager from './auth/token-manager.js';

class Requests {
  static baseUrl = '/api/v1';
  static headers = {
    ...{
      'Content-Type': 'application/json',
      'Accept': 'application/json',
    }, ...(TokenManager.hasToken() ? {
      'Authorization': 'Bearer ' + TokenManager.getToken(),
    } : {})
  };

  static async postData(path, data = {}, headers = {}, silent = false, ...query) {
    const p = this.baseUrl + path + Requests.createQueryString(query);
    const response = await fetch(p, {
      method: 'POST',
      headers: {...Requests.headers, ...headers},
      body: JSON.stringify(data),
    });
    if (!silent && !response.ok) {
      ErrorToast.show();
      throw new Error(`${response.status} ${response.statusText}`);
    }
    return response;
  }

  static async getData(path, headers = {}, ...query) {
    const p = this.baseUrl + path + Requests.createQueryString(query);
    const response = await fetch(p, {
      method: 'GET',
      headers: {...Requests.headers, ...headers},
    });
    if (!response.ok) {
      ErrorToast.show();
      throw new Error(`${response.status} ${response.statusText}`);
    }
    return response.json();
  }

  static async getDataRaw(path, headers = {}, ...query) {
    const p = this.baseUrl + path + Requests.createQueryString(query);
    return fetch(p, {
      method: 'GET',
      headers: {...Requests.headers, ...headers},
    });
  }

  static async putData(path, data, headers = {}) {
    const response = await fetch(this.baseUrl + path, {
      method: 'PUT',
      // Extend standard headers with custom headers
      headers: {...Requests.headers, ...headers},
      body: JSON.stringify(data),
    });
    if (!response.ok) {
      ErrorToast.show();
      throw new Error(`${response.status} ${response.statusText}`);
    }
    return response.json();
  }

  static createQueryString(query) {
    if (query.length % 2 === 1) {
      throw new Error('Wrong amount of parameters for creating query.');
    }
    let result = '';
    for (let i = 0; i < query.length / 2; i++) {
      if (query[i * 2 + 1] !== null) {
        let bind = i === 0 ? '?' : '&';
        result += bind + query[i * 2] + '=' + query[i * 2 + 1];
      }
    }
    return result;
  }

  static async deleteData(path, headers = {}) {
    const res = await fetch(this.baseUrl + path, {
      method: 'DELETE',
      headers: {...Requests.headers, ...headers},
    });
    if (!res.ok) {
      ErrorToast.show();
      throw new Error(res.statusText);
    }
    return res;
  }
}

export default Requests;
