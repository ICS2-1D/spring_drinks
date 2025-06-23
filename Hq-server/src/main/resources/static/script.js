// This script assumes a backend server is running and provides the necessary API endpoints.
// It is designed to replace the functionality of the original ClientCli.java.

// --- DOM Element References ---
const drinksListDiv = document.getElementById('drinks-list');
const cartItemsDiv = document.getElementById('cart-items');
const totalAmountSpan = document.getElementById('total-amount-value');
const orderForm = document.getElementById('order-form');
const messageBox = document.getElementById('message-box');
const emptyCartMessage = document.getElementById('empty-cart-message');

// --- State Management ---
let allDrinks = []; // Stores all drinks fetched from the backend {id, drinkName, drinkPrice, drinkQuantity}
let cart = [];      // Stores items in the cart { drinkId, name, price, quantity }

// --- Utility Functions ---

/**
 * Displays a message in the message box with appropriate styling.
 * Hides the message after 5 seconds.
 * @param {string} message - The message to display.
 * @param {'success' | 'error' | 'info'} type - The type of message for styling.
 */
function displayMessage(message, type) {
    messageBox.textContent = message;
    messageBox.style.display = 'block';
    
    // Reset styles
    messageBox.style.color = 'white';
    messageBox.style.backgroundColor = '#6b21a8'; // Default info color

    if (type === 'success') {
        messageBox.style.backgroundColor = '#16a34a'; // Green
    } else if (type === 'error') {
        messageBox.style.backgroundColor = '#dc2626'; // Red
    }

    setTimeout(() => {
        messageBox.style.display = 'none';
    }, 5000);
}

// --- Core Application Logic ---

/**
 * Fetches all available drinks from the backend and renders them.
 * This corresponds to the `getDrinksMenu` function in ClientCli.
 */
async function fetchAndRenderDrinks() {
    drinksListDiv.innerHTML = '<p>Loading drinks...</p>';
    try {
        // This simulates a request that would be handled by the SocketClient in the Java version.
        // We assume a REST endpoint `/api/drinks` exists on the hq-server.
        const response = await fetch('/drinks'); 
        if (!response.ok) {
            throw new Error(`Failed to fetch drinks. Status: ${response.status}`);
        }
        allDrinks = await response.json();
        renderDrinks();
    } catch (error) {
        console.error('Error fetching drinks:', error);
        drinksListDiv.innerHTML = '<p style="color: red;">Failed to load drinks. Please try again later.</p>';
        displayMessage('Failed to load drinks menu.', 'error');
    }
}

/**
 * Renders the fetched drinks into the UI.
 */
function renderDrinks() {
    drinksListDiv.innerHTML = ''; // Clear previous content
    if (!allDrinks || allDrinks.length === 0) {
        drinksListDiv.innerHTML = '<p>No drinks available at the moment.</p>';
        return;
    }

    allDrinks.forEach(drink => {
        const card = document.createElement('div');
        card.className = 'drink-card';
        card.innerHTML = `
            <div>
                <h3>${drink.drinkName}</h3>
                <p>Price: <span class="price">$${drink.drinkPrice.toFixed(2)}</span></p>
                <p>Available: ${drink.drinkQuantity}</p>
            </div>
            <button class="add-to-cart-btn" data-drink-id="${drink.id}" ${drink.drinkQuantity === 0 ? 'disabled' : ''}>
                ${drink.drinkQuantity === 0 ? 'Out of Stock' : 'Add to Cart'}
            </button>
        `;
        drinksListDiv.appendChild(card);
    });

    // Add event listeners after rendering
    document.querySelectorAll('.add-to-cart-btn').forEach(button => {
        button.addEventListener('click', handleAddToCart);
    });
}

/**
 * Updates the display of the shopping cart and recalculates the total price.
 */
function updateCartDisplay() {
    cartItemsDiv.innerHTML = ''; 

    if (cart.length === 0) {
        emptyCartMessage.style.display = 'block';
    } else {
        emptyCartMessage.style.display = 'none';
        cart.forEach(item => {
            const cartItemElement = document.createElement('div');
            cartItemElement.className = 'cart-item';
            cartItemElement.innerHTML = `
                <div class="cart-item-info">
                    <p>${item.name}</p>
                    <p style="font-size: 0.8rem;">$${item.price.toFixed(2)} x ${item.quantity}</p>
                </div>
                <div class="cart-item-controls">
                    <button data-id="${item.drinkId}" data-action="decrease">-</button>
                    <span>${item.quantity}</span>
                    <button data-id="${item.drinkId}" data-action="increase">+</button>
                    <button data-id="${item.drinkId}" data-action="remove" style="background-color: #fee2e2; color: #b91c1c;">X</button>
                </div>
            `;
            cartItemsDiv.appendChild(cartItemElement);
        });
    }

    const total = cart.reduce((sum, item) => sum + item.price * item.quantity, 0);
    totalAmountSpan.textContent = `$${total.toFixed(2)}`;
    
    // Add listeners for cart controls
    cartItemsDiv.querySelectorAll('button').forEach(button => {
        button.addEventListener('click', (event) => {
            const drinkId = parseInt(event.target.dataset.id);
            const action = event.target.dataset.action;
            handleCartUpdate(drinkId, action);
        });
    });
}


