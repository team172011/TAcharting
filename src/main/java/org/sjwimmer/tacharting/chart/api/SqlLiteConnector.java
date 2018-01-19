package org.sjwimmer.tacharting.chart.api;


import org.sjwimmer.tacharting.chart.TaTimeSeries;
import org.sjwimmer.tacharting.chart.parameters.GeneralTimePeriod;
import org.sjwimmer.tacharting.chart.parameters.Parameter;
import org.sjwimmer.tacharting.chart.parameters.TimeFormatType;
import org.sjwimmer.tacharting.chart.utils.CalculationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.BaseTick;
import org.ta4j.core.Decimal;
import org.ta4j.core.Tick;
import org.ta4j.core.TimeSeries;

import java.io.File;
import java.sql.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

/**
 * Connection Manager for the "in app" SQL-LIte database
 *
 * Database structure:
 *     SQLLite database with one table for each {@link TimeFormatType}
 *     Structure of tables:
 *         (Symbol VARCHAR (50),
 *         Date INTEGER,
 *         Open INTEGER,
 *         High INTEGER,
 *         Low INTEGER,
 *         Close INTEGER,
 *         Volume INTEGER,
 *         Currency VARCHAR (3))
 */

public class SqlLiteConnector implements SQLConnector {
    private final Logger log = LoggerFactory.getLogger(SqlLiteConnector.class);

    private Connection con = null;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("Fehler beim Laden des JDBC-Treibers");
            e.printStackTrace();
        }
    }

    public SqlLiteConnector() {
        File dataBaseFile = new File(Parameter.DATABASE_PATH);
        if( dataBaseFile.mkdirs()){
            log.debug("Created new directory for database: {} ", dataBaseFile.getPath());
        }
        try {
            getConnection();
            Statement statement = con.createStatement();
            for(GeneralTimePeriod table: GeneralTimePeriod.values()){
               if(statement.execute(createTableStatement(table.toString()))){
                   log.debug("Created new table for {}",table);
               }
            }
        } catch (SQLException s){
            log.warn("Failed to connect to sql-lite database: {} ", s.getMessage());
        }
    }

    /**
     * Tries to establish a valid connection if there exists none
     * @throws SQLException SQLException
     */
    private void getConnection() throws SQLException{
        if (con == null){
            String connectionString = String.format("jdbc:sqlite:%s",Parameter.DATABASE_PATH+Parameter.S+Parameter.DATABASE_NAME);
            con = DriverManager.getConnection(connectionString);
            log.debug("Connected to Database {}", connectionString);
        }
    }

    public synchronized List<String> getSymbolList(GeneralTimePeriod table) throws SQLException{
        getConnection();
        String procedure = String.format("SELECT DISTINCT Symbol FROM %s", table);
        Statement stmt = con.createStatement();
        ResultSet res = stmt.executeQuery(procedure);
        ArrayList<String> symbols = new ArrayList<>();
        while(res.next()){
            symbols.add(res.getString("Symbol"));
            //symbols.add(res.getString("Symbol").replaceAll("\\s",""));
        }
        return symbols;
    }

    @Override
    public void removeData(TimeSeries series) throws SQLException {
        //TODO: implement
    }

    @Override
    public synchronized void insertData(TaTimeSeries series, boolean shouldReplace) throws SQLException{

        Statement statement = con.createStatement();
        getConnection();
        String table = series.getTimeFormatType().toString();
        String insert = "INSERT INTO";
        if(shouldReplace) {
            insert = "INSERT OR REPLACE INTO";
        }
        for (Tick cd : series.getTickData()) {
            String procedure =
                    String.format("%s %s (Symbol, Currency, Date, Open, High, Low, Close, Volume) VALUES('%s', '%s', '%s', '%s', %s, %s, %s, %s)",
                            insert,table,series.getName(), series.getCurrency().getCurrencyCode(), cd.getEndTime().toEpochSecond(), cd.getOpenPrice(), cd.getMaxPrice(), cd.getMinPrice(), cd.getClosePrice(), cd.getVolume());
            statement.execute(procedure);
        }
    }

    /**
     * returns data for the symbol <p>
     * @param symbol
     * @return Data from corresponding symbol
     * @throws SQLException
     */
    @Override
    public synchronized TimeSeries getTimeSeries(String symbol, Currency currency, GeneralTimePeriod type, ZonedDateTime from, ZonedDateTime to) throws SQLException{
        String table = type.toString();
        String query = String.format(
                "SELECT * FROM %s WHERE Symbol = '%s' AND Currency = '%s' AND Date >= '%s' AND Date <= '%s' order by Date asc, Symbol desc;",
                table,symbol,currency.getCurrencyCode(), from.toEpochSecond(), to.toEpochSecond());


        getConnection();
        Statement statement = con.createStatement();
        log.debug("Query: {}",query);

        ResultSet rset = statement.executeQuery(query);
        return transformResultSet_T(rset,type);
    }

    /**
     *
     * @param rset the result set
     * @return a TimeSeries object
     * @throws SQLException d
     */
    private TimeSeries transformResultSet_T(ResultSet rset, GeneralTimePeriod timeFormatType) throws SQLException {
        List<Tick> ticks = new ArrayList<>();

        String name=null;
        Currency currency=null;

        while (rset.next()){
            try {
                Instant i = Instant.ofEpochSecond(Long.parseLong(rset.getString("Date")));
                ZonedDateTime time = ZonedDateTime.ofInstant(i, ZoneId.systemDefault());
                name = rset.getString("Symbol");
                currency = Currency.getInstance(rset.getString("Currency"));
                BaseTick line = new BaseTick(time,
                    CalculationUtils.integerToCurrencyValue(rset.getInt("Open"), currency),
                    CalculationUtils.integerToCurrencyValue(rset.getInt("High"), currency),
                    CalculationUtils.integerToCurrencyValue(rset.getInt("Low"), currency),
                    CalculationUtils.integerToCurrencyValue(rset.getInt("Close"), currency),
                    Decimal.valueOf(rset.getInt("Volume")));
                ticks.add(line);
            } catch (DateTimeParseException dte){
                dte.printStackTrace();
                log.error("Could not be transformed: {} {}",name==null?"unnamed":name,rset.getString("Datum"));

            }
        }
        return new TaTimeSeries(name,ticks,currency,timeFormatType);
    }

    private static String createTableStatement(String tableName){
        return String.format("CREATE TABLE IF NOT EXISTS %s (Symbol VARCHAR (50), Date INTEGER, Open INTEGER, High INTEGER, Low INTEGER, Close INTEGER, Volume INTEGER,Currency VARCHAR (3));",tableName);
    }



}
