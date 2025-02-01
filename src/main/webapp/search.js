$(document).ready(function() {
    checkLoginStatus();

    $('#searchForm').submit(function(e) {
        e.preventDefault();
        var ticker = $('#searchQuery').val().toUpperCase();
        const validTickers = ['TSLA', 'AAPL', 'MSFT', 'AMD', 'NVDA', 'COIN', 'ROKU', 'RBLX'];

        if (validTickers.includes(ticker)) {
            $.ajax({
                type: "GET",
                url: "stockInfo",
                data: { ticker: ticker },
                success: function(response) {
                    var stockData = typeof response === 'string' ? JSON.parse(response) : response;
                    checkLoginStatus(function(isLoggedIn) {
                        if (isLoggedIn) {
                            $('#stockDetails').html(generateDetailedStockHtml(stockData));
                        } else {
                            $('#stockDetails').html(generateStockHtml(stockData));
                        }
                        $('#searchContainer').hide();
                    });
                },
                error: function() {
                    alert("Error retrieving stock information.");
                }
            });
        } else {
            alert("Please enter a valid ticker: TSLA, AAPL, MSFT, COIN, ROKU, RBLX, or AMD");
        }
    });

    $(document).on('click', '#logoutLink', function(e) {
        e.preventDefault();
        $.ajax({
            type: "GET",
            url: "logout",
            success: function() {
                window.location.reload(); 
            },
            error: function() {
                alert("Error logging out.");
            }
        });
    });

    $(document).on('click', '.home-search-link', function(e) {
        e.preventDefault();
        $('#searchContainer').show();
        $('#stockDetails').empty();
    });
});

function generateStockHtml(stockData) {
    var profile = stockData.profile;
    var quote = stockData.quote;
	var changeClass = quote.d > 0 ? 'positive' : 'negative'; 
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
        success: function(response) {
            alert(response.message);
            if (response.success) {
               
            }
        },
        error: function() {
            alert("Failed to process stock purchase.");
        }
    });
}

function checkLoginStatus(callback) {
    $.ajax({
        type: "GET",
        url: "checkLogin",
        success: function(response) {
            if (response.trim() === "loggedIn") {
                $('.navigation-links').html('<a href="#" class="home-search-link">Home/Search</a> <a href="#" id="logoutLink">Logout</a> <a href="Portfolio.html">Portfolio</a>');
             
                if (callback) callback(true);
            } else {
                $('.navigation-links').html('<a href="#" class="home-search-link">Home/Search</a> <a href="LoginRegister.html">Login / Sign Up</a>');
                if (callback) callback(false);
            }
        },
        error: function() {
            alert("Error checking login status.");
            if (callback) callback(false);
        }
    });
}