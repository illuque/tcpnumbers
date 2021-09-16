import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

// TODO:I properly test
public class NumbersApprover {

    private final AtomicInteger totalUniqueNumbers;
    private final AtomicInteger uniqueNumbersInRound;
    private final AtomicInteger duplicateNumbersInRound;

    private final Set<Integer> uniqueNumbersSet;
    private final BlockingQueue<Integer> numbersQueue;

    private static NumbersApprover instance;

    public static NumbersApprover getInstance() {
        if (instance == null) {
            instance = new NumbersApprover();
        }
        return instance;
    }

    private NumbersApprover() {
        this.uniqueNumbersSet = Collections.newSetFromMap(new ConcurrentHashMap<>());
        this.totalUniqueNumbers = new AtomicInteger(0);
        this.uniqueNumbersInRound = new AtomicInteger(0);
        this.duplicateNumbersInRound = new AtomicInteger(0);
        this.numbersQueue = new LinkedBlockingDeque<>();
    }

    public void add(int number) {
        boolean isNew = uniqueNumbersSet.add(number);
        if (isNew) {
            boolean queued;
            do {
                queued = numbersQueue.add(number);
            } while (!queued);

            uniqueNumbersInRound.incrementAndGet();
            totalUniqueNumbers.incrementAndGet();
        } else {
            duplicateNumbersInRound.incrementAndGet();
        }
    }

    public Integer pollNumber() {
        return numbersQueue.poll();
    }

    public Report newReportRound() {
        int uniquesInRound = uniqueNumbersInRound.getAndSet(0);
        int duplicatedInRound = duplicateNumbersInRound.getAndSet(0);
        return new Report(uniquesInRound, duplicatedInRound, totalUniqueNumbers.get());
    }

    public static class Report {
        private final int uniquesInRound;
        private final int duplicatedInRound;
        private final int totalUniqueNumbers;

        private Report(int uniquesInRound, int duplicatedInRound, int totalUniqueNumbers) {
            this.uniquesInRound = uniquesInRound;
            this.duplicatedInRound = duplicatedInRound;
            this.totalUniqueNumbers = totalUniqueNumbers;
        }

        public int getUniquesInRound() {
            return uniquesInRound;
        }

        public int getDuplicatedInRound() {
            return duplicatedInRound;
        }

        public int getTotalUniqueNumbers() {
            return totalUniqueNumbers;
        }
    }

}
