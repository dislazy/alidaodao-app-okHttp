package com.alidaodao.app.okHttp;

import java.io.File;
/**
 *
 * @desc General OK Http package-File upload bean based on java.io.File
 * @author bosong
 * @date 2020/4/18 16:07
 */
public class UploadFile extends UploadFileBase {

    /**
     * file
     */
    private File file;

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
