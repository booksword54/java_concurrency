import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.logging.Logger;

public class ForkJoinDemo {
    public static void main(String[] args) {
        // fork/join 框架是 Java 7 中引入的 ，它是一个工具，通过 「 分而治之 」 的方法尝试将所有可用的处理器内核使用起来帮助加速并行处理。

        // 在实际使用过程中，这种 「 分而治之 」的方法意味着框架首先要 fork ，递归地将任务分解为较小的独立子任务，直到它们足够简单以便异步执行。
        // 然后，join 部分开始工作，将所有子任务的结果递归地连接成单个结果，或者在返回 void 的任务的情况下，程序只是等待每个子任务执行完毕。

        // 为了提供有效的并行执行，fork/join 框架使用了一个名为 ForkJoinPool 的线程池，用于管理 ForkJoinWorkerThread 类型的工作线程。

        // 一、ForkJoinPool 线程池
        // ForkJoinPool 是 fork/join 框架的核心，是 ExecutorService 的一个实现，用于管理工作线程，并提供了一些工具来帮助获取有关线程池状态和性能的信息。
        // 工作线程一次只能执行一个任务。
        // ForkJoinPool 线程池并不会为每个子任务创建一个单独的线程，
        // 相反，池中的每个线程都有自己的双端队列用于存储任务 （ double-ended queue ）( 或 deque，发音 deck ）。
        // 这种架构使用了一种名为工作窃取（ work-stealing ）算法来平衡线程的工作负载。

        // 1、工作窃取（ work-stealing ）算法
        // 简单来说，就是 空闲的线程试图从繁忙线程的 deques 中 窃取 工作。
        // 默认情况下，每个工作线程从其自己的双端队列中获取任务。但如果自己的双端队列中的任务已经执行完毕，双端队列为空时，
        // 工作线程就会从另一个忙线程的双端队列尾部或全局入口队列中获取任务，因为这是最大概率可能找到工作的地方。
        // 这种方法最大限度地减少了线程竞争任务的可能性。它还减少了工作线程寻找任务的次数，因为它首先在最大可用的工作块上工作。

        // 2、ForkJoinPool 线程池的实例化
        // 在Java 8 中，创建 ForkJoinPool 实例的最简单的方式就是使用其静态方法 commonPool()。
        // commonPool() 静态方法，见名思义，就是提供了对公共池的引用，公共池是每个 ForkJoinTask 的默认线程池。
        // 根据Oracle 的官方文档，使用预定义的公共池可以减少资源消耗，因为它会阻止每个任务创建一个单独的线程池。
        ForkJoinPool commonForkJoinPool = ForkJoinPool.commonPool();
        // 如果要在 Java 7 中实现相同的行为，则需要通过创建 ForkJoinPool 的实例并将其赋值给实用程序类的公共静态字段。
        // public static ForkJoinPool forkJoinPool = new ForkJoinPool(2);
        // 使用构造函数实例化 ForkJoinPool 时，可以创建具有指定级别的并行性，线程工厂和异常处理程序的自定义线程池。
        // 在上面的示例中，线程池的并行度级别为 2 ，意味着线程池将使用 2 个处理器核心。
        // 然后就可以通过这个公共静态字段轻松的访问 ForkJoinPool 的实例
        ForkJoinPool forkJoinPool = ForkJoinDemo.forkJoinPool;

        // 二、ForkJoinTask
        // ForkJoinTask 是 ForkJoinPool 线程之中执行的任务的基本类型。我们日常使用时，一般不直接使用 ForkJoinTask ，而是扩展它的两个子类中的任意一个
        // 1、 任务不返回结果(返回void）的RecursiveAction；
        // 2、 返回值的任务的RecursiveTask<V>；
        // 这两个类都有一个抽象方法 compute() ，用于定义任务的逻辑。
        // 我们所要做的，就是继承任意一个类，然后实现 compute() 方法。

        // 二、一、RecursiveAction 使用示例
        // 见 class CustomRecursiveAction
        // 1、 创建一个表示工作总量的对象；
        // 在这个示例中，我们使用了一个字符串类型 ( String ) 的名为 workload 属性来表示要处理的工作单元。
        // 2、 选择合适的阈值；
        // 同时，为了演示 fork/join 框架的 fork 行为，在该示例中，如果 workload.length() 大于指定的阈值，那么就使用 createSubtask() 方法拆分任务。
        // 3、 定义分割工作的方法；
        // 在createSubtasks() 方法中，输入的字符串被递归地划分为子串，然后创建基于这些子串的 CustomRecursiveTask 实例。
        // 4、 定义执行工作的方法；
        // 然后在compute() 方法中使用 invokeAll() 方法将任务列表提交给 ForkJoinPool 线程池。

        // 二、二、RecursiveTask 使用示例
        // 对于有返回值的任务，除了将每个子任务的结果在一个结果中合并，其它逻辑和 RecursiveAction 都差不多。
        // 见 class CustomRecursiveTask
        // 在上面这个示例中，任务由存储在 CustomRecursiveTask 类的 arr 字段中的数组表示。
        // createSubtask() 方法递归地将任务划分为较小的工作，直到每个部分小于阈值。
        // 然后，invokeAll()方法将子任务提交给公共拉取并返回 Future 列表。
        // 要触发执行，需要为每个子任务调用 join() 方法。
        // 使用了 Java 8 的流 ( Stream ) API ， sum() 方法用于将子结果组合到最终结果中。


        // 三、将任务提交到 ForkJoinPool 线程池中
        // 只要使用很少的方法，就可以把任务提交到 ForkJoinPool 线程池中。
        // 1、 submit()或execute()方法；
        CustomRecursiveAction customRecursiveAction = new CustomRecursiveAction("workload");
        CustomRecursiveTask customRecursiveTask = new CustomRecursiveTask(new int[]{1, 2, 3});
        forkJoinPool.submit(customRecursiveAction);
        forkJoinPool.execute(customRecursiveAction);
        forkJoinPool.submit(customRecursiveTask);
        forkJoinPool.execute(customRecursiveTask);

        // 2、 使用invoke()方法fork任务并等待结果，不需要任何手动连接(join)；
        Integer invokeForkJoinPoolResult = forkJoinPool.invoke(customRecursiveTask);

        // 3、 invokeAll()方法是将ForkJoinTasks序列提交给ForkJoinPool的最方便的方法它将任务作为参数(两个任务，varargs或集合），
        // fork它们，并按照生成它们的顺序返回Future对象的集合；

        // 4、 或者，我们还可以使用单独的fork()和join()方法；
        // fork() 方法将任务提交给线程池，但不会触发任务的执行。
        customRecursiveAction.fork();
        customRecursiveTask.fork();
        // join() 方法则用于触发任务的执行。在 RecursiveAction 的情况下，join() 返回 null，但对于 RecursiveTask<V> ，它返回任务执行的结果。
        customRecursiveAction.join();
        customRecursiveTask.join();

        // 上面的RecursiveTask<V> 示例中，我们使用 invokeAll() 方法向线程池提交一系列子任务。
        // 同样的工作，也可以使用 fork() 和 join() 来完成，但这可能会对结果的排序产生影响。

        // 为了避免混淆，当涉及到多个任务且要保证任务的顺序时，通常都是使用 ForkJoinPool.invokeAll() 。

        // 四、结束语
        // 使用fork/join 框架可以加速处理大型任务，但要实现这一结果，应遵循一些指导原则：
        // 1、使用尽可能少的线程池。绝大多数情况下，最好的决定是每个应用程序或系统只使用一个线程池。 (是线程池而不是线程)。
        // 2、当不需要任何调整时，使用默认的公共线程池。
        // 3、使用合理的阈值。将 ForkJoingTask 任务拆分为子任务。
        // 4、避免在 ForkJoinTasks 中出现任何阻塞
    }

