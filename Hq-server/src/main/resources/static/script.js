// DOM Element References
const drinksListDiv = document.getElementById('drinks-list');
const cartItemsDiv = document.getElementById('cart-items');
const totalAmountSpan = document.getElementById('total-amount');
const orderForm = document.getElementById('order-form');
const messageBox = document.getElementById('message-box');
const emptyCartMessage = document.getElementById('empty-cart-message');
const branchDisplay = document.getElementById('branch-display');

// Global state
let allDrinks = [];
let cart = [];
let assignedBranch = null;

function displayMessage(message, type) {
    messageBox.textContent = message;
    messageBox.classList.remove('hidden', 'bg-brand-success', 'bg-brand-danger', 'bg-brand-accent', 'text-white');
    const typeClasses = {
        success: ['bg-brand-success', 'text-white'],
        error: ['bg-brand-danger', 'text-white'],
        info: ['bg-brand-accent', 'text-white'],
    };
    messageBox.classList.add(...(typeClasses[type] || typeClasses.info));
    setTimeout(() => messageBox.classList.add('hidden'), 5000);
}

async function connectToServer() {
    try {
        const response = await fetch('/connect');
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || `Server connection failed: ${response.statusText}`);
        }

        const data = await response.json();
        assignedBranch = data.branch;

        if (assignedBranch) {
            branchDisplay.textContent = `📍 Branch: ${assignedBranch}`;
            branchDisplay.classList.add('font-semibold', 'text-brand-accent');
            fetchDrinks();
        } else {
            throw new Error('Branch information not received from server.');
        }

    } catch (error) {
        console.error('Connection error:', error);
        branchDisplay.textContent = 'Connection Failed';
        branchDisplay.classList.add('text-brand-danger');
        drinksListDiv.innerHTML = `<p class="col-span-full text-center text-brand-danger py-10">Could not connect to the server. Please refresh. Error: ${error.message}</p>`;
    }
}


async function fetchDrinks() {
    if (!assignedBranch) {
        drinksListDiv.innerHTML = '<p class="col-span-full text-center text-brand-text-dim py-10">Waiting for branch assignment...</p>';
        return;
    }
    drinksListDiv.innerHTML = `<p class="col-span-full text-center text-brand-text-dim py-10">Loading drinks for ${assignedBranch} branch...</p>`;
    try {
        const response = await fetch(`/drinks?branch=${assignedBranch}`);
        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
        allDrinks = await response.json();
        renderDrinks();
    } catch (error) {
        console.error('Error fetching drinks:', error);
        drinksListDiv.innerHTML = '<p class="col-span-full text-center text-brand-danger py-10">Failed to load drinks. Please try again later.</p>';
    }
}

function renderDrinks() {
    drinksListDiv.innerHTML = '';
    if (!allDrinks || allDrinks.length === 0) {
        drinksListDiv.innerHTML = '<p class="col-span-full text-center text-brand-text-dim py-10">No drinks available at this branch.</p>';
        return;
    }

    allDrinks.forEach(drink => {
        const drinkCard = document.createElement('div');
        drinkCard.className = 'bg-brand-card border border-brand-border rounded-xl p-5 flex flex-col justify-between transition-transform transform hover:-translate-y-1 shadow-lg';
        const isOutOfStock = drink.drinkQuantity <= 0;

        drinkCard.innerHTML = `
            <div>
                <h3 class="text-lg font-bold text-white mb-2">${drink.drinkName}</h3>
                <p class="text-brand-text-dim mb-1">Price: <span class="font-semibold text-brand-accent">KSH ${drink.drinkPrice.toFixed(2)}</span></p>
                <p class="text-sm ${isOutOfStock ? 'text-brand-danger' : 'text-brand-success'}">
                    ${isOutOfStock ? 'Out of Stock' : `Available: ${drink.drinkQuantity}`}
                </p>
            </div>
            <button class="add-to-cart-btn mt-4 w-full font-semibold py-2 px-4 rounded-lg transition-colors ${isOutOfStock ? 'bg-gray-600 cursor-not-allowed' : 'bg-brand-accent hover:bg-brand-accent-hover text-white'}"
                data-drink-id="${drink.id}" ${isOutOfStock ? 'disabled' : ''}>
                ${isOutOfStock ? 'Unavailable' : 'Add to Cart'}
            </button>
        `;
        drinksListDiv.appendChild(drinkCard);
    });

    document.querySelectorAll('.add-to-cart-btn').forEach(button => {
        button.addEventListener('click', handleAddToCart);
    });
}

function handleAddToCart(event) {
    const drinkId = parseInt(event.target.dataset.drinkId);
    const drink = allDrinks.find(d => d.id === drinkId);
    if (!drink) return;

    const cartItem = cart.find(item => item.drinkId === drinkId);
    if (cartItem) {
        if (cartItem.quantity < drink.drinkQuantity) {
            cartItem.quantity++;
            displayMessage(`${drink.drinkName} quantity updated.`, 'info');
        } else {
            displayMessage(`No more ${drink.drinkName} in stock!`, 'error');
        }
    } else {
        cart.push({ drinkId: drink.id, name: drink.drinkName, price: drink.drinkPrice, quantity: 1 });
        displayMessage(`${drink.drinkName} added to cart!`, 'success');
    }
    updateCartDisplay();
}

