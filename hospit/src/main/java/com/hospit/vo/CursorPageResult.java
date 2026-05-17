package com.hospit.vo;

import lombok.Data;
import java.util.List;

@Data
public class CursorPageResult<T> {
    private List<T> records;
    private String cursor;
    private boolean hasMore;
    private int pageSize;

    public CursorPageResult() {}

    public CursorPageResult(List<T> records, String cursor, boolean hasMore, int pageSize) {
        this.records = records;
        this.cursor = cursor;
        this.hasMore = hasMore;
        this.pageSize = pageSize;
    }
}
