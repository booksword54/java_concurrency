import java.util.ArrayList;
import java.util.List;

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


    // 死锁了，怎么办？
    // 在上一篇文章中，我们用 Account.class 作为互斥锁，来解决银行业务里面的转账问题，虽然这个方案不存在并发问题，但是所有账户的转账操作都是串行的，例如账户 A 转账户 B、账户 C 转账户 D 这两个转账操作现实世界里是可以并行的，但是在这个方案里却被串行化了，这样的话，性能太差。
    // 试想互联网支付盛行的当下，8 亿网民每人每天一笔交易，每天就是 8 亿笔交易；每笔交易都对应着一次转账操作，8 亿笔交易就是 8 亿次转账操作，也就是说平均到每秒就是近 1 万次转账操作，若所有的转账操作都串行，性能完全不能接受。
    // 那下面我们就尝试着把性能提升一下。
    // 现实世界里，账户转账操作是支持并发的，而且绝对是真正的并行，银行所有的窗口都可以做转账操作。只要我们能仿照现实世界做转账操作，串行的问题就解决了。
    // 我们试想在古代，没有信息化，账户的存在形式真的就是一个账本，而且每个账户都有一个账本，这些账本都统一存放在文件架上。银行柜员在给我们做转账时，要去文件架上把转出账本和转入账本都拿到手，然后做转账。这个柜员在拿账本的时候可能遇到以下三种情况：
    // 1、 文件架上恰好有转出账本和转入账本，那就同时拿走；
    // 2、 如果文件架上只有转出账本和转入账本之一，那这个柜员就先把文件架上有的账本拿到手，同时等着其他柜员把另外一个账本送回来；
    // 3、 转出账本和转入账本都没有，那这个柜员就等着两个账本都被送回来；
    // 上面这个过程在编程的世界里怎么实现呢？其实用两把锁就实现了，转出账本一把，转入账本另一把。在 transfer() 方法内部，我们首先尝试锁定转出账户 this（先把转出账本拿到手），然后尝试锁定转入账户 target（再把转入账本拿到手），只有当两者都成功时，才执行转账操作。这个逻辑可以图形化为下图这个样子。
    // 而至于详细的代码实现，如下所示。经过这样的优化后，账户 A 转账户 B 和账户 C 转账户 D 这两个转账操作就可以并行了。
    // 见 Account。transferWithDeadLock

    // 上面的实现看上去很完美，并且也算是将锁用得出神入化了。相对于用 Account.class 作为互斥锁，锁定的范围太大，而我们锁定两个账户范围就小多了，这样的锁，上一章我们介绍过，叫细粒度锁。使用细粒度锁可以提高并行度，是性能优化的一个重要手段。
    // 的确，使用细粒度锁是有代价的，这个代价就是可能会导致死锁。
    // 现实世界里的死等，就是编程领域的死锁了。死锁的一个比较专业的定义是：一组互相竞争资源的线程因互相等待，导致“永久”阻塞的现象。
    // 上面转账的代码是怎么发生死锁的呢？我们假设线程 T1 执行账户 A 转账户 B 的操作，账户 A.transfer(账户 B)；同时线程 T2 执行账户 B 转账户 A 的操作，账户 B.transfer(账户 A)。当 T1 和 T2 同时执行完①处的代码时，T1 获得了账户 A 的锁（对于 T1，this 是账户 A），而 T2 获得了账户 B 的锁（对于 T2，this 是账户 B）。之后 T1 和 T2 在执行②处的代码时，T1 试图获取账户 B 的锁时，发现账户 B 已经被锁定（被 T2 锁定），所以 T1 开始等待；T2 则试图获取账户 A 的锁时，发现账户 A 已经被锁定（被 T1 锁定），所以 T2 也开始等待。于是 T1 和 T2 会无期限地等待下去，也就是我们所说的死锁了。

    // 如何预防死锁
    //并发程序一旦死锁，一般没有特别好的方法，很多时候我们只能重启应用。因此，解决死锁问题最好的办法还是规避死锁。
    // 那如何避免死锁呢？要避免死锁就需要分析死锁发生的条件，有个叫 Coffman 的牛人早就总结过了，只有以下这四个条件都发生时才会出现死锁：
    // 1、 互斥，共享资源X和Y只能被一个线程占用；
    // 2、 占有且等待，线程T1已经取得共享资源X，在等待共享资源Y的时候，不释放共享资源X；
    // 3、 不可抢占，其他线程不能强行抢占线程T1占有的资源；
    // 4、 循环等待，线程T1等待线程T2占有的资源，线程T2等待线程T1占有的资源，就是循环等待；
    // 反过来分析，也就是说只要我们破坏其中一个，就可以成功避免死锁的发生。

    // 其中，互斥这个条件我们没有办法破坏，因为我们用锁为的就是互斥。不过其他三个条件都是有办法破坏掉的，到底如何做呢？
    // 1、 对于“占用且等待”这个条件，我们可以一次性申请所有的资源，这样就不存在等待了；
    // 2、 对于“不可抢占”这个条件，占用部分资源的线程进一步申请其他资源时，如果申请不到，可以主动释放它占有的资源，这样不可抢占这个条件就破坏掉了；
    // 3、 对于“循环等待”这个条件，可以靠按序申请资源来预防所谓按序申请，是指资源是有线性顺序的，申请的时候可以先申请资源序号小的，再申请资源序号大的，这样线性化后自然就不存在循环了；
    // 我们已经从理论上解决了如何预防死锁，那具体如何体现在代码上呢？下面我们就来尝试用代码实践一下这些理论。

    // 1. 破坏占用且等待条件
    // 对应到编程领域，“同时申请”这个操作是一个临界区，我们也需要一个角色（Java 里面的类）来管理这个临界区，我们就把这个角色定为 Allocator。它有两个重要功能，分别是：同时申请资源 apply() 和同时释放资源 free()。账户 Account 类里面持有一个 Allocator 的单例（必须是单例，只能由一个人来分配资源）。当账户 Account 在执行转账操作的时候，首先向 Allocator 同时申请转出账户和转入账户这两个资源，成功后再锁定这两个资源；当转账操作执行完，释放锁之后，我们需通知 Allocator 同时释放转出账户和转入账户这两个资源。具体的代码实现如下。
    // 见 class Allocator

    // 2. 破坏不可抢占条件
    // 破坏不可抢占条件看上去很简单，核心是要能够主动释放它占有的资源，这一点 synchronized 是做不到的。原因是 synchronized 申请资源的时候，如果申请不到，线程直接进入阻塞状态了，而线程进入阻塞状态，啥都干不了，也释放不了线程已经占有的资源。
    // Java 在语言层次确实没有解决这个问题，不过在 SDK 层面还是解决了的，java.util.concurrent 这个包下面提供的 Lock 是可以轻松解决这个问题的。关于这个话题，咱们后面会详细讲。

    // 3. 破坏循环等待条件
    // 破坏这个条件，需要对资源进行排序，然后按序申请资源。这个实现非常简单，我们假设每个账户都有不同的属性 id，这个 id 可以作为排序字段，申请的时候，我们可以按照从小到大的顺序来申请。比如下面代码中，①~⑥处的代码对转出账户（this）和转入账户（target）排序，然后按照序号从小到大的顺序锁定账户。这样就不存在“循环”等待了。

    // 当我们在编程世界里遇到问题时，应不局限于当下，可以换个思路，向现实世界要答案，利用现实世界的模型来构思解决方案，这样往往能够让我们的方案更容易理解，也更能够看清楚问题的本质。
    // 我们今天这一篇文章主要讲了用细粒度锁来锁定多个资源时，要注意死锁的问题。这个就需要你能把它强化为一个思维定势，遇到这种场景，马上想到可能存在死锁问题。当你知道风险之后，才有机会谈如何预防和避免，因此，识别出风险很重要。
    // 预防死锁主要是破坏三个条件中的一个，有了这个思路后，实现就简单了。但仍需注意的是，有时候预防死锁成本也是很高的。例如上面转账那个例子，我们破坏占用且等待条件的成本就比破坏循环等待条件的成本高，破坏占用且等待条件，我们也是锁了所有的账户，而且还是用了死循环 while(!actr.apply(this, target));方法，不过好在 apply() 这个方法基本不耗时。 在转账这个例子中，破坏循环等待条件就是成本最低的一个方案。
    // 所以我们在选择具体方案的时候，还需要评估一下操作成本，从中选择一个成本最低的方案。
}

