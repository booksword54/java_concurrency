import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.*;

public class LockDemo {
    public static void main(String[] args) {
        // 对于Java 来讲，锁 （ Lock ) 是一种比标准同步块 （ synchronized block ） 更灵活，更复杂的线程同步机制。
        // 其实，Java 1.5 就已经存在 Lock 接口了。这个 Lock 接口在 java.util.concurrent.lock 包中定义，提供了大量的锁操作。
        // 本文中，我们将讲解 Lock 接口的不同实现并介绍如何在应用程序中使用锁。

        // 一、锁 ( lock ) 和同步块 ( synchronized block ) 之间的差异
        // 使用synchronized 块和使用 Lock API 之间几乎没有区别：
        // - 同步块完全包含在方法中 : 在独立的方法中，我们可以使用 Lock 提供的 lock() 和 unlock() 实现锁和解锁操作。
        // - 同步块不支持公平竞争，任何线程都可以获取释放的锁定，且不能指定优先级。但锁 ( Lock ) 就不一样了，可以通过指定公平属性来实现 Lock 中的公平性。这可以确保最长的等待线程被授予锁定权限。
        // - 如果线程无法访问同步块，则会阻塞该线程。Lock 则提供了 tryLock() 方法。线程只有在可用且不被任何其他线程保持时才获取锁定。这减少了线程等待锁定的阻塞时间。
        // - 处于 「 等待 」 状态以获取对同步块的访问的线程不能被中断。Lock 提供了一个 lockInterruptibly() 方法，可用于在等待锁定时中断线程。

        // 从上面的对比来看，同步块的所有机制，锁 ( Lock ) 都有相应的 API 对应。
        // 二、Lock API
        // 方法	说明
        // void lock()	尝试获取锁（如果可用），如果锁不可用，则线程会被阻塞，直到锁被释放
        // oid lockInterruptibly()	类似于 lock()，但它允许被阻塞的线程被中断并通过抛出的 java.lang.InterruptedException 恢复执行
        // boolean tryLock()	lock() 方法的非阻塞版本，它会立即尝试获取锁定，如果锁定成功则返回 true
        // boolean tryLock(long timeout, TimeUnit timeUnit)	类似于 tryLock()，但它可以指定超时，达到超时之后就会自动放弃获取锁
        // void unlock()	解锁 Lock 实例

        // 锁定的实例应该始终被解锁以避免死锁情况。
        // 锁的推荐使用方式是将锁相关的代码块放在 try/catch 和 finally 块中。
        Lock lock = new ReentrantLock();
        lock.lock();
        try {
            // access to shared resource
        } finally {
            lock.unlock();
        }

        // 除了Lock 接口之外，java.util.concurrent.lock 包还提供了一个 ReadWriteLock 接口，俗称 「读写锁」，它维护一对锁，一个用于只读操作，一个用于写操作。
        // 对于读写锁，只要没有写入，读锁定可以由多个线程同时保持。
        // ReadWriteLock 声明了两个方法用于获取读取或写入锁
        // 方法	说明
        // Lock readLock()	返回一个用于读取的锁
        // Lock writeLock()	返回一个用于写的锁

        // 三、锁的实现
        // 1、ReentrantLock 锁
        // ReentrantLock 类实现了 Lock 接口。它提供了相同的并发和内存语义，如使用 synchronized 方法和语句访问的隐式监视器锁，而且可以被子类化。
        // 我们写一个范例演示下如何使用 ReenrtantLock 来实现同步
        // 见 class SharedObject
        // 正如上面的示例所示，我们需要确保在 try-finally 块中包装 lock() 和 unlock() 调用以避免死锁情况。

        // 现在，让我们来看看 tryLock() 的工作原理
        // 见 void performTryLock()
        // 上面这个范例中，调用 tryLock() 的线程将等待一秒钟，如果锁定不可用则放弃等待。

        // 2、ReentrantReadWriteLock
        // ReentrantReadWriteLock 类实现了 ReadWriteLock 接口。
        // 我们来看一下线程获取 ReadLock 或 WriteLock 的规则：
        // - 读锁 : 如果没有线程获得写锁定或请求它，则多个线程可以获取读锁定。
        // - 写锁 : 如果没有线程正在读或写，则只有一个线程可以获取写锁。

        // 我们写一个范例演示下如何使用 ReadWriteLock
        // 见 class SynchronizedHashMapWithReadWriteLock

        // 3、StampedLock
        // StampedLock 是 Java 8 中引入的。它支持读写锁定。
        // 不同的是，锁的获取方法返回的戳记 （ stamp ） 可以用于释放锁定或检查锁定是否仍然有效。

        // 4、Condition
        // Condition 类让线程能够在执行临界区时等待某些条件发生。当线程获得对临界区的访问但没有执行其操作的必要条件时，可能会发生这种情况。
        // 例如，读线程可以访问共享队列的锁，该队列仍然没有任何数据可供使用。
        // 传统上，Java 为线程互通提供了 wait()、notify() 和 notifyAll() 方法。
        // Condition 类有类似的机制，而且，还允许我们指定多个条件。
        // 见 class ReentrantLockWithCondition
    }


}

