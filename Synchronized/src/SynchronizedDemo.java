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

    // 如何用一把锁保护多个资源？
    // 受保护资源和锁之间合理的关联关系应该是 N:1 的关系，也就是说可以用一把锁来保护多个资源，但是不能用多把锁来保护一个资源，并且结合文中示例，我们也重点强调了“不能用多把锁来保护一个资源”这个问题。而至于如何保护多个资源，我们今天就来聊聊。
    // 一、保护没有关联关系的多个资源
    // 在现实世界里，球场的座位和电影院的座位就是没有关联关系的，这种场景非常容易解决，那就是球赛有球赛的门票，电影院有电影院的门票，各自管理各自的。
    // 同样这对应到编程领域，也很容易解决。例如，银行业务中有针对账户余额（余额是一种资源）的取款操作，也有针对账户密码（密码也是一种资源）的更改操作，我们可以为账户余额和账户密码分配不同的锁来解决并发问题，这个还是很简单的。
    // 相关的示例代码如下，账户类 Account 有两个成员变量，分别是账户余额 balance 和账户密码 password。取款 withdraw() 和查看余额 getBalance() 操作会访问账户余额 balance，我们创建一个 final 对象 balLock 作为锁（类比球赛门票）；而更改密码 updatePassword() 和查看密码 getPassword() 操作会修改账户密码 password，我们创建一个 final 对象 pwLock 作为锁（类比电影票）。不同的资源用不同的锁保护，各自管各自的，很简单。
    // 见 class Account

    // 当然，我们也可以用一把互斥锁来保护多个资源，例如我们可以用 this 这一把锁来管理账户类里所有的资源：账户余额和用户密码。具体实现很简单，示例程序中所有的方法都增加同步关键字 synchronized 就可以了
    // 但是用一把锁有个问题，就是性能太差，会导致取款、查看余额、修改密码、查看密码这四个操作都是串行的。而我们用两把锁，取款和修改密码是可以并行的。用不同的锁对受保护资源进行精细化管理，能够提升性能。这种锁还有个名字，叫细粒度锁。

    // 二、保护有关联关系的多个资源
    // 如果多个资源是有关联关系的，那这个问题就有点复杂了。例如银行业务里面的转账操作，账户 A 减少 100 元，账户 B 增加 100 元。这两个账户就是有关联关系的。那对于像转账这种有关联关系的操作，我们应该怎么去解决呢？先把这个问题代码化。我们声明了个账户类：Account，该类有一个成员变量余额：balance，还有一个用于转账的方法：transfer()，然后怎么保证转账操作 transfer() 没有并发问题呢？
    // 见 class Account
    // 用同一把锁来保护多个资源，也就是现实世界的“包场”，那在编程领域应该怎么“包场”呢？
    // 很简单，只要我们的锁能覆盖所有受保护资源就可以了。在上面的例子中，this 是对象级别的锁，所以 A 对象和 B 对象都有自己的锁，如何让 A 对象和 B 对象共享一把锁呢？
    // 用 Account.class 作为共享的锁。Account.class 是所有 Account 对象共享的，而且这个对象是 Java 虚拟机在加载 Account 类的时候创建的，所以我们不用担心它的唯一性。使用 Account.class 作为共享的锁，我们就无需在创建 Account 对象时传入了，代码更简单。

    // 如果资源之间没有关系，很好处理，每个资源一把锁就可以了。如果资源之间有关联关系，就要选择一个粒度更大的锁，这个锁应该能够覆盖所有相关的资源。除此之外，还要梳理出有哪些访问路径，所有的访问路径都要设置合适的锁，这个过程可以类比一下门票管理。
    // 我们再引申一下上面提到的关联关系，关联关系如果用更具体、更专业的语言来描述的话，其实是一种“原子性”特征，在前面的文章中，我们提到的原子性，主要是面向 CPU 指令的，转账操作的原子性则是属于是面向高级语言的，不过它们本质上是一样的。

    // “原子性”的本质是什么？其实不是不可分割，不可分割只是外在表现，其本质是多个资源间有一致性的要求，操作的中间状态对外不可见。
    // 例如，在 32 位的机器上写 long 型变量有中间状态（只写了 64 位中的 32 位），在银行转账的操作中也有中间状态（账户 A 减少了 100，账户 B 还没来得及发生变化）。所以解决原子性问题，是要保证中间状态对外不可见。

}

class Account {
    // 锁：保护账户余额
    private final Object balLock = new Object();
    // 账户余额
    private Integer balance;
    // 锁：保护账户密码
    private final Object pwLock = new Object();
    // 帐户密码
    private String password;

    // 转账
    void transfer(Account target, int amount) {
        // 此处检查所有对象共享的锁
        synchronized (Account.class) {
            if (this.balance > amount) {
                this.balance -= amount;
                target.balance += amount;
            }
        }
    }

    // 取款
    void withdraw(Integer amount) {
        synchronized (balLock) {
            if (this.balance > amount) {
                this.balance -= amount;
            }
        }
    }

    // 查看余额
    Integer getBalance() {
        synchronized (balLock) {
            return balance;
        }
    }

    // 更改密码
    void updatePassword(String password) {
        synchronized (pwLock) {
            this.password = password;
        }
    }

    // 查看密码
    String getPassword() {
        synchronized (pwLock) {
            return password;
        }
    }

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