// 破坏占用且等待条件
class Allocator {

    private List<Object> als = new ArrayList<>();

    // 一次性申请所有资源
    synchronized boolean apply(Object from, Object to) {
        if (als.contains(from) || als.contains(to)) {
            return false;
        }
        als.add(from);
        als.add(to);
        return true;
    }

    // 归还资源
    synchronized void free(Object from, Object to) {
        als.remove(from);
        als.remove(to);
    }
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


    // 转账 破坏循环等待条件
    private int id;

    void transferWithId(Account target, int amount) {
        Account left = this;
        Account right = target;
        if (this.id > target.id) {
            // 按照序号从小到大的顺序锁定账户。就不存在“循环”等待了。
            left = target;
            right = this;
        }
        // 锁定序号小的账户
        synchronized (left) {
            // 锁定序号大的账户
            synchronized (right) {
                if (this.balance > amount) {
                    this.balance -= amount;
                    target.balance += amount;
                }
            }
        }
    }

    // 转账 allocator 破坏占用且等待条件
    // allocator 是单例
    private static Allocator allocator = new Allocator();

    void transferAllocator(Account target, int amount) {
        // 一次性申请转出账户和转入账户，直到成功
        while (!allocator.apply(this, target)) ;
        try {
            // 锁定转出账户
            synchronized (this) {
                // 锁定转入账户
                synchronized (target) {
                    if (this.balance > amount) {
                        this.balance -= amount;
                        target.balance += amount;
                    }
                }
            }
        } finally {
            allocator.free(this, target);
        }
    }

    // 死锁转账
    void transferWithDeadLock(Account target, int amt) {
        // 锁定转出账户
        synchronized (this) {
            // 锁定转入账户
            synchronized (target) {
                if (this.balance > amt) {
                    this.balance -= amt;
                    target.balance += amt;
                }
            }
        }
    }

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

