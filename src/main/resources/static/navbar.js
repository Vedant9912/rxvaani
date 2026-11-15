// Shared Navigation Component for RxVaani Clinical EHR
function renderNavbar(activePage) {
  const doctorName = localStorage.getItem('rx_doctor_name') || 'Dr. Vedant Sharma, MBBS, MD';
  const clinicName = localStorage.getItem('rx_clinic_name') || 'Aarogya Clinic & Diagnostics';

  const navItems = [
    { id: 'home', label: 'Home', path: '/', icon: '🏠' },
    { id: 'video', label: 'Video Demo', path: '/video', icon: '🎬' },
    { id: 'consultation', label: 'OPD Room', path: '/consultation', icon: '🩺' },
    { id: 'studio', label: 'Voice Studio', path: '/studio', icon: '🎙️' },
    { id: 'prescription', label: 'Prescription Pad', path: '/prescription', icon: '📄' },
    { id: 'records', label: 'Patient Records', path: '/records', icon: '📋' },
    { id: 'settings', label: 'Settings', path: '/settings', icon: '⚙️' }
  ];

  return `
    <header class="bg-white border-b border-slate-200 sticky top-0 z-40 shadow-xs">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        
        <!-- Logo -->
        <a href="/" class="flex items-center space-x-3 text-decoration-none">
          <div class="w-10 h-10 rounded-xl bg-gradient-to-tr from-sky-600 to-teal-500 flex items-center justify-center text-white font-black text-xl shadow-md shadow-sky-500/20">
            ℞
          </div>
          <div>
            <div class="flex items-center space-x-2">
              <span class="text-xl font-extrabold tracking-tight text-slate-900">RxVaani</span>
              <span class="text-[10px] font-bold px-2 py-0.5 rounded-full bg-emerald-50 text-emerald-700 border border-emerald-200 flex items-center space-x-1">
                <span class="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse"></span>
                <span>Gnani Prisma v3.0</span>
              </span>
            </div>
            <p class="text-[11px] text-slate-500 font-medium hidden sm:block">
              AI Clinical EHR & Prescription Scribe
            </p>
          </div>
        </a>

        <!-- Navigation Links -->
        <nav class="hidden md:flex items-center space-x-1">
          ${navItems.map(item => `
            <a href="${item.path}" class="px-3 py-1.5 rounded-xl text-xs font-bold transition flex items-center space-x-1.5 ${
              activePage === item.id 
                ? 'bg-sky-50 text-sky-700 border border-sky-200 shadow-2xs' 
                : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'
            }">
              <span>${item.icon}</span>
              <span>${item.label}</span>
            </a>
          `).join('')}
        </nav>

        <!-- Right Side CTA / Profile -->
        <div class="flex items-center space-x-3">
          <a href="/consultation" class="px-3.5 py-1.5 rounded-xl bg-sky-600 hover:bg-sky-500 text-white font-bold text-xs shadow-sm transition hidden sm:flex items-center space-x-1.5">
            <span>+ New Consultation</span>
          </a>
          <div class="hidden lg:flex flex-col text-right">
            <span class="text-xs font-bold text-slate-900">${doctorName}</span>
            <span class="text-[11px] text-slate-500 font-medium">${clinicName}</span>
          </div>
        </div>

      </div>
    </header>
  `;
}
