// Get references to relevant DOM elements
const authSection = document.getElementById('auth-section'); // Main container for auth forms
const loginFormContainer = document.getElementById('login-form-container'); // Container for login form
const registerFormContainer = document.getElementById('register-form-container'); // Container for register form

const loginForm = document.getElementById('login-form');
const registerForm = document.getElementById('register-form');
const logoutBtn = document.getElementById('logout-btn');
const loginMessageBox = document.getElementById('login-message-box');
const registerMessageBox = document.getElementById('register-message-box'); // New message box for register form

const adminContentSection = document.getElementById('admin-content-section');
const manageDrinksList = document.getElementById('manage-drinks-list');
const refreshReportBtn = document.getElementById('refresh-report-btn');
const totalSalesSpan = document.getElementById('total-sales');
const drinksSoldReportDiv = document.getElementById('drinks-sold-report');
const reportMessageBox = document.getElementById('report-message-box');

const showRegisterBtn = document.getElementById('show-register-btn'); // Button to switch to register form
const showLoginBtn = document.getElementById('show-login-btn');     // Button to switch to login form


// Variable to store the authentication token after successful login
let authToken = null;

/**
 * Displays a message in a specified message box, with appropriate styling.
 * @param {HTMLElement} boxElement - The DOM element (message box) to display the message in.
 * @param {string} message - The text message to display.
 * @param {'success' | 'error' | 'info'} type - The type of message for styling (e.g., 'success', 'error').
 */
function displayMessage(boxElement, message, type) {
    boxElement.textContent = message; // Set the message text
    // Remove all existing color classes
    boxElement.classList.remove('bg-green-100', 'text-green-800', 'bg-red-100', 'text-red-800', 'bg-blue-100', 'text-blue-800');

    // Add new styling based on message type
    if (type === 'success') {
        boxElement.classList.add('bg-green-100', 'text-green-800');
    } else if (type === 'error') {
        boxElement.classList.add('bg-red-100', 'text-red-800');
    } else { // Default to info
        boxElement.classList.add('bg-blue-100', 'text-blue-800');
    }
    boxElement.classList.remove('hidden'); // Make the message box visible

    // Hide the message after 5 seconds
    setTimeout(() => {
        boxElement.classList.add('hidden');
    }, 5000);
}

/**
 * Handles administrator login.
 * @param {Event} event - The form submission event.
 */
async function handleLogin(event) {
    event.preventDefault(); // Prevent default form submission

    // Use specific IDs for login form inputs
    const username = document.getElementById('loginUsername').value.trim();
    const password = document.getElementById('loginPassword').value.trim();

    if (!username || !password) {
        displayMessage(loginMessageBox, 'Please enter both username and password.', 'error');
        return;
    }

    displayMessage(loginMessageBox, 'Logging in...', 'info');

    try {
        const response = await fetch('/admin/login', { // POST request to admin login endpoint
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ username, password }), // Send username and password
        });

        if (response.ok) {
            const data = await response.text(); // Assuming the token is returned as plain text
            authToken = data; // Store the token
            sessionStorage.setItem('adminAuthToken', authToken); // Persist token in session storage
            displayMessage(loginMessageBox, 'Login successful!', 'success');
            showAdminContent(); // Show admin sections
        } else {
            const errorText = await response.text(); // Get error message from response body
            displayMessage(loginMessageBox, `Login failed: ${errorText}`, 'error');
            authToken = null; // Clear token on failed login
        }
    } catch (error) {
        console.error('Error during login:', error);
        displayMessage(loginMessageBox, 'An unexpected error occurred during login.', 'error');
        authToken = null;
    }
}

/**
 * Handles administrator registration.
 * @param {Event} event - The form submission event.
 */
async function handleRegister(event) {
    event.preventDefault(); // Prevent default form submission

    // Use specific IDs for register form inputs
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
        const response = await fetch('/admin/register', { // POST request to admin register endpoint
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ username, password }), // Send username and password
        });

        if (response.ok) {
            displayMessage(registerMessageBox, 'Registration successful! You can now log in.', 'success');
            registerForm.reset(); // Clear the form
            showLoginForm(); // Switch back to login form
        } else {
            const errorText = await response.text();
            displayMessage(registerMessageBox, `Registration failed: ${errorText}`, 'error');
            console.error('Registration error:', errorText);
        }
    } catch (error) {
        console.error('Error during registration:', error);
        displayMessage(registerMessageBox, 'An unexpected error occurred during registration.', 'error');
    }
}


