// Get references to relevant DOM elements
const authSection = document.getElementById('auth-section'); // Main container for auth forms
const loginFormContainer = document.getElementById('login-form-container'); // Container for login form
const registerFormContainer = document.getElementById('register-form-container'); // Container for register form

const loginForm = document.getElementById('login-form');
const registerForm = document.getElementById('register-form');
const logoutBtn = document.getElementById('logout-btn');
const loginMessageBox = document.getElementById('login-message-box');
const registerMessageBox = document.getElementById('register-message-box'); // New message box for register form

// NEW: Main Menu elements (IDs now refer to <a> tags)
const mainMenuSection = document.getElementById('main-menu-section');
const menuViewStockBtn = document.getElementById('menu-view-stock'); // This is now an <a> tag
const menuUpdatePricesBtn = document.getElementById('menu-update-prices'); // This is now an <a> tag
const menuSetBranchInfoBtn = document.getElementById('menu-set-branch-info'); // This is now an <a> tag
const menuViewSalesBtn = document.getElementById('menu-view-sales'); // This is now an <a> tag
// REMOVED: menuSyncHqBtn (as Sync to HQ is removed from HTML)
const menuLogoutBtn = document.getElementById('menu-logout'); // This is now an <a> tag within the menu

// NEW: Main content area container and back button
const adminMainContentArea = document.getElementById('admin-main-content-area');
const backToMenuBtn = document.getElementById('back-to-menu-btn');

// Existing admin content sections (now specifically targetable)
const manageDrinksSection = document.getElementById('manage-drinks-section'); // Wrapped the manage drinks content
const salesReportSection = document.getElementById('sales-report-section');   // Wrapped the sales report content

// NEW: Placeholder sections for other menu items
const stockViewSection = document.getElementById('stock-view-section');
const branchInfoSection = document.getElementById('branch-info-section');
// REMOVED: syncHqSection (as Sync to HQ is removed from HTML)
// REMOVED: syncMessageBox (as Sync to HQ is removed from HTML)
// REMOVED: performSyncBtn (as Sync to HQ is removed from HTML)

