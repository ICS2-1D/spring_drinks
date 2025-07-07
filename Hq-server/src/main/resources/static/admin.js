// DOM Element References
const authSection = document.getElementById('auth-section');
const loginFormContainer = document.getElementById('login-form-container');
const registerFormContainer = document.getElementById('register-form-container');
const loginForm = document.getElementById('login-form');
const registerForm = document.getElementById('register-form');
const logoutBtn = document.getElementById('logout-btn');
const loginMessageBox = document.getElementById('login-message-box');
const registerMessageBox = document.getElementById('register-message-box');

// Main Menu
const mainMenuSection = document.getElementById('main-menu-section');
const menuViewStockBtn = document.getElementById('menu-view-stock');
const menuUpdateDrinksBtn = document.getElementById('menu-update-drinks');
const menuViewSalesBtn = document.getElementById('menu-view-sales');
const menuLogoutBtn = document.getElementById('menu-logout');

// Admin Content Area
const adminMainContentArea = document.getElementById('admin-main-content-area');
const backToMenuBtn = document.getElementById('back-to-menu-btn');

// Admin Content Sections
const manageDrinksSection = document.getElementById('manage-drinks-section');
const salesReportSection = document.getElementById('sales-report-section');
const stockViewSection = document.getElementById('stock-view-section');

// Specific element references
const manageDrinksList = document.getElementById('manage-drinks-list');
const viewStockList = document.getElementById('view-stock-list');
const salesReportContent = document.getElementById('sales-report-content');
const reportMessageBox = document.getElementById('report-message-box');
const branchSelect = document.getElementById('branch-select');
const getBranchReportBtn = document.getElementById('get-branch-report-btn');
const getConsolidatedReportBtn = document.getElementById('get-consolidated-report-btn');

const showRegisterBtn = document.getElementById('show-register-btn');
const showLoginBtn = document.getElementById('show-login-btn');

// Global state
let authToken = null;

/**
 * Displays a message in a specified message box with appropriate styling.
 * @param {HTMLElement} boxElement - The DOM element for the message box.
 * @param {string} message - The message to display.
 * @param {'success' | 'error' | 'info'} type - The type of message for styling.
 */
function displayMessage(boxElement, message, type) {
    boxElement.textContent = message;
    boxElement.classList.remove('bg-brand-success', 'bg-brand-danger', 'bg-brand-accent', 'text-white');

    if (type === 'success') {
        boxElement.classList.add('bg-brand-success', 'text-white');
    } else if (type === 'error') {
        boxElement.classList.add('bg-brand-danger', 'text-white');
    } else { // 'info'
        boxElement.classList.add('bg-brand-accent', 'text-white');
    }
    boxElement.classList.remove('hidden');

    setTimeout(() => boxElement.classList.add('hidden'), 5000);
}

// --- Authentication ---

async function handleLogin(event) {
    event.preventDefault();
    const username = document.getElementById('loginUsername').value.trim();
    const password = document.getElementById('loginPassword').value.trim();

    if (!username || !password) {
        displayMessage(loginMessageBox, 'Please enter both username and password.', 'error');
        return;
    }

    displayMessage(loginMessageBox, 'Logging in...', 'info');

    try {
        const response = await fetch('/admin/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password }),
        });

        if (response.ok) {
            authToken = await response.text();
            sessionStorage.setItem('adminAuthToken', authToken);
            displayMessage(loginMessageBox, 'Login successful!', 'success');
            showMainMenu();
        } else {
            const errorText = await response.text();
            displayMessage(loginMessageBox, `Login failed: ${errorText}`, 'error');
            authToken = null;
        }
    } catch (error) {
        console.error('Error during login:', error);
        displayMessage(loginMessageBox, 'An unexpected error occurred during login.', 'error');
    }
}

