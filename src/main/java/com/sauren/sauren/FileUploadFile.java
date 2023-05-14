package com.sauren.sauren;

import java.io.File;
import java.io.Serial;
import java.io.Serializable;

public class FileUploadFile implements Serializable //класс класса для передачи
{
    @Serial
    private static final long serialVersionUID = 1L;
    private File file;// файл
    private String file_md5;// имя файла
    private int starPos;// начальная позиция
    private byte[] bytes;// байтовый массив файла
    private int endPos;// Конечная позиция
    public int getStarPos() {
        return starPos;
    }
    public void setStarPos(int starPos) {
        this.starPos = starPos;
    }
    public int getEndPos() {
        return endPos;
    }
    public void setEndPos(int endPos) {
        this.endPos = endPos;
    }
    public byte[] getBytes() {
        return bytes;
    }
    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }
    public File getFile() {
        return file;
    }
    public void setFile(File file) {
        this.file = file;
    }
    public String getFile_md5() {
        return file_md5;
    }

    public void setFileName(String file_md5) {
        this.file_md5 = file_md5;
    }
}