    public static ForkJoinPool forkJoinPool = new ForkJoinPool(2);

}

class CustomRecursiveAction extends RecursiveAction {

    // 在这个示例中，我们使用了一个字符串类型 ( String ) 的名为 workload 属性来表示要处理的工作单元。
    private String workload = "";

    private static final int THRESHOLD = 4;

    private static Logger logger = Logger.getAnonymousLogger();

    public CustomRecursiveAction(String workload) {
        this.workload = workload;
    }

    @Override
    protected void compute() {
        // 同时，为了演示 fork/join 框架的 fork 行为，在该示例中，如果 workload.length() 大于指定的阈值，
        if (workload.length() > THRESHOLD) {
            // 那么就使用 createSubtask() 方法拆分任务。
            // 在createSubtasks() 方法中，输入的字符串被递归地划分为子串，然后创建基于这些子串的 CustomRecursiveTask 实例。
            // 当递归分割字符串完毕时，createSubtasks() 方法返回 List<CustomRecursiveAction> 作为结果。
            List<CustomRecursiveAction> subTasks = createSubTasks();
            // 然后在compute() 方法中使用 invokeAll() 方法将任务列表提交给 ForkJoinPool 线程池。
            ForkJoinTask.invokeAll(subTasks);
        } else {
            processing(workload);
        }
    }

