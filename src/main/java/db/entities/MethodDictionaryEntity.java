package db.entities;

public class MethodDictionaryEntity {
    private String fullMethodSignature;
    private int id;
    private int startOffset;
    private String fileName;

    public MethodDictionaryEntity(String fullMethodSignature, int startOffset, String fileName) {
        this.fullMethodSignature = fullMethodSignature;
        this.id = -1;
        this.startOffset = startOffset;
        this.fileName = fileName;
    }

    public MethodDictionaryEntity(int id, String fullMethodSignature, int startOffset) {
        this.id = id;
        this.fullMethodSignature = fullMethodSignature;
        this.startOffset = startOffset;
    }

    public String getFullMethodSignature() {
        return fullMethodSignature;
    }

    public void setFullMethodSignature(String fullMethodSignature) {
        this.fullMethodSignature = fullMethodSignature;
    }

    public int getId() {
        return id;
    }

    public int getStartOffset() {
        return startOffset;
    }

    public void setStartOffset(int startOffset) {
        this.startOffset = startOffset;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        return "MethodDictionaryEntity{" +
                "fullMethodSignature='" + fullMethodSignature + '\'' +
                ", id=" + id +
                ", fileName='" + fileName + '\'' +
                '}';
    }
}
