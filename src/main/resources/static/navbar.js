// Shared Navigation Component for RxVaani Clinical EHR
function renderNavbar(activePage) {
  const doctorName = localStorage.getItem('rx_doctor_name') || 'Dr. Vedant Sharma, MBBS, MD';
  const clinicName = localStorage.getItem('rx_clinic_name') || 'Aarogya Clinic & Diagnostics';

  const navItems = [
    { id: 'home', label: 'Home', path: '/', icon: '🏠' },
    { id: 'consultation', label: 'OPD Room', path: '/consultation', icon: '🩺' },
    { id: 'studio', label: 'Voice Studio', path: '/studio', icon: '🎙️' },
    { id: 'prescription', label: 'Prescription Pad', path: '/prescription', icon: '📄' },
    { id: 'records', label: 'Patient Records', path: '/records', icon: '📋' },
    { id: 'settings', label: 'Settings', path: '/settings', icon: '⚙️' }
  ];

  return `
    <style>
      #rx-navbar {
        background: #000000;
        border-bottom: 1px solid #26364D;
        position: sticky;
        top: 0;
        z-index: 40;
      }
      #rx-navbar .nav-link {
        display: inline-flex;
        align-items: center;
        gap: 0.375rem;
        padding: 0.375rem 0.75rem;
        border-radius: 0.75rem;
        font-size: 0.75rem;
        font-weight: 700;
        color: #94A3B8;
        transition: background 0.2s, color 0.2s;
        text-decoration: none;
        border: 1px solid transparent;
      }
      #rx-navbar .nav-link:hover {
        background: #162338;
        color: #F8FAFC;
      }
      #rx-navbar .nav-link.active {
        background: rgba(14,165,233,0.12);
        color: #0EA5E9;
        border-color: rgba(14,165,233,0.25);
      }
      #rx-navbar .btn-new {
        display: inline-flex;
        align-items: center;
        gap: 0.375rem;
        padding: 0.375rem 0.875rem;
        border-radius: 0.75rem;
        background: #0EA5E9;
        color: #fff;
        font-size: 0.75rem;
        font-weight: 700;
        text-decoration: none;
        transition: background 0.2s, transform 0.15s;
        box-shadow: 0 0 16px rgba(14,165,233,0.25);
      }
      #rx-navbar .btn-new:hover {
        background: #0284C7;
        transform: translateY(-1px);
      }
      #rx-navbar .logo-pill {
        width: 2.5rem;
        height: 2.5rem;
        border-radius: 0.75rem;
        background: linear-gradient(135deg, #0369A1, #0D9488);
        display: flex;
        align-items: center;
        justify-content: center;
        color: white;
        font-size: 1.25rem;
        font-weight: 900;
        box-shadow: 0 4px 14px rgba(14,165,233,0.3);
        flex-shrink: 0;
      }
      #rx-navbar .ai-badge {
        font-size: 0.625rem;
        font-weight: 700;
        padding: 0.125rem 0.5rem;
        border-radius: 9999px;
        background: rgba(20,184,166,0.15);
        color: #2DD4BF;
        border: 1px solid rgba(20,184,166,0.3);
        display: inline-flex;
        align-items: center;
        gap: 0.25rem;
      }
      #rx-navbar .ai-dot {
        width: 0.375rem;
        height: 0.375rem;
        border-radius: 9999px;
        background: #14B8A6;
        animation: rx-pulse 2s cubic-bezier(0.4,0,0.6,1) infinite;
      }
      @keyframes rx-pulse {
        0%, 100% { opacity: 1; }
        50% { opacity: 0.4; }
      }
      #rx-navbar .doc-name { font-size: 0.75rem; font-weight: 700; color: #F8FAFC; }
      #rx-navbar .clinic-name { font-size: 0.6875rem; color: #94A3B8; font-weight: 500; }
    </style>
    <header id="rx-navbar">
      <div style="max-width:80rem;margin:0 auto;padding:0 1rem;height:4rem;display:flex;align-items:center;justify-content:space-between;gap:1rem;">
        
        <!-- Logo -->
        <a href="/" style="display:flex;align-items:center;gap:0.75rem;text-decoration:none;flex-shrink:0;">
          <div class="logo-pill">℞</div>
          <div>
            <div style="display:flex;align-items:center;gap:0.5rem;">
              <span style="font-size:1.25rem;font-weight:800;color:#F8FAFC;letter-spacing:-0.025em;">RxVaani</span>
              <span class="ai-badge">
                <span class="ai-dot"></span>
                <span>AI Powered</span>
              </span>
            </div>
            <p style="font-size:0.6875rem;color:#94A3B8;font-weight:500;display:none;" class="rx-nav-subtitle">AI Clinical EHR &amp; Prescription Scribe</p>
          </div>
        </a>

        <!-- Navigation Links -->
        <nav style="display:none;align-items:center;gap:0.25rem;" id="rx-nav-links">
          ${navItems.map(item => `
            <a href="${item.path}" class="nav-link ${activePage === item.id ? 'active' : ''}">
              <span>${item.icon}</span>
              <span>${item.label}</span>
            </a>
          `).join('')}
        </nav>

        <!-- Right Side -->
        <div style="display:flex;align-items:center;gap:0.75rem;flex-shrink:0;">
          <a href="/consultation" class="btn-new" id="rx-new-btn">
            <span>+ New Consultation</span>
          </a>
          <div style="display:none;flex-direction:column;text-align:right;" id="rx-doc-info">
            <span class="doc-name">${doctorName}</span>
            <span class="clinic-name">${clinicName}</span>
          </div>
        </div>

      </div>
    </header>
    <script>
      (function() {
        var mq = window.matchMedia('(min-width: 768px)');
        var mql = window.matchMedia('(min-width: 1024px)');
        function applyLayout() {
          var nav = document.getElementById('rx-nav-links');
          var docInfo = document.getElementById('rx-doc-info');
          var sub = document.querySelector('.rx-nav-subtitle');
          if (nav) nav.style.display = mq.matches ? 'flex' : 'none';
          if (docInfo) docInfo.style.display = mql.matches ? 'flex' : 'none';
          if (sub) sub.style.display = mq.matches ? 'block' : 'none';
        }
        applyLayout();
        mq.addEventListener('change', applyLayout);
        mql.addEventListener('change', applyLayout);
      })();
    <\/script>
  `;
}
