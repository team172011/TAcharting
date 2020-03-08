package org.sjwimmer.tacharting.implementation.service;

import java.time.*;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.stream.Collectors;

import org.sjwimmer.tacharting.chart.api.OHLCVDataSource;
import org.sjwimmer.tacharting.chart.model.TaBarSeries;
import org.sjwimmer.tacharting.chart.model.types.GeneralTimePeriod;
import org.sjwimmer.tacharting.chart.parameters.Parameter;
import org.sjwimmer.tacharting.implementation.model.api.key.IEXKey;
import org.sjwimmer.tacharting.implementation.util.FormatUtils;
import org.sjwimmer.tacharting.implementation.util.BarSeriesConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.BarSeries;
import org.ta4j.core.num.PrecisionNum;

import pl.zankowski.iextrading4j.api.stocks.Chart;
import pl.zankowski.iextrading4j.api.stocks.ChartRange;
import pl.zankowski.iextrading4j.client.IEXTradingClient;
import pl.zankowski.iextrading4j.client.rest.request.stocks.ChartRequestBuilder;

public class IEXDataSource implements OHLCVDataSource<IEXKey, Object>{

	private final static Logger log = LoggerFactory.getLogger(IEXDataSource.class);
	private final IEXDataSourceConfiguration config;
	final IEXTradingClient iexTradingClient = IEXTradingClient.create();
	
	public IEXDataSource(IEXDataSourceConfiguration configuration) {
		this.config = configuration;
	}
	
	public IEXDataSource() {
		this(new IEXDataSourceConfiguration());
	}
	
	@Override
	public List<String> getAllAvailableSymbols() throws Exception {
		return new ArrayList<String>();
	}

	@Override
	public TaBarSeries getSymbolData(IEXKey symbol) throws Exception {
		return getSymbolData(symbol,ZonedDateTime.now().minusYears(100),ZonedDateTime.now().plusYears(100));
	}

	@Override
	public TaBarSeries getSymbolData(IEXKey key, ZonedDateTime from, ZonedDateTime to) {
		IEXBarSeriesConverter converter = new IEXBarSeriesConverter(key.toString());
		List<Chart> data = new ArrayList<>();
		try {
			data = iexTradingClient.executeRequest(
					new ChartRequestBuilder()
						.withSymbol(key.toString())
						.withChartRange(config.getChartRange())
						.build());
		} catch(NullPointerException npe) {
			log.error(npe.getMessage());
		}	
		return converter.convert(data);
	}

	@Override
	public List<TaBarSeries> getSymbolData(List<IEXKey> symbols, ZonedDateTime from, ZonedDateTime to){
		return symbols.stream().map(line -> {
			try {
				return getSymbolData(line);
			} catch (Exception e) {
				log.error(String.format("Error requesting data for %s (%s)",symbols.toString(), e.getMessage()));
				BarSeries series = new BaseBarSeries(line.toString(),Parameter.numFunction);
				return new TaBarSeries(series, config.getCurrency() ,rangeToPeriod(config.getChartRange()));
			}
		}).collect(Collectors.toList());
	}

	//TODO check if this mapping is correct
	private GeneralTimePeriod rangeToPeriod(ChartRange chartRange) {
		switch(chartRange) {
			case DYNAMIC:{
				return GeneralTimePeriod.MINUTE;
			}
			case INTRADAY:{
				return GeneralTimePeriod.REALTIME;
			}
			default:{
				return GeneralTimePeriod.DAY;
			}
		}
	}

	@Override
	public boolean connect(Object c) {
		try {
			iexTradingClient.executeRequest(
					new ChartRequestBuilder()
					.withSymbol("AAPL")
					.withChartRange(ChartRange.ONE_MONTH)
					.build());
		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
			return false;
		}
		return true;
		
	}

	@Override
	public void disconnect() {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean isReady() {
		return connect(Void.TYPE);
	}
	
	class IEXBarSeriesConverter implements BarSeriesConverter<List<Chart>>{

		private final String name;
		
		public IEXBarSeriesConverter(String name){
			this.name = name;
		}
		
		@Override
		public TaBarSeries convert(List<Chart> others) {
			BarSeries series = new BaseBarSeries(name, PrecisionNum::valueOf);
			for(Chart other: others) {
				ZonedDateTime time = ZonedDateTime.of(LocalDate.parse(other.getDate()),LocalTime.of(12, 0,0), ZoneId.systemDefault());
				series.addBar(
						new BaseBar(Duration.ZERO, time, Parameter.numFunction.apply(other.getOpen()), Parameter.numFunction.apply(other.getHigh()), Parameter.numFunction.apply(other.getLow()), Parameter.numFunction.apply(other.getClose()), Parameter.numFunction.apply(other.getVolume()),Parameter.numFunction.apply(0)));
			}
			return new TaBarSeries(series,Currency.getInstance("USD") ,FormatUtils.extractPeriod(series));
		}	
	}
}
