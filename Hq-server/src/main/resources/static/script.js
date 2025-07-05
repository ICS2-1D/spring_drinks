// Get references to DOM elements for manipulation
const drinksListDiv = document.getElementById('drinks-list');
const cartItemsDiv = document.getElementById('cart-items');
const totalAmountSpan = document.getElementById('total-amount');
const orderForm = document.getElementById('order-form');
const messageBox = document.getElementById('message-box');
const emptyCartMessage = document.getElementById('empty-cart-message');

// Global arrays to hold fetched drinks data and current cart items
let allDrinks = []; // Stores all drinks fetched from the backend
let cart = [];      // Stores objects like { drinkId, name, price, quantity }

/**
 * Displays a message in the message box, with a specific styling (success/error).
 * @param {string} message - The message text to display.
 * @param {'success' | 'error' | 'info'} type - The type of message to determine styling.
 */
function displayMessage(message, type) {
    messageBox.textContent = message; // Set the message text
    // Remove previous styling classes
    messageBox.classList.remove('bg-green-100', 'text-green-800', 'bg-red-100', 'text-red-800', 'bg-blue-100', 'text-blue-800');
    // Add new styling based on message type, using the new dark theme colors
    if (type === 'success') {
        messageBox.classList.add('bg-green-800', 'text-green-200'); // Darker green background, lighter text
    } else if (type === 'error') {
        messageBox.classList.add('bg-red-800', 'text-red-200'); // Darker red background, lighter text
    } else { // Default to info styling
        messageBox.classList.add('bg-custom-turquoise', 'text-dark-card'); // Turquoise background, dark text
    }
    messageBox.classList.remove('hidden'); // Make the message box visible

    // Hide the message after 5 seconds
    setTimeout(() => {
        boxElement.classList.add('hidden');
    }, 5000);
}

/**
 * Fetches all available drinks from the backend API and displays them.
 */
