import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Practice {
    public static void main(String[] args) {
        // 并发编程实战
        // Q1: 进程和线程的区别？
        // 1、 进程和线程都是并发单元，但它们有一个根本区别：进程不共享公共内存，而线程则共享
        // 2、 从操作系统的角度来看，进程是一个独立的软件，在其自己的虚拟内存空间中运行任何一个多任务操作系统（这几乎意味着任何现代操作系统）都必须将内存中的进程分开，这样一个失败的进程就不会通过加扰公共内存来拖累所有其它进程因此，进程通常是隔离的，它们通过进程间通信进行协作，进程间通信由操作系统定义为一种中间API；
        // 3、 相反，线程是应用程序的一部分，它与同一应用程序的其他线程共享公共内存使用公共内存可以减少大量开销，因此使用线程可以更快的交换数据和进行线程间协作；


        // Q2: 如何创建一个线程实例并且运行它？
        // 创建一个线程的实例，有两种方法可供选择:
        // 1、 把Runnable的实例传递给Thread的构造函数并调用start()方法；
        Thread runnableThread = new Thread(() -> {
            System.out.println("Hello World from Runnable");
        });
        runnableThread.start();
        // Runnable是一个函数接口，因此可以作为 lambda 表达式传递

        // 2、 因为线程本身也实现了Runnable接口，所以另一种创建线程的方法是创建一个匿名子类，覆写它的run()方法，然后调用start()；
        Thread subClassThread = new Thread() {
            @Override
            public void run() {
                System.out.println("Hello World from subclass");
            }
        };
        subClassThread.start();


        // Q3: 描述线程的不同状态以及何时发生状态转换 ？
        // 1、 一般情况下，我们会使用Thread.getState()方法检查线程(Thread)的状态；
        // 2、 线程的不同状态都定义在Thread.State枚举中；
        // 3、 线程的所有状态如下所示；
        // 1、 NEW:一个尚未调用Thread.start()方法启动的新Thread实例；
        // 2、 RUNNABLE:一个正在运行的线程它被称为runnable，因为在任何给定时间，它要么正在运行要么在等待线程调度当调用Thread.start()方法时，会将一个NEW线程进入RUNNABLE状态；
        // 3、 BLOCKED:如果正在运行的线程需要进入同步部分但由于另一个线程持有此部分的监视器而无法执行此操作，则该线程将被阻塞；
        // 4、 WAITING:如果线程等待另一个线程执行特定操作，则该线程进入此状态例如，一个线程在它持有的监视器上调用Object.wait()法时进入此状态，或者在另一个线程上调用Thread.join()方法也会进入此状态；
        // 5、 TIMED_WAITING:跟WAITING状态差不多但线程在调用Thread.sleep()、Object.wait()、或Thread.join()和其他一些方法的定时版本后进入此状态；
        // 6、 TERMINATED：当一个线程已经完成它的Runnable.run()方法的执行并终止时进入此状态；


        // Q4: Runnable 和 Callable 接口有什么区别？它们是如何使用的？
        // 1、 Runnable接口表示必须在单独的线程中运行的计算单位，它只有一个run()方法Runnable接口不允许此方法返回值或抛出未经检查的异常；
        // 2、 Callable接口表示具有返回值的任务，它只有一个call()方法call()方法可以返回一个值(可以是Void)，也可以抛出一个异常Callable通常在ExecutorService实例中用于启动异步任务，然后调用返回的Future实例以获取其值；


        // Q5: 什么是守护线程，它的使用场景是什么？如何创建守护线程 ？
        // 1、 守护线程是一个不阻止Java虚拟机(JVM)退出的线程当所有非守护线程终止时，JVM只是放弃所有剩余的守护线程；
        // 2、 守护线程通常用于为其他线程执行一些支持或服务任务，但我们应该考虑到它们可能随时被放弃；
        // 3、 要将一个线程作为守护线程启动，应该在调用start()之前使用setDaemon()方法设置为守护线程如下所示；
        Thread daemon = new Thread(() -> System.out.println("Hello from daemon"));
        daemon.setDaemon(true);
        daemon.start();
        // 奇怪的是，如果将上面的代码放在 main() 内运行，则可能无法打印该消息。而发生这种情况的原因，是因为 main() 线程在守护线程运行到打印消息之前就已经终止。
        // 我们不应该在守护线程中执行任何 I/O 操作，因为它们甚至无法执行其 finally 块并在被放弃时关闭资源。


        // Q6: 什么是 Thread 的中断标志？怎么设置和检查它？它与 InterruptedException 有什么关系？
        // 1、 中断(interrupt)标志或中断状态是线程中断时设置的内部线程标志(flag属性）；
        // 2、 要设置一个线程的中断标志，只需要简单的在线程对象上调用thread.interrupt()方法；
        // 3、 如果在某个方法内部的一个线程抛出了InterruptedException（wait、join、sleep等），那么此方法会立即抛出InterruptedException线程可以根据自己的逻辑自由处理此异常如果一个线程不在这样的方法中并且调用了thread.interrupt()，则不会发生任何特殊情况；
        // 4、 线程的中断状态可以通过使用静态Thread.interrupted()方法或实例的isInterrupted()方法定期检查这两个方法的区别是静态Thread.interrupt()会清除了中断标志，而isInterrupted()则不会；


        // Q7: 什么是 Executor 和 ExecutorService ？这两个接口有什么区别？
        // 1、 Executor和ExecutorService是java.util.concurrent框架提供的两个相关接口；
        // 2、 Executor是一个非常简单的接口，只有一个execute()方法接受Runnable实例来执行在大多数情况下，这是我们的任务执行代码应该依赖的接口；
        // 3、 ExecutorService扩展了Executor接口，并且添加了许多其它方法以处理和检查并发任务执行服务的生命周期（在关闭时终止任务）和更复杂的异步任务处理，包括Futures；


        // Q8: java.util.concurrent 标准库中 ExecutorService 的可用实现是什么 ？
        // ExecutorService 接口有三个标准实现
        // 1、 ThreadPoolExecutor:使用线程池执行任务一旦某个线程完成执行任务，它就会回到线程池中如果池中的所有线程都忙，则任务必须等待轮到它；
        // 2、 ScheduledThreadPoolExecutor:允许安排任务执行，而不是简单的在线程可用时立即运行任务它还可以按固定频率或固定延迟安排任务；
        // 3、 ForkJoinPool:是一个特殊的ExecutorService，用于处理递归算法任务如果你使用常规ThreadPoolExecutor进行递归算法，那么你很快发现所有线程都在忙着等待较低级别的递归完成ForkJoinPool实现了所谓的工作窃取算法，允许它更有效地使用可用线程；


        // Q9: 什么是 Java 内存模型（ JMM ）？描述下其目的和基本思想
        // JMM规定了多个线程如何访问并发 Java 应用程序中的公共内存，以及一个线程的数据更改如何对其他线程可见。
        // 对内存模型的需求源于这样一个事实：Java 代码访问数据的方式并不像它在底层实际发生的那样。
        // 在保证内存读写的可观察结果是相同的情况下，Java 编译器，JIT 编译器甚至 CPU 都可以对内存读写进行重新排序或优化。
        // 当我们的应用程序扩展到多个线程时，这会导致反直觉的结果，因为大多数这些优化只会考虑单个执行线程（ 跨线程优化器仍然非常难以实现 ）。
        // 另一个可怕的问题是现代系统中的内存是多层的：处理器的多个内核可能会在其缓存或读/写缓冲区中保留一些非刷新数据，这也会影响从其它内核观察到的内存状态。
        // 更糟糕的是，不同内存访问架构的存在将打破Java 「 一次编写，随处运行 」 的承诺。
        // 但另所有 Java 程序员高兴的是，JMM 指定了在设计多线程应用程序时可能依赖的一些保证。坚持这些保证有助于程序员编写在各种体系结构之间稳定且可移植的多线程代码。

        // JMM的主要概念是：
        // 动作 ( Action ) : 这些是线程间的动作，可以由一个线程执行并由另一个线程检测，如读取或写入变量，锁定/解锁监视器等等。
        // 同步动作 ( Synchronization actions ) : 某个动作子集，例如读取/写入易失性变量，或锁定/解锁监视器。
        // 程序顺序 ( Program Order ) : 俗称 PO，单个线程内可观察的动作总顺序。
        // 同步顺序 ( Synchronization Order ) : 俗称 SO，所有同步操作之间的总顺序 – 它必须与程序顺序一致，也就是说，如果两个同步操作在PO 中一个接一个地出现，它们在 SO 中以相同的顺序出现 。
        // 同步与（ synchronizes-with） : 俗称 SW ，某些同步操作之间的关系，例如解锁监视器和锁定同一监视器（ 在另一个或同一个线程中 ）。
        // 发生在顺序之前 ( Happens-before Order ) : 将 PO 与 SW 结合（ 在集合论中称为传递闭包 ），以创建线程之间所有动作的部分排序。如果一个动作发生在另一个动作之前，则第二个动作可以观察到第一个动作的结果（ 例如，在一个线程中写入变量并在另一个线程中读取 ）。
        // 发生在一致性之前 ( Happens-before consistency ) : 如果每次读取都遵循先前发生的顺序中对该位置的最后一次写入，或者通过数据竞争进行其他一些写入操作，则一组操作是 HB 一致的。
        // 执行 ( Execution ) : 它们之间有一组有序的动作和一致性规则

        // 对于给定的程序，我们可以观察到具有各种结果的多个不同的执行.但是如果一个程序正确同步，那么它的所有执行似乎都是顺序一致的，这意味着我们可以将多线程程序推断为一系列按顺序发生的动作。这样可以省去考虑引擎盖下重新排序，优化或数据缓存的麻烦。

        // Q10: 什么是易失 （ volatile ） 字段，JMM 对这样的领域有什么保证？
        // 根据Java 内存模型 （ 参见 Q9 ） ，volatile 字段具有特殊属性。volatile 变量的读取和写入是同步操作，这意味着它们具有总排序（ 所有线程将遵循这些操作的一致顺序 ）。根据此顺序，保证读取 volatile 变量可以观察到对此变量的最后一次写入。
        // 如果你有一个从多个线程访问的字段，且至少有一个线程写入它，那么你应该考虑使它变得 volatile ，否则某个线程从这个字段读取的内容并不会得到一丝的保证。
        // volatile 的另一个保证是写入和读取 64 位值（ long 类型和 double 类型 ）的原子性。如果没有 volatile 修饰符，读取此类字段可能会观察到另一个线程部分写入的值。


        // Q11: 以下哪项操作是原子操作 ?
        // 写入一个非 volatile int 类型
        // 写入一个 volatile int 类型
        // 写入一个非 volatile long 类型
        // 写入一个 volatile long 类型
        // 递增一个 volatile long 类型
        // 1、 对int类型（32位）变量的写入保证是原子的，无论它是否是易失性的；
        // 2、 long类型（64位）变量可能需要在两个单独的步骤中写入，例如，在32位体系结构上，因此默认情况下，没有原子性保证但是，如果添加了volatile修饰符，则保证以原子方式访问long变量；
        // 3、 递增操作通常由多个步骤完成（检索值，更改它并写回），因此它永远不会保证是原子的，变量是易变的如果要实现值的原子增量，则应使用类AtomicInteger，AtomicLong等；


        // Q12: JMM 对添加了 final 修饰符的类的字段有什么特殊保证 ？
        // JVM基本上会保证在任何线程获取对象之前初始化类的 final 字段。
        // 如果没有这种保证，由于重新排序或其他优化，在初始化该对象的所有字段之前，可以向另一个线程发布对象的引用，即变得可见。这可能会导致对这些字段的访问。
        // 这就是为什么在创建不可变对象时，应始终将其所有字段设为 final，即使它们不能通过 getter 方法访问。


        // Q13: 方法定义中 synchronized 关键字的含义是什么？静态方法？在一个块之前 ？
        // 块(block ) 之前的 synchronized 关键字表示进入该块的任何线程都必须获取监视器（ 括号中的对象 ）。
        // synchronized(object) {}
        // synchronized void instanceMethod() {}
        // 对于静态同步方法，监视器是表示声明类的 Class 对象。
        // static synchronized void staticMethod() {}


        // Q14: 如果两个线程同时在不同的对象实例上调用 synchronized 方法，这些线程中的一个是否会阻塞？如果该方法是静态的，该怎么办?
        // 如果方法是实例方法，则实例充当方法的监视器。在不同实例上调用该方法的两个线程获取不同的监视器，因此它们都不会被阻塞。
        // 如果方法是静态的，则监视器是 Class 对象。对于两个线程，监视器是相同的，因此其中一个可能会阻塞并等待另一个退出 synchronized 方法。


        // Q15: Object类的 wait，notify 和 notifyAll 方法的目的是什么 ？
        // 拥有对象监视器的线程（ 例如，已进入由对象保护的同步部分的线程 ）可以调用 object.wait() 来临时释放监视器并为其他线程提供获取监视器的机会。例如，这可以在等待某个条件的情况下完成。
        // 当另一个获取监视器的线程满足条件时，它可以调用 object.notify() 或 object.notifyAll() 并释放监视器。notify() 方法唤醒处于等待状态的单个线程，notifyAll() 方法唤醒等待此监视器的所有线程，并且它们都竞争重新获取锁定。
        // 下面的BlockingQueue 实现演示了多个线程如何通过 wait-notify 模式一起工作。如果我们将一个元素放入一个空队列，那么在 take() 方法中等待的所有线程都会唤醒并尝试接收该值。如果我们将一个元素放入一个已经满了的队列，put() 方法将等待对 get() 方法的调用。get() 方法删除一个元素，并通知在 put() 方法中等待队列对新项目有空位置的线程。


        // Q16: 描述死锁，存活锁和饥饿的条件。描述这些情况的可能原因 ?
        // 死锁 （ DeadLock ） 是一组无法进行的线程中的条件，因为组中的每个线程都必须获取已由组中的另一个线程获取的某些资源。最简单的情况是两个线程需要锁定两个资源才能进行，第一个资源已被一个线程锁定，第二个资源已被另一个线程锁定。因为这些线程永远不会获得对两个资源的锁定，因此永远不会进展。
        // 存活锁 ( LiveLock ) 是多线程对自己生成的条件或事件做出反应的一种情况。事件发生在一个线程中，必须由另一个线程处理。在此处理期间，发生的新事件必须在第一个线程中处理，依此类推。这样的线程是活着的并且没有被阻挡，但是仍然没有取得任何进展，因为它们用无用的工作压倒了对方
        // 饥饿锁 ( Starvation ) 是线程无法获取资源的情况，因为其他线程（或多个线程）占用它太长时间或具有更高的优先级。线程无法取得进展，因此无法完成有用的工作。


        // Q17: 描述 fork/join 框架的用途和用例
        // fork/join 框架允许并行化递归算法。使用 ThreadPoolExecutor 之类的并行递归的主要问题是，可能会快速耗尽线程，因为每个递归步骤都需要自己的线程，而堆栈中的线程将处于空闲状态并等待。
        // fork/join 框架入口点是 ForkJoinPool 类，它是 ExecutorService 的一个实现。它实现了工作窃取算法，空闲线程会试图从忙线程中 「 窃取 」 工作。这允许在不同线程之间传播计算并在使用比通常的线程池所需的更少的线程时取得进展


    }

}

class CustomBlockingQueue<T> {

    private List<T> queue = new LinkedList<>();

    private int limit = 10;

    public synchronized void put(T item) {
        while (queue.size() == limit) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (queue.isEmpty()) {
            notifyAll();
        }
        queue.add(item);
    }

    public synchronized T take() throws InterruptedException {
        while (queue.isEmpty()) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (queue.size() == limit) {
            notifyAll();
        }
        return queue.remove(0);
    }
}
