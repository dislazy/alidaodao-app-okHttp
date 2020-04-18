package com.alidaodao.app.okHttp.callback;

/**
 *
 * @desc 异步http请求回调接口,支持 Lambda 表达式
 * @author bosong
 * @date 2020/4/18 15:48
 */
@FunctionalInterface
public interface IAsyncCallback {

    /**
     * @desc 异步http请求回调接口
     * @author bosong
     * @date 2020/4/18 15:48
     * @param responseBody r
     * @return 响应结果
     */
    void doCallback(String responseBody);
    
}
