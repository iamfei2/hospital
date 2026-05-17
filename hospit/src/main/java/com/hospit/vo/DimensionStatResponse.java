package com.hospit.vo;

import lombok.Data;
import java.util.List;

@Data
public class DimensionStatResponse {
    private List<String> dimensions;
    private List<String> xAxis;
    private List<SeriesData> series;

    @Data
    public static class SeriesData {
        private String name;
        private List<Long> data;

        public SeriesData() {}

        public SeriesData(String name, List<Long> data) {
            this.name = name;
            this.data = data;
        }
    }

    public DimensionStatResponse() {}

    public DimensionStatResponse(List<String> dimensions, List<String> xAxis, List<SeriesData> series) {
        this.dimensions = dimensions;
        this.xAxis = xAxis;
        this.series = series;
    }
}
