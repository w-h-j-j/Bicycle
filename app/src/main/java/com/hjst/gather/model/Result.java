package com.hjst.gather.model;

/**
 * 通用 API 响应包装
 * JSON 结构：{code:0, msg:"xxx", data:...}，T 为 data 的具体类型
 */
public class Result<T> {

    private int code;
    private String msg;
    private T data;

    /** 业务成功判断，服务端约定 code=0 为成功 */
    public boolean isSuccess() {
        return code == 0;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Result{code=" + code + ", msg='" + msg + "', data=" + data + '}';
    }
}
