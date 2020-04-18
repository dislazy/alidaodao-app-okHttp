package com.alidaodao.app.okHttp.callback;

/**
 *
 * @desc 异步http下载请求回调接口,支持 Lambda 表达式
 * @author bosong
 * @date 2020/4/18 15:49
 */
@FunctionalInterface
public interface IAsyncCallback4Download {
    /**
     * @desc 异步回调接口的执行方法,传入 okhttp3.Response
     * @author bosong
     * @date 2020/4/18 15:49
     * @param  response byte[]
     * @return void
     */
    void doCallback(byte[] response);
    
}
