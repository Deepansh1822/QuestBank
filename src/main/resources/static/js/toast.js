/**
 * Q-Gen Premium Toast Notification System
 */

(function () {
    // Ensure styles are present even if CSS bundle fails
    const style = document.createElement('style');
    style.innerHTML = `
        #qgen-toast-container {
            position: fixed;
            bottom: 2rem;
            right: 2rem;
            z-index: 9999;
            display: flex;
            flex-direction: column;
            gap: 1rem;
            pointer-events: none;
        }

        .qgen-toast {
            background: rgba(15, 23, 42, 0.98);
            backdrop-filter: blur(15px);
            border: 1px solid rgba(255, 255, 255, 0.1);
            color: white;
            padding: 1.25rem 1.5rem;
            border-radius: 16px;
            box-shadow: 0 15px 35px rgba(0, 0, 0, 0.4);
            display: flex;
            align-items: flex-start;
            gap: 1rem;
            min-width: 350px;
            max-width: 500px;
            pointer-events: auto;
            animation: qgen-toast-in 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275) forwards;
            position: relative;
            overflow: hidden;
        }

        .qgen-toast::after {
            content: '';
            position: absolute;
            bottom: 0;
            left: 0;
            height: 4px;
            width: 100%;
            background: rgba(255, 255, 255, 0.1);
        }

        .qgen-toast-progress {
            position: absolute;
            bottom: 0;
            left: 0;
            height: 4px;
            background: var(--toast-accent, #4f46e5);
            width: 100%;
            animation: qgen-toast-progress-bar 5s linear forwards;
        }

        .qgen-toast i.main-icon {
            font-size: 1.5rem;
            flex-shrink: 0;
        }

        .qgen-toast-success { --toast-accent: #10b981; border-left: 4px solid #10b981; }
        .qgen-toast-error { --toast-accent: #ef4444; border-left: 4px solid #ef4444; }
        .qgen-toast-info { --toast-accent: #4f46e5; border-left: 4px solid #4f46e5; }
        .qgen-toast-warning { --toast-accent: #f59e0b; border-left: 4px solid #f59e0b; }

        .qgen-toast-success i.main-icon { color: #10b981; }
        .qgen-toast-error i.main-icon { color: #ef4444; }
        .qgen-toast-info i.main-icon { color: #4f46e5; }
        .qgen-toast-warning i.main-icon { color: #f59e0b; }

        .qgen-toast-content {
            flex: 1;
            font-size: 1rem;
            font-weight: 500;
            line-height: 1.5;
            padding-right: 1.5rem;
        }

        .qgen-toast-close {
            position: absolute;
            top: 0.75rem;
            right: 0.75rem;
            cursor: pointer;
            opacity: 0.4;
            transition: all 0.2s;
            padding: 5px;
            font-size: 1rem;
        }

        .qgen-toast-close:hover {
            opacity: 1;
            transform: rotate(90deg);
        }

        .qgen-toast.hiding {
            animation: qgen-toast-out 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275) forwards;
        }

        @keyframes qgen-toast-in {
            from { opacity: 0; transform: translateY(30px) scale(0.9) rotate(2deg); }
            to { opacity: 1; transform: translateY(0) scale(1) rotate(0deg); }
        }

        @keyframes qgen-toast-out {
            from { opacity: 1; transform: translateX(0) scale(1); }
            to { opacity: 0; transform: translateX(50px) scale(0.9); }
        }

        @keyframes qgen-toast-progress-bar {
            from { width: 100%; }
            to { width: 0%; }
        }
    `;
    document.head.appendChild(style);

    window.showToast = function (message, type = 'info') {
        let container = document.getElementById('qgen-toast-container');
        if (!container) {
            container = document.createElement('div');
            container.id = 'qgen-toast-container';
            document.body.appendChild(container);
        }

        const toast = document.createElement('div');
        toast.className = `qgen-toast qgen-toast-${type}`;

        const icons = {
            success: 'fa-circle-check',
            error: 'fa-circle-exclamation',
            info: 'fa-circle-info',
            warning: 'fa-triangle-exclamation'
        };

        const icon = icons[type] || icons.info;
        const displayMsg = typeof message === 'object' ? (message.message || JSON.stringify(message)) : message;

        toast.innerHTML = `
            <i class="fa-solid ${icon} main-icon"></i>
            <div class="qgen-toast-content">${displayMsg}</div>
            <i class="fa-solid fa-xmark qgen-toast-close"></i>
            <div class="qgen-toast-progress"></div>
        `;

        container.appendChild(toast);

        const remove = () => {
            if (toast.classList.contains('hiding')) return;
            toast.classList.add('hiding');
            setTimeout(() => toast.remove(), 400);
        };

        toast.querySelector('.qgen-toast-close').addEventListener('click', remove);

        // Auto remove after 5s
        setTimeout(remove, 5000);
    };
})();
