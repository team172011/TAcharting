package org.sjwimmer.tacharting.chart.api;

import org.sjwimmer.tacharting.chart.model.SQLKey;
import org.sjwimmer.tacharting.chart.model.TaTimeSeries;
import org.sjwimmer.tacharting.chart.model.types.GeneralTimePeriod;
import org.sjwimmer.tacharting.chart.model.types.TimeFormatType;
import org.sjwimmer.tacharting.chart.parameters.Parameter;
import org.sjwimmer.tacharting.chart.utils.CalculationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;
import org.ta4j.core.Decimal;

import java.io.IOException;
import java.sql.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

/**
 * Connection Manager for a SQLLite database
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
 * TODO: work with PreparedStatements instead of simple Statements. Currently this is not possible because
 * TODO: prepared statements wrap the data in the wrong format
 * TODO:   -must be possible because date is stored as long value now
 *
 * TODO: create unique index on key (symbol, date, currency)
 */

public class SqlLiteConnector implements SQLConnector {

    private final Logger log = LoggerFactory.getLogger(SqlLiteConnector.class);
    private Connection con = null;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("Error while loading JDBC-Driver");
            e.printStackTrace();
        }
    }

    public SqlLiteConnector() {
        try {
            getConnection();
            Statement statement = con.createStatement();
            for(GeneralTimePeriod table: GeneralTimePeriod.values()){
               if(statement.execute(createTableStatement(table.toString()))){
                   log.debug("Created new table for {}",table);
               }
            }
        } catch (SQLException s){
            log.error("Failed to connect to sql-lite database: {} ", s.getMessage());
        }
    }

    /**
     * Tries to establish a valid connection if none existis
     * @throws SQLException SQLException
     */
    private void getConnection() throws SQLException{
        if (con == null){
            String connectionString = String.format("jdbc:sqlite:%s",Parameter.DATABASE_PATH+Parameter.S+Parameter.DATABASE_NAME);
            con = DriverManager.getConnection(connectionString);
            log.debug("Connected to Database {}", connectionString);
        }
    }

    public synchronized List<SQLKey> getKeyList(GeneralTimePeriod table) throws SQLException{
        getConnection();
        String procedure = String.format("SELECT DISTINCT Symbol, Currency FROM %s", table);
        Statement stmt = con.createStatement();
        ResultSet res = stmt.executeQuery(procedure);
        ArrayList<SQLKey> keys = new ArrayList<>();
        while(res.next()){
            String symbol = res.getString("Symbol").replaceAll("\\s","");
            Currency currency = Currency.getInstance(res.getString("Currency"));
            keys.add(new SQLKey(symbol,table,currency));
        }
        return keys;
    }

    @Override
    public boolean removeData(TaTimeSeries series) throws SQLException {
        return removeData(series.getKey());
    }

    //TODO test
    @Override
    public boolean removeData(SQLKey key) throws SQLException {
        getConnection();
        PreparedStatement pstmt = con.prepareStatement("DELETE FROM ? WHERE SYMBOL = ? AND Currency = ?");
        pstmt.setString(0,key.period.toString());
        pstmt.setString(1,key.currency.toString());
        return pstmt.execute();
    }

    @Override
    public synchronized void insertData(TaTimeSeries series, boolean shouldReplace) throws SQLException{

        Statement statement = con.createStatement();
        getConnection();
        String table = series.getTimeFormatType().toString();
        String insert = "INSERT OR IGNORE INTO";
        if(shouldReplace) {
            insert = "INSERT OR REPLACE INTO";
        }

        int fractionDigits = series.getCurrency().getDefaultFractionDigits();
        int base = (int) Math.pow(10,fractionDigits);
        for (Bar cd : series.getBarData()) {
            String procedure =
                    String.format("%s %s (Symbol, Currency, Date, Open, High, Low, Close, Volume) " +
                                    "VALUES('%s', '%s', '%s', '%s', %s, %s, %s, %s)",
                            insert,
                            table,
                            series.getName(),
                            series.getCurrency().getCurrencyCode(),
                            cd.getEndTime().toEpochSecond(),
                            cd.getOpenPrice().multipliedBy(Decimal.valueOf(base)),
                            cd.getMaxPrice().multipliedBy(Decimal.valueOf(base)),
                            cd.getMinPrice().multipliedBy(Decimal.valueOf(base)),
                            cd.getClosePrice().multipliedBy(Decimal.valueOf(base)),
                            cd.getVolume());
            statement.execute(procedure);
        }
    }

    /**
     * returns data for the symbol <p/>
     * @param key the symbol
     * @return Data from corresponding symbol
     * @throws SQLException SQLException
     */
    @Override
    public synchronized TaTimeSeries getSeries(SQLKey key, ZonedDateTime from, ZonedDateTime to) throws SQLException{
        String table = key.period.toString();
        String query = String.format(
                "SELECT * FROM %s WHERE Symbol = '%s' AND Currency = '%s' AND Date >= '%s' AND Date <= '%s' order by Date asc, Symbol desc;",
                table,key.symbol,key.currency.getCurrencyCode(), from.toEpochSecond(), to.toEpochSecond());


        getConnection();
        Statement statement = con.createStatement();
        log.debug("Query: {}",query);

        ResultSet rset = statement.executeQuery(query);
        return transformResultSet(rset, key.period);
    }

    @Override
    public Bar getLastBar(SQLKey key) {
        return null;
    }

    @Override
    public Bar getFirstBar(SQLKey key) {
        return null;
    }

    /**
     *
     * @param rset the result set
     * @return a TimeSeries object
     * @throws SQLException d
     */
    private TaTimeSeries transformResultSet(ResultSet rset, GeneralTimePeriod timeFormatType) throws SQLException {
        List<Bar> ticks = new ArrayList<>();

        String name=null;
        Currency currency=null;

        while (rset.next()){
            try {
                Instant i = Instant.ofEpochSecond(Long.parseLong(rset.getString("Date")));
                ZonedDateTime time = ZonedDateTime.ofInstant(i, ZoneId.systemDefault());
                name = rset.getString("Symbol");
                currency = Currency.getInstance(rset.getString("Currency"));
                BaseBar line = new BaseBar(time,
                    CalculationUtils.integerToCurrencyValue(rset.getInt("Open"), currency),
                    CalculationUtils.integerToCurrencyValue(rset.getInt("High"), currency),
                    CalculationUtils.integerToCurrencyValue(rset.getInt("Low"), currency),
                    CalculationUtils.integerToCurrencyValue(rset.getInt("Close"), currency),
                    Decimal.valueOf(rset.getInt("Volume")));
                ticks.add(line);
            } catch (DateTimeParseException dte){
                dte.printStackTrace();
                log.error("Could not be transformed: {} {}",
                        name==null?"unnamed":name,
                        rset.getString("Datum"));

            }
        }
        return new TaTimeSeries(name,ticks,currency,timeFormatType);
    }

    private static String createTableStatement(String tableName){
        return String.format("CREATE TABLE IF NOT EXISTS %s (" +
                "Symbol VARCHAR (50), " +
                "Date INTEGER, " +
                "Open INTEGER, " +
                "High INTEGER, " +
                "Low INTEGER, " +
                "Close INTEGER, " +
                "Volume INTEGER, " +
                "Currency VARCHAR (3)," +
                        "PRIMARY KEY (" +
                        "Symbol," +
                        "Date," +
                        "Currency)" +
                        ");"
                ,tableName);
    }

    @Override
    public TaTimeSeries getSeries(SQLKey resource) throws IOException {
        try {
            return getSeries(resource, ZonedDateTime.now().minusYears(100), ZonedDateTime.now());
        } catch (SQLException sqle){
            sqle.printStackTrace();
        }
        return null;
    }
}