async function handleRegister(event) {
    event.preventDefault();
    const username = document.getElementById('registerUsername').value.trim();
    const password = document.getElementById('registerPassword').value.trim();
    const confirmPassword = document.getElementById('confirmPassword').value.trim();

    if (!username || !password || !confirmPassword) {
        displayMessage(registerMessageBox, 'Please fill in all fields.', 'error');
        return;
    }
    if (password !== confirmPassword) {
        displayMessage(registerMessageBox, 'Passwords do not match.', 'error');
        return;
    }

    displayMessage(registerMessageBox, 'Registering...', 'info');

    try {
        const response = await fetch('/admin/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password }),
        });

        if (response.ok) {
            displayMessage(registerMessageBox, 'Registration successful! You can now log in.', 'success');
            registerForm.reset();
            showLoginForm();
        } else {
            const errorText = await response.text();
            displayMessage(registerMessageBox, `Registration failed: ${errorText}`, 'error');
        }
    } catch (error) {
        console.error('Error during registration:', error);
        displayMessage(registerMessageBox, 'An unexpected error occurred.', 'error');
    }
}

function handleLogout() {
    authToken = null;
    sessionStorage.removeItem('adminAuthToken');
    mainMenuSection.classList.add('hidden');
    adminMainContentArea.classList.add('hidden');
    logoutBtn.classList.add('hidden');
    authSection.classList.remove('hidden');
    showLoginForm();
    loginForm.reset();
    registerForm.reset();
}

// --- UI Navigation & View Management ---

function showMainMenu() {
    authSection.classList.add('hidden');
    adminMainContentArea.classList.add('hidden');
    mainMenuSection.classList.remove('hidden');
    logoutBtn.classList.remove('hidden');
    hideAllAdminSubSections();
}

function showAdminContentSection(sectionToShow) {
    mainMenuSection.classList.add('hidden');
    hideAllAdminSubSections();
    adminMainContentArea.classList.remove('hidden');
    sectionToShow.classList.remove('hidden');
}

function hideAllAdminSubSections() {
    [manageDrinksSection, salesReportSection, stockViewSection].forEach(sec => sec.classList.add('hidden'));
}

function showLoginForm() {
    loginFormContainer.classList.remove('hidden');
    registerFormContainer.classList.add('hidden');
    loginMessageBox.classList.add('hidden');
    registerMessageBox.classList.add('hidden');
}

function showRegisterForm() {
    registerFormContainer.classList.remove('hidden');
    loginFormContainer.classList.add('hidden');
    loginMessageBox.classList.add('hidden');
    registerMessageBox.classList.add('hidden');
}

// --- API Calls & Rendering ---

async function fetchAndRender(endpoint, renderer, loadingElement, errorMessage) {
    loadingElement.innerHTML = `<p class="text-brand-text-dim text-center py-4">Loading...</p>`;
    if (!authToken) {
        displayMessage(loginMessageBox, 'Authentication required.', 'error');
        handleLogout();
        return;
    }

    try {
        const response = await fetch(endpoint, {
            headers: { 'Authorization': `Bearer ${authToken}` }
        });

        if (!response.ok) {
            if (response.status === 401 || response.status === 403) handleLogout();
            const errorText = await response.text();
            throw new Error(errorText || `HTTP error! Status: ${response.status}`);
        }
        const data = await response.json();
        renderer(data);
    } catch (error) {
        console.error(errorMessage, error);
        loadingElement.innerHTML = `<p class="text-brand-danger text-center py-4">${errorMessage}: ${error.message}</p>`;
    }
}

// View Stock
function renderViewStock(drinks) {
    viewStockList.innerHTML = '';
    if (!drinks || drinks.length === 0) {
        viewStockList.innerHTML = '<p class="text-brand-text-dim text-center py-4">No drinks in stock.</p>';
        return;
    }
    const table = document.createElement('table');
    table.className = 'min-w-full bg-brand-bg border border-brand-border rounded-lg overflow-hidden';
    table.innerHTML = `
        <thead class="bg-brand-card">
            <tr>
                <th class="py-3 px-4 text-left text-sm font-semibold text-brand-text-light">Drink Name</th>
                <th class="py-3 px-4 text-left text-sm font-semibold text-brand-text-light">Price</th>
                <th class="py-3 px-4 text-left text-sm font-semibold text-brand-text-light">Quantity</th>
            </tr>
        </thead>
        <tbody>
            ${drinks.map(drink => `
                <tr class="border-b border-brand-border last:border-b-0 hover:bg-brand-card/50">
                    <td class="py-3 px-4 text-white">${drink.drinkName}</td>
                    <td class="py-3 px-4 text-white">KSH ${drink.drinkPrice.toFixed(2)}</td>
                    <td class="py-3 px-4 text-white">${drink.drinkQuantity}</td>
                </tr>
            `).join('')}
        </tbody>`;
    viewStockList.appendChild(table);
}

