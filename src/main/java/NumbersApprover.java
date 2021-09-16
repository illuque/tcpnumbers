import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

// TODO:I properly test
public class NumbersApprover {

    private static final int REPORT_FREQUENCY_IN_SECONDS = 10;

    private final AtomicInteger totalUniqueNumbers;
    private final AtomicInteger uniqueNumbersInRound;
    private final AtomicInteger duplicateNumbersInRound;

    private final Set<Integer> uniqueNumbersSet;

    private boolean reporterEnabled;

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
        this.reporterEnabled = false;
    }

    public boolean add(int number) {
        boolean isNew = uniqueNumbersSet.add(number);
        if (isNew) {
            uniqueNumbersInRound.incrementAndGet();
            totalUniqueNumbers.incrementAndGet();
        } else {
            duplicateNumbersInRound.incrementAndGet();
        }

        return isNew;
    }

    public void initReporter() {
        reporterEnabled = true;
        this.initReporterThread();
    }

    public void shutDownReporter() {
        reporterEnabled = false;
    }

    private void initReporterThread() {
        Runnable runnable = () -> {
            try {
                while (reporterEnabled) {
                    TimeUnit.SECONDS.sleep(REPORT_FREQUENCY_IN_SECONDS);
                    newRound();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(-1);
            }
        };

        Thread reporterThread = new Thread(runnable);
        reporterThread.start();
    }

    private void newRound() {
        int uniquesInRound = uniqueNumbersInRound.getAndSet(0);
        int duplicatedInRound = duplicateNumbersInRound.getAndSet(0);
        System.out.printf("Received %d unique numbers, %d duplicates. Unique total: %d%s", uniquesInRound, duplicatedInRound, totalUniqueNumbers.get(), System.lineSeparator());
    }

}
