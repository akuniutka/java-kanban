package util;

public class IdGenerator {
    private static int nextId = 1;

    public int nextId() {
        return nextId++;
    }
}
