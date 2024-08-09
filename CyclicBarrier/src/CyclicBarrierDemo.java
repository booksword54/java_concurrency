
import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;

public class CyclicBarrierDemo implements Runnable {
    // 用 CyclicBarrierDemo 实现线程同步
    // 非常值得一提的是，CyclicBarrier 的计数器有自动重置的功能，当减到 0 的时候，会自动重置你设置的初始值。这个功能用起来实在是太方便了。
    /**
     * 创建4个屏障
     * 表示4个线程并发统计文件的字符数
     * this:表示4个屏障用完后执行当前线程
     */
    private CyclicBarrier cyclicBarrier = new CyclicBarrier(4, this);

    /**
     * 适用线程池执行线程
     */
    private Executor executor = Executors.newFixedThreadPool(4);

    /**
     * 保存每个线程执行的结果
     */
    private Map<String, Integer> result = new ConcurrentHashMap<>();

    /**
     * 随机数生成器
     */
    private Random random = new Random();

    /**
     * 统计方法
     */
    private void count() {
        for (int i = 0; i < 4; i++) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    // 计算当前文件的字符数
                    result.put(Thread.currentThread().getName(), random.nextInt(5));
                    System.out.println(Thread.currentThread().getName());
                    // 计算完成插入屏障
                    try {
                        cyclicBarrier.await();
                    } catch (BrokenBarrierException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    }

    @Override
    public void run() {
        int res = 0;
        // 汇总每个线程的执行结果
        for (Map.Entry<String, Integer> entry : result.entrySet()) {
            res += entry.getValue();
        }
        // 将结果保存到map中
        result.put("res", res);
        System.out.println("final result " + res);
    }

    public static void main(String[] args) {
        CyclicBarrierDemo cyclicBarrierDemo = new CyclicBarrierDemo();
        cyclicBarrierDemo.count();
    }
}
