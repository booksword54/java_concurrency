import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CountDownLatchDemo {
    public static void main(String[] args) throws InterruptedException {
        // 一、并发编程中使用 CountDownLatch
        // 简而言之，CountDownLatch 有一个计数器字段，我们可以根据需要减少它，因此，我们可以使用它来阻止调用线程，直到它被计数到零。
        // 如果我们正在进行一些并行处理，我们可以使用与计数器相同的值来实例化 CountDownLatch，因为我们想要处理多个线程。然后，我们可以在每个线程完成后调用 countdown()，保证调用 await() 的依赖线程将阻塞，直到工作线程完成。

        // 二、使用 CountDownLatch 等待线程池完成
        // 我们通过创建一个 Worker 来尝试这个模式，并使用 CountDownLatch 字段来指示它何时完成
        // 见 class Worker
        List<String> outputScraper = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch countDownLatch = new CountDownLatch(5);
        List<Thread> workers = Stream
                .generate(() -> new Thread(new Worker(outputScraper, countDownLatch)))
                .limit(5)
                .collect(Collectors.toList());
        workers.forEach(Thread::start);

        countDownLatch.await();
        outputScraper.add("Latch released");

        for (String s : outputScraper) {
            System.out.println(s);
        }
        // 上面这个示例中，"Latch release" 将始终是最后一个输出 – 因为它取决于 CountDownLatch 的释放。
        // 注意，如果我们没有调用 await() 方法，我们将无法保证线程执行的顺序，因此测试会随机失败。


        // 三、在等待开始的线程池中使用 CountDownLatch
        // 我们重用前面的示例，但是这次开启了了数千个线程而不是 5 个线程，很可能许多早期的线程在后面的线程上调用 start() 之前已经完成了处理。这可能会使尝试重现并发问题变得困难，因为我们无法让所有线程并行运行。
        // 为了解决这个问题，我们让 CountdownLatch 的工作方式与上一个示例有所不同。在某些子线程完成之前，我们可以阻止每个子线程直到所有其他子线程都启动，而不是阻塞父线程。
        // 我们把上一个示例的 run() 方法修改下，使其在处理之前阻塞
        // 见 class WaitingWorker
        List<String> waitingOutputScraper = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch readyThreadCounter = new CountDownLatch(5);
        CountDownLatch callingThreadBlocker = new CountDownLatch(1);
        CountDownLatch completedThreadCounter = new CountDownLatch(5);
        List<Thread> waitingWorkers = Stream
                .generate(() -> new Thread(new WaitingWorker(waitingOutputScraper, readyThreadCounter, callingThreadBlocker, completedThreadCounter)))
                .limit(5)
                .collect(Collectors.toList());

        waitingWorkers.forEach(Thread::start);

        readyThreadCounter.await();
        waitingOutputScraper.add("Workers Ready");
        callingThreadBlocker.countDown();
        completedThreadCounter.await();
        waitingOutputScraper.add("Workers Complete");

        for (String s : waitingOutputScraper) {
            System.out.println(s);
        }
        // 这种模式对于尝试重现并发错误非常有用，可以用来强制数千个线程尝试并行执行某些逻辑。

        // 四、让 CountdownLatch 尽早结束
        // 有时，我们可能会遇到一个情况，即在 CountdownLatch 倒计时之前，Workers 已经终止了错误。这可能导致它永远不会达到零并且 await() 永远不会终止。
        // 我们修改下之前的测试以使用 BrokenWorker，来演示 await() 将如何永久阻塞
        // 见 class BrokenWorker
        List<String> brokenOutputScraper = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch brokenCountDownLatch = new CountDownLatch(5);
        List<Thread> brokenWorkers = Stream
                .generate(() -> new Thread(new BrokenWorker(brokenOutputScraper, brokenCountDownLatch)))
                .limit(5)
                .collect(Collectors.toList());

        brokenWorkers.forEach(Thread::start);
        // brokenCountDownLatch.await();
        // 显然，这不是我们想要的行为 – 应用程序继续比无限阻塞要好得多。
        // 为了解决这个问题，我们在调用 await() 时添加一个超时参数。
        boolean completed = brokenCountDownLatch.await(3L, TimeUnit.SECONDS);
        System.out.println(completed);
        // 然后可以看到，测试最终会超时，await() 将返回 false
    }
}

class BrokenWorker implements Runnable {

    private List<String> outputScraper;

    private CountDownLatch countDownLatch;

    public BrokenWorker(List<String> outputScraper, CountDownLatch countDownLatch) {
        this.outputScraper = outputScraper;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void run() {
        // do work
        if (true) {
            throw new RuntimeException("Broken");
        }
        outputScraper.add("Count Down");
        countDownLatch.countDown();
    }
}


class WaitingWorker implements Runnable {

    private List<String> outputScraper;

    private CountDownLatch readyThreadCounter;

    private CountDownLatch callingThreadBlocker;

    private CountDownLatch completedThreadCounter;

    public WaitingWorker(List<String> outputScraper, CountDownLatch readyThreadCounter, CountDownLatch callingThreadBlocker, CountDownLatch completedThreadCounter) {
        this.outputScraper = outputScraper;
        this.readyThreadCounter = readyThreadCounter;
        this.callingThreadBlocker = callingThreadBlocker;
        this.completedThreadCounter = completedThreadCounter;
    }

    @Override
    public void run() {
        readyThreadCounter.countDown();
        try {
            callingThreadBlocker.await();
            // do work
            outputScraper.add("Count Down");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            completedThreadCounter.countDown();
        }
    }
}

class Worker implements Runnable {

    private List<String> outputScraper;

    private CountDownLatch countDownLatch;

    public Worker(List<String> outputScraper, CountDownLatch countDownLatch) {
        this.outputScraper = outputScraper;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void run() {
        // do work
        outputScraper.add("Count Down");
        countDownLatch.countDown();
    }
}