// Update Drinks
function renderManageDrinks(drinks) {
    manageDrinksList.innerHTML = '';
    if (!drinks || drinks.length === 0) {
        manageDrinksList.innerHTML = '<p class="text-brand-text-dim text-center py-4">No drinks found.</p>';
        return;
    }
    drinks.forEach(drink => {
        const drinkCard = document.createElement('div');
        drinkCard.className = 'bg-brand-bg border border-brand-border rounded-xl p-4';
        drinkCard.innerHTML = `
            <h3 class="text-lg font-bold text-white mb-3">${drink.drinkName}</h3>
            <form class="update-drink-form space-y-3" data-drink-id="${drink.id}">
                <div>
                    <label for="price-${drink.id}" class="block text-brand-text-dim text-xs font-medium mb-1">Price</label>
                    <input type="number" id="price-${drink.id}" value="${drink.drinkPrice.toFixed(2)}" step="0.01" min="0" required class="w-full bg-brand-card border border-brand-border rounded-lg px-3 py-2 text-white focus:outline-none focus:ring-1 focus:ring-brand-accent">
                </div>
                <div>
                    <label for="quantity-${drink.id}" class="block text-brand-text-dim text-xs font-medium mb-1">Quantity</label>
                    <input type="number" id="quantity-${drink.id}" value="${drink.drinkQuantity}" min="0" required class="w-full bg-brand-card border border-brand-border rounded-lg px-3 py-2 text-white focus:outline-none focus:ring-1 focus:ring-brand-accent">
                </div>
                <button type="submit" class="w-full bg-brand-accent hover:bg-brand-accent-hover text-white font-semibold py-2 rounded-lg transition">Update</button>
                <div id="drink-message-box-${drink.id}" class="mt-2 text-center text-xs hidden"></div>
            </form>`;
        manageDrinksList.appendChild(drinkCard);
    });
    document.querySelectorAll('.update-drink-form').forEach(form => form.addEventListener('submit', handleUpdateDrink));
}

async function handleUpdateDrink(event) {
    event.preventDefault();
    const form = event.target;
    const drinkId = form.dataset.drinkId;
    const drinkMessageBox = form.querySelector(`#drink-message-box-${drinkId}`);
    const updatedData = {
        id: parseInt(drinkId),
        drinkPrice: parseFloat(form.querySelector(`[id^="price-"]`).value),
        drinkQuantity: parseInt(form.querySelector(`[id^="quantity-"]`).value)
    };

    displayMessage(drinkMessageBox, 'Updating...', 'info');

    try {
        // CORRECTED ENDPOINT
        const response = await fetch(`/admin/drinks/${drinkId}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${authToken}` },
            body: JSON.stringify(updatedData)
        });
        if (response.ok) {
            displayMessage(drinkMessageBox, 'Updated!', 'success');
        } else {
            const errorText = await response.text();
            throw new Error(errorText || 'Failed to update drink');
        }
    } catch (error) {
        console.error('Error updating drink:', error);
        displayMessage(drinkMessageBox, error.message, 'error');
    }
}

// Sales Report
function fetchSalesReport(branch = null) {
    // CORRECTED ENDPOINT LOGIC
    const endpoint = branch ? `/reports/branch/${branch}` : '/reports/consolidated';
    fetchAndRender(endpoint, (data) => renderSalesReport(data, !branch), salesReportContent, 'Failed to load sales report');
}

