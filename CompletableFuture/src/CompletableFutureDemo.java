import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CompletableFutureDemo {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        // 一、Java 中的异步计算
        // 异步计算很难推理的，因为我们的大脑是同步的，会将任何计算看成是一系列的同步计算。
        // 我们在实现异步计算时，往往会把回调的动作分散在代码中或者深深地嵌套在彼此内部，这种情况下，当我们需要处理其中一个步骤中可能发生的错误时，情况变得更糟。
        // 尽管 Java 5 已经看到了这种恶性循环，提供了Future 接口作为异步计算的结果，但它没有提供任何方法来组合这些计算或处理可能的错误。
        // 直到Java 8，才引入了 CompletableFuture 类。该类不仅实现了 Future 接口，还实现了 CompletionStage 接口。此接口定义了可与异步计算步骤组合的异步计算步骤契约。

        // 简单来说，CompletionStage 接口规范了一个异步计算步骤如何与另一个异步计算步骤组合。
        // CompletableFuture 类还是一个集大成者，既是一个构建块，也是一个框架，提供了大约 50 种不同的方法来构造，组合，执行异步计算步骤和处理错误。
        // API数量如此之多，第一眼看到简直就傻眼了，不过好在它们可以分门别类，因为它们大多属于几个明确且不同的用例。


        // 二、将 CompletableFuture 当作简单的 Future 使用
        // 因为CompletableFuture 类实现了 Future 接口，因此我们可以将其用作 Future 实现，但需要自己实现额外的完成逻辑。
        // 例如，我们可以使用无任何参数的构造函数来创建此类的实例，用于表示未来的某些结果，然后将其交给使用者，并在将来的某个时间调用 complete() 方法完成。
        // 消费者可以使用 get() 方法来阻止当前线程，直到提供此结果。
        CompletableFuture<String> completableFuture = new CompletableFuture<>();
        Executors.newCachedThreadPool()
                .submit(() -> {
                    Thread.sleep(500);
                    completableFuture.complete("Hello");
                    return null;
                });
        String result = completableFuture.get();
        System.out.println("Hello".equals(result));
        // 上面的实例中，我们创建了一个创建 CompletableFuture 实例的方法，把计算分离到另一个线程中并立即返回 Future。
        // 当计算完成后，该方法通过将结果提供给 complete() 方法来完成 Future。
        // 为了分离计算，我们使用了 Executor API。这种创建和完成 CompletableFuture 的方法可以与任何并发机制或 API（ 包括原始线程 ）一起使用。

        // 注意:get() 方法会抛出一些已检查的异常，即 ExecutionException（ 封装计算期间发生的异常 ）和 InterruptedException（ 表示执行方法的线程被中断的异常 )。
        // 如果你已经知道计算的结果，可以将表示此计算的结果作为参数传递给 completedFuture() 静态方法，这样，Future 的 get() 方法永远不会阻塞，而是立即返回此结果。
        CompletableFuture<String> expectedCompletableFuture = CompletableFuture.completedFuture("Hello");
        String expectedResult = expectedCompletableFuture.get();
        System.out.println("Hello".equals(expectedResult));

        // 当然了，有时候，你可能希望取消 Future 的执行。
        // 假设我们没有找到结果并决定完全取消异步执行，这可以通过调用 Future 的 cancel() 方法完成。此方法接收一个布尔参数 mayInterruptIfRunning。
        CompletableFuture<String> cancelledCompletableFuture = new CompletableFuture<>();
        Executors.newCachedThreadPool()
                .submit(() -> {
                    Thread.sleep(500);
                    cancelledCompletableFuture.cancel(false);
                    return null;
                });
        //String cancelledResult = cancelledCompletableFuture.get(); // throws exception
        // 在上面这个异步方法的修改版本的范例中，当我们使用 Future.get() 方法阻塞结果时，如果 future 取消了，那么将抛出CancellationException 异常。
        // 但是，在类型为 CompletableFuture 的情况下，cancel() 方法没有任何效果，因为 CompletableFuture 并不会响应中断也不会处理中断。


        // 三、用于封装计算逻辑的 CompletableFuture
        // 上面讲解的这些代码，都允许我们选择任何并发执行机制，但是，如果我们想跳过这个样板代码并简单地异步执行一些代码呢？
        // CompletableFuture 的静态方法 runAsync() 和 supplyAsync() 允许我们从 Runnable 和 Supplier 中创建 CompletableFuture 实例。
        // Runnable 和 Supplier 都是功能接口，由 Java 8 的新功能，可以将它们的实例作为lambda 表达式传递：
        // - Runnable 接口与线程中使用的旧接口相同，不允许返回值
        // - Supplier 接口是一个通用的功能接口，只有一个没有参数的方法，并返回一个参数化类型的值
        CompletableFuture<String> supplierCompletableFuture = CompletableFuture
                .supplyAsync(() -> "Hello");
        System.out.println("Hello".equals(supplierCompletableFuture.get()));


        // 四、处理异步计算的结果
        // 处理计算结果的最通用方法是将其提供给函数。CompletableFuture.thenApply() 方法就是这样做的：
        // 接受一个 Function 实例，用它来处理结果并返回一个用于保存函数返回的值 Future。
        CompletableFuture<String> functionalCompletableFuture = CompletableFuture
                .supplyAsync(() -> "Hello")
                .thenApply(s -> s + " World");
        System.out.println("Hello World".equals(functionalCompletableFuture.get()));

        // 如果你不需要在 Future 链中返回值，则可以使用 Consumer 功能接口的实例。
        // 它只有一个方法，该方法接受一个参数并返回 void。而相应的，CompletableFuture 也提供了一个使用 Consumer 实例的方法 thenAccept() 。
        // 该方法接收一个 Consumer 并将其传递给计算结果。
        CompletableFuture<Void> consumerCompletableFuture = CompletableFuture
                .supplyAsync(() -> "Hello")
                .thenApply(s -> s + " ")
                .thenAccept(s -> System.out.println(s + "World"));
        consumerCompletableFuture.get();
        // 上面这个示例中，最后的 future.get() 方法会返回空值 Void 。
        // 最后，如果你既不需要计算的值也不想在链的末尾返回一些值，那么你可以将 Runnable lambda 传递给 thenRun() 方法。
        CompletableFuture<Void> runnableCompletableFuture = CompletableFuture
                .supplyAsync(() -> "Hello")
                .thenApply(s -> s + " ")
                .thenAccept(s -> System.out.println(s + "World"))
                .thenRun(() -> System.out.println("Completed"));
        runnableCompletableFuture.get();
        // 上面这个示例中，调用 future.get() 方法之后会在控制台打印一行 Completed


        // 五、组合 Futures
        // CompletableFuture API 最吸引人的部分，应该是能够在一系列链式计算步骤中组合 CompletableFuture 实例。
        // 这种链式的结果本身就是CompletableFuture，允许进一步链接和组合。
        // 这种方法在函数式语言中无处不在，通常被称为 「一元 ( monadic ) 设计模式 」。
        // CompletableFuture 提供了方法 thenCompose() 用于按顺序链接两个 Futures。
        // 该方法的参数是一个能够返回 CompletableFuture 实例的函数或表达式。
        // 而该函数或表达式的参数则是先前计算步骤的结果，这允许我们在下一个 CompletableFuture 的 lambda 中使用这个值。
        CompletableFuture<String> composedCompletableFuture = CompletableFuture
                .supplyAsync(() -> "简单")
                .thenCompose(s -> CompletableFuture.supplyAsync(() -> s + "教程"))
                .thenCompose(s -> CompletableFuture.supplyAsync(() -> s + "easy"));
        System.out.println("简单教程easy".equals(composedCompletableFuture.get()));
        // thenCompose() 方法与 thenApply() 一起实现了一元设计模式的基本构建块，它们与Java 8 中提供的 Stream 和 Optional 类的 map 和 flatMap 方法密切相关。
        // 两个方法都接收一个函数并将其应用于计算结果，但 thenCompose() （ flatMap() ）方法接收一个函数，该函数返回相同类型的另一个对象，这样，就允许将这些类的实例组合为构建块。

        // 如果要执行两个独立的 Futures 并对其结果执行某些操作，可以使用 Future 的 thenCombine() 并传递能够接收两个参数的函数或表达式来处理这两个结果。
        CompletableFuture<String> combinedCompletableFuture = CompletableFuture
                .supplyAsync(() -> "Hello")
                .thenCombine(CompletableFuture.supplyAsync(() -> " World"), (returnFirst, returnSecond) -> returnFirst + returnSecond)
                .thenCombine(CompletableFuture.supplyAsync(() -> "你好世界"), (returnFirst, returnSecond) -> returnFirst + returnSecond);
        System.out.println("Hello World你好世界".equals(combinedCompletableFuture.get()));

        // 更简单的情况是，当你想要使用两个 Futures 的结果时，但有不想把任何结果值传递给 Future 链，则可以使用 thenAcceptBoth() 方法，如下所示
        CompletableFuture acceptBothCompletableFuture = CompletableFuture
                .supplyAsync(() -> "Hello")
                .thenAcceptBoth(CompletableFuture.supplyAsync(() -> " World"), (returnFirst, returnSecond) -> System.out.println(returnFirst + returnSecond));


        // 六、并行执行多个 Future
        // 当我们需要并行执行多个 Futures 时，通常是希望等待所有 Futures 执行完成然后处理它们的组合结果。
        // CompletableFuture.allOf() 静态方法允许等待作为 var-arg 提供的所有 Future 的完成。
        CompletableFuture<String> futureOne = CompletableFuture.supplyAsync(() -> "Hello");
        CompletableFuture<String> futureTwo = CompletableFuture.supplyAsync(() -> "World");
        CompletableFuture<String> futureThree = CompletableFuture.supplyAsync(() -> "Hi");

        CompletableFuture<Void> allOfCompletableFuture = CompletableFuture.allOf(futureOne, futureTwo, futureThree);
        allOfCompletableFuture.get();
        System.out.println(futureOne.isDone());
        System.out.println(futureTwo.isDone());
        System.out.println(futureThree.isDone());
        // 上面的示例中，你应该留意到了 CompletableFuture.allOf() 方法的返回类型是 CompletableFuture <Void>，这个方法的局限是它不会返回所有 Future 的综合结果。相反，你必须手动从 Futures 获取结果。幸运的是，CompletableFuture.join() 方法和 Java 8 Streams API 可以做到这一点
        String allOfResult = Stream.of(futureOne, futureTwo, futureThree)
                .map(CompletableFuture::join)
                .collect(Collectors.joining(" "));
        System.out.println("Hello World Hi".equals(allOfResult));
        // CompletableFuture.join() 方法类似于 get() 方法，但是如果 Future 未正常完成，它会抛出未经检查的异常，这种机制，使得它可以作为 Stream.map() 的参数。


        // 七、处理错误
        // 对于异步计算步骤链中的错误处理，惯用的方法是调整 throw/catch 。
        // CompletableFuture 类允许我们在特殊的 handle() 方法中处理它，而不是在语法块中捕获异常。
        // 此handle() 方法接收接收两个参数：计算结果（ 如果成功完成 ）和抛出异常（ 如果某些计算步骤未正常完成 ）。
        String name = null;
        CompletableFuture<String> exceptionHandlingCompletableFuture = CompletableFuture.supplyAsync(() -> {
            if (name == null) {
                throw new RuntimeException("Computation Error!");
            }
            return "Hello, " + name;
        }).handle((s, t) -> s != null ? s : "Hello, Stranger");
        System.out.println("Hello, Stranger".equals(exceptionHandlingCompletableFuture.get()));
        // 上面这个示例中，我们使用 handle() 方法在问候语的异步计算完成时提供默认值，因为没有提供 name 。
        // 作为替代方案，假设我们想要手动使用某个值完成 Future ，就像第一个示例中所示，但同时又需要有能力通过异常完成它。那么，可以使用 completeExceptionally() 方法。
        CompletableFuture<String> completeExceptionallyCompletableFuture = new CompletableFuture<>();
        completeExceptionallyCompletableFuture.completeExceptionally(new RuntimeException("Calculation Failed"));
        completeExceptionallyCompletableFuture.get(); // ExecutionException: java.lang.RuntimeException: Calculation Failed
        // 上面这个示例的 completableFuture.get() 方法会抛出 ExecutionException，并使用RuntimeException 作为异常发生的原因。
        // 在上面的例子中，我们也可以使用 handle() 方法异步处理异常，但使用 get() 方法是更典型的同步异常处理机制。


        // 八、异步方法
        // CompletableFuture 类中的大多数流式 API 方法都又两个带有 Async 后缀的变体。这些变体方法通常用于在另一个线程中运行相应的执行步骤。
        // - 没有 Async 后缀的方法使用当前调用线程运行下一个执行阶段。
        // - 不带 Executor 参数的 Async 后缀方法使用 ForkJoinPool.commonPool() 方法访问 Executor 的公共 fork/join 线程池实现运行一个步骤。
        // - 带有 Executor 参数的 Async 后缀方法使用传递的 Executor 运行一个步骤。

        // 下面这个范例中，我们使用了 Function 实例处理计算结果。和之前范例的唯一可见的区别就是 thenApplyAsync() 方法。但在幕后，函数的应用程序被包装到 ForkJoinTask 实例中（ 有关 fork/join 框架的更多信息，请阅读我们的 一文秒懂 Java Fork/Join ），这样可以进一步并行化我们的计算并更有效地使用系统资源。
        CompletableFuture<String> asyncCompletableFuture = CompletableFuture
                .supplyAsync(() -> "Hello")
                // 函数的应用程序被包装到 ForkJoinTask 实例中
                // 进一步并行化我们的计算并更有效地使用系统资源
                .thenApplyAsync(s -> s + " World");
        System.out.println("Hello World".equals(asyncCompletableFuture.get()));


        // 九、Java 9 CompletableFuture 新增的 API
        // Java 9 提供了一下变更进一步强化了 CompletableFuture：
        // - 添加了新的工厂方法
        // - 支持延时和超时
        // - 改进了对子类化的支持

        // Java 9 同时也引入了新的 CompletableFuture 实例 API
        // - Executor defaultExecutor()
        // - CompletableFuture newIncompleteFuture()
        // - CompletableFuture copy()
        // - CompletionStage minimalCompletionStage()
        // - CompletableFuture completeAsync(Supplier<? extends T> supplier, Executor executor)
        // - CompletableFuture completeAsync(Supplier<? extends T> supplier)
        // - CompletableFuture orTimeout(long timeout, TimeUnit unit)
        // - CompletableFuture completeOnTimeout(T value, long timeout, TimeUnit unit)

        // 还添加了一些静态的使用方法
        // - Executor delayedExecutor(long delay, TimeUnit unit, Executor executor)
        // - Executor delayedExecutor(long delay, TimeUnit unit)
        // - CompletionStage completedStage(U value)
        // - CompletionStage failedStage(Throwable ex)
        // - CompletableFuture failedFuture(Throwable ex)

        // 最后，为了解决超时问题，Java 9 引入了另外两个新功能
        // - orTimeout()
        // - completeOnTimeout()


    }


}
