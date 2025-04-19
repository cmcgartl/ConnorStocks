// Wait until DOM is fully loaded before executing any script
$(document).ready(function () {

	checkLoginStatus(); // Update navigation bar based on user login status

	// Handle stock search form submission
	$('#searchForm').submit(function (e) {
		e.preventDefault(); // Prevent default form submission behavior

		var ticker = $('#searchQuery').val().toUpperCase(); // Normalize input to uppercase
		const validTickers = ['TSLA', 'AAPL', 'MSFT', 'AMD', 'NVDA', 'COIN', 'ROKU', 'RBLX'];

		if (validTickers.includes(ticker)) {
			// If input is valid, send AJAX request to retrieve stock info
			$.ajax({
				type: "GET",
				url: "stockInfo",
				data: { ticker: ticker },
				success: function (response) {
					var stockData = typeof response === 'string' ? JSON.parse(response) : response;

					// Show stock info differently based on login status
					checkLoginStatus(function (isLoggedIn) {
						if (isLoggedIn) {
							$('#stockDetails').html(generateDetailedStockHtml(stockData));
						} else {
							$('#stockDetails').html(generateStockHtml(stockData));
						}
						$('#searchContainer').hide(); // Hide search box once a result is shown
					});
				},
				error: function () {
					alert("Error retrieving stock information.");
				}
			});
		} else {
			alert("Please enter a valid ticker: TSLA, AAPL, MSFT, COIN, ROKU, RBLX, or AMD");
		}
	});

	// Handle user logout
	$(document).on('click', '#logoutLink', function (e) {
		e.preventDefault();
		$.ajax({
			type: "GET",
			url: "logout",
			success: function () {
				window.location.reload(); // Reload page after logout
			},
			error: function () {
				alert("Error logging out.");
			}
		});
	});

	// Bring back the search input if user clicks 'Home/Search'
	$(document).on('click', '.home-search-link', function (e) {
		e.preventDefault();
		$('#searchContainer').show(); // Show the search bar again
		$('#stockDetails').empty();   // Clear any existing search results
	});
});

// Builds public-facing stock data (for users not logged in)
function generateStockHtml(stockData) {
	var profile = stockData.profile;
	var quote = stockData.quote;
	var changeClass = quote.d > 0 ? 'positive' : 'negative'; // Conditional styling based on price change

	return `
		<div class="searchInfo">
			<div class="ticker-box">
				<h2>${profile.ticker} - ${profile.name}</h2>
				<p>${profile.exchange}</p>
			</div>
			<div class="summary-box">
				<h3>Summary</h3>
				<p>High Price: ${quote.h}</p>
				<p>Low Price: ${quote.l}</p>
				<p>Open Price: ${quote.o}</p>
				<p>Close Price: ${quote.pc}</p>
			</div>
			<div class="company-info-box">
				<h3>Company Information</h3>
				<p>IPO Date: ${profile.ipo}</p>
				<p>Market Cap: ${profile.marketCapitalization}</p>
				<p>Share Outstanding: ${profile.shareOutstanding}</p>
				<p>Website: <a href="${profile.weburl}">${profile.weburl}</a></p>
				<p>Phone: ${profile.phone}</p>
			</div>
		</div>
	`;
}

// Builds logged-in user version, includes buy form
function generateDetailedStockHtml(stockData) {
	var profile = stockData.profile;
	var quote = stockData.quote;
	var changeClass = quote.d > 0 ? 'positive' : 'negative';

	return `
		<div class="searchInfo">
			<div class="ticker-box">
				<h2>${profile.ticker} - ${profile.name}</h2>
				<p>${profile.exchange}</p>
				<p>Last Price: ${quote.l}</p>
				<p class="change ${changeClass}">Change Today: ${quote.d.toFixed(2)}%</p>
			</div>
			<div class="summary-box">
				<h3>Summary</h3>
				<p>High Price: ${quote.h}</p>
				<p>Low Price: ${quote.l}</p>
				<p>Open Price: ${quote.o}</p>
				<p>Close Price: ${quote.pc}</p>
			</div>
			<div class="company-info-box">
				<h3>Company Information</h3>
				<p>IPO Date: ${profile.ipo}</p>
				<p>Market Cap: ${profile.marketCapitalization}</p>
				<p>Share Outstanding: ${profile.shareOutstanding}</p>
				<p>Website: <a href="${profile.weburl}">${profile.weburl}</a></p>
				<p>Phone: ${profile.phone}</p>
				<label for="stockQty">Enter quantity to buy:</label>
				<input type="number" id="stockQty" name="stockQty" min="1" value="1">
				<button onclick="buyStock('${profile.ticker}');">Buy</button>
			</div>
		</div>
	`;
}

// Send stock purchase request to server
function buyStock(ticker) {
	var qty = parseInt($('#stockQty').val(), 10);
	if (isNaN(qty) || qty <= 0) {
		alert('Please enter a valid quantity.');
		return;
	}

	$.ajax({
		type: "POST",
		url: "buyStock",
		data: {
			ticker: ticker,
			quantity: qty
		},
		success: function (response) {
			alert(response.message);
			// You could refresh the portfolio here if needed
		},
		error: function () {
			alert("Failed to process stock purchase.");
		}
	});
}

// Check login status and update navigation accordingly
function checkLoginStatus(callback) {
	$.ajax({
		type: "GET",
		url: "checkLogin",
		success: function (response) {
			if (response.trim() === "loggedIn") {
				// Update nav bar with options for authenticated users
				$('.navigation-links').html(`
					<a href="#" class="home-search-link">Home/Search</a>
					<a href="#" id="logoutLink">Logout</a>
					<a href="Portfolio.html">Portfolio</a>
				`);
				if (callback) callback(true);
			} else {
				// Guest user nav bar
				$('.navigation-links').html(`
					<a href="#" class="home-search-link">Home/Search</a>
					<a href="LoginRegister.html">Login / Sign Up</a>
				`);
				if (callback) callback(false);
			}
		},
		error: function () {
			alert("Error checking login status.");
			if (callback) callback(false);
		}
	});
}