function renderSalesReport(report, isConsolidated) {
    salesReportContent.innerHTML = '';
    if (isConsolidated) {
        // Render Consolidated Report
        const reportContainer = document.createElement('div');
        reportContainer.innerHTML = `
            <div class="bg-brand-bg p-4 rounded-lg mb-4 border border-brand-border">
                <h3 class="text-xl font-bold text-white">Consolidated Report</h3>
                <p class="text-2xl font-bold text-brand-success">Grand Total: KSH ${report.grandTotalSales.toFixed(2)}</p>
            </div>
        `;
        if (report.salesByBranch && Object.keys(report.salesByBranch).length > 0) {
            for (const [branchName, branchReport] of Object.entries(report.salesByBranch)) {
                const branchDiv = document.createElement('div');
                branchDiv.className = 'mb-6';
                branchDiv.innerHTML = `<h4 class="text-lg font-semibold text-brand-accent mb-2">Branch: ${branchName}</h4>`;
                branchDiv.appendChild(createSingleReportTable(branchReport));
                reportContainer.appendChild(branchDiv);
            }
        } else {
            reportContainer.innerHTML += '<p class="text-brand-text-dim text-center py-4">No sales data available for any branch.</p>';
        }
        salesReportContent.appendChild(reportContainer);
    } else {
        // Render Single Branch Report
        salesReportContent.appendChild(createSingleReportTable(report));
    }
}

function createSingleReportTable(report) {
    const container = document.createElement('div');
    const table = document.createElement('table');
    table.className = 'min-w-full bg-brand-bg border border-brand-border rounded-lg overflow-hidden';
    
    let tableRows = '<p class="p-4 text-brand-text-dim">No sales recorded for this period.</p>';
    if (report.drinksSold && Object.keys(report.drinksSold).length > 0) {
        tableRows = Object.entries(report.drinksSold)
            .sort(([, a], [, b]) => b.totalPrice - a.totalPrice)
            .map(([drinkName, sale]) => `
                <tr class="border-b border-brand-border last:border-b-0 hover:bg-brand-card/50">
                    <td class="py-3 px-4 text-white">${drinkName}</td>
                    <td class="py-3 px-4 text-white">${sale.quantity}</td>
                    <td class="py-3 px-4 text-white font-semibold">KSH ${sale.totalPrice.toFixed(2)}</td>
                </tr>
            `).join('');
    }

    table.innerHTML = `
        <thead class="bg-brand-card">
            <tr>
                <th class="py-2 px-4 text-left text-xs font-semibold text-brand-text-light uppercase tracking-wider">Drink</th>
                <th class="py-2 px-4 text-left text-xs font-semibold text-brand-text-light uppercase tracking-wider">Qty Sold</th>
                <th class="py-2 px-4 text-left text-xs font-semibold text-brand-text-light uppercase tracking-wider">Revenue</th>
            </tr>
        </thead>
        <tbody>${tableRows}</tbody>`;

    container.innerHTML = `<p class="text-lg font-bold text-white mb-2">Total Branch Sales: <span class="text-brand-accent">KSH ${report.totalSales.toFixed(2)}</span></p>`;
    container.appendChild(table);
    return container;
}


// --- Event Listeners ---
document.addEventListener('DOMContentLoaded', () => {
    authToken = sessionStorage.getItem('adminAuthToken');
    if (authToken) {
        showMainMenu();
    } else {
        showLoginForm();
    }
});

loginForm.addEventListener('submit', handleLogin);
registerForm.addEventListener('submit', handleRegister);
showRegisterBtn.addEventListener('click', showRegisterForm);
showLoginBtn.addEventListener('click', showLoginForm);
logoutBtn.addEventListener('click', handleLogout);
backToMenuBtn.addEventListener('click', showMainMenu);

// Menu buttons
menuViewStockBtn.addEventListener('click', (e) => {
    e.preventDefault();
    showAdminContentSection(stockViewSection);
    fetchAndRender('/drinks', renderViewStock, viewStockList, 'Failed to load stock.');
});

menuUpdateDrinksBtn.addEventListener('click', (e) => {
    e.preventDefault();
    showAdminContentSection(manageDrinksSection);
    fetchAndRender('/drinks', renderManageDrinks, manageDrinksList, 'Failed to load drinks.');
});

menuViewSalesBtn.addEventListener('click', (e) => {
    e.preventDefault();
    showAdminContentSection(salesReportSection);
    // Clear previous report content when navigating to the section
    salesReportContent.innerHTML = '<p class="text-brand-text-dim text-center py-4">Select a report type to view data.</p>';
});

menuLogoutBtn.addEventListener('click', (e) => {
    e.preventDefault();
    handleLogout();
});

// Sales Report buttons
getBranchReportBtn.addEventListener('click', () => fetchSalesReport(branchSelect.value));
getConsolidatedReportBtn.addEventListener('click', () => fetchSalesReport());
