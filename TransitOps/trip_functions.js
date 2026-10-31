    // ============================================================
    // TRIPS - UPDATED WITH REFRESH AND DISPATCH ICONS
    // ============================================================
    async function loadTrips() {
        const body = document.getElementById('tripTableBody');
        body.innerHTML = '<tr><td colspan="4" style="text-align:center;padding:20px;color:#6a7f9a;"><i class="fas fa-spinner fa-spin"></i> Loading trips...</td></tr>';
        
        const data = await fetchAPI(`${API_BASE}/api/trips`);
        
        if (data && data.length > 0) {
            body.innerHTML = data.map(t => {
                const statusClass = (t.status || 'draft').toLowerCase();
                const statusIcon = {
                    'draft': 'fa-pen',
                    'dispatched': 'fa-rocket',
                    'completed': 'fa-check-circle',
                    'cancelled': 'fa-times-circle'
                }[statusClass] || 'fa-circle';
                
                const statusColor = {
                    'draft': '#6c757d',
                    'dispatched': '#007bff',
                    'completed': '#28a745',
                    'cancelled': '#dc3545'
                }[statusClass] || '#6c757d';

                let actions = '';
                if (t.status === 'DRAFT') {
                    actions = `
                        <button class="btn btn-primary btn-sm" onclick="dispatchTrip(${t.id})" title="Dispatch this trip">
                            <i class="fas fa-rocket"></i> Dispatch
                        </button>
                        <button class="btn btn-danger btn-sm" onclick="cancelTrip(${t.id})" title="Cancel this trip">
                            <i class="fas fa-times"></i>
                        </button>
                    `;
                } else if (t.status === 'DISPATCHED') {
                    actions = `
                        <button class="btn btn-success btn-sm" onclick="completeTrip(${t.id})" title="Complete this trip">
                            <i class="fas fa-check"></i> Complete
                        </button>
                        <button class="btn btn-danger btn-sm" onclick="cancelTrip(${t.id})" title="Cancel this trip">
                            <i class="fas fa-times"></i>
                        </button>
                    `;
                } else if (t.status === 'COMPLETED') {
                    actions = `<span style="color:#28a745;font-weight:500;"><i class="fas fa-check-circle"></i> Completed</span>`;
                } else if (t.status === 'CANCELLED') {
                    actions = `<span style="color:#dc3545;font-weight:500;"><i class="fas fa-times-circle"></i> Cancelled</span>`;
                }

                return `
                    <tr>
                        <td><strong>${t.tripId || 'N/A'}</strong></td>
                        <td>
                            <i class="fas fa-flag" style="color:#3ecf8e;font-size:12px;"></i>
                            ${t.source || ''}
                            <i class="fas fa-arrow-right" style="color:#3ecf8e;font-size:12px;margin:0 6px;"></i>
                            <i class="fas fa-flag-checkered" style="color:#3ecf8e;font-size:12px;"></i>
                            ${t.destination || ''}
                        </td>
                        <td>
                            <span class="status-badge ${statusClass}" style="background:${statusColor}22;color:${statusColor};">
                                <i class="fas ${statusIcon}"></i> ${t.status || 'DRAFT'}
                            </span>
                        </td>
                        <td>
                            <div style="display:flex;gap:6px;flex-wrap:wrap;align-items:center;">
                                ${actions}
                            </div>
                        </td>
                    </tr>
                `;
            }).join('');
        } else {
            body.innerHTML = `
                <tr>
                    <td colspan="4" style="text-align:center;padding:30px;color:#6a7f9a;">
                        <i class="fas fa-inbox" style="font-size:32px;display:block;margin-bottom:10px;color:#dce3ec;"></i>
                        No trips found. Create your first trip!
                    </td>
                </tr>
            `;
        }
    }

    // Auto-refresh every 10 seconds for dispatcher
    let refreshInterval = null;

    function startAutoRefresh() {
        if (refreshInterval) clearInterval(refreshInterval);
        refreshInterval = setInterval(() => {
            const activePage = document.querySelector('.page.active');
            if (activePage && activePage.id === 'page-trips') {
                loadTrips();
            }
        }, 10000);
    }

    // Enhanced dispatch with feedback
    async function dispatchTrip(id) {
        try {
            // Show loading state
            const button = document.querySelector(`button[onclick*="dispatchTrip(${id})"]`);
            if (button) {
                button.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Dispatching...';
                button.disabled = true;
            }
            
            const response = await fetch(`${API_BASE}/api/trips/${id}/dispatch`, { 
                method: 'POST',
                headers: { 'Content-Type': 'application/json' }
            });
            
            if (response.ok) {
                showToast('? Trip dispatched successfully!', 'success');
                loadTrips();
                loadDashboard();
            } else {
                const error = await response.json();
                showToast('? ' + (error.error || 'Failed to dispatch trip'), 'error');
            }
        } catch (e) {
            showToast('? Error dispatching trip', 'error');
        } finally {
            // Reset button
            const button = document.querySelector(`button[onclick*="dispatchTrip(${id})"]`);
            if (button) {
                button.innerHTML = '<i class="fas fa-rocket"></i> Dispatch';
                button.disabled = false;
            }
        }
    }

    // Enhanced complete with feedback
    async function completeTrip(id) {
        const dist = prompt('Enter actual distance (km):');
        if (dist && !isNaN(dist) && parseFloat(dist) > 0) {
            try {
                const response = await fetch(`${API_BASE}/api/trips/${id}/complete?actualDistance=${dist}`, { 
                    method: 'POST' 
                });
                if (response.ok) {
                    showToast('? Trip completed successfully!', 'success');
                    loadTrips();
                    loadDashboard();
                } else {
                    const error = await response.json();
                    showToast('? ' + (error.error || 'Failed to complete trip'), 'error');
                }
            } catch (e) {
                showToast('? Error completing trip', 'error');
            }
        }
    }

    // Enhanced cancel with feedback
    async function cancelTrip(id) {
        if (confirm('Are you sure you want to cancel this trip?')) {
            try {
                const response = await fetch(`${API_BASE}/api/trips/${id}/cancel`, { 
                    method: 'POST' 
                });
                if (response.ok) {
                    showToast('? Trip cancelled successfully!', 'success');
                    loadTrips();
                    loadDashboard();
                } else {
                    const error = await response.json();
                    showToast('? ' + (error.error || 'Failed to cancel trip'), 'error');
                }
            } catch (e) {
                showToast('? Error cancelling trip', 'error');
            }
        }
    }

    // Toast notification
    function showToast(message, type = 'success') {
        const toast = document.createElement('div');
        const colors = {
            success: '#3ecf8e',
            error: '#e06b6b',
            info: '#0b1e33'
        };
        toast.style.cssText = `
            position: fixed;
            bottom: 20px;
            right: 20px;
            background: ${colors[type] || colors.info};
            color: #fff;
            padding: 12px 24px;
            border-radius: 10px;
            font-size: 14px;
            font-weight: 500;
            z-index: 9999;
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
            animation: slideIn 0.3s ease;
            max-width: 400px;
        `;
        toast.textContent = message;
        document.body.appendChild(toast);
        
        setTimeout(() => {
            toast.style.animation = 'slideOut 0.3s ease';
            setTimeout(() => toast.remove(), 300);
        }, 3000);
    }

    // Add animation styles
    const styleSheet = document.createElement("style");
    styleSheet.textContent = `
        @keyframes slideIn {
            from { transform: translateX(100%); opacity: 0; }
            to { transform: translateX(0); opacity: 1; }
        }
        @keyframes slideOut {
            from { transform: translateX(0); opacity: 1; }
            to { transform: translateX(100%); opacity: 0; }
        }
    `;
    document.head.appendChild(styleSheet);

    // Override the navigateTo to start auto-refresh for trips
    const originalNavigateTo = navigateTo;
    navigateTo = function(pageId) {
        originalNavigateTo(pageId);
        if (pageId === 'trips') {
            startAutoRefresh();
            loadTrips();
        }
    };