class ReentrantLockWithCondition {

    Stack<String> stack = new Stack<>();
    int CAPACITY = 5;

    ReentrantLock lock = new ReentrantLock();
    Condition stackEmptyCondition = lock.newCondition();
    Condition stackFullCondition = lock.newCondition();

    public void pushToStack(String item) throws InterruptedException {
        try {
            lock.lock();
            while (stack.size() == CAPACITY) {
                stackFullCondition.await();
            }
            stack.push(item);
            stackEmptyCondition.signalAll();
        }  finally {
            lock.unlock();
        }
    }

    public String popFromStack() throws InterruptedException {
        try {
            lock.lock();
            while (stack.size() == 0) {
                stackEmptyCondition.await();
            }
            String pop = stack.pop();
            stackFullCondition.signalAll();
            return pop;
        } finally {
            lock.unlock();
        }
    }
}

class StampedLockDemo {
    Map<String, String> map = new HashMap<>();

    private StampedLock lock = new StampedLock();

    public void put(String key, String value) {
        long stamp = lock.writeLock();
        try {
            map.put(key, value);
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    public String get(String key) {
        long stamp = lock.readLock();
        try {
            return map.get(key);
        } finally {
            lock.unlockRead(stamp);
        }
    }

    public String readWithOptimisticLock(String key) {
        long stamp = lock.tryOptimisticRead();
        if (!lock.validate(stamp)) {
            stamp = lock.readLock();
            try {
                return map.get(key);
            } finally {
                lock.unlock(stamp);
            }
        }
        return map.get(key);
    }
}

class SynchronizedHashMapWithReadWriteLock {

    Map<String, String> syncHashMap = new HashMap<>();
    ReadWriteLock lock = new ReentrantReadWriteLock();
    Lock writeLock = lock.writeLock();
    Lock readLock = lock.readLock();

    public void put(String key, String value) {
        try {
            writeLock.lock();
            syncHashMap.put(key, value);
        } finally {
            writeLock.unlock();
        }
    }

    public String remove(String key) {
        try {
            writeLock.lock();
            return syncHashMap.remove(key);
        } finally {
            writeLock.unlock();
        }
    }

    public String get(String key) {
        try {
            readLock.lock();
            return syncHashMap.get(key);
        } finally {
            readLock.unlock();
        }
    }

    public boolean containsKey(String key) {
        try {
            readLock.lock();
            return syncHashMap.containsKey(key);
        } finally {
            readLock.unlock();
        }
    }

}

class SharedObject {
    ReentrantLock lock = new ReentrantLock();
    int counter = 0;

    public void perform() {
        lock.lock();
        try {
            // Critical Section Here
            counter++;
        } finally {
            lock.unlock();
        }
    }

    public void performTryLock() throws InterruptedException {
        boolean isLockAcquired = lock.tryLock(1, TimeUnit.SECONDS);
        if (isLockAcquired) {
            try {
                // Critical Section Here
            } finally {
                lock.unlock();
            }
        }
    }
}
