**Overview**
ConnorStocks is a mock stock trading dynamic web application that allows users to buy and sell stocks as well as manage a portfolio based on realtime data from Finnhub API. Using Java, JavaScript, HTML, CSS, MySQL, and Tomcat, the web app provides a well designed and fun mock trading experience.

**Features**

-Real-Time Stock Data: Allows users to search for stock tickers and Fetches/displays stock information dynamically.

-User Portfolio Management: Allows users to track, buy, and sell the stocks the they own all from one portfolio page.

--Dynamic Web Interface: Built using JavaScript and Java Servlets for a responsive experience, with styled UI using HTML and CSS

Database Integration: Uses MySQL for storing user authentication, portfolio, and stock info.

-Secure Authentication: Implements login/logout functionality with session management.

**Tech Stack**

-Backend: Java, Tomcat, Servlets, JDBC

-Frontend: HTML, CSS, JavaScript,

-Database: MySQL

**Installation & Setup**

**Prerequisites**

-Java Development Kit (JDK) installed

-Apache Tomcat Server configured

-MySQL Database set up

-Git installed (optional, for version control)

**Steps to Run**

-Clone the repository:

-git clone https://github.com/your-repo/JoeStocks.git

-Import the project into Eclipse (or any Java IDE supporting web applications).

-Configure MySQL database and update database credentials in the dbConfig file.

-Deploy the application on Tomcat.

**Database Schema**

-The application uses a MySQL database with key tables:

-users (id, username, password, email)

-stocks (id, symbol, name, price, date)

-portfolio (user_id, stock_id, quantity, purchase_price)









