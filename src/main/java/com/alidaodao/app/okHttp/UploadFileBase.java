package com.alidaodao.app.okHttp;

/**
 *
 * @desc General OK Http package-File upload bean base class
 * @author bosong
 * @date 2020/4/18 16:06
 */
abstract class UploadFileBase {

    /**
     * File parameter name
     */
    private String paramName;

    /**
     * File type
     */
    private String mediaType;

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

}
