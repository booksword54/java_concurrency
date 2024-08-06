public class DaemonThreadDemo {
    public static void main(String[] args) {
        // 一、守护线程和用户线程的区别
        // Java 提供了两种类型的线程：守护线程和 用户线程
        // - 用户线程 是高优先级线程。JVM 会在终止之前等待任何用户线程完成其任务。
        // - 守护线程 是低优先级线程。其唯一作用是为用户线程提供服务。

        // 由于守护线程的作用是为用户线程提供服务，并且仅在用户线程运行时才需要，因此一旦所有用户线程完成执行，JVM 就会终止。也就是说 守护线程不会阻止 JVM 退出。
        // 这也是为什么通常存在于守护线程中的无限循环不会导致问题，因为任何代码（包括 finally 块 ）都不会在所有用户线程完成执行后执行。
        // 这也是为什么我们并不推荐 在守护线程中执行 I/O 任务 。因为可能导致无法正确关闭资源。
        // 但是，守护线程并不是 100% 阻止 JVM 退出的。守护线程中设计不良的代码可能会阻止 JVM 退出。例如，在正在运行的守护线程上调用Thread.join() 可以阻止应用程序的关闭。

        // 二、守护线程能用来做什么
        // 常见的做法，就是将守护线程用于后台支持任务，比如垃圾回收、释放未使用对象的内存、从缓存中删除不需要的条目。
        // 按照这个解释，那么大多数 JVM 线程都是守护线程。

        // 三、如何创建守护线程
        // 守护线程也是一个线程，因此它的创建和启动其实和普通线程没什么区别？
        // 要将普通线程设置为守护线程，方法很简单，只需要调用 Thread.setDaemon() 方法即可。
        // 例如下面这段代码，假设我们继承 Thread 类创建了一个新类 NewThread 。那么我们就可以创建这个类的实例并设置为守护线程
        Thread daemonThread = new Thread(() -> System.out.println("Hello"));
        daemonThread.setDaemon(true);
        daemonThread.start();

        // 在Java 语言中，线程的状态是自动继承的。任何线程都会继承创建它的线程的守护程序状态。怎么理解呢？
        // 1、 如果一个线程是普通线程（用户线程），那么它创建的子线程默认也是普通线程（用户线程）；
        // 2、 如果一个线程是守护线程，那么它创建的子线程默认也是守护线程；

        // 因此，我们可以推演出： 由于主线程是用户线程，因此在 main() 方法内创建的任何线程默认为用户线程。
        // 需要注意的是调用 setDaemon() 方法的时机，该方法只能在创建 Thread 对象并且在启动线程前调用。在线程运行时尝试调用 setDaemon() 将抛出 IllegalThreadStateException 异常。
        Thread errorDaemonThread = new Thread(() -> System.out.println("Hello"));
        errorDaemonThread.start();
        errorDaemonThread.setDaemon(true); // IllegalThreadStateException

        // 四、如何检查一个线程是守护线程还是用户线程？
        // 检查一个线程是否是守护线程，可以简单地调用方法 isDaemon() ，如下代码所示
        System.out.println(daemonThread.isDaemon()); // true
    }
}
