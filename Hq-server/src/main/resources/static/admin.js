// This script assumes a backend server is running and provides the necessary admin API endpoints.
// It is designed to replace the functionality of the original AdminCli.java.

// --- DOM Element References ---
const authSection = document.getElementById('auth-section');
const loginFormContainer = document.getElementById('login-form-container');
const registerFormContainer = document.getElementById('register-form-container');
const adminContentSection = document.getElementById('admin-content-section');

const loginForm = document.getElementById('login-form');
const registerForm = document.getElementById('register-form');

const loginMessageBox = document.getElementById('login-message-box');
const registerMessageBox = document.getElementById('register-message-box');

const showRegisterBtn = document.getElementById('show-register-btn');
const showLoginBtn = document.getElementById('show-login-btn');
const logoutBtn = document.getElementById('logout-btn');

const manageDrinksList = document.getElementById('manage-drinks-list');
const refreshReportBtn = document.getElementById('refresh-report-btn');
const salesReportContent = document.getElementById('sales-report-content');

// --- State Management ---
let authToken = null;

// --- Utility Functions ---

/**
 * Displays a message in a specified message box.
 * @param {HTMLElement} boxElement - The message box element.
 * @param {string} message - The message to display.
 * @param {'success' | 'error'} type - The type of message for styling.
 */
function displayMessage(boxElement, message, type) {
    boxElement.textContent = message;
    boxElement.style.display = 'block';
    boxElement.style.color = 'white';
    boxElement.style.backgroundColor = (type === 'success') ? '#16a34a' : '#dc2626'; // Green for success, Red for error

    setTimeout(() => {
        boxElement.style.display = 'none';
    }, 5000);
}

// --- View Toggling ---
function showLoginForm() {
    loginFormContainer.style.display = 'block';
    registerFormContainer.style.display = 'none';
}

function showRegisterForm() {
    loginFormContainer.style.display = 'none';
    registerFormContainer.style.display = 'block';
}

function showAdminDashboard(show) {
    if (show) {
        authSection.style.display = 'none';
        adminContentSection.style.display = 'block';
        logoutBtn.style.display = 'block';
        // Initial data fetch upon showing dashboard
        fetchAndRenderDrinksForManagement();
        fetchAndRenderSalesReport();
    } else {
        authSection.style.display = 'block';
        adminContentSection.style.display = 'none';
        logoutBtn.style.display = 'none';
    }
}

// --- Core Admin Logic ---

/**
 * Fetches all drinks for the admin management view.
 * Corresponds to `viewStock` in AdminCli.
 */
async function fetchAndRenderDrinksForManagement() {
    manageDrinksList.innerHTML = '<p>Loading drinks...</p>';
    if (!authToken) return;

    try {
        // We assume an admin-specific endpoint `/api/admin/drinks` exists that provides full details.
        const response = await fetch('/api/admin/drinks', {
            headers: { 'Authorization': `Bearer ${authToken}` }
        });

        if (!response.ok) {
            if (response.status === 401 || response.status === 403) handleLogout();
            throw new Error('Failed to fetch drinks.');
        }

        const drinks = await response.json();
        renderManageDrinks(drinks);
    } catch (error) {
        console.error('Error fetching drinks for management:', error);
        manageDrinksList.innerHTML = '<p style="color: red;">Could not load drinks.</p>';
    }
}

/**
 * Renders the list of drinks with forms to update them.
 * @param {Array} drinks - An array of drink objects.
 */
function renderManageDrinks(drinks) {
    manageDrinksList.innerHTML = '';
    if (!drinks || drinks.length === 0) {
        manageDrinksList.innerHTML = '<p>No drinks found.</p>';
        return;
    }

    drinks.forEach(drink => {
        const card = document.createElement('div');
        card.className = 'drink-manage-card';
        card.innerHTML = `
            <h4>${drink.drinkName} (ID: ${drink.id})</h4>
            <form class="update-drink-form" data-drink-id="${drink.id}">
                <div>
                    <label for="price-${drink.id}">Price:</label>
                    <input type="number" id="price-${drink.id}" value="${drink.drinkPrice.toFixed(2)}" step="0.01" min="0">
                </div>
                <div>
                    <label for="quantity-${drink.id}">Quantity:</label>
                    <input type="number" id="quantity-${drink.id}" value="${drink.drinkQuantity}" min="0">
                </div>
                <button type="submit">Update</button>
                <div class="message-box" id="drink-message-${drink.id}" style="font-size: 0.8rem; margin-top: 0.5rem;"></div>
            </form>
        `;
        manageDrinksList.appendChild(card);
    });
    
    // Add listeners to new forms
    document.querySelectorAll('.update-drink-form').forEach(form => {
        form.addEventListener('submit', handleUpdateDrink);
    });
}

/**
 * Fetches and renders the sales report.
 * Corresponds to `viewSalesReport` in AdminCli.
 */
async function fetchAndRenderSalesReport() {
    salesReportContent.innerHTML = '<p>Generating report...</p>';
    if (!authToken) return;

    try {
        // Assumes endpoint `/api/admin/sales-report` exists.
        const response = await fetch('/api/admin/sales-report', {
            headers: { 'Authorization': `Bearer ${authToken}` }
        });

        if (!response.ok) {
             if (response.status === 401 || response.status === 403) handleLogout();
            throw new Error('Failed to generate sales report.');
        }
        
        const report = await response.json();
        renderSalesReport(report);

    } catch(error) {
        console.error('Error fetching sales report:', error);
        salesReportContent.innerHTML = '<p style="color: red;">Could not generate report.</p>';
    }
}

