'use strict';

import storage from './storage';

class Notification {
    constructor (notification) {
        this.notification = notification;
        this.notificationClose = notification.querySelector('.js-close-notification');
    }

    init() {
        if (!storage.getCookie('importantNotice')) {
            this.notification.classList.remove('hidden');
        }

        if (this.notificationClose) {
            this.notificationClose.style.display = 'block';

            this.notificationClose.addEventListener('click', () => {
                this.notification.parentNode.removeChild(this.notification);

                storage.setCookie(
                    storage.categories.preferences,
                    'importantNotice',
                    true,
                    1
                );
            });
        }
    }
}

export default Notification;
