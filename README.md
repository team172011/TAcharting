# TAcharting
### An application for downloading, plotting and analysing financial data

## Features
- [x] Supports all (Decimal based) indicators from [Ta4j](https://github.com/ta4j/ta4j).
- [x] Add Strategies and run them on several securities
- [x] Yahoo api connection
- [x] Import excel and csv files
- [x] Stores settings and parameters of indicators permanently
- [x] Possibility to add your custom indicator implementation to the application
- [x] Load, store and update financial data in SQLlite or custom database

## What is TAcharting? 
_(features may be still under development)_<br/>
##### On the one hand the aim of TAcharting is to provide a self contained application for technical analysis that is easy to start and use:
 - plot financial data from CSV/Excel files 
 - connect with yahoo financial api and request financial data
 - automatically store data in database
 - add indicators and save indicator properties
 - customize indicators
 - save graph as png
 - save indicators and financial data as excel/csv
 
##### On the other hand TAcharting should stay developer friendly and give you a plurality of possibility to customize, embed or automate:
 - write your own `Indicator`s and plot them on the chart
 - write a trading [strategy](https://github.com/ta4j/ta4j/blob/master/ta4j-core/src/main/java/org/ta4j/core/Strategy.java) and let it run and plot by TAcharting
 - write additional `Connector` to connect the application other data sources
 - write your own `SQLConnector` to connect with custom database
 - write your own `SQLPropertiesManager` to store indicator parameters in a customized way
 - embed `ChartController` or `TAChart` to support your own application

### How to run
_TAcharting_ is a JavaFx(java 1.8) application managed by maven. You have to install [git](https://git-scm.com/downloads), [maven](https://maven.apache.org/download.cgi) and the [jdk1.8+](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
to build this application on 
linux or windows (not testet on mac, but it should also work there)<br>
**1. Clone repository:**
```
git clone https://github.com/team172011/TAcharting.git
cd TAcharting
``` 
**2. run maven to create executable jar**
````git
mvn package
````
**3. run the generated jar**
````git
java -jar target/jfx/app/tacharting-*.jar

````


### How to add custom indicators
* Start at the [Wiki](https://github.com/team172011/ta4j-charting/wiki) or take a look at program starting [Example](https://github.com/team172011/ta4j-charting/blob/master/src/example/Example.java)

![Overview](https://github.com/team172011/ta4j-charting/blob/master/src/main/java/org/sjwimmer/tacharting/data/screenshots/overview.png)


### Ta4j and jFreeChart
* https://github.com/ta4j/ta4j/
* https://github.com/jfree/jfreechart
