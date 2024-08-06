public class SynchronizedDemo {

    // Java 编译器会在 synchronized 修饰的方法或代码块前后自动加上加锁 lock() 和解锁 unlock()
    // 当修饰静态方法的时候，锁定的是当前类的 Class 对象，在上面的例子中就是 Class X；
    // 当修饰非静态方法的时候，锁定的是当前实例对象 this。

    // 修饰非静态方法
    // synchronized(this)
    synchronized void foo() {
        // 临界区
    }

    // 修饰静态方法
    // synchronized(X.class)
    synchronized static void bar() {
        // 临界区
    }

    // 修饰代码块
    Object obj = new Object();

    void baz() {
        synchronized (obj) {
            // 临界区
        }
    }

    // 用 synchronized 解决 count+=1 问题
    class SafeCalc {
        long value = 0L;

        // value 的值对 get() 方法不是可见
        // 管程中锁的规则，是只保证后续对这个锁的加锁的可见性
        //  get() 方法也得 synchronized 一下
        long get() {
            return value;
        }
        // 只有一个线程能够执行 addOne() 方法，所以一定能保证原子操作，那是否有可见性问题
        // 锁的解锁 Happens-Before 于后续对这个锁的加锁。
        // 同一时刻只有一个线程执行临界区的代码；而所谓“对一个锁解锁 Happens-Before 后续对这个锁的加锁”，指的是前一个线程的解锁操作对后一个线程的加锁操作可见
        // 综合 Happens-Before 的传递性原则，得出
        // 前一个线程在临界区修改的共享变量（该操作在解锁之前），对后续进入临界区（该操作在加锁之后）的线程是可见的

        // 多个线程同时执行 addOne() 方法，可见性是可以保证的
        synchronized void addOne() {
            value += 1;
        }
    }

    // 锁和受保护资源的关系
    // 把 value 改成静态变量，把 addOne() 方法改成静态方法，
    // 此时 get() 方法和 addOne() 方法是否存在并发问题呢？
    // 见 class UnSafeCalc

    // 发现改动后的代码是用两个锁保护一个资源
    // 临界区 get() 和 addOne() 是用两个锁保护的，
    // 因此这两个临界区没有互斥关系，临界区 addOne() 对 value 的修改对临界区 get() 也没有可见性保证，这就导致并发问题了

}

class UnSafeCalc {
    static long value = 0L;
    synchronized long get() {
        return value;
    }

    synchronized static void addOne() {
        value += 1;
    }
}

