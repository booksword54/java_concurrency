import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ThreadPoolDemo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 本文我们将讲解 Java 中的线程池 ( Thread Pool )，从 Java 标准库中的线程池的不同实现开始，到 Google 开发的 Guava 库的前世今生。
        // Java 语言的实现中，把 Java 线程一一映射到操作系统级的线程，而后者是操作系统的资源，这意味着，如果开发者毫无节制地创建线程，那么线程资源就会被快速的耗尽。
        // 出于模拟并行性的目的，Java 线程之间的上下文切换也由操作系统完成。因为线程上下文切换需要消耗时间，所以，一个简单的观点是：产生的线程越多，每个线程花在实际工作上的时间就越少。

        // 为了节制创建线程的数量，也为了节省创建线程的开销，因此提出了线程池的概念。线程池模式有助于节省多线程应用程序中的资源，还可以在某些预定义的限制内包含并行性。
        // 当我们使用线程池时，我们可以以并行任务的形式编写并发代码并将其提交到线程池的实例中执行。
        // 这个线程池实例控制了多个重用线程以执行这些任务。

        // 这种线程池模式，允许我们控制应用程序创建的线程数，生命周期，以及计划任务的执行并将传入的任务保留在队列中。

        // 一、Java 中的线程池
        // 1、Executors、Executor 和 ExecutorService
        // Executors 是一个帮助类，提供了创建几种预配置线程池实例的方法。如果你不需要应用任何自定义的微调，可以调用这些方法创建默认配置的线程池，因为它能节省很多时间和代码。
        // Executor 和 ExecutorService 接口则用于与 Java 中不同线程池的实现协同工作。通常，你应该将代码与线程池的实际实现分离，并在整个应用程序中使用这些接口。
        // Executor 接口提供了一个 execute() 方法将 Runnable 实例提交到线程池中执行。
        // 下面的代码是一个快速示例，演示了如何使用 Executors API 获取包含了单个线程池和无限队列支持的 Executor 实例，以便按顺序执行任务。
        Executor singleThreadExecutor = Executors.newSingleThreadExecutor();
        // 获取了Executor 示例后，我们就可以使用 execute() 方法将一个只在屏幕上打印 Hello World 的任务提交到队列中执行。
        singleThreadExecutor.execute(() -> System.out.println("Hello World"));
        // 上面这个示例使用了 lambda （ Java 8特性 ）提交任务，JVM 会自动推断该任务为 Runnable

        // ExecutorService 接口则包含大量用于控制任务进度和管理服务终止的方法。我们可以使用此接口来提交要执行的任务，还可以使用此接口返回的 Future 实例控制任务的执行。
        // 下面的示例中，我们创建了一个 ExecutorService 的实例，提交了一个任务，然后使用返回的 Future 的 get() 方法等待提交的任务完成并返回值。
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        Future<String> future = executorService.submit(() -> "Hello World");
        String result = future.get();

        // 在实际使用时，我们并不会立即调用 future.get() 方法，可能会等待一些时间，推迟调用它直到我们需要它的值用于计算等目的。
        // ExecutorService 中的 submit() 方法被重载为支持 Runnable 或 Callable ，它们都是功能接口，可以接收一个 lambdas 作为参数（ 从 Java 8 开始 ）：
        // 使用 Runnable 作为参数的方法不会抛出异常也不会返回任何值 ( 返回 void )
        // 使用 Callable 作为参数的方法则可以抛出异常也可以返回值。

        // 如果想让编译器将参数推断为 Callable 类型，只需要 lambda 返回一个值即可。
        // ExecutorService 接口的更多使用范例和特性，你可以访问前面的章节 一文秒懂 Java ExecutorService。

        // 二、ThreadPoolExecutor
        // ThreadPoolExecutor 是一个可被继承 ( extends ) 的线程池实现，包含了用于微调的许多参数和钩子。
        // 我们并不会讨论 ThreadPoolExecutor 类中的所有的参数和钩子，只会讨论几个主要的配置参数：
        // 1、 corePoolSize；
        // 2、 maximumPoolSize；
        // 3、 keepAliveTime；

        // ThreadPoolExecutor 创建的线程池由固定数量的核心线程组成，
        // 这些线程在 ThreadPoolExecutor 生命周期内始终存在，
        // 除此之外还有一些额外的线程可能会被创建，并会在不需要时主动销毁。
        // corePoolSize 参数用于指定在线程池中实例化并保留的核心线程数。
        // 如果所有核心线程都忙，并且提交了更多任务，则允许线程池增长到 maximumPoolSize 。
        // keepAliveTime 参数是额外的线程（ 即，实例化超过 corePoolSize 的线程 ）在空闲状态下的存活时间。
        // 这三个参数涵盖了广泛的使用场景，但最典型的配置是在 Executors 静态方法中预定义的。

        // 1、Executors.newFixedThreadPool()
        // 例如，Executors.newFixedThreadPool() 静态方法创建了一个 ThreadPoolExecutor ，
        // 它的参数 corePoolSize 和 maximumPoolSize 都是相等的，且参数 keepAliveTime 始终为 0 ，
        // 也就意味着此线程池中的线程数始终相同。
        ThreadPoolExecutor fixedExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);
        fixedExecutor.submit(() -> {
            Thread.sleep(1000);
            return null;
        });
        fixedExecutor.submit(() -> {
            Thread.sleep(1000);
            return null;
        });
        fixedExecutor.submit(() -> {
            Thread.sleep(1000);
            return null;
        });
        System.out.println(fixedExecutor.getPoolSize() == 2); // true
        System.out.println(fixedExecutor.getQueue()
                                   .size() == 1); // true
        // 上面这个示例中，我们实例化了一个固定线程数为 2 的 ThreadPoolExecutor。
        // 这意味着如果同时运行的任务的数量始终小于或等于 2 ，那么这些任务会立即执行。否则，其中一些任务可能会被放入队列中等待轮到它们。
        // 上面这个示例中，我们创建了三个 Callable 任务，通过睡眠模拟 1000 毫秒的繁重工作。
        // 前两个任务将立即执行，第三个任务必须在队列中等待。
        // 我们可以通过在提交任务后立即调用 getPoolSize() 和 getQueue().size() 来方法来验证。

        // 2、Executors.newCachedThreadPool()
        // Executors 还提供了 Executors.newCachedThreadPool() 静态方法创建另一个预配置的 ThreadPoolExecutor。
        // 该方法创建的线程池没有任何核心线程，因为它将 corePoolSize 属性设置为 0，
        // 但同时有可以创建最大数量的额外线程，因为它将 maximumPoolSize 设置为 Integer.MAX_VALUE ，
        // 且将 keepAliveTime 的值设置为 60 秒。

        // 这些参数值意味着缓存的线程池可以无限制地增长以容纳任何数量的已提交任务。
        // 但是，当不再需要线程时，它们将在 60秒不活动后被销毁。
        // 这种线程池的使用场景一般是你的应用程序中有很多短期任务。
        ThreadPoolExecutor cachedExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        cachedExecutor.submit(() -> {
            Thread.sleep(1000);
            return null;
        });
        cachedExecutor.submit(() -> {
            Thread.sleep(1000);
            return null;
        });
        cachedExecutor.submit(() -> {
            Thread.sleep(1000);
            return null;
        });
        System.out.println(cachedExecutor.getPoolSize() == 3); // true
        System.out.println(cachedExecutor.getQueue()
                                   .size() == 0); // true
        // 上面这个示例中的队列大小始终为 0 ，因为在内部使用了 SynchronousQueue 的实例。
        // 在 SynchronousQueue 中，插入和删除操作总是成对出现且同时发生。因此队列实际上从不包含任何内容。

        // 3、Executors.newSingleThreadExecutor()
        // Executors.newSingleThreadExecutor() 静态方法则创建另一种典型的只包含单个线程的 ThreadPoolExecutor 实例。
        // 这种单线程执行程序是创建事件循环的理想选择。
        // 在这个单线程 ThreadPoolExecutor 实例中，属性 corePoolSize 和属性 maximumPoolSize 的值都为 1，而属性 keepAliveTime 的值为 0 。
        // 在单线程 ThreadPoolExecutor 实例中，所有的任务都按顺序执行。因此，下面的示例中，任务完成后标志的值是 2。
        AtomicInteger counter = new AtomicInteger();
        ExecutorService singleExecutor = Executors.newSingleThreadExecutor(); // 无法强制转换
        singleExecutor.submit(() -> {
            counter.set(1);
        });
        singleExecutor.submit(() -> {
            counter.compareAndSet(1, 2);
        });
        // 此外，单线程 ThreadPoolExecutor 实例使用了不可变包装器进行修饰，因此在创建后无法重新配置。当然了，这也是我们无法将该示例强制转换为 ThreadPoolExecutor 的原因。

        // 三、ScheduledThreadPoolExecutor
        // ScheduledThreadPoolExecutor 扩展自 ThreadPoolExecutor 类，并且添加了其它方法实现了 ScheduledExecutorService 接口。
        // - schedule() 方法允许在指定的延迟后执行一次任务
        // - scheduleAtFixedRate() 方法允许在指定的初始延迟后执行任务，然后以一定的周期重复执行，其中 period 参数用于指定两个任务的开始时间之间的间隔时间，因此任务执行的频率是固定的。
        // - scheduleWithFixedDelay() 方法类似于 scheduleAtFixedRate() ，它也重复执行给定的任务，但period 参数用于指定前一个任务的结束和下一个任务的开始之间的间隔时间。也就是指定下一个任务延时多久后才执行。执行频率可能会有所不同，具体取决于执行任何给定任务所需的时间。

        // 静态方法 Executors.newScheduledThreadPool() 方法用于创建包含了
        // 指定 corePoolSize，无上限 maximumPoolSize 和 0 存活时间 keepAliveTime 的 ScheduledThreadPoolExecutor 实例。
        // 例如下面的示例创建了一个包含了 5 个核心线程的 ScheduledThreadPoolExecutor 实例，且每隔 500 毫秒运行一个输出 Hello World 的任务
        ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(5);
        scheduledExecutor.schedule(() -> {
            System.out.println("Hello World");
        }, 500, TimeUnit.MILLISECONDS);
        // 下面的代码则演示了如何在 500 毫秒延迟后执行任务，然后每 100 毫秒重复执行一次。
        CountDownLatch lock = new CountDownLatch(3);
        ScheduledFuture<?> scheduledFuture = scheduledExecutor.scheduleAtFixedRate(() -> {
            System.out.println("Hello World");
            lock.countDown();
        }, 500, 100, TimeUnit.MILLISECONDS);
        lock.await(1000, TimeUnit.MILLISECONDS);
        scheduledFuture.cancel(true);

        // 四、ForkJoinPool
        // ForkJoinPool 是Java 7 中引入的 fork/join 框架的核心之一。
        // 解决了一个常见的问题： 如何在递归中生成多个任务。
        // 因为，即使是使用一个简单的 ThreadPoolExecutor ，也会在不断的递归中快速耗尽线程。因为每个任务或子任务都需要自己的线程来运行。

        // 在fork/join 框架中，任何任务都可以生成 ( fork ) 多个子任务并使用 join() 方法等待它们的完成。
        // fork/join 框架的好处是它不会为每个任务或子任务创建新线程，而是实现了 工作窃取 ( Work Stealing ) 算法。

        // 接下来，我们看一个使用 ForkJoinPool 。遍历节点树并计算所有叶值之和的简单示例，在这个示例中，树是一个由节点，int 值和一组子节点组成。
        // 见 class CountingTask
        // 在树上运行计算的代码非常简单：
        TreeNode tree = new TreeNode(5,
                                     new TreeNode(3), new TreeNode(2,
                                                                   new TreeNode(2), new TreeNode(8)));
        ForkJoinPool commonForkJoinPool = ForkJoinPool.commonPool();
        int sum = commonForkJoinPool.invoke(new CountingTask(tree));

    }

    public static class CountingTask extends RecursiveTask<Integer> {

        private final TreeNode node;

        public CountingTask(TreeNode node) {
            this.node = node;
        }

        @Override
        protected Integer compute() {
            return node.value + node.children.stream()
                    .map(childNode -> new CountingTask(childNode).fork())
                    .mapToInt(ForkJoinTask::join)
                    .sum();
        }
    }

    static class TreeNode {
        int value;
        Set<TreeNode> children;

        TreeNode(int value, TreeNode... children) {
            this.value = value;
            this.children = new HashSet<>(Arrays.asList(children));
        }
    }

}


