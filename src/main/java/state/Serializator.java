package state;

import com.esotericsoftware.kryo.Kryo;
import git4idea.GitCommit;

public final class Serializator {
    private final Kryo kryo;

    public Serializator() {
        kryo = new Kryo();
    }

    public void synchronizeHistory(GitCommit commit) {
        commit.getChanges().stream();
    }
}
