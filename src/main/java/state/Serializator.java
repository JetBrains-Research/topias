package state;

import com.esotericsoftware.kryo.Kryo;
import git4idea.GitCommit;

public final class Serializator {
    public Serializator() {
        final Kryo kryo = new Kryo();
    }

    public void synchronizeHistory(GitCommit commit) {
        commit.getChanges().stream();
    }
}
