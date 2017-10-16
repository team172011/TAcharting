/*
 The MIT License (MIT)

 Copyright (c) 2017 Wimmer, Simon-Justus

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package example;

import com.opencsv.CSVReader;
import eu.verdelhan.ta4j.BaseTick;
import eu.verdelhan.ta4j.BaseTimeSeries;
import eu.verdelhan.ta4j.Tick;
import eu.verdelhan.ta4j.TimeSeries;

import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class for loading example/testing data from this repository
 */
public class Loader {

    private static final DateTimeFormatter DATE_FORMAT_HOURLY_MINUTE = DateTimeFormatter.ofPattern("yyyy-MM-dd H:m:s z");
    private static final DateTimeFormatter DATE_FORMAT_Daily = DateTimeFormatter.ofPattern("yyyy/MM/dd");


    public static TimeSeries getHourlyTimeSeries(String pathToCsv, String name){

        List<Tick> ticks = new ArrayList<>();
        CSVReader reader;
        try {
            reader = new CSVReader(new FileReader(pathToCsv));
            String[] line;
            reader.readNext();
            while ((line = reader.readNext()) != null) {
                ZonedDateTime date = ZonedDateTime.parse(line[0]+" "+line[1]+" PST", DATE_FORMAT_HOURLY_MINUTE);
                double open = Double.parseDouble(line[2]);
                double high = Double.parseDouble(line[3]);
                double low = Double.parseDouble(line[4]);
                double close = Double.parseDouble(line[5]);
                double volume = Double.parseDouble(line[6]);

                ticks.add(new BaseTick(date, open, high, low, close, volume));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (ticks.get(0).getEndTime().isAfter(ticks.get(ticks.size()-1).getEndTime()))
            Collections.reverse(ticks);

        return new BaseTimeSeries(name, ticks);
    }

    public static TimeSeries getMinuteTimeSeries(String pathToCsv, String name){
        return getHourlyTimeSeries(pathToCsv, name);
    }

    public static TimeSeries getDailyTimeSerie(URL file, String name){

        List<Tick> ticks = new ArrayList<>();
        CSVReader reader;
        try {
            reader = new CSVReader(new FileReader(file.getFile()));
            String[] line;
            reader.readNext();
            reader.readNext();
            while ((line = reader.readNext()) != null) {
                ZonedDateTime date = LocalDate.parse(line[0], DATE_FORMAT_Daily).atStartOfDay(ZoneId.systemDefault());
                double close = Double.parseDouble(line[1]);
                double volume = Double.parseDouble(line[2]);
                double open = Double.parseDouble(line[3]);
                double high = Double.parseDouble(line[4]);
                double low = Double.parseDouble(line[5]);

                ticks.add(new BaseTick(date, open, high, low, close, volume));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (ticks.get(0).getEndTime().isAfter(ticks.get(ticks.size()-1).getEndTime()))
            Collections.reverse(ticks);
        return new BaseTimeSeries(name, ticks);
    }

	
	
}