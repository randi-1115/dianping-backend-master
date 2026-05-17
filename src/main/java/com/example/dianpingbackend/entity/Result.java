package com.example.dianpingbackend.entity;

import lombok.Data;

@Data
public class Result<T> {//泛型类 用于封装接口的返回结果
    private Integer code;//状态码 0表示成功 其他表示失败
    private String msg;//提示信息
    private T data;//返回的数据

    //静态方法 用于快速创建成功或失败的结果对象
    public static <T> Result<T> ok(String msg){
        Result<T> r = new Result<>();
        r.code=0;
        r.msg=msg;
        return r;
    }
    //重载ok方法 允许直接传入数据
    public static <T> Result<T> ok(String msg , T data){
        Result<T> r=ok(msg);
        r.data = data;
        return r;
    }

    public static <T> Result<T> fail(Integer code,String msg){
        Result<T> r = new Result<>();
        r.code = code;
        r.msg = msg;
        return r;
    }
}