    private List<CustomRecursiveAction> createSubTasks() {
        List<CustomRecursiveAction> subTasks = new ArrayList<>();
        // 在createSubtasks() 方法中，输入的字符串被递归地划分为子串，然后创建基于这些子串的 CustomRecursiveTask 实例。
        String partOne = workload.substring(0, workload.length() / 2);
        String partTwo = workload.substring(workload.length() / 2, workload.length());
        subTasks.add(new CustomRecursiveAction(partOne));
        subTasks.add(new CustomRecursiveAction(partTwo));
        // 当递归分割字符串完毕时，createSubtasks() 方法返回 List<CustomRecursiveAction> 作为结果。
        return subTasks;
    }

    private void processing(String work) {
        String result = work.toUpperCase();
        logger.info("This result - (" + result + ") - was processed by "
                            + Thread.currentThread().getName());
    }

}

class CustomRecursiveTask extends RecursiveTask<Integer> {

    // 在上面这个示例中，任务由存储在 CustomRecursiveTask 类的 arr 字段中的数组表示。
    private int[] arr;

    private static final int THRESHOLD = 20;

    public CustomRecursiveTask(int[] arr) {
        this.arr = arr;
    }

    @Override
    protected Integer compute() {
        if (arr.length > THRESHOLD) {
            // createSubtask() 方法递归地将任务划分为较小的工作，直到每个部分小于阈值。然后，invokeAll()方法将子任务提交给公共拉取并返回 Future 列表。
            return ForkJoinTask.invokeAll(createSubTasks())
                    .stream()
                    // 要触发执行，需要为每个子任务调用 join() 方法。
                    .mapToInt(ForkJoinTask::join)
                    // sum() 方法用于将子结果组合到最终结果中。
                    .sum();
        }
        return processing(arr);
    }

    private Collection<CustomRecursiveTask> createSubTasks() {
        List<CustomRecursiveTask> dividedTaskList = new ArrayList<>();
        dividedTaskList.add(new CustomRecursiveTask(
                Arrays.copyOfRange(arr, 0, arr.length / 2)
        ));
        dividedTaskList.add(new CustomRecursiveTask(
                Arrays.copyOfRange(arr, arr.length / 2, arr.length)
        ));
        return dividedTaskList;
    }

    private Integer processing(int[] arr) {
        return Arrays.stream(arr)
                .filter(a -> a > 10 && a < 27)
                .map(a -> a * 10)
                .sum();
    }
}