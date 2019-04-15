package jdbc.entities;

import java.sql.Date;

public class MethodChangeLogEntity {
    private final long dateChanged;
    private final String authorName;
    private final String branchName;
    private final int signatureId;

    public MethodChangeLogEntity(long dateChanged, String authorName, String branchName, int signatureId) {
        this.dateChanged = dateChanged;
        this.authorName = authorName;
        this.branchName = branchName;
        this.signatureId = signatureId;
    }

    public long getDateChanged() {
        return dateChanged;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getBranchName() {
        return branchName;
    }

    public int getSignatureId() {
        return signatureId;
    }
}