function updateCartItem(drinkId, action) {
    const cartItemIndex = cart.findIndex(item => item.drinkId === drinkId);
    if (cartItemIndex === -1) return;

    const cartItem = cart[cartItemIndex];
    const stockItem = allDrinks.find(d => d.id === drinkId);

    switch (action) {
        case 'increase':
            if (stockItem && cartItem.quantity < stockItem.drinkQuantity) cartItem.quantity++;
            else displayMessage('Maximum stock reached!', 'error');
            break;
        case 'decrease':
            cartItem.quantity--;
            if (cartItem.quantity <= 0) cart.splice(cartItemIndex, 1);
            break;
        case 'remove':
            cart.splice(cartItemIndex, 1);
            break;
    }
    updateCartDisplay();
}

function updateCartDisplay() {
    cartItemsDiv.innerHTML = '';
    emptyCartMessage.classList.toggle('hidden', cart.length > 0);

    cart.forEach(item => {
        const cartItemElement = document.createElement('div');
        cartItemElement.className = 'flex items-center justify-between bg-brand-bg p-2 rounded-md mb-2';
        cartItemElement.innerHTML = `
            <div>
                <p class="font-semibold text-white">${item.name}</p>
                <p class="text-sm text-brand-text-dim">KSH ${item.price.toFixed(2)}</p>
            </div>
            <div class="flex items-center space-x-3">
                <button class="cart-item-update text-lg font-bold" data-id="${item.drinkId}" data-action="decrease">-</button>
                <span class="font-bold text-brand-accent">${item.quantity}</span>
                <button class="cart-item-update text-lg font-bold" data-id="${item.drinkId}" data-action="increase">+</button>
                <button class="cart-item-update text-brand-danger" data-id="${item.drinkId}" data-action="remove">&times;</button>
            </div>
        `;
        cartItemsDiv.appendChild(cartItemElement);
    });

    const total = cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);
    totalAmountSpan.textContent = `KSH ${total.toFixed(2)}`;

    cartItemsDiv.querySelectorAll('.cart-item-update').forEach(button => {
        button.addEventListener('click', (e) => {
            const drinkId = parseInt(e.currentTarget.dataset.id);
            const action = e.currentTarget.dataset.action;
            updateCartItem(drinkId, action);
        });
    });
}

async function handleOrderSubmission(event) {
    event.preventDefault();
    if (cart.length === 0) {
        displayMessage('Your cart is empty!', 'error');
        return;
    }
    if (!assignedBranch) {
        displayMessage('Cannot place order: branch not assigned. Please refresh.', 'error');
        return;
    }

    const customerName = document.getElementById('customerName').value.trim();
    const customerPhoneNumber = document.getElementById('customerPhoneNumber').value.trim();

    if (!customerName || !customerPhoneNumber) {
        displayMessage('Please fill in all your details.', 'error');
        return;
    }

    const orderRequest = {
        customerName,
        customerPhoneNumber,
        branch: assignedBranch,
        items: cart.map(item => ({ drinkId: item.drinkId, quantity: item.quantity }))
    };

    try {
        displayMessage('Placing your order...', 'info');
        const response = await fetch('/order', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(orderRequest)
        });
        const responseData = await response.json();

        if (response.ok) {
            displayMessage('Order placed! Simulating payment...', 'success');
            await simulatePayment(responseData.orderId, customerPhoneNumber);
            cart = [];
            updateCartDisplay();
            orderForm.reset();
            fetchDrinks(); // Refresh drinks to show updated stock
        } else {
            throw new Error(responseData.message || 'Failed to place order.');
        }
    } catch (error) {
        console.error('Order submission error:', error);
        displayMessage(error.message, 'error');
    }
}

async function simulatePayment(orderId, customerPhoneNumber) {
    const paymentRequest = {
        orderId,
        customerNumber: customerPhoneNumber,
        paymentMethod: 'M-PESA',
        paymentStatus: 'SUCCESS'
    };

    try {
        const response = await fetch('/payments', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(paymentRequest)
        });
        const responseData = await response.json();

        if (response.ok) {
            displayMessage(`Payment successful! Tx ID: ${responseData.transactionId}`, 'success');
        } else {
            throw new Error(responseData.message || 'Payment simulation failed.');
        }
    } catch (error) {
        console.error('Payment simulation error:', error);
        displayMessage('Order placed, but payment failed. Contact support.', 'error');
    }
}

document.addEventListener('DOMContentLoaded', () => {
    connectToServer();
    orderForm.addEventListener('submit', handleOrderSubmission);
});
