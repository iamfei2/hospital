package com.hospit.common;

public class Result {
    private int code;
    private String msg;
    private long total;
    private Object data;

    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }
    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }

    public static Result fail() { return result(400, "操作失败", 0L, null); }
    public static Result fail(String msg) { return result(400, msg, 0L, null); }
    public static Result success() { return result(200, "操作成功", 0L, null); }
    public static Result success(Object data) { return result(200, "操作成功", 0L, data); }
    public static Result success(Object data, String msg) { return result(200, msg, 0L, data); }
    public static Result success(Object data, Long total) { return result(200, "操作成功", total, data); }
    
    public static Result result(int code, String msg, long total, Object data) {
        Result res = new Result();
        res.setCode(code);
        res.setMsg(msg);
        res.setData(data);
        res.setTotal(total);
        return res;
    }
}
