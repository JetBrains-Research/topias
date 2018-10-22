package state;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public final class Serializator {
    private final Kryo kryo;
    private final Output output;

    public Serializator() {
        kryo = new Kryo();
        output = new Output(1024);
        kryo.register(BranchInfo.class);
    }

    public void saveState(BranchInfo state) {
        kryo.writeObject(output, state);
    }

    public BranchInfo loadState() {
        return kryo.readObject(new Input(output.getBuffer(), 0, output.position()), BranchInfo.class);
    }
}
