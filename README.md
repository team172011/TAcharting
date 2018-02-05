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
* Start at the [Wiki](https://github.com/team172011/ta4j-charting/wiki) or take a look at the [Example](https://github.com/team172011/ta4j-charting/blob/master/src/example/Example.java)

![Overview](https://github.com/team172011/ta4j-charting/blob/master/src/main/java/org/sjwimmer/tacharting/data/screenshots/overview.png)

![Charting Application based on ta4j](https://github.com/team172011/ta4j-charting/blob/master/src/main/java/org/sjwimmer/tacharting/data/screenshots/showOtherIndicators.png)


### Plot OHLC data as candle sticks
![Show Chart](https://github.com/team172011/ta4j-charting/blob/master/src/main/java/org/sjwimmer/tacharting/data/screenshots/show_graph.png)

### Plot a strategy, basic and custom indicators
![Show Strategy](https://github.com/team172011/ta4j-charting/blob/master/src/main/java/org/sjwimmer/tacharting/data/screenshots/show_strategy.png)

### Set up the parameters of the indicators via gui
![Show Chart](https://github.com/team172011/ta4j-charting/blob/master/src/main/java/org/sjwimmer/tacharting/data/screenshots/IndicatorSettings.PNG)

### Plot several trading records from backtestings and analyse entry, exit and hold intervals
![Show Strategy](https://github.com/team172011/ta4j-charting/blob/master/src/main/java/org/sjwimmer/tacharting/data/screenshots/plotRecord.png)

### Ta4j and jFreeChart
* https://github.com/ta4j/ta4j/
* https://github.com/jfree/jfreechart
