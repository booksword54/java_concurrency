import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

public class BlockingQueueDemo {
    public static void main(String[] args) {
        // java.util.concurrent 包提供的用于解决并发生产者 – 消费者问题的最有用的类 – BlockQueue。我们将介绍BlockingQueue 接口的 API 以及如何使用该接口的方法使编写并发程序更容易。
        // 一、BlockingQueue 的队列类型
        // java.util.concurrent 提供了两种类型的 BlockingQueue：
        // 1、 无限队列（unboundedQueue）–几乎可以无限增长；
        // 2、 有限队列（boundedQueue）–定义了最大容量；

        // 二、无限队列
        LinkedBlockingQueue<Object> unboundedQueue = new LinkedBlockingQueue<>();
        // 上面这段代码中，blockingQueue 的容量将设置为 Integer.MAX_VALUE 。
        // 向无限队列添加元素的所有操作都将永远不会阻塞，因此它可以增长到非常大的容量。
        // 使用无限 BlockingQueue 设计生产者 – 消费者模型时最重要的是 消费者应该能够像生产者向队列添加消息一样快地消费消息 。否则，内存可能会填满，然后就会得到一个 OutOfMemory 异常。

        // 二、有限队列
        // 第二种类型的队列是有限队列。我们可以通过将容量作为参数传递给构造函数来创建这样的队列
        LinkedBlockingQueue<Object> boundedQueue = new LinkedBlockingQueue<>(10);
        // 上面这句代码中，我们设置了 blockingQueue 的容量为 10 。这意味着当消费者尝试将元素添加到已经满了的队列时，结果取决于添加元素的方法（ offer() 、add() 、put() ) ，它将阻塞，直到有足够的空间可以插入元素。否则，添加操作将会失败。
        // 使用有限队列是设计并发程序的好方法，因为当我们将元素插入到已经满了的队列时，这些操作需要等到消费者赶上并在队列中提供一些空间。这种机制可以让那个我们不做任何其它更改就可以实现节流。

        // 三、BlockingQueue API
        // BlockingQueue 接口的所有方法可以分为两大类：负责向队列添加元素的方法和检索这些元素的方法。
        // 在队列满/空的情况下，来自这两个组的每个方法的行为都不同。

        // 1、添加元素
        // 方法	    说明
        // add()	如果插入成功则返回 true，否则抛出 IllegalStateException 异常
        // put()	将指定的元素插入队列，如果队列满了，那么会阻塞直到有空间插入
        // offer()	如果插入成功则返回 true，否则返回 false
        // offer(E e, long timeout, TimeUnit unit)	尝试将元素插入队列，如果队列已满，那么会阻塞直到有空间插入
        // 2、检索元素
        // 方法	说明
        // take()	获取队列的头部元素并将其删除，如果队列为空，则阻塞并等待元素变为可用
        // poll(long timeout, TimeUnit unit)	检索并删除队列的头部，如有必要，等待指定的等待时间以使元素可用，如果超时，则返回 null
        // 在构建生产者 – 消费者程序时，这些方法是 BlockingQueue 接口中最重要的构建块。

        // 四、多线程生产者 – 消费者示例
        // 接下来我们创建一个由两部分组成的程序 – 生产者 ( Producer ) 和消费者 ( Consumer ) 。
        // 生产者将生成一个 0 到 100 的随机数，并将该数字放在 BlockingQueue 中。我们将创建 4 个线程用于生成随机数并使用 put() 方法阻塞，直到队列中有可用空间。
        // 需要记住的重要一点是，我们需要阻止我们的消费者线程无限期地等待元素出现在队列中。
        // 从生产者向消费者发出信号的好方法是，不需要处理消息，而是发送称为毒 （ poison ） 丸 （ pill ） 的特殊消息。 我们需要发送尽可能多的毒 （ poison ） 丸 （ pill ） ，因为我们有消费者。然后当消费者从队列中获取特殊的毒 （ poison ） 丸 （ pill ）消息时，它将优雅地完成执行。
        // 见 class NumbersProducer

        // 我们的生成器构造函数将 BlockingQueue 作为参数，用于协调生产者和使用者之间的处理。我们看到方法 generateNumbers() 将 100 个元素放入队列中。它还需要有毒 （ poison ） 丸 （ pill ） 消息，以便知道在执行完成时放入队列的消息类型。该消息需要将 poisonPillPerProducer 次放入队列中。
        // 每个消费者将使用 take() 方法从 BlockingQueue 获取一个元素，因此它将阻塞，直到队列中有一个元素。从队列中取出一个 Integer 后，它会检查该消息是否是毒 （ poison ） 丸 （ pill ） ，如果是，则完成一个线程的执行。否则，它将在标准输出上打印出结果以及当前线程的名称。
        // 这将使我们深入了解消费者的内部运作机制
        // 见 class NumbersConsumer

        // 需要注意的重要事项是队列的使用。与生成器构造函数中的相同，队列作为参数传递。我们可以这样做，是因为 BlockingQueue 可以在线程之间共享而无需任何显式同步。
        // 既然我们有生产者和消费者，我们就可以开始我们的计划。我们需要定义队列的容量，并将其设置为 100 个元素。
        // 我们希望有 4 个生产者线程，并且有许多消费者线程将等于可用处理器的数量
        int BOUND = 10;
        int N_PRODUCERS = 4;
        int N_CONSUMERS = Runtime.getRuntime().availableProcessors();
        int poisonPill = Integer.MAX_VALUE;
        int poisonPillProducer = N_CONSUMERS / N_PRODUCERS;
        int mod = N_CONSUMERS % N_PRODUCERS;

        BlockingQueue<Integer> blockingQueue = new LinkedBlockingQueue<>(BOUND);

        new Thread(new NumbersProducer(blockingQueue, poisonPill, poisonPillProducer + mod)).start();
        for (int i = 1; i < N_PRODUCERS; i++) {
            new Thread(new NumbersProducer(blockingQueue, poisonPill, poisonPillProducer)).start();
        }

        for (int j = 0; j < N_CONSUMERS; j++) {
            new Thread(new NumbersConsumer(blockingQueue, poisonPill)).start();
        }
        // BlockingQueue 是使用具有容量的构造创建的。我们正在创造 4 个生产者和 N 个消费者。我们将我们的毒 （ poison ） 丸 （ pill ）消息指定为 Integer.MAX_VALUE，因为我们的生产者在正常工作条件下永远不会发送这样的值。这里要注意的最重要的事情是 BlockingQueue 用于协调它们之间的工作。
        // 当我们运行程序时，4 个生产者线程将随机整数放入 BlockingQueue 中，消费者将从队列中获取这些元素。每个线程将打印到标准输出线程的名称和结果。

    }
}

class NumbersProducer implements Runnable {

    private BlockingQueue<Integer> queue;

    private final int poisonPill;

    private final int poisonPillPerProducer;

    public NumbersProducer(BlockingQueue<Integer> queue, int poisonPill, int poisonPillPerProducer) {
        this.queue = queue;
        this.poisonPill = poisonPill;
        this.poisonPillPerProducer = poisonPillPerProducer;
    }

    @Override
    public void run() {
        try {
            generateNumbers();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void generateNumbers() throws InterruptedException {
        for (int i = 0; i < 100; i++) {
            queue.put(ThreadLocalRandom.current().nextInt(100));
        }
        for (int j = 0; j < poisonPillPerProducer; j++) {
            queue.put(poisonPill);
        }
    }
}

class NumbersConsumer  implements Runnable {

    private BlockingQueue<Integer> queue;

    private final int poisonPill;

    public NumbersConsumer(BlockingQueue<Integer> queue, int poisonPill) {
        this.queue = queue;
        this.poisonPill = poisonPill;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Integer number = queue.take();
                if (number.equals(poisonPill)) {
                    return;
                }
                System.out.println(Thread.currentThread().getName() + " result: " + number);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}