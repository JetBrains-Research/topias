package jdbc.entities;

public class MethodDictionaryEntity {
    private String fullMethodSignature;
    private int id;
    private int startOffset;

    public MethodDictionaryEntity(String fullMethodSignature, int startOffset) {
        this.fullMethodSignature = fullMethodSignature;
        this.id = -1;
        this.startOffset = startOffset;
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
}