/**
 * Renders the sales report data into a table.
 * @param {object} report - The sales report data from the backend.
 */
function renderSalesReport(report) {
    if (!report || !report.drinksSold) {
        salesReportContent.innerHTML = '<p>No sales data available.</p>';
        return;
    }
    
    let tableHtml = `
        <h3>Total Sales: $${report.totalSales.toFixed(2)}</h3>
        <table>
            <thead>
                <tr>
                    <th>Drink Name</th>
                    <th>Quantity Sold</th>
                    <th>Total Revenue</th>
                </tr>
            </thead>
            <tbody>
    `;

    // Sort drinks by total price for better reporting
    const sortedDrinks = Object.entries(report.drinksSold).sort(([, a], [, b]) => b.totalPrice - a.totalPrice);

    for (const [drinkName, sale] of sortedDrinks) {
        tableHtml += `
            <tr>
                <td>${drinkName}</td>
                <td>${sale.quantity}</td>
                <td>$${sale.totalPrice.toFixed(2)}</td>
            </tr>
        `;
    }

    tableHtml += '</tbody></table>';
    salesReportContent.innerHTML = tableHtml;
}


// --- Event Handlers ---

/**
 * Handles the admin login form submission.
 * Corresponds to `login` in AdminCli.
 * @param {Event} event
 */
async function handleLogin(event) {
    event.preventDefault();
    const username = document.getElementById('loginUsername').value.trim();
    const password = document.getElementById('loginPassword').value.trim();

    if (!username || !password) return;

    try {
        // Assumes endpoint `/api/admin/login` exists.
        const response = await fetch('/api/admin/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });
        
        const responseData = await response.text(); // Token is expected as plain text

        if (!response.ok) {
            throw new Error(responseData || 'Login failed');
        }

        authToken = responseData;
        sessionStorage.setItem('adminAuthToken', authToken);
        showAdminDashboard(true);

    } catch (error) {
        console.error('Login error:', error);
        displayMessage(loginMessageBox, error.message, 'error');
    }
}

/**
 * Handles the admin registration form submission.
 * Corresponds to `signup` in AdminCli.
 * @param {Event} event
 */
async function handleRegister(event) {
    event.preventDefault();
    const username = document.getElementById('registerUsername').value.trim();
    const password = document.getElementById('registerPassword').value.trim();

    if (!username || !password) return;

    try {
        // Assumes endpoint `/api/admin/register` exists.
        const response = await fetch('/api/admin/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'Registration failed');
        }
        
        displayMessage(registerMessageBox, 'Registration successful! Please log in.', 'success');
        setTimeout(showLoginForm, 2000); // Switch to login form after a short delay

    } catch (error) {
        console.error('Registration error:', error);
        displayMessage(registerMessageBox, error.message, 'error');
    }
}

/**
 * Handles updating a drink's details.
 * Corresponds to `updateDrinkDetails` in AdminCli.
 * @param {Event} event
 */
async function handleUpdateDrink(event) {
    event.preventDefault();
    const form = event.target;
    const drinkId = form.dataset.drinkId;
    const price = form.querySelector(`#price-${drinkId}`).value;
    const quantity = form.querySelector(`#quantity-${drinkId}`).value;
    const messageBox = form.querySelector(`#drink-message-${drinkId}`);

    const updateData = {
        drinkPrice: parseFloat(price),
        drinkQuantity: parseInt(quantity)
    };
    
    try {
        // Assumes endpoint `/api/admin/drinks/{id}` exists for updates.
        const response = await fetch(`/api/admin/drinks/${drinkId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${authToken}`
            },
            body: JSON.stringify(updateData)
        });

        if (!response.ok) {
            throw new Error('Failed to update drink.');
        }
        
        displayMessage(messageBox, 'Updated!', 'success');
        
    } catch (error) {
        console.error('Update drink error:', error);
        displayMessage(messageBox, error.message, 'error');
    }
}

/**
 * Handles user logout.
 */
function handleLogout() {
    authToken = null;
    sessionStorage.removeItem('adminAuthToken');
    showAdminDashboard(false);
    showLoginForm();
}


// --- Initialization ---
document.addEventListener('DOMContentLoaded', () => {
    // Buttons to toggle between login and register forms
    showRegisterBtn.addEventListener('click', showRegisterForm);
    showLoginBtn.addEventListener('click', showLoginForm);
    
    // Auth form submissions
    loginForm.addEventListener('submit', handleLogin);
    registerForm.addEventListener('submit', handleRegister);
    
    // Admin dashboard actions
    logoutBtn.addEventListener('click', handleLogout);
    refreshReportBtn.addEventListener('click', fetchAndRenderSalesReport);

    // Check for a token in session storage on page load
    const storedToken = sessionStorage.getItem('adminAuthToken');
    if (storedToken) {
        authToken = storedToken;
        showAdminDashboard(true);
    } else {
        showAdminDashboard(false);
    }
});