/**
 * Shows the admin content sections and hides the login/register forms.
 * Also fetches initial data for drinks and sales report.
 */
async function showAdminContent() {
    authSection.classList.add('hidden'); // Hide auth section
    adminContentSection.classList.remove('hidden'); // Show admin content
    logoutBtn.classList.remove('hidden'); // Show logout button

    // Fetch initial data only after successful authentication
    await fetchDrinksForManagement();
    await fetchSalesReport();
}

/**
 * Handles administrator logout.
 */
function handleLogout() {
    authToken = null; // Clear the authentication token
    sessionStorage.removeItem('adminAuthToken'); // Remove token from session storage
    // Hide admin content and show login form
    adminContentSection.classList.add('hidden');
    logoutBtn.classList.add('hidden');
    authSection.classList.remove('hidden'); // Show auth section again
    showLoginForm(); // Ensure login form is shown on logout
    loginForm.reset(); // Clear login form fields
    registerForm.reset(); // Clear register form fields
    // Clear message boxes
    loginMessageBox.classList.add('hidden');
    registerMessageBox.classList.add('hidden');
    reportMessageBox.classList.add('hidden');
}

/**
 * Fetches all drinks for management (including current price and quantity).
 * Displays them with forms to update.
 */
async function fetchDrinksForManagement() {
    manageDrinksList.innerHTML = '<p class="col-span-full text-center text-gray-500">Loading drinks for management...</p>';
    if (!authToken) {
        displayMessage(loginMessageBox, 'You are not authenticated.', 'error');
        return;
    }

    try {
        // Fetch from /drinks endpoint (assuming it allows authenticated access for full details)
        const response = await fetch('/drinks', {
            headers: {
                'Authorization': `Bearer ${authToken}` // Include auth token
            }
        });

        if (!response.ok) {
            if (response.status === 401 || response.status === 403) { // 401 Unauthorized, 403 Forbidden
                displayMessage(loginMessageBox, 'Authentication failed or insufficient permissions. Please log in again.', 'error');
                handleLogout(); // Force logout if token is invalid/expired
                return;
            }
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const drinks = await response.json(); // Parse the JSON response
        renderManageDrinks(drinks); // Render the drinks for management
    } catch (error) {
        console.error('Error fetching drinks for management:', error);
        manageDrinksList.innerHTML = '<p class="col-span-full text-center text-red-500">Failed to load drinks for management. ' + error.message + '</p>';
        displayMessage(loginMessageBox, 'Failed to load drinks.', 'error');
    }
}

/**
 * Renders the fetched drinks for management, each with an update form.
 * @param {Array} drinks - Array of drink objects.
 */
function renderManageDrinks(drinks) {
    manageDrinksList.innerHTML = ''; // Clear existing content
    if (drinks.length === 0) {
        manageDrinksList.innerHTML = '<p class="col-span-full text-center text-gray-500">No drinks found.</p>';
        return;
    }

    drinks.forEach(drink => {
        const drinkCard = document.createElement('div');
        drinkCard.className = 'bg-white rounded-xl shadow-lg p-5 border border-gray-200';
        drinkCard.innerHTML = `
            <h3 class="text-xl font-semibold text-gray-900 mb-2">${drink.drinkName} (ID: ${drink.id})</h3>
            <form class="update-drink-form space-y-3" data-drink-id="${drink.id}">
                <div>
                    <label for="price-${drink.id}" class="block text-gray-700 text-sm font-medium mb-1">Price:</label>
                    <input type="number" id="price-${drink.id}" name="drinkPrice" value="${drink.drinkPrice.toFixed(2)}" step="0.01" min="0.01" required
                           class="shadow-sm appearance-none border rounded-lg w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:ring-1 focus:ring-red-700">
                </div>
                <div>
                    <label for="quantity-${drink.id}" class="block text-gray-700 text-sm font-medium mb-1">Quantity:</label>
                    <input type="number" id="quantity-${drink.id}" name="drinkQuantity" value="${drink.drinkQuantity}" min="0" required
                           class="shadow-sm appearance-none border rounded-lg w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:ring-1 focus:ring-red-700">
                </div>
                <button type="submit"
                        class="w-full bg-red-800 hover:bg-red-900 text-white font-bold py-2 px-4 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-700 focus:ring-offset-2 transition duration-300 ease-in-out">
                    Update Drink
                </button>
                <div id="drink-message-box-${drink.id}" class="mt-2 p-2 text-center rounded-lg text-xs hidden"></div>
            </form>
        `;
        manageDrinksList.appendChild(drinkCard);
    });

    // Add event listeners to all update forms
    document.querySelectorAll('.update-drink-form').forEach(form => {
        form.addEventListener('submit', handleUpdateDrink);
    });
}

/**
 * Handles updating a drink's price and/or quantity.
 * @param {Event} event - The form submission event.
 */
async function handleUpdateDrink(event) {
    event.preventDefault(); // Prevent default form submission

    const form = event.target;
    const drinkId = form.dataset.drinkId; // Get drink ID from form's data attribute
    const priceInput = form.querySelector(`[name="drinkPrice"]`);
    const quantityInput = form.querySelector(`[name="drinkQuantity"]`);
    const drinkMessageBox = form.querySelector(`#drink-message-box-${drinkId}`);

    const updatedDrinkDto = {
        id: parseInt(drinkId), // Ensure ID is parsed as number
        drinkPrice: parseFloat(priceInput.value),
        drinkQuantity: parseInt(quantityInput.value)
    };

    // Basic validation
    if (isNaN(updatedDrinkDto.drinkPrice) || updatedDrinkDto.drinkPrice <= 0) {
        displayMessage(drinkMessageBox, 'Price must be a positive number.', 'error');
        return;
    }
    if (isNaN(updatedDrinkDto.drinkQuantity) || updatedDrinkDto.drinkQuantity < 0) {
        displayMessage(drinkMessageBox, 'Quantity must be a non-negative integer.', 'error');
        return;
    }

    displayMessage(drinkMessageBox, 'Updating...', 'info');

    try {
        // Correct endpoint for updating drink as per AdminController.java
        const response = await fetch(`/admin/drinks/${drinkId}/price`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${authToken}` // Include auth token
            },
            body: JSON.stringify(updatedDrinkDto) // Send updated drink data
        });

        if (response.ok) {
            displayMessage(drinkMessageBox, 'Drink updated successfully!', 'success');
            // Re-fetch drinks to update the displayed list and reflect new quantities in stock
            fetchDrinksForManagement();
        } else {
            const errorText = await response.text();
            if (response.status === 401 || response.status === 403) {
                displayMessage(loginMessageBox, 'Authentication failed or insufficient permissions. Please log in again.', 'error');
                handleLogout();
            } else {
                displayMessage(drinkMessageBox, `Failed to update drink: ${errorText}`, 'error');
            }
            console.error('Error updating drink:', errorText);
        }
    } catch (error) {
        console.error('Network or unexpected error during drink update:', error);
        displayMessage(drinkMessageBox, 'An unexpected error occurred.', 'error');
    }
}

/**
 * Fetches the sales report from the backend and displays it.
 */
async function fetchSalesReport() {
    drinksSoldReportDiv.innerHTML = '<p class="text-gray-500 text-center py-4">Generating sales report...</p>';
    totalSalesSpan.textContent = '$0.00'; // Reset total sales display

    if (!authToken) {
        displayMessage(loginMessageBox, 'You are not authenticated.', 'error');
        return;
    }

    try {
        // Correct endpoint for sales report as per AdminController.java
        const response = await fetch('/admin/sales/report', {
            headers: {
                'Authorization': `Bearer ${authToken}` // Include auth token
            }
        });

        if (!response.ok) {
            if (response.status === 401 || response.status === 403) {
                displayMessage(loginMessageBox, 'Authentication failed or insufficient permissions. Please log in again.', 'error');
                handleLogout();
                return;
            }
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const report = await response.json(); // Parse the JSON sales report
        renderSalesReport(report); // Render the sales report
    } catch (error) {
        console.error('Error fetching sales report:', error);
        drinksSoldReportDiv.innerHTML = '<p class="text-red-500 text-center py-4">Failed to load sales report.</p>';
        displayMessage(reportMessageBox, 'Failed to load sales report.', 'error');
    }
}

/**
 * Renders the sales report data into the HTML.
 * @param {Object} report - The sales report DTO object.
 */
function renderSalesReport(report) {
    totalSalesSpan.textContent = `$${report.totalSales ? report.totalSales.toFixed(2) : '0.00'}`;
    drinksSoldReportDiv.innerHTML = ''; // Clear previous report content

    if (report.drinksSold && Object.keys(report.drinksSold).length > 0) {
        const table = document.createElement('table');
        table.className = 'min-w-full bg-white border border-gray-300 rounded-lg overflow-hidden';
        table.innerHTML = `
            <thead>
                <tr class="bg-gray-100 border-b">
                    <th class="py-3 px-4 text-left text-sm font-semibold text-gray-700">Drink Name</th>
                    <th class="py-3 px-4 text-left text-sm font-semibold text-gray-700">Quantity Sold</th>
                    <th class="py-3 px-4 text-left text-sm font-semibold text-gray-700">Total Revenue</th>
                </tr>
            </thead>
            <tbody id="sales-report-body">
                <!-- Sales rows will go here -->
            </tbody>
        `;
        drinksSoldReportDiv.appendChild(table);

        const tbody = document.getElementById('sales-report-body');

        // Convert Map to array of entries and sort by total sales for display
        // Ensure to handle cases where report.drinksSold might be an empty object or null
        const sortedDrinks = report.drinksSold ? Object.entries(report.drinksSold).sort(([, a], [, b]) => b.totalPrice - a.totalPrice) : [];

        if (sortedDrinks.length > 0) {
            sortedDrinks.forEach(([drinkName, sale]) => {
                const row = document.createElement('tr');
                row.className = 'border-b last:border-b-0 hover:bg-gray-50';
                row.innerHTML = `
                    <td class="py-3 px-4 text-gray-800">${drinkName}</td>
                    <td class="py-3 px-4 text-gray-800">${sale.quantity}</td>
                    <td class="py-3 px-4 text-gray-800">$${sale.totalPrice.toFixed(2)}</td>
                `;
                tbody.appendChild(row);
            });
        } else {
            drinksSoldReportDiv.innerHTML = '<p class="text-gray-500 text-center py-4">No sales data available yet.</p>';
        }

    } else {
        drinksSoldReportDiv.innerHTML = '<p class="text-gray-500 text-center py-4">No sales data available yet.</p>';
    }
}

/**
 * Switches the view to show the login form and hides the register form.
 */
function showLoginForm() {
    loginFormContainer.classList.remove('hidden');
    registerFormContainer.classList.add('hidden');
    // Clear any messages when switching forms
    loginMessageBox.classList.add('hidden');
    registerMessageBox.classList.add('hidden');
}

/**
 * Switches the view to show the register form and hides the login form.
 */
function showRegisterForm() {
    registerFormContainer.classList.remove('hidden');
    loginFormContainer.classList.add('hidden');
    // Clear any messages when switching forms
    loginMessageBox.classList.add('hidden');
    registerMessageBox.classList.add('hidden');
}


// Event Listeners
document.addEventListener('DOMContentLoaded', () => {
    // Check if authToken exists in sessionStorage (for persistence across refresh)
    const storedAuthToken = sessionStorage.getItem('adminAuthToken');
    if (storedAuthToken) {
        authToken = storedAuthToken;
        showAdminContent(); // If token exists, directly show admin content
    } else {
        // If no token, ensure login form is visible initially
        showLoginForm();
    }
});

// Authentication form submissions
loginForm.addEventListener('submit', handleLogin);
registerForm.addEventListener('submit', handleRegister);

// Toggle between login and register forms
showRegisterBtn.addEventListener('click', showRegisterForm);
showLoginBtn.addEventListener('click', showLoginForm);

// Logout button
logoutBtn.addEventListener('click', handleLogout);

// Sales report refresh button
refreshReportBtn.addEventListener('click', fetchSalesReport);