const manageDrinksList = document.getElementById('manage-drinks-list');
const refreshReportBtn = document.getElementById('refresh-report-btn');
const totalSalesSpan = document.getElementById('total-sales');
const drinksSoldReportDiv = document.getElementById('drinks-sold-report');
const reportMessageBox = document.getElementById('report-message-box');
const viewStockList = document.getElementById('view-stock-list'); // For read-only stock view


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

    // Add new styling based on message type, using the new dark theme colors
    if (type === 'success') {
        boxElement.classList.add('bg-green-800', 'text-green-200'); // Darker green background, lighter text
    } else if (type === 'error') {
        boxElement.classList.add('bg-red-800', 'text-red-200'); // Darker red background, lighter text
    } else { // Default to info
        boxElement.classList.add('bg-custom-turquoise', 'text-dark-card'); // Turquoise background, dark text
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
            showMainMenu(); // MODIFIED: Show main menu instead of admin content directly
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
 * Shows the main menu and hides all other content sections.
 */
function showMainMenu() {
    authSection.classList.add('hidden'); // Hide auth section
    adminMainContentArea.classList.add('hidden'); // Hide specific content sections
    mainMenuSection.classList.remove('hidden'); // Show main menu

    logoutBtn.classList.remove('hidden'); // Ensure global logout button is visible
    hideAllAdminSubSections(); // Ensure no sub-sections are visible when returning to menu
}


/**
 * Hides all specific admin content sections (manage drinks, sales report, etc.).
 */
function hideAllAdminSubSections() {
    manageDrinksSection.classList.add('hidden');
    salesReportSection.classList.add('hidden');
    stockViewSection.classList.add('hidden');
    branchInfoSection.classList.add('hidden');
    // REMOVED: syncHqSection.classList.add('hidden'); // Removed as Sync to HQ is gone
    // Also clear any lingering messages in these sections
    reportMessageBox.classList.add('hidden');
    // REMOVED: syncMessageBox.classList.add('hidden'); // Removed as Sync to HQ is gone
    // Loop through all drink message boxes and hide them if necessary (more robust)
    document.querySelectorAll('[id^="drink-message-box-"]').forEach(box => {
        box.classList.add('hidden');
    });
}

/**
 * Now, showAdminContent will just call showMainMenu.
 * Further functions will then navigate to specific content areas.
 */
async function showAdminContent() {
    // This function will now simply show the main menu
    showMainMenu();
    // Pre-fetch data for the first view or if already logged in (optional, can be done when a menu item is clicked)
    // await fetchDrinksForManagement(); // Don't fetch until "Update Drink Prices" is clicked
    // await fetchSalesReport(); // Don't fetch until "View Sales Report" is clicked
}

/**
 * Handles administrator logout.
 */
function handleLogout() {
    authToken = null; // Clear the authentication token
    sessionStorage.removeItem('adminAuthToken'); // Remove token from session storage
    // Hide all admin content and show login form
    mainMenuSection.classList.add('hidden');      // Hide main menu
    adminMainContentArea.classList.add('hidden'); // Hide specific content sections
    logoutBtn.classList.add('hidden');            // Hide global logout button
    authSection.classList.remove('hidden');       // Show auth section again
    showLoginForm(); // Ensure login form is shown on logout
    loginForm.reset(); // Clear login form fields
    registerForm.reset(); // Clear register form fields
    // Clear message boxes
    loginMessageBox.classList.add('hidden');
    registerMessageBox.classList.add('hidden');
    reportMessageBox.classList.add('hidden');
    // REMOVED: syncMessageBox.classList.add('hidden'); // Clear sync message box too
}

/**
 * Fetches all drinks for management (including current price and quantity).
 * Displays them with forms to update.
 */
async function fetchDrinksForManagement() {
    // Updated loading message color for dark theme
    manageDrinksList.innerHTML = '<p class="col-span-full text-center text-light-text">Loading drinks for management...</p>';
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
        // Updated error message color for dark theme
        manageDrinksList.innerHTML = '<p class="col-span-full text-center text-red-400">Failed to load drinks for management. ' + error.message + '</p>';
        displayMessage(loginMessageBox, 'Failed to load drinks.', 'error');
    }
}

/**
 * Fetches all drinks for read-only viewing (View Stock).
 * This can be a simplified version or reuse the same data.
 */