async function fetchDrinks() {
    // Updated loading message color for dark theme
    drinksListDiv.innerHTML = '<p class="col-span-full text-center text-light-text">Loading drinks...</p>'; // Show loading message
    try {
        const response = await fetch('/drinks'); // Make GET request to /drinks endpoint
        if (!response.ok) {
            // If response is not OK (e.g., 404, 500), throw an error
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        allDrinks = await response.json(); // Parse the JSON response into the allDrinks array
        renderDrinks(); // Call function to render the fetched drinks
    } catch (error) {
        console.error('Error fetching drinks:', error);
        // Updated error message color for dark theme
        drinksListDiv.innerHTML = '<p class="col-span-full text-center text-red-400">Failed to load drinks. Please try again later.</p>';
        displayMessage('Failed to load drinks. Check console for details.', 'error');
    }
}

/**
 * Renders the fetched drinks into the drinks-list section of the HTML.
 */
function renderDrinks() {
    drinksListDiv.innerHTML = ''; // Clear existing content
    if (allDrinks.length === 0) {
        // Updated empty message color for dark theme
        drinksListDiv.innerHTML = '<p class="col-span-full text-center text-light-text">No drinks available at the moment.</p>';
        return;
    }

    allDrinks.forEach(drink => {
        // Create a card element for each drink
        const drinkCard = document.createElement('div');
        // Updated card background, shadow, and border for dark theme
        drinkCard.className = 'bg-dark-card rounded-xl shadow-lg p-5 border border-dark-highlight flex flex-col justify-between transform hover:scale-105 transition duration-300 ease-in-out';
        drinkCard.innerHTML = `
            <div>
                <h3 class="text-xl font-semibold text-light-text mb-2">🥤 ${drink.drinkName}</h3> <!-- Added drink emoji here -->
                <p class="text-light-text mb-3">Price: <span class="font-bold text-custom-turquoise">KSH ${drink.drinkPrice.toFixed(2)}</span></p>
                <p class="text-light-text mb-4">Available: <span class="font-medium ${drink.drinkQuantity > 0 ? 'text-green-400' : 'text-red-400'}">${drink.drinkQuantity}</span></p>
            </div>
            <button class="add-to-cart-btn mt-4 w-full bg-custom-turquoise hover:bg-opacity-80 text-dark-card font-bold py-2 px-4 rounded-lg focus:outline-none focus:ring-2 focus:ring-custom-turquoise focus:ring-offset-2 focus:ring-offset-dark-card transition duration-300 ease-in-out ${drink.drinkQuantity === 0 ? 'opacity-50 cursor-not-allowed' : ''}"
                    data-drink-id="${drink.id}"
                    ${drink.drinkQuantity === 0 ? 'disabled' : ''}>
                ${drink.drinkQuantity === 0 ? 'Out of Stock' : 'Add to Cart'}
            </button>
        `;
        drinksListDiv.appendChild(drinkCard);
    });

    // Add event listeners to all "Add to Cart" buttons
    document.querySelectorAll('.add-to-cart-btn').forEach(button => {
        button.addEventListener('click', addToCart);
    });
}

/**
 * Adds a selected drink to the shopping cart.
 * @param {Event} event - The click event from the "Add to Cart" button.
 */
function addToCart(event) {
    const drinkId = parseInt(event.target.dataset.drinkId); // Get drink ID from button's data attribute
    const drink = allDrinks.find(d => d.id === drinkId); // Find the drink in the allDrinks array

    if (!drink) {
        displayMessage('Selected drink not found!', 'error');
        return;
    }

    if (drink.drinkQuantity <= 0) {
        displayMessage('Sorry, this drink is out of stock!', 'error');
        return;
    }

    // Check if the drink is already in the cart
    const existingCartItem = cart.find(item => item.drinkId === drinkId);

    if (existingCartItem) {
        // If in cart, increment quantity, ensuring it doesn't exceed available stock
        if (existingCartItem.quantity < drink.drinkQuantity) {
            existingCartItem.quantity++;
            displayMessage(`${drink.drinkName} quantity updated in cart.`, 'info');
        } else {
            displayMessage(`Cannot add more ${drink.drinkName}. Max available reached!`, 'error');
        }
    } else {
        // If not in cart, add as a new item with quantity 1
        cart.push({
            drinkId: drink.id,
            name: drink.drinkName,
            price: drink.drinkPrice,
            quantity: 1
        });
        displayMessage(`${drink.drinkName} added to cart!`, 'success');
    }
    updateCartDisplay(); // Update the cart display in the UI
}

/**
 * Removes or decreases the quantity of a drink from the cart.
 * @param {number} drinkIdToUpdate - The ID of the drink to remove/decrease.
 * @param {boolean} removeAll - If true, removes all quantity of the item; otherwise, decreases by one.
 */
function updateCartItemQuantity(drinkIdToUpdate, action = 'increase') {
    const existingCartItemIndex = cart.findIndex(item => item.drinkId === drinkIdToUpdate);
    if (existingCartItemIndex === -1) return; // Item not found in cart

    const existingCartItem = cart[existingCartItemIndex];
    const drinkInStock = allDrinks.find(d => d.id === drinkIdToUpdate);

    if (action === 'increase') {
        if (drinkInStock && existingCartItem.quantity < drinkInStock.drinkQuantity) {
            existingCartItem.quantity++;
            displayMessage(`${existingCartItem.name} quantity increased.`, 'info');
        } else if (drinkInStock) {
            displayMessage(`Cannot add more ${existingCartItem.name}. Max available reached!`, 'error');
        }
    } else if (action === 'decrease') {
        existingCartItem.quantity--;
        if (existingCartItem.quantity <= 0) {
            cart.splice(existingCartItemIndex, 1); // Remove item if quantity drops to 0 or less
            displayMessage(`${existingCartItem.name} removed from cart.`, 'success');
        } else {
            displayMessage(`${existingCartItem.name} quantity decreased.`, 'info');
        }
    } else if (action === 'remove') {
        cart.splice(existingCartItemIndex, 1);
        displayMessage(`${existingCartItem.name} completely removed from cart.`, 'success');
    }
    updateCartDisplay();
}

/**
 * Updates the display of items in the shopping cart and recalculates the total.
 */
function updateCartDisplay() {
    cartItemsDiv.innerHTML = ''; // Clear current cart display

    if (cart.length === 0) {
        // Updated empty cart message color for dark theme
        emptyCartMessage.classList.remove('hidden'); // Show "cart is empty" message
    } else {
        emptyCartMessage.classList.add('hidden'); // Hide "cart is empty" message
        cart.forEach(item => {
            const cartItemElement = document.createElement('div');
            // Updated cart item background and shadow for dark theme
            cartItemElement.className = 'flex items-center justify-between bg-dark-highlight p-3 rounded-lg mb-2 shadow-sm border border-dark-highlight';
            cartItemElement.innerHTML = `
                <div class="flex-grow">
                    <p class="font-semibold text-light-text">🥤 ${item.name}</p> <!-- Added drink emoji here -->
                    <p class="text-light-text text-sm">KSH ${item.price.toFixed(2)} x ${item.quantity}</p>
                </div>
                <div class="flex items-center space-x-2">
                    <button class="bg-dark-input-bg hover:bg-custom-turquoise text-light-text font-bold py-1 px-2 rounded-full text-lg leading-none transition duration-200" data-id="${item.drinkId}" data-action="decrease">-</button>
                    <span class="font-bold text-custom-turquoise">${item.quantity}</span>
                    <button class="bg-dark-input-bg hover:bg-custom-turquoise text-light-text font-bold py-1 px-2 rounded-full text-lg leading-none transition duration-200" data-id="${item.drinkId}" data-action="increase">+</button>
                    <button class="bg-red-800 hover:bg-red-700 text-light-text font-bold py-1 px-2 rounded-lg text-sm transition duration-200" data-id="${item.drinkId}" data-action="remove">Remove</button>
                </div>
            `;
            cartItemsDiv.appendChild(cartItemElement);
        });
    }

    // Update total amount
    const total = cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);
    totalAmountSpan.textContent = `KSH ${total.toFixed(2)}`;

    // Add event listeners for cart quantity controls and remove buttons
    cartItemsDiv.querySelectorAll('button').forEach(button => {
        button.addEventListener('click', (event) => {
            const drinkId = parseInt(event.target.dataset.id);
            const action = event.target.dataset.action;
            updateCartItemQuantity(drinkId, action);
        });
    });
}

/**
 * Handles the order form submission, sending the order to the backend.
 * @param {Event} event - The form submission event.
 */
async function handleOrderSubmission(event) {
    event.preventDefault(); // Prevent default form submission and page reload

    if (cart.length === 0) {
        displayMessage('Your cart is empty. Please add drinks before placing an order.', 'error');
        return;
    }

    // Get customer information from the form
    const customerName = document.getElementById('customerName').value.trim();
    const customerPhoneNumber = document.getElementById('customerPhoneNumber').value.trim();
    const branch = document.getElementById('branch').value;

    if (!customerName || !customerPhoneNumber || !branch) {
        displayMessage('Please fill in all customer details and select a branch.', 'error');
        return;
    }

    // Map cart items to OrderItemRequest DTO format
    const orderItems = cart.map(item => ({
        drinkId: item.drinkId,
        quantity: item.quantity
    }));

    // Construct the OrderRequest payload
    const orderRequest = {
        customerName: customerName,
        customerPhoneNumber: customerPhoneNumber,
        branch: branch,
        items: orderItems
    };

    try {
        displayMessage('Placing your order...', 'info');
        const response = await fetch('/order', { // POST request to /order endpoint
            method: 'POST',
            headers: {
                'Content-Type': 'application/json' // Specify content type as JSON
            },
            body: JSON.stringify(orderRequest) // Convert payload to JSON string
        });

        const responseData = await response.json(); // Parse response JSON

        if (response.ok) {
            displayMessage('Order placed successfully! Simulating payment...', 'success');
            // If order is successful, proceed to simulate payment
            await simulatePayment(responseData.orderId, customerPhoneNumber);
            // Clear cart and form upon successful order and payment
            cart = [];
            updateCartDisplay();
            orderForm.reset(); // Reset the form fields
            // After successful order and payment, re-fetch drinks to update stock display
            fetchDrinks();
        } else {
            // Handle HTTP errors or backend validation errors
            const errorMessage = responseData.message || 'Failed to place order.';
            displayMessage(errorMessage, 'error');
            console.error('Order placement error:', responseData);
        }
    } catch (error) {
        console.error('Network or unexpected error during order placement:', error);
        displayMessage('An unexpected error occurred during payment. Please check your order status later.', 'error');
    }
}

/**
 * Simulates a payment for the given order.
 * @param {number} orderId - The ID of the placed order.
 * @param {string} customerPhoneNumber - The customer's phone number for payment.
 */
async function simulatePayment(orderId, customerPhoneNumber) {
    const paymentRequest = {
        orderId: orderId,
        customerNumber: customerPhoneNumber,
        paymentMethod: 'M-PESA', // Hardcoded as per CLI example
        paymentStatus: 'SUCCESS' // Assuming immediate success for simulation
    };

    try {
        const response = await fetch('/payments', { // POST request to /payments endpoint
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(paymentRequest)
        });

        const responseData = await response.json();

        if (response.ok) {
            displayMessage(`Payment for Order ID ${orderId} successful! Transaction: ${responseData.transactionId}`, 'success');
        } else {
            const errorMessage = responseData.message || 'Payment simulation failed.';
            displayMessage(errorMessage, 'error');
            console.error('Payment simulation error:', responseData);
        }
    } catch (error) {
        console.error('Network or unexpected error during payment simulation:', error);
        displayMessage('An unexpected error occurred during payment. Please check your order status later.', 'error');
    }
}

// Event Listeners
document.addEventListener('DOMContentLoaded', fetchDrinks); // Fetch drinks when the page loads
orderForm.addEventListener('submit', handleOrderSubmission); // Handle form submiss