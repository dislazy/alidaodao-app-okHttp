package com.alidaodao.app.okHttp.callback;

import okhttp3.Response;

/**
 *
 * @desc 异步http请求回调接口,支持 Lambda 表达式
 * @author bosong
 * @date 2020/4/18 15:50
 */
@FunctionalInterface
public interface IAsyncCallback4Response {

    /**
     * @desc 异步回调接口的执行方法,传入 okhttp3.Response
     * @author bosong
     * @date 2020/4/18 15:50
     * @param  response Response
     * @return void
     */
    void doCallback(Response response);
    
}