async function fetchStockForViewing() {
    // Updated loading message color for dark theme
    viewStockList.innerHTML = '<p class="col-span-full text-center text-light-text">Loading stock data...</p>';
    if (!authToken) {
        displayMessage(loginMessageBox, 'You are not authenticated.', 'error');
        return;
    }

    try {
        const response = await fetch('/drinks', { // Assuming the same endpoint works for read-only
            headers: {
                'Authorization': `Bearer ${authToken}`
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

        const drinks = await response.json();
        renderViewStock(drinks);
    } catch (error) {
        console.error('Error fetching stock for viewing:', error);
        // Updated error message color for dark theme
        viewStockList.innerHTML = '<p class="col-span-full text-center text-red-400">Failed to load stock data. ' + error.message + '</p>';
        displayMessage(loginMessageBox, 'Failed to load stock.', 'error');
    }
}

/**
 * Renders the fetched drinks for read-only viewing.
 * @param {Array} drinks - Array of drink objects.
 */
function renderViewStock(drinks) {
    viewStockList.innerHTML = ''; // Clear existing content
    if (drinks.length === 0) {
        // Updated empty message color for dark theme
        viewStockList.innerHTML = '<p class="col-span-full text-center text-light-text">No drinks found in stock.</p>';
        return;
    }

    const table = document.createElement('table');
    // Updated table background, border, and overflow for dark theme
    table.className = 'min-w-full bg-dark-card border border-dark-highlight rounded-lg overflow-hidden';
    table.innerHTML = `
        <thead>
            <tr class="bg-dark-highlight border-b border-dark-highlight">
                <th class="py-3 px-4 text-left text-sm font-semibold text-light-text">Drink Name</th>
                <th class="py-3 px-4 text-left text-sm font-semibold text-light-text">Price</th>
                <th class="py-3 px-4 text-left text-sm font-semibold text-light-text">Quantity</th>
            </tr>
        </thead>
        <tbody id="view-stock-body">
        </tbody>
    `;
    viewStockList.appendChild(table);

    const tbody = document.getElementById('view-stock-body');
    drinks.forEach(drink => {
        const row = document.createElement('tr');
        // Updated row border and hover for dark theme
        row.className = 'border-b border-dark-highlight last:border-b-0 hover:bg-dark-input-bg';
        row.innerHTML = `
            <td class="py-3 px-4 text-light-text">${drink.drinkName}</td>
            <td class="py-3 px-4 text-light-text">KSH ${drink.drinkPrice.toFixed(2)}</td>
            <td class="py-3 px-4 text-light-text">${drink.drinkQuantity}</td>
        `;
        tbody.appendChild(row);
    });
}


/**
 * Renders the fetched drinks for management, each with an update form.
 * @param {Array} drinks - Array of drink objects.
 */
function renderManageDrinks(drinks) {
    manageDrinksList.innerHTML = ''; // Clear existing content
    if (drinks.length === 0) {
        // Updated empty message color for dark theme
        manageDrinksList.innerHTML = '<p class="col-span-full text-center text-light-text">No drinks found.</p>';
        return;
    }

    drinks.forEach(drink => {
        const drinkCard = document.createElement('div');
        // Updated card background, shadow, and border for dark theme
        drinkCard.className = 'bg-dark-card rounded-xl shadow-lg p-5 border border-dark-highlight';
        drinkCard.innerHTML = `
            <h3 class="text-xl font-semibold text-custom-turquoise mb-2">${drink.drinkName} (ID: ${drink.id})</h3>
            <form class="update-drink-form space-y-3" data-drink-id="${drink.id}">
                <div>
                    <label for="price-${drink.id}" class="block text-light-text text-sm font-medium mb-1">Price:</label>
                    <input type="number" id="price-${drink.id}" name="drinkPrice" value="${drink.drinkPrice.toFixed(2)}" step="0.01" min="0.01" required
                           class="shadow-sm appearance-none border border-dark-highlight rounded-lg w-full py-2 px-3 bg-dark-input-bg text-light-text leading-tight focus:outline-none focus:ring-1 focus:ring-custom-turquoise">
                </div>
                <div>
                    <label for="quantity-${drink.id}" class="block text-light-text text-sm font-medium mb-1">Quantity:</label>
                    <input type="number" id="quantity-${drink.id}" name="drinkQuantity" value="${drink.drinkQuantity}" min="0" required
                           class="shadow-sm appearance-none border border-dark-highlight rounded-lg w-full py-2 px-3 bg-dark-input-bg text-light-text leading-tight focus:outline-none focus:ring-1 focus:ring-custom-turquoise">
                </div>
                <button type="submit"
                        class="w-full bg-custom-turquoise hover:bg-opacity-80 text-dark-card font-bold py-2 px-4 rounded-lg focus:outline-none focus:ring-2 focus:ring-custom-turquoise focus:ring-offset-2 focus:ring-offset-dark-card transition duration-300 ease-in-out">
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
            fetchDrinksForManagement(); // This should refresh the "Update Drink Prices" view
            fetchStockForViewing(); // Also refresh the "View Stock" view if it's currently active
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
    // Updated loading message color for dark theme
    drinksSoldReportDiv.innerHTML = '<p class="text-light-text text-center py-4">Generating sales report...</p>';
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
        // Updated error message color for dark theme
        drinksSoldReportDiv.innerHTML = '<p class="text-red-400 text-center py-4">Failed to load sales report.</p>';
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
        // Updated table background, border, and overflow for dark theme
        table.className = 'min-w-full bg-dark-card border border-dark-highlight rounded-lg overflow-hidden';
        table.innerHTML = `
            <thead>
                <tr class="bg-dark-highlight border-b border-dark-highlight">
                    <th class="py-3 px-4 text-left text-sm font-semibold text-light-text">Drink Name</th>
                    <th class="py-3 px-4 text-left text-sm font-semibold text-light-text">Quantity Sold</th>
                    <th class="py-3 px-4 text-left text-sm font-semibold text-light-text">Total Revenue</th>
                </tr>
            </thead>
            <tbody id="sales-report-body">
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
                // Updated row border and hover for dark theme
                row.className = 'border-b border-dark-highlight last:border-b-0 hover:bg-dark-input-bg';
                row.innerHTML = `
                    <td class="py-3 px-4 text-light-text">${drinkName}</td>
                    <td class="py-3 px-4 text-light-text">${sale.quantity}</td>
                    <td class="py-3 px-4 text-light-text">KSH ${sale.totalPrice.toFixed(2)}</td>
                `;
                tbody.appendChild(row);
            });
        } else {
            // Updated empty message color for dark theme
            drinksSoldReportDiv.innerHTML = '<p class="text-light-text text-center py-4">No sales data available yet.</p>';
        }

    } else {
        // Updated empty message color for dark theme
        drinksSoldReportDiv.innerHTML = '<p class="text-light-text text-center py-4">No sales data available yet.</p>';
    }
}

// REMOVED: handleSyncToHQ function (as Sync to HQ is removed)


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
        showMainMenu(); // MODIFIED: Directly show main menu if authenticated
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

// Logout button (header)
logoutBtn.addEventListener('click', handleLogout);

// Sales report refresh button (inside sales report section)
refreshReportBtn.addEventListener('click', fetchSalesReport);

// NEW: Main Menu A-Tag Event Listeners
// Changed from .addEventListener('click', () => { ... }) to handle <a> tags
menuViewStockBtn.addEventListener('click', (event) => {
    event.preventDefault(); // Prevent default link behavior (page reload)
    hideAllAdminSubSections(); // Hide all other sections
    mainMenuSection.classList.add('hidden'); // Hide the main menu
    adminMainContentArea.classList.remove('hidden'); // Show the content area wrapper
    stockViewSection.classList.remove('hidden'); // Show specific section
    fetchStockForViewing(); // Fetch data for this section
});

menuUpdatePricesBtn.addEventListener('click', (event) => {
    event.preventDefault(); // Prevent default link behavior (page reload)
    hideAllAdminSubSections(); // Hide all other sections
    mainMenuSection.classList.add('hidden'); // Hide the main menu
    adminMainContentArea.classList.remove('hidden'); // Show the content area wrapper
    manageDrinksSection.classList.remove('hidden'); // Show specific section
    fetchDrinksForManagement(); // Fetch data for this section
});

menuSetBranchInfoBtn.addEventListener('click', (event) => {
    event.preventDefault(); // Prevent default link behavior (page reload)
    hideAllAdminSubSections(); // Hide all other sections
    mainMenuSection.classList.add('hidden'); // Hide the main menu
    adminMainContentArea.classList.remove('hidden'); // Show the content area wrapper
    branchInfoSection.classList.remove('hidden'); // Show specific section
    // No specific fetch function for "Set Branch Info" unless you add one later
});

menuViewSalesBtn.addEventListener('click', (event) => {
    event.preventDefault(); // Prevent default link behavior (page reload)
    hideAllAdminSubSections(); // Hide all other sections
    mainMenuSection.classList.add('hidden'); // Hide the main menu
    adminMainContentArea.classList.remove('hidden'); // Show the content area wrapper
    salesReportSection.classList.remove('hidden'); // Show specific section
    fetchSalesReport(); // Fetch data for this section
});

// REMOVED: menuSyncHqBtn.addEventListener (as Sync to HQ is removed)

menuLogoutBtn.addEventListener('click', (event) => {
    event.preventDefault(); // Prevent default link behavior (page reload)
    handleLogout(); // Menu logout button also calls the main handler
});

// NEW: Back to Main Menu button
backToMenuBtn.addEventListener('click', showMainMenu);

// REMOVED: Sync to HQ button listener (as Sync to HQ is removed)
