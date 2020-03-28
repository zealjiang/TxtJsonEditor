package com.example.zealjiang.bean;

/**
 * Created by lizhijiang on 2018/1/22.
 */

public class FileBean {

    public enum FileType{
        DIRECTORY,FILE
    }

    private FileType fileType;
    private String lastModifyTime;
    private String fileName;
    private String bakName;
    private String fileSize;

    public FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }

    public String getLastModifyTime() {
        return lastModifyTime;
    }

    public void setLastModifyTime(String lastModifyTime) {
        this.lastModifyTime = lastModifyTime;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getBakName() {
        return bakName;
    }

    public void setBakName(String bakName) {
        this.bakName = bakName;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }
}
