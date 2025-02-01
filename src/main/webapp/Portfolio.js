$(document).ready(function () {
    $.ajaxSetup({ cache: false }); 
    fetchPortfolio();

  function fetchPortfolio() {
        $.ajax({
            type: "GET",
            url: "portfolio",
            success: function (response) {
                // Parse response if it's a string
                var data = typeof response === 'string' ? JSON.parse(response) : response;
                var stocksHtml = buildStocksHtml(data.stocks);
                var total = 0;
                for (var ticker in data.stocks) {
					var stock = data.stocks[ticker];
        			if(stock.quantity > 0){
						total++
					}
				}
                if(total > 0){
					userMessage = `<p>Here are your stocks:</p>`;
				}
				else{
					userMessage = `<p>You don't own any stocks!<br>Go buy some!</p>`;
				}
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
                console.error("Error loading portfolio:", xhr.responseText);
                $('#portfolioDetails').html('<p>Error loading portfolio. Please try again later.</p>');
                if (xhr.status === 401) {
                    alert('Please log in to view your portfolio.');
                    window.location.href = "LoginRegister.html";
                }
            }
        });
    }

function buildStocksHtml(stocks) {
    var html = '';
    for (var ticker in stocks) {
        var stock = stocks[ticker];
        if(stock.quantity > 0){
            var changeClass = stock.change > 0 ? 'positive' : 'negative';    
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
   

$(document).on('submit', '.trade-form', function (e) {
    e.preventDefault();

    var form = $(this);

    // Get the action from the button that was clicked
    var action = e.originalEvent.submitter.value;

    console.log("Detected action: ", action);

    if (action !== 'buy' && action !== 'sell') {
        alert('Invalid action');
        return;
    }

var data = form.serialize() + '&ticker=' + form.data('ticker') + '&action=' + action; // Append action
    console.log("Serialized data: ", data);

    $.ajax({
        type: "POST",
        url: "tradeStock",
        data: data,
        success: function (response) {
            console.log("Trade response:", response);
            var message = response.message; 
            if (message) {
                alert(message);
            } else {
                alert('Success but no message provided');
            }
            fetchPortfolio(); // Refresh portfolio after trade
        },
        error: function (xhr) {
            console.error("Failed to process trade:", xhr.responseText);
            alert("Failed to process trade. Please try again.");
        }
    });
});
});