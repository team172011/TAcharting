package org.sjwimmer.tacharting.chart.types;

/**
 * Enum for available time periods that can be requested from the yahoo api
 */
public enum YahooTimePeriod {
        daily(GeneralTimePeriod.DAY),
        weekly(GeneralTimePeriod.FIVE_DAY),
        monthly(GeneralTimePeriod.MONTH);

        /** corresponding general {@link GeneralTimePeriod} for this YahooTimePeriod */
        public final GeneralTimePeriod generalTimePeriod;

        /**
         * @param period corresponding general {@link GeneralTimePeriod} for this YahooTimePeriod
         */
        YahooTimePeriod(GeneralTimePeriod period){
            this.generalTimePeriod = period;
        }

        public String toYahooString() {
            switch (this){
                case daily:
                    return "1d";
                case weekly:
                    return "5d";
                case monthly:
                    return "1mo";
                default:
                    return "1d";
            }
        }

        public static YahooTimePeriod of(final String name){
            switch (name){
                case "1d":
                    return daily;
                case "5d":
                    return weekly;
                case "1mo":
                    return monthly;
                default:
                    throw new IllegalArgumentException(String.format("String value not supported: \"%s\"",name));
            }
        }
}
