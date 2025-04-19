// Wait for the DOM to be fully loaded before running script
$(document).ready(function () {

	// Disable caching for all AJAX calls to ensure fresh data on every request
	$.ajaxSetup({ cache: false });

	// Initial portfolio fetch upon page load
	fetchPortfolio();

	// Fetches the user's portfolio from the server and renders it
	function fetchPortfolio() {
		$.ajax({
			type: "GET",
			url: "portfolio",
			success: function (response) {
				// Ensure response is parsed into a usable object
				var data = typeof response === 'string' ? JSON.parse(response) : response;

				// Generate HTML for all stocks the user owns
				var stocksHtml = buildStocksHtml(data.stocks);

				// Count how many stocks the user currently owns
				var total = 0;
				for (var ticker in data.stocks) {
					var stock = data.stocks[ticker];
					if (stock.quantity > 0) total++;
				}

				// Conditional user message based on whether any stocks are held
				var userMessage = total > 0
					? `<p>Here are your stocks:</p>`
					: `<p>You don't own any stocks!<br>Go buy some!</p>`;

				// Inject dynamic HTML into portfolio container
				$('#portfolioDetails').html(`
					<div class="welcome-message-box">
						<h3>Welcome To Your Portfolio!</h3>
					</div>
					<div class="portfolio-details-box">
						<div class="portfolio-status-box">
							<p1>Cash Balance: $${data.balance.toFixed(2)}</p1>
							<p2>Total Account Value: $${data.totalAccountValue.toFixed(2)}</p2>
						</div>
						<div class="message-box">
							${userMessage}
						</div>
					</div>
					${stocksHtml}`);
			},
			error: function (xhr) {
				// Handles failure cases such as authentication errors or server issues
				console.error("Error loading portfolio:", xhr.responseText);
				$('#portfolioDetails').html('<p>Error loading portfolio. Please try again later.</p>');

				if (xhr.status === 401) {
					alert('Please log in to view your portfolio.');
					window.location.href = "LoginRegister.html";
				}
			}
		});
	}

	// Builds HTML snippets for each stock the user owns
	function buildStocksHtml(stocks) {
		var html = '';
		for (var ticker in stocks) {
			var stock = stocks[ticker];
			if (stock.quantity > 0) {
				var changeClass = stock.change > 0 ? 'positive' : 'negative'; // Green/red text
				html += `
					<div class="stock-item">
						<div class="stock-ticker-box">
							<h3>${ticker}</h3>
						</div>
						<div class="stock-info-box">
							<p>Quantity Owned: ${stock.quantity}</p>
							<p>Avg. Cost Per Share: $${stock.avgCost.toFixed(2)}</p>
							<p>Total Cost: $${stock.totalCost.toFixed(2)}</p>
							<p class="change ${changeClass}">Change Today: ${stock.change.toFixed(2)}%</p>
							<p>Current Price: $${stock.currentPrice.toFixed(2)}</p>
							<p>Market Value: $${stock.marketValue.toFixed(2)}</p>
						</div>
						<div class="stock-trade-box">
							<form class="trade-form" data-ticker="${ticker}">
								<input type="number" name="quantity" min="1" placeholder="Enter quantity" required>
								<button type="submit" value="buy">Buy</button>
								<button type="submit" value="sell">Sell</button>
							</form>
						</div>
					</div>`;
			}
		}
		return html;
	};

	// Handle trade form submissions (buy/sell)
	$(document).on('submit', '.trade-form', function (e) {
		e.preventDefault();

		var form = $(this);
		var action = e.originalEvent.submitter.value; // Determines whether 'buy' or 'sell'

		if (action !== 'buy' && action !== 'sell') {
			alert('Invalid action');
			return;
		}

		// Serialize form input and include ticker and action type
		var data = form.serialize() + '&ticker=' + form.data('ticker') + '&action=' + action;

		$.ajax({
			type: "POST",
			url: "tradeStock",
			data: data,
			success: function (response) {
				// Display trade result and refresh portfolio view
				alert(response.message || 'Success');
				fetchPortfolio(); // Refresh UI with updated data
			},
			error: function (xhr) {
				console.error("Failed to process trade:", xhr.responseText);
				alert("Failed to process trade. Please try again.");
			}
		});
	});
});