// --- Event Handlers ---

/**
 * Handles adding a drink to the cart when its button is clicked.
 * @param {Event} event
 */
function handleAddToCart(event) {
    const drinkId = parseInt(event.target.dataset.drinkId);
    const drink = allDrinks.find(d => d.id === drinkId);

    if (!drink) return;

    const cartItem = cart.find(item => item.drinkId === drinkId);

    if (cartItem) {
        if (cartItem.quantity < drink.drinkQuantity) {
            cartItem.quantity++;
        } else {
            displayMessage(`No more ${drink.drinkName} in stock.`, 'error');
        }
    } else {
        cart.push({ drinkId: drink.id, name: drink.drinkName, price: drink.drinkPrice, quantity: 1 });
    }
    updateCartDisplay();
}

/**
 * Handles increasing, decreasing, or removing items from the cart.
 * @param {number} drinkId - The ID of the drink to update.
 * @param {'increase' | 'decrease' | 'remove'} action - The action to perform.
 */
function handleCartUpdate(drinkId, action) {
    const cartItemIndex = cart.findIndex(item => item.drinkId === drinkId);
    if (cartItemIndex === -1) return;

    const cartItem = cart[cartItemIndex];
    const stockDrink = allDrinks.find(d => d.id === drinkId);

    switch (action) {
        case 'increase':
            if (stockDrink && cartItem.quantity < stockDrink.drinkQuantity) {
                cartItem.quantity++;
            } else {
                displayMessage(`No more ${cartItem.name} in stock.`, 'error');
            }
            break;
        case 'decrease':
            cartItem.quantity--;
            if (cartItem.quantity <= 0) {
                cart.splice(cartItemIndex, 1);
            }
            break;
        case 'remove':
            cart.splice(cartItemIndex, 1);
            break;
    }
    updateCartDisplay();
}

/**
 * Handles the order form submission. It gathers cart and customer data,
 * sends it to the backend, and processes the response.
 * This corresponds to the `checkout` function in ClientCli.
 * @param {Event} event
 */
async function handleOrderSubmission(event) {
    event.preventDefault();

    if (cart.length === 0) {
        displayMessage('Your cart is empty.', 'error');
        return;
    }

    const customerName = document.getElementById('customerName').value.trim();
    const customerPhoneNumber = document.getElementById('customerPhoneNumber').value.trim();
    const branch = document.getElementById('branch').value;

    if (!customerName || !customerPhoneNumber || !branch) {
        displayMessage('Please fill in all your details.', 'error');
        return;
    }

    const orderRequest = {
        customerName,
        customerPhoneNumber,
        branch,
        items: cart.map(item => ({ drinkId: item.drinkId, quantity: item.quantity }))
    };
    
    displayMessage('Placing your order...', 'info');

    try {
        // We assume a REST endpoint `/api/orders` exists.
        const response = await fetch('/orders', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(orderRequest)
        });

        const responseData = await response.json();

        if (!response.ok) {
            throw new Error(responseData.message || 'Failed to place order.');
        }
        
        displayMessage(`Order #${responseData.orderNumber} placed! Processing payment...`, 'success');
        
        // Corresponds to `simulatePayment` in ClientCli
        await processPayment(responseData.orderId, customerPhoneNumber);

        // Reset state after successful order
        cart = [];
        updateCartDisplay();
        orderForm.reset();
        fetchAndRenderDrinks(); // Re-fetch drinks to show updated stock

    } catch (error) {
        console.error('Order submission error:', error);
        displayMessage(error.message, 'error');
    }
}

/**
 * Simulates the payment process after an order is successfully created.
 * @param {number} orderId - The ID of the order to pay for.
 * @param {string} phoneNumber - The customer's phone number.
 */
async function processPayment(orderId, phoneNumber) {
    try {
        // We assume a REST endpoint `/api/payments` exists to record payment.
        const response = await fetch('/payments', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                orderId: orderId,
                customerNumber: phoneNumber,
                paymentMethod: 'WEB_UI',
                paymentStatus: 'SUCCESS'
            })
        });

        if (!response.ok) throw new Error('Payment recording failed.');
        
        displayMessage('Payment successful and recorded!', 'success');

    } catch (error) {
        console.error('Payment processing error:', error);
        displayMessage('Order placed, but payment recording failed. Please contact support.', 'error');
    }
}


// --- Initialization ---
document.addEventListener('DOMContentLoaded', () => {
    fetchAndRenderDrinks();
    orderForm.addEventListener('submit', handleOrderSubmission);
});
