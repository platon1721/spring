package ee.taltech.icd0011.helpers;

import java.util.concurrent.atomic.AtomicLong;

public class IdGenerator {

    private static final AtomicLong NEXT_ID = new AtomicLong(1);


    public static long nextId() {
        return NEXT_ID.getAndIncrement();
    }
}
