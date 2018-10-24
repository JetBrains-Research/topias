package state;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.LinkedList;

public final class Serializator {

    public static void saveState(BranchInfo state) {
        final Kryo kryo = new Kryo();
        kryo.register(BranchInfo.class);
        kryo.register(LinkedList.class);
        final Output output = new Output(1024);
        kryo.writeObject(output, state);
        output.flush();
        output.close();
    }

    public static BranchInfo loadState() {
        final Kryo kryo = new Kryo();
        kryo.register(BranchInfo.class);
        kryo.register(LinkedList.class);
        final Output output = new Output(1024);
        final Input input = new Input(output.getBuffer(), 0, output.position());
        final BranchInfo info = kryo.readObject(input, BranchInfo.class);
        return info != null ? info : new BranchInfo();
    }
}
