# Smyths

This project is aimed at creating an open API for syncing the projects found on the Irish toy retailer [Smyths Toys](https://www.smythstoys.com/). I started this as a hobby and hope to one day migrate it to a production level environment for "real" users. 

I am using IntelliJ to develop this and should be easily imported from existing projects via the build.gradle file or through cloning this repo directly.

### Features Implemented
* Syncing of products from the smyths website 
    * Over 6000 as of 25/05/2017
    * Can take over an hour to fully do (On my slow connection anyway)
* Syncing of store locations from smyths website
* Polling jobs to sync data
* Storing of data in POSTGIS [docker](https://hub.docker.com/r/mdillon/postgis) instance 
    * This needs to change, because.. It's crap.

### Features to implement
* Muli-threaded syncing of products, locations and links.
* Users ~Ugh~ Yay!
    * Logins
    * Favourites
    * Item alerts
* Smarter Search
    * Research Algorithms
    * Improve Database querying
* Migrate from POSTGIS to something more kotlin friendly (it's awkward to use)
* Crypto Service
* Make a docker instance

### Brief Overview

#### Catalogue Controller
This is the main controller of the project and is responsible for syncing the data from the website. 

#### Display Controller
This is the controller that handles the requests for the histories of each product needed to be displayed in a pretty way by the front end.

#### Location Controller
This is the controller that handles the requests for the store locations in smyths.

#### Product Controller
This is the controller that handles the requests for the products stored locally in my database or by using their search bar.

#### Product Service
In short it is effectively a web scraper. For each url the database, it will parse the HTML at that url and extract the products and save them to the database under the SmythsProductRepository. This table and service should only keep track of the latest products that match the smyths website exactly. 

This also updates the historical data too.

#### History Service
In short it is effectively a web scraper. As the name suggests each time a sync job is carried out for the products a new row is added for each product, regardless if it is the same as the previous day. In short, it stores the histocial data about all the products.

For each url the database, it will parse the HTML at that url and extract the products and save them to the database under the SmythsHistoricalProductRepository. 

#### Link Service
This is another web scraper. This is the first step in the syncing process, it starts off at the main page of smyths.ie and then proceeds to extract all the url's for each category present in the headers nav bar.

#### Location Service
This is also web scraper. Polls the url for the smyth's store locations found in the Constants class.

#### Http Service
Sends off the http requests and handles the responses, returns html or json depending on the call.

#### Crypto Service
TBC

#### Database 
Each Table is created on boot. This can be seen in the SmythApp file. Each Table is it's own object located in the Database file, this is a list of the table in my schema (that would be a more suitable name for it). This is using the kotlin POSTGIS driver for POSTGres located [here](https://github.com/JetBrains/Exposed) (It's very shitty and I will change it at some stage).
