package com.alidaodao.app.okHttp;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.alidaodao.app.okHttp.callback.IAsyncCallback;
import com.alidaodao.app.okHttp.callback.IAsyncCallback4Download;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;
import com.alidaodao.app.okHttp.callback.IAsyncCallback4Response;
import com.alidaodao.app.okHttp.utils.HttpsUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.FormBody.Builder;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 *
 * @desc Generic OkHttp package
 * @author bosong
 * @date 2020/4/18 16:04
 */
public final class CommonOkHttpClient {

    private final OkHttpClient okHttpClient;

    CommonOkHttpClient(long readTimeout, long writeTimeout, long connectTimeout, HttpsUtils.SSLParams sslParams) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.readTimeout(readTimeout, TimeUnit.MILLISECONDS);
        builder.writeTimeout(writeTimeout, TimeUnit.MILLISECONDS);
        builder.connectTimeout(connectTimeout, TimeUnit.MILLISECONDS);

        if (sslParams != null) {
            builder.sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager);
            if (sslParams.hostnameVerifier != null) {
                builder.hostnameVerifier(sslParams.hostnameVerifier);
            }
        }
        okHttpClient = builder.build();
    }

    /**
     * @desc Send get request
     * @author bosong
     * @date 2020/4/18 16:30
     * @param url url
     * @param callback callback
     * @return 响应
     */
    public String get(String url, IAsyncCallback callback) {
        Request request = new Request.Builder().get().url(url).build();
        return (String) sendRequest(request, false, callback, null);
    }

    /**
     * @desc Send get request
     * @author bosong
     * @date 2020/4/18 16:30
     * @param url url
     * @param callback callback
     * @param headerExt head
     * @return 响应
     */
    public Response get(String url, Map<String, String> headerExt, IAsyncCallback4Response callback) {
        okhttp3.Request.Builder reqBuilder = new Request.Builder().get().url(url);
        if (headerExt != null && headerExt.size() > 0) {
            headerExt.forEach(reqBuilder::addHeader);
        }
        Request request = reqBuilder.build();
        return (Response) sendRequest(request, true, null, callback);
    }

    /**
     * @desc Send post request
     * @author bosong
     * @date 2020/4/18 16:30
     * @param url url
     * @param callback callback
     * @return 响应
     */
    public String post(String url, IAsyncCallback callback) {
        return (String) doPost(url, null, null, callback, null, false, null);
    }

    /**
     * @desc Send post request
     * @author bosong
     * @date 2020/4/18 16:30
     * @param url url
     * @param callback callback
     * @param jsonStr json
     * @return 响应
     */
    public String post(String url, String jsonStr, IAsyncCallback callback) {
        return (String) doPost(url, null, jsonStr, callback, null, false, null);
    }

    /**
     * @desc Send post request
     * @author bosong
     * @date 2020/4/18 16:30
     * @param url url
     * @param callback callback
     * @param xmlStr xml
     * @return 响应
     */
    public String post(String url, IAsyncCallback callback, String xmlStr) {
        return (String) doPost(url, null, xmlStr, "application/xml", callback, null, false, null);
    }

    /**
     * @desc Send post request
     * @author bosong
     * @date 2020/4/18 16:30
     * @param url url
     * @param callback callback
     * @param jsonStr json
     * @param headerExt head
     * @return 响应
     */
    public Response post(String url, String jsonStr, Map<String, String> headerExt, IAsyncCallback4Response callback) {
        return (Response) doPost(url, null, jsonStr, null, callback, true, headerExt);
    }

    /**
     * @desc Send post request
     * @author bosong
     * @date 2020/4/18 16:30
     * @param url url
     * @param callback callback
     * @param param param
     * @return 响应
     */
    public String post(String url, Map<String, String> param, IAsyncCallback callback) {
        return (String) doPost(url, param, null, callback, null, false, null);
    }

    /**
     * @desc upload file
     * @author bosong
     * @date 2020/4/18 16:20
     * @param url url
     * @param param Traditional parameter method
     * @param files fileList
     * @param callback  Asynchronous callback method, pass null as synchronous
     * @return Object
     */
    public <T extends UploadFileBase> String post(String url, Map<String, String> param, List<T> files, IAsyncCallback callback) {
        okhttp3.MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
        if (param != null) {
            param.forEach(builder::addFormDataPart);
        }
        files.forEach((file) -> {
            if (file instanceof UploadFile) {
                UploadFile fileTmp = (UploadFile) file;
                builder.addFormDataPart(file.getParamName(), fileTmp.getFile().getName(), RequestBody.create(MediaType.parse(fileTmp.getMediaType()), fileTmp.getFile()));
            } else if (file instanceof UploadByteFile) {
                UploadByteFile fileTmp = (UploadByteFile) file;
                builder.addFormDataPart(file.getParamName(), fileTmp.getFileName(), RequestBody.create(MediaType.parse(fileTmp.getMediaType()), fileTmp.getFileBytes()));
            }
        });
        MultipartBody uploadBody = builder.build();
        Request request = new Request.Builder()
                .post(uploadBody)
                .url(url).
                        build();
        return (String) sendRequest(request, false, callback, null);
    }

    /**
     * @desc doPost
     * @author bosong
     * @date 2020/4/18 16:20
     * @param url url
     * @param param Traditional parameter method
     * @param jsonStr   json parameter method
     * @param callback  Asynchronous callback method, pass null as synchronous
     * @param isNeedResponse Do you need a Response object
     * @param callback4Response The callback passed into the Response object
     * @param headerExt  Parameters added to the request header
     * @return Object
     */
    private Object doPost(String url, Map<String, String> param, String jsonStr, IAsyncCallback callback, IAsyncCallback4Response callback4Response, boolean isNeedResponse, Map<String, String> headerExt) {
        if (StringUtils.isBlank(jsonStr)) {
            return doPost(url, param, jsonStr, "application/x-www-form-urlencoded", callback, callback4Response, isNeedResponse, headerExt);
        } else {
            return doPost(url, param, jsonStr, "application/json", callback, callback4Response, isNeedResponse, headerExt);
        }
    }

    /**
     * @desc doPost
     * @author bosong
     * @date 2020/4/18 16:20
     * @param url url
     * @param param Traditional parameter method
     * @param postStr   json parameter method
     * @param callback  Asynchronous callback method, pass null as synchronous
     * @param isNeedResponse Do you need a Response object
     * @param callback4Response The callback passed into the Response object
     * @param headerExt  Parameters added to the request header
     * @return Object
     */
    public Object doPost(String url, Map<String, String> param, String postStr, String dataMediaType, IAsyncCallback callback, IAsyncCallback4Response callback4Response, boolean isNeedResponse, Map<String, String> headerExt) {
        RequestBody body = okhttp3.internal.Util.EMPTY_REQUEST;
        if (StringUtils.isNotBlank(postStr)) {
            body = RequestBody.create(MediaType.parse(dataMediaType + "; charset=utf-8"), postStr);
        } else if (!CollectionUtils.isEmpty(param)) {
            Builder builder = new FormBody.Builder();
            param.forEach(builder::add);
            body = builder.build();
        }
        okhttp3.Request.Builder reqBuilder = new Request.Builder().post(body).url(url);
        if (headerExt != null && headerExt.size() > 0) {
            headerExt.forEach(reqBuilder::addHeader);
        }
        Request request = reqBuilder.build();
        return sendRequest(request, isNeedResponse, callback, callback4Response);
    }

    /**
     * @desc sendRequest
     * @author bosong
     * @date 2020/4/18 16:20
     * @param request Traditional parameter method
     * @param callback  Asynchronous callback method, pass null as synchronous
     * @param isNeedResponse Do you need a Response object
     * @param callback4Response The callback passed into the Response object
     * @return Object
     */
    private Object sendRequest(Request request, boolean isNeedResponse, IAsyncCallback callback, IAsyncCallback4Response callback4Response) {
        if (callback == null && callback4Response == null) {
            // 同步
            try {
                Response response = okHttpClient.newCall(request).execute();
                if (isNeedResponse) {
                    return response;
                } else {
                    assert response.body() != null;
                    return response.body().string();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // 异步
            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (isNeedResponse) {
                        callback4Response.doCallback(null);
                    } else {
                        assert callback != null;
                        callback.doCallback(null);
                    }
                }

                @Override
                public void onResponse(Call call, Response response) {
                    try {
                        if (isNeedResponse) {
                            callback4Response.doCallback(response);
                        } else {
                            assert callback != null;
                            assert response.body() != null;
                            callback.doCallback(response.body().string());
                        }
                    } catch (IOException e) {
                        callback.doCallback(null);
                    }
                }
            });
        }
        return null;
    }

    /**
     * @desc download
     * @author bosong
     * @date 2020/4/18 16:20
     * @param url url
     * @param callback  Asynchronous callback method, pass null as synchronous
     * @param isNeedResponse Do you need a Response object
     * @param callback4Response The callback passed into the Response object
     * @return Object
     */
    private Object download(String url, boolean isNeedResponse, Map<String, String> headerExt, IAsyncCallback4Download callback, IAsyncCallback4Response callback4Response) {
        okhttp3.Request.Builder reqBuilder = new Request.Builder().get().url(url);
        if (headerExt != null && headerExt.size() > 0) {
            headerExt.forEach(reqBuilder::addHeader);
        }
        Request request = reqBuilder.build();

        if (callback == null && callback4Response == null) {
            // 同步
            try {
                Response response = okHttpClient.newCall(request).execute();
                if (isNeedResponse) {
                    return response;
                } else {
                    assert response.body() != null;
                    return response.body().bytes();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // 异步
            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (isNeedResponse) {
                        callback4Response.doCallback(null);
                    } else {
                        assert callback != null;
                        callback.doCallback(null);
                    }
                }

                @Override
                public void onResponse(Call call, Response response) {
                    try {
                        if (isNeedResponse) {
                            callback4Response.doCallback(response);
                        } else {
                            assert response.body() != null;
                            assert callback != null;
                            callback.doCallback(response.body().bytes());
                        }
                    } catch (IOException e) {
                        callback.doCallback(null);
                    }
                }
            });
        }
        return null;
    }

    /**
     * @desc download
     * @author bosong
     * @date 2020/4/18 16:26
     * @param  headerExt Extended request header information
     * @param callback callback
     * @param url url
     * @return byte[]
     */
    public byte[] download(String url, Map<String, String> headerExt, IAsyncCallback4Download callback) {
        return (byte[]) download(url, false, headerExt, callback, null);
    }

    /**
     * @desc Download the file and return to okhttp3.Response
     * @author bosong
     * @date 2020/4/18 16:18
     * @param  headerExt Extended request header information
     * @param callback callback
     * @param url url
     * @return Response
     */
    public Response download(Map<String, String> headerExt, IAsyncCallback4Response callback, String url) {
        return (Response) download(url, true, headerExt, null, callback);
    }

    /**
     * @desc Download the file and return it
     * @author bosong
     * @date 2020/4/18 16:18
     * @param  url url
     * @param callback callback
     * @return byte[]
     */
    public byte[] download(String url, IAsyncCallback4Download callback) {
        return (byte[]) download(url, false, null, callback, null);
    }

    /**
     * @desc Download the file and return to okhttp 3
     * @author bosong
     * @date 2020/4/18 16:17
     * @param  callback Return okhttp3.Response callback
     * @param url url
     * @return Response
     */
    public Response download(IAsyncCallback4Response callback, String url) {
        return (Response) download(url, true, null, null, callback);
    }

}
