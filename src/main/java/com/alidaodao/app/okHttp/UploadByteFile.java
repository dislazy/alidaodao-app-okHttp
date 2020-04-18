package com.alidaodao.app.okHttp;

/**
 *
 * @desc File upload bean based on byte [] bytes
 * @author bosong
 * @date 2020/4/18 16:08
 */
public class UploadByteFile extends UploadFileBase {

    /**
     * File binary data
     */
    private byte[] fileBytes;

    /**
     * file name
     */
    private String fileName;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public byte[] getFileBytes() {
        return fileBytes;
    }

    public void setFileBytes(byte[] fileBytes) {
        this.fileBytes = fileBytes;
    }
    
}
