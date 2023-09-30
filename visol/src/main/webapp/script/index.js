import './components/select-resource.js';
import './components/datetime-input.js';
import './components/full-button.js';
import './components/full-button-group.js';
import './components/icon-circle.js';
import './components/icon-plain.js';
import './components/modals/vessel-modal.js';
import './components/modals/vessel-modal-create.js';
import './components/modals/vessel-modal-update.js';
import './components/navbar.js';
import './components/planner-berth.js';
import './components/planner-schedule.js';
import './components/unscheduled-vessels.js';
import './components/drop-down.js';
import './components/vessel-card.js';
import './components/error-toast.js';
import './components/planner-header.js';
import './components/side-bar.js';
import './components/berth-closed.js';
import './components/current-time.js';
import './components/export-button.js';

import './pages/landing-page.js';
import './pages/vessel-planner.js';
import './pages/port-authority-researcher.js';
import './pages/terminal-manager.js';
import './pages/login-page.js';

import './requests.js';
import './api.js';
import './auth/employee.js';
import './auth/state.js';
import './auth/token-manager.js';

// Makes it so modulo works properly for negative numbers as well
Number.prototype.mod = function (n) {
  'use strict';
  return ((this % n) + n) % n;
};

String.prototype.capitalizeFirstLetter = function () {
  'use strict';
  return this.charAt(0).toUpperCase() + this.slice(1);
};

// removes duplicates from array
Array.prototype.unique = function() {
  let a = this.concat();
  for(let i = 0; i < a.length; ++i) {
    for(let j = i + 1; j < a.length; ++j) {
      if(a[i] === a[j])
        a.splice(j--, 1);
    }
  }
  return a;
};
