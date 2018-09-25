package org.sjwimmer.tacharting.implementation.service;

import java.util.Currency;

import pl.zankowski.iextrading4j.api.stocks.ChartRange;

public class IEXDataSourceConfiguration {
	
	private final ChartRange chartRange;
	private final Currency currency;
	
	
	public IEXDataSourceConfiguration(ChartRange chartRange, Currency currency) {
		this.chartRange = chartRange;
		this.currency = currency;
	}
	
	public IEXDataSourceConfiguration() {
		this.chartRange = ChartRange.FIVE_YEARS;
		this.currency = Currency.getInstance("USD");
	}

	public ChartRange getChartRange() {
		return chartRange;
	}

	public Currency getCurrency() {
		// TODO Auto-generated method stub
		return null;
	}
}
