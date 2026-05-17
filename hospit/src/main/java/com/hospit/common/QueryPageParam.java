package com.hospit.common;

import java.util.HashMap;

public class QueryPageParam {
    private static int page_size=5;
    private static int page_num=1;

    private int pageSize=page_size;
    private int pageNum = page_num;
    private HashMap param=new HashMap();

    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
    public int getPageNum() { return pageNum; }
    public void setPageNum(int pageNum) { this.pageNum = pageNum; }
    public HashMap getParam() { return param; }
    public void setParam(HashMap param) { this.param = param; }
}
