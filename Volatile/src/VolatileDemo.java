public class VolatileDemo {
    // Happens-Before
    // 1. 程序的顺序性规则
    // 前面的操作 Happens-Before 于后续的任意操作。
    int x = 0;
    volatile boolean v = false;

    public void writer() {
        x = 42;
        v = true;
    }

    public void reader() {
        if (v == true) {
            // 这里 x 会是 42
        }
    }

    // 2. volatile 变量规则
    // 对一个 volatile 变量的写操作， Happens-Before 于后续对这个 volatile 变量的读操作。

    // 3. 传递性
    // 这条规则是指如果 A Happens-Before B，且 B Happens-Before C，那么 A Happens-Before C。
    // 1、 “x=42”Happens-Before写变量“v=true”，这是规则1的内容；
    // 2、 写变量“v=true”Happens-Before读变量“v=true”，这是规则2的内容；
    // 再根据这个传递性规则，我们得到结果“x=42” Happens-Before 读变量“v=true”。
    // 线程 B 读到了“v=true”，那么线程 A 设置的“x=42”对线程 B 是可见的

    // 4. 管程中锁的规则
    // 锁的解锁 Happens-Before 于后续对这个锁的加锁
    // 管程是一种通用的同步原语，在 Java 中指的就是 synchronized，synchronized 是 Java 里对管程的实现
    // 管程中的锁在 Java 里是隐式实现
    public void testSynchronized() {
        synchronized (this) { // 此处自动加锁
            // x 是共享变量, 初始值 =10
            if (this.x < 12) {
                this.x = 12;
            }
        } // 此处自动解锁
    }
    // 假设 x 的初始值是 10，线程 A 执行完代码块后 x 的值会变成 12
    // 线程 B 进入代码块时，能够看到线程 A 对 x 的写操作，也就是线程 B 能够看到 x==12


    // 5. 线程 start() 规则
    // 主线程 A 启动子线程 B 后，子线程 B 能够看到主线程在启动子线程 B 前的操作
    // 线程 A 调用线程 B 的 start() 方法（即在线程 A 中启动线程 B），那么该 start() 操作 Happens-Before 于线程 B 中的任意操作
    int var = 10;

    public void testThreadStart() {
        Thread B = new Thread(() -> {
            // 主线程调用 B.start() 之前
            // 所有对共享变量的修改，此处皆可见
            // 此例中，var==77
        });
        // 此处对共享变量 var 修改
        var = 77;
        // 主线程启动子线程
        B.start();
    }

    // 6. 线程 join() 规则
    // 线程 A 中，调用线程 B 的 join() 并成功返回，那么线程 B 中的任意操作 Happens-Before 于该 join() 操作的返回
    // 主线程 A 等待子线程 B 完成（主线程 A 通过调用子线程 B 的 join() 方法实现），
    // 当子线程 B 完成后（主线程 A 中 join() 方法返回），主线程能够看到子线程的操作
    public void testJoin() throws InterruptedException {
        Thread B = new Thread(() -> {
            System.out.println(var);
            // 此处对共享变量 var 修改
            var = 66;
        });
        // 例如此处对共享变量修改，
        // 则这个修改结果对线程 B 可见
        var = 77;
        // 主线程启动子线程
        B.start();
        B.join();
        // 子线程所有对共享变量的修改
        // 在主线程调用 B.join() 之后皆可见
        // 此例中，var=66
        System.out.println(var);
    }

    // 7.我们忽视的 final
    //  volatile 为的是禁用缓存以及编译优化，我们再从另外一个方面来看，有没有办法告诉编译器优化得更好一点呢
    // final 修饰变量时，初衷是告诉编译器：这个变量生而不变，可以优化

    // 利用双重检查方法创建单例，构造函数的错误重排导致线程可能看到 final 变量的值会变化

    //  Java 内存模型对 final 类型变量的重排进行了约束。现在只要我们提供正确构造函数没有“逸出”，就不会出问题了
    // 在下面例子中，在构造函数里面将 this 赋值给了全局变量 global.obj，这就是“逸出“
    final int finalX;
    FinalDemo global = new FinalDemo();

    // 错误的构造函数
    public VolatileDemo() {
        finalX = 3;
        // 此处就是讲 this 逸出，
        // 线程通过 global.obj 读取 finalX 是有可能读到 0 的。因此我们一定要避免“逸出”。
        global.obj = this;
    }

}

class Singleton {
    static Singleton instance;

    final int x;

    public Singleton() {
        this.x = 6;
    }

    static Singleton getInstance() {
        if (instance == null) {
            synchronized (Singleton.class) {
                if (instance == null) {
                    // 利用双重检查方法创建单例，构造函数的错误重排导致线程可能看到 final 变量的值会变化
                    // 假设线程 A 先执行 getInstance() 方法，当执行完指令 2 时恰好发生了线程切换，切换到了线程 B 上；
                    // 如果此时线程 B 也执行 getInstance() 方法，那么线程 B 在执行第一个判断时会发现 `instance != null` ，
                    // 所以直接返回 instance，而此时的 instance 是没有初始化过的，如果我们这个时候访问 instance 的成员变量就可能触发空指针异常。
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }
}

class FinalDemo {
    VolatileDemo obj;
}
