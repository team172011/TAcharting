/*
 GNU Lesser General Public License

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

package org.sjwimmer.tacharting.example;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.ta4j.core.BaseTick;
import org.ta4j.core.BaseTimeSeries;
import org.ta4j.core.Tick;
import org.ta4j.core.TimeSeries;

import java.io.*;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class for loading org.sjwimmer.tacharting.example/testing org.sjwimmer.tacharting.data from this repository
 */
public class Loader {

    private static final DateTimeFormatter DATE_FORMAT_HOURLY_MINUTE = DateTimeFormatter.ofPattern("yyyy-MM-dd H:m:s z");
    private static final DateTimeFormatter DATE_FORMAT_Daily = DateTimeFormatter.ofPattern("yyyy/MM/dd");


    public static TimeSeries getHourlyTimeSeries(URL file, String name){

        List<Tick> ticks = new ArrayList<>();
        CSVReader reader;
        String nameInCSV="";
        try {
            reader = new CSVReaderBuilder(new FileReader(file.getFile())).withSkipLines(1).build();
            String[] line;
            nameInCSV = reader.readNext()[0];
            if(nameInCSV==null || nameInCSV.equals("")){
                nameInCSV = name;
            }
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

        return new BaseTimeSeries(nameInCSV, ticks);
    }

    public static TimeSeries getMinuteTimeSeries(URL file, String name){
        return getHourlyTimeSeries(file, name);
    }

    public static TimeSeries getDailyTimeSeries(String fileName){
        // load a TimeSeries
        InputStream inputStream = Loader.class.getClassLoader().getResourceAsStream(fileName);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        List<Tick> ticks = new ArrayList<>();
        CSVReader reader;
        String nameInCSV="";
        try {
            reader = new CSVReaderBuilder(bufferedReader).withSkipLines(1).build();
            String[] line;
            nameInCSV = reader.readNext()[0];
            if(nameInCSV==null||nameInCSV.equals("")){
                nameInCSV=fileName;
            }
            while ((line = reader.readNext()) != null) {
                ZonedDateTime date = LocalDate.parse(line[0], DATE_FORMAT_Daily).atStartOfDay(ZoneId.systemDefault());
                double close = Double.parseDouble(line[1]);
                double volume = Double.parseDouble(line[2]);
                double open = Double.parseDouble(line[3]);
                double high = Double.parseDouble(line[4]);
                double low = Double.parseDouble(line[5]);

                ticks.add(new BaseTick(date, open, high, low, close, volume));
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (ticks.get(0).getEndTime().isAfter(ticks.get(ticks.size()-1).getEndTime()))
            Collections.reverse(ticks);
        return new BaseTimeSeries(nameInCSV, ticks);
    }

	
	
}