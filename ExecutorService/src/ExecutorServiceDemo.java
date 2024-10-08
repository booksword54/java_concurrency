import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ExecutorServiceDemo {
    public static void main(String[] args) throws ExecutionException, InterruptedException, TimeoutException {
        // 一、实例化 ExecutorService
        // 实例化ExecutorService 的方式有两种：一种是工厂方法，另一种是直接创建。

        // 1、Executors.newFixedThreadPool() 工厂方法创建 ExecutorService 实例
        // 创建ExecutorService 实例的最简单方法是使用 Executors 类的提供的工厂方法。比如
        ExecutorService fixedExecutorService = Executors.newFixedThreadPool(10);
        ExecutorService singleExecutorService = Executors.newSingleThreadExecutor();
        // 当然还有其它很多工厂方法，每种工厂方法都可以创建满足特定用例的预定义 ExecutorService 实例。
        // 你所需要做的就是找到自己想要的合适的方法。这些方法都在 Oracle 的 JDK 官方文档中有列出

        // 2、直接创建 ExecutorService 的实例
        // 因为ExecutorService 是只是一个接口，因此可以使用其任何实现类的实例。Java java.util.concurrent 包已经预定义了几种实现可供我们选择，或者你也可以创建自己的实现。
        // 例如，ThreadPoolExecutor 类实现了 ExecutorService 接口并提供了一些构造函数用于配置执行程序服务及其内部池。
        ExecutorService executorService = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        // 你可能会注意到，上面的代码与工厂方法 newSingleThreadExecutor() 的 源代码 非常相似。对于大多数情况，不需要详细的手动配置。


        // 二、将任务分配给 ExecutorService
        // ExecutorService 可以执行 Runnable 和 Callable 任务。为了使本文简单易懂。我们将使用两个两个原始任务，如下面的代码所示。
        Runnable runnable = () -> {
            try {
                TimeUnit.MILLISECONDS.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        Callable<String> callable = () -> {
            TimeUnit.MILLISECONDS.sleep(300);
            return "Task's execution";
        };
        List<Callable<String>> callableList = new ArrayList<>();
        callableList.add(callable);
        callableList.add(callable);
        callableList.add(callable);
        // 创建完了任务之后，就可以使用多种方法将任务分配给 ExecutorService ，
        // 比如 execute() 方法，还有 submit()、invokeAny() 和 invokeAll() 等方法。这些方法都继承自 Executor 接口。

        // 1、 首先来看看execute()方法；
        // 该方法返回值为空 ( void )。因此使用该方法没有任何可能获得任务执行结果或检查任务的状态，是正在运行 ( running ) 还是执行完毕 ( executed )。
        executorService.execute(runnable); // void

        // 2、 其次看看submit()方法；
        // submit() 方法会将一个 Callable 或 Runnable 任务提交给 ExecutorService 并返回 Future 类型的结果。
        Future<String> future = executorService.submit(callable); // Future

        // 3、 然后是invokeAny()方法；
        // invokeAny() 方法将一组任务分配给 ExecutorService，使每个任务执行，并返回任意一个成功执行的任务的结果 ( 如果成功执行 )
        String randomResult = executorService.invokeAny(callableList);

        // 4、 最后是invokeAll()方法；
        // invokeAll() 方法将一组任务分配给 ExecutorService ，使每个任务执行，
        // 并以 Future 类型的对象列表的形式返回所有任务执行的结果。
        List<Future<String>> wholeResultList = executorService.invokeAll(callableList);
        // 在继续深入理解 ExecutorService 之前，我们必须先讲解下另外两件事：关闭 ExecutorService 和处理 Future 返回类型。

        // 二、一、关闭 ExecutorService
        // 一般情况下，ExecutorService 并不会自动关闭，即使所有任务都执行完毕，或者没有要处理的任务，也不会自动销毁 ExecutorService 。
        // 它会一直处于等待状态，等待我们给它分配新的工作。

        // 这种机制，在某些情况下是非常有用的，比如，如果应用程序需要处理不定期出现的任务，或者在编译时不知道这些任务的数量。
        // 但另一方面，这也带来了副作用：即使应用程序可能已经到达它的终点，但并不会被停止，因为等待的 ExecutorService 将导致 JVM 继续运行。
        // 这样，我们就需要主动关闭 ExecutorService。

        // 要正确的关闭 ExecutorService，可以调用实例的 shutdown() 或 shutdownNow() 方法。
        // 1、 shutdown()方法：
        executorService.shutdown();
        // shutdown() 方法并不会立即销毁 ExecutorService 实例，
        // 而是首先让 ExecutorService 停止接受新任务，并在所有正在运行的线程完成当前工作后关闭。

        // 2、 shutdownNow()方法：
        List<Runnable> notExecutedTaskList = executorService.shutdownNow();
        // shutdownNow() 方法会尝试立即销毁 ExecutorService 实例，所以并不能保证所有正在运行的线程将同时停止。
        // 该方法会返回等待处理的任务列表，由开发人员自行决定如何处理这些任务。

        // 3、shutdown() + shutdownNow() + awaitTermination()方法：
        // 因为提供了两个方法，因此关闭 ExecutorService 实例的最佳实战 （ 也是 Oracle 所推荐的 ）就是同时使用这两种方法并结合 awaitTermination() 方法。
        // 使用这种方式，ExecutorService 首先停止执行新任务，等待指定的时间段完成所有任务。如果该时间到期，则立即停止执行。
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }

        // 二、二、Future 接口
        // submit() 方法和 invokeAll() 方法返回一个 Future 接口的对象或 Future 类型的对象集合。
        // 这些 Future 接口的对象允许我们获取任务执行的结果或检查任务的状态 ( 是正在运行还是执行完毕 ）。

        // 1、Future 接口 get() 方法
        // Future 接口提供了一个特殊的阻塞方法 get()，它返回 Callable 任务执行的实际结果，但如果是 Runnable 任务，则只会返回 null。
        // 因为get() 方法是阻塞的。如果调用 get() 方法时任务仍在运行，那么调用将会一直被执阻塞，直到任务正确执行完毕并且结果可用时才返回。
        // 而且更重要的是，正在被执行的任务随时都可能抛出异常或中断执行。因此我们要将 get() 调用放在 try catch 语句块中，并捕捉 InterruptedException 或 ExecutionException 异常。
        String result = "";
        try {
            result = future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        // 因为get() 方法是阻塞的，而且并不知道要阻塞多长时间。因此可能导致应用程序的性能降低。如果结果数据并不重要，那么我们可以使用超时机制来避免长时间阻塞。
        result = future.get(200, TimeUnit.MILLISECONDS);
        // 这个get() 的重载，第一个参数为超时的时间，第二个参数为时间的单位。上面的实例所表示就的就是等待 200 毫秒。
        // 注意，这个 get() 重载方法，如果在超时时间内正常结束，那么返回的是 Future 类型的结果，如果超时了还没结束，那么将抛出 TimeoutException 异常。
        // 除了get() 方法之外，Future 还提供了其它很多方法，我们将几个重要的方法罗列在此
        //isDone()	检查已分配的任务是否已处理
        //cancel()	取消任务执行
        //isCancelled()	检查任务是否已取消
        boolean isDone = future.isDone();
        boolean cancel = future.cancel(true);
        boolean isCancelled = future.isCancelled();

        // 三、ScheduledExecutorService 接口
        // ScheduledExecutorService 接口用于在一些预定义的延迟之后运行任务和（ 或 ）定期运行任务。
        // 同样的，实例化 ScheduledExecutorService 的最佳方式是使用 Executors 类的工厂方法。
        ScheduledExecutorService singleThreadScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        // 有了实例之后，事情就好办了，比如，要在固定延迟后安排单个任务的执行，可以使用 ScheduledExecutorService 实例的 scheduled() 方法
        // scheduled() 方法有两个重载，分别用于执行 Runnable 任务或 Callable 任务。
        ScheduledFuture<String> callableScheduledFuture = singleThreadScheduledExecutorService.schedule(callable, 1, TimeUnit.SECONDS);
        ScheduledFuture<?> runnableScheduledFuture = singleThreadScheduledExecutorService.schedule(runnable, 1, TimeUnit.SECONDS);
        // 上面这个实例中的代码在执行 callable/runnable 之前延迟了一秒钟。

        // 另外，ScheduledExecutorService 实例还提供了另一个重要方法 scheduleAtFixedRate() ，它允许在固定延迟后定期执行任务。
        // 如果处理器需要更多时间来执行分配的任务，那么可以使用 scheduleAtFixedRate() 方法的 period 参数，ScheduledExecutorService 将等到当前任务完成后再开始下一个任务。
        ScheduledFuture<?> scheduledAtFixedRateScheduledFuture = singleThreadScheduledExecutorService.scheduleAtFixedRate(runnable, 100, 450, TimeUnit.MILLISECONDS);
        // 上面的代码块将在 100 毫秒的初始延迟后执行任务，之后，它将每 450 毫秒执行相同的任务。

        // 如果任务迭代之间必须具有固定长度的延迟，那么可以使用 scheduleWithFixedDelay() 方法 。例如，以下代码将保证当前执行结束与另一个执行开始之间的暂停时间为 150 毫秒。
        ScheduledFuture<?> scheduledWithFixedDelayScheduledFuture = singleThreadScheduledExecutorService.scheduleWithFixedDelay(runnable, 100, 150, TimeUnit.MILLISECONDS);
        // 根据scheduleAtFixedRate() 和 scheduleWithFixedDelay() 方法契约，在任务执行期间，如果 ExecutorService 终止了或任务抛出了异常，那么任务将自动结束。

        // 四、ExecutorService 或 Fork/Join
        // Fork/Join 是 Java 7 提供的新框架，在 Java 7 发布之后，许多开发人员都作出了将 ExecutorService 框架替换为 fork/join 框架的决定。
        // 但，这并不总是正确的决定。尽管 fork/join 使用起来更加简单且频繁使用时更带来更快的性能，但开发人员对并发执行的控制量也有所减少。

        // 使用ExecutorService ，开发人员能够控制生成的线程数以及应由不同线程执行的任务粒度。ExecutorService 的最佳用例是处理独立任务，例如根据 「 一个任务的一个线程 」 方案的事务或请求。
        // 而相比之下，根据 Oracle 文档，fork/join 旨在简化和加速工作，可以将任务递归地分成更小的部分。

        // 六、后记
        // 尽管ExecutorService 相对简单，但仍有一些常见的陷阱。我们罗列于此
        // 1、 保持未使用的ExecutorService存活；
        //   本文中对如何关闭 ExecutorService 已经做出了详细解释。

        // 2、 使用固定长度的线程池时设置了错误的线程池容量；
        //   使用ExecutorService 最重要的一件事，就是确定应用程序有效执行任务所需的线程数
        //   太大的线程池只会产生不必要的开销，只会创建大多数处于等待模式的线程。
        //   太少的线程池会让应用程序看起来没有响应，因为队列中的任务等待时间很长。

        // 3、 在取消任务后调用Future的get()方法；
        //   尝试获取已取消任务的结果将触发 CancellationException 异常。

        // 4、 使用Future的get()方法意外地阻塞了很长时间；
        //   应该使用超时来避免意外的等待。

    }
}
