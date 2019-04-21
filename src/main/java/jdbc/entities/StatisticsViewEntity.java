package jdbc.entities;

import java.sql.Date;

public class StatisticsViewEntity {
    private final String fullSignature;
    private final int changesCount;
    private final String fileName;
    private final int startOffset;

    public StatisticsViewEntity(String fullSignature, int changesCount, String fileName, int startOffset) {
        this.fullSignature = fullSignature;
        this.changesCount = changesCount;
        this.fileName = fileName;
        this.startOffset = startOffset;
    }

    public String getFullSignature() {
        return fullSignature;
    }

    public int getChangesCount() {
        return changesCount;
    }

    public String getFileName() {
        return fileName;
    }

    public int getStartOffset() {
        return startOffset;
    }
}
