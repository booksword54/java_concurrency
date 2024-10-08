import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

public class ThreadLocalRandomDemo {
    public static void main(String[] args) throws InterruptedException {
        // 随机数生成是一个非常常见的操作，而且 Java 也提供了 java.util.Random 类用于生成随机数，而且呢，这个类也是线程安全的，就是有一点不好，在多线程下，它的性能不佳。
        // 为什么多线程下，Random 的性能不佳？
        // 因为，它采用了多个线程共享一个 Random 实例。这样就会导致多个线程争用。
        // 为了解决这个问题，Java 7 引入了 java.util.concurrent.ThreadLocalRandom 类，用于在多线程环境中生成随机数。
        // 本文接下来的部分，就来看看如何 ThreadLocalRandom 如何执行以及如何在实际应用程序中使用它。

        // 一、ThreadLocalRandom Via Random
        // ThreadLocalRandom 是 ThreadLocal 类和 Random 类的组合，它与当前线程隔离，通过简单地避免对 Random 对象的任何并发访问，在多线程环境中实现了更好的性能。
        // 也就是说，相比于 java.util.Random 类全局的提供随机数生成， 使用 ThreadLocalRandom，一个线程获得的随机数不受另一个线程的影响。
        // 另一个与 Random 类不同的是，ThreadLocalRandom 不支持显式设置种子。因为它重写了从 Random 继承的 setSeed(long seed) 方法，会在调用时始终抛出 UnsupportedOperationException。
        // 接下来我们看看如何使用 ThreadLocalRandom 生成随机 int、long 和 double 值。


        // 二、使用 ThreadLocalRandom 生成随机数
        // 根据Oracle 文档，我们只需要调用 ThreadLocalRandom.current() 方法，就能返回当前线程的 ThreadLocalRandom 实例。然后，我们可以通过实例的相关方法来生成随机值。
        // 比如下面的代码，生成一个没有任何边界的随机 int 值
        int unboundedRandomValue = ThreadLocalRandom.current().nextInt();

        // 接下来，我们看看如何生成有边界的随机 int 值，这意味着我们需要传递边界下限和边界上限作为参数
        int boundedRandomValue = ThreadLocalRandom.current().nextInt(0, 100);
        // 请注意，这是一个左闭右开区间，也就是说，上面的实例生成的随机数在 [0,100) 之间，包含了 0 但不包含 100。
        // 同样的，我们可以通过调用 nextLong() 和 nextDouble() 方法生成 long 和 double 类型的随机值，调用方式与上面示例中 nextInt() 类似。
        // Java 8 还添加了 nextGaussian() 方法从生成器序列中生成下一个正态分布的值，其值范围在 0.0 和 1.0 之间。
        // 与Random 方法类似，ThreadLocalRandom 也提供了 doubles() 、ints() 和 longs() 方法生成一序列流式 ( stream ) 的随机值。


        // 三、使用 JMH 比较 ThreadLocalRandom 和 Random
        // 记下来，我们看看如何在多线程环境中分别使用这两个类生成随机值
        // 首先，我们创建一个示例，其中所有线程共享一个 Random 实例。
        ExecutorService executorService = Executors.newWorkStealingPool();
        List<Callable<Integer>> randomCallables = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 1000; i++) {
            randomCallables.add(() -> {
                return random.nextInt();
            });
        }
        executorService.invokeAll(randomCallables);
        // 上面的代码中，我们把使用 Random 实例生成随机值的任务提交给 ExecutorService 。

        // 接着，类似地，我们使用 ThreadLocalRandom 而不是 Random 实例
        List<Callable<Integer>> threadLocalRandomCallables = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            threadLocalRandomCallables.add(() -> {
                return ThreadLocalRandom.current().nextInt();
            });
        }
        executorService.invokeAll(threadLocalRandomCallables);
        // 上面的代码，为线程池中的每个线程单独使用了一个 ThreadLocalRandom 实例。
        // ThreadLocalRandom 在高度并发的环境中更有效。
    }
}
