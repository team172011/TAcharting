package org.sjwimmer.tacharting.chart.model;

import javax.json.JsonObject;

public interface JsonRessource {


    JsonObject createJsonObject();

    /**
     *
     * @param dataSize the max number of data elements (bars, indicatorValues) starting from
     *                 the most recent
     * @return constrained json object with data of the resource
     */
    JsonObject createJsonObject(int dataSize);
}
