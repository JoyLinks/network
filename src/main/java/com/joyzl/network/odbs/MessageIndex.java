package com.joyzl.network.odbs;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

/**
 * 消息流标识与缓存，索引存取，环形数组模式实现，索引从0开始，到达最大值时翻转（归零）；<br>
 * 客户端发起消息使用奇数索引标识，服务端发起消息使用偶数索引标识，以区分消息；<br>
 * 客户端和服务端应保持相同的最大消息队列数量，当消息标识出现跳跃时可保持一致；<br>
 * 客户端始终只能发起奇数标识的消息，可能会缓存服务端发起的偶数消息；<br>
 * 服务端始终只会接收客户端发起的奇数消息，且以相同的消息标识回复；<br>
 * 服务端发起的偶数消息无须在服务端通过标识缓存，应直接进入流发送；<br>
 * 服务端广播消息在每个连接获取特定的偶数标识。
 * <p>
 * 消息标识跳跃出现在环形数组中的消息未能按添加顺序释放的情形。<br>
 * 为了避免额外的移动开销，流处理会跳过被占用的空间对应的标识。
 * </p>
 * <p>
 * 如果消息包含资源（文件或输入流）且实现了Closeable接口，清空流时将自动关闭。
 * </p>
 * <p>
 * {@link MessageStream}对象用于流式发送，{@link MessageIndex}用于流式接收。
 * </p>
 * 
 * @author ZhangXi 2025年4月11日
 */
public class MessageIndex<M> implements Iterator<M>, Iterable<M> {

	final static int MAX = Integer.MAX_VALUE / 2;
	private final M[] elements;
	private int capacity, size, index = 0;

	public MessageIndex() {
		this(128);
	}

	@SuppressWarnings("unchecked")
	public MessageIndex(int capacity) {
		elements = (M[]) new Object[capacity];
		this.capacity = capacity;
	}

	public M get(int i) {
		if (i < 0) {
			throw new IndexOutOfBoundsException(i);
		}
		i = i % elements.length;
		return elements[i];
	}

	public void add(M m, int i) {
		if (i > index) {
			index = i;
		}
		i = i % elements.length;
		if (elements[i] != null) {
			throw new IllegalStateException("EXISTS");
		}
		elements[i] = m;
		size++;
	}

	public int add(M m) {
		if (size == capacity) {
			throw new IllegalStateException("FULL");
		}
		while (elements[index % elements.length] != null) {
			// 略过之前存储且还未释放的位置
			// 此时索引标记将被跳过
			// 超过最大值将翻转
			if (++index == MAX) {
				index = 0;
			}
		}
		size++;
		elements[index % elements.length] = m;
		if (index == MAX) {
			index = 0;
			return MAX;
		} else {
			return index++;
		}
	}

	public M remove(int i) {
		if (i < 0) {
			throw new IndexOutOfBoundsException(i);
		}
		i = i % elements.length;
		final M m = elements[i];
		elements[i] = null;
		size--;
		return m;
	}

	public void remove(M m) {
		for (int i = 0; i < elements.length; i++) {
			if (elements[i] == m) {
				elements[i] = null;
				size--;
				break;
			}
		}
	}

	public void clear() throws IOException {
		size = 0;
		for (int i = 0; i < elements.length; i++) {
			if (elements[i] != null) {
				if (elements[i] instanceof Closeable) {
					((Closeable) elements[i]).close();
				}
				elements[i] = null;
			}
		}
	}

	/**
	 * 是否还有消息
	 */
	public boolean isEmpty() {
		return size == 0;
	}

	/**
	 * 容量，可并行消息数量
	 */
	public int capacity() {
		return capacity;
	}

	/**
	 * 消息数量
	 */
	public int size() {
		return size;
	}

	////////////////////////////////////////////////////////////////////////////////
	// 消息遍历支持，不会创建中间对象，不保证遍历顺序与添加顺序相同

	private int i;

	@Override
	public Iterator<M> iterator() {
		i = 0;
		return this;
	}

	@Override
	public boolean hasNext() {
		while (i < elements.length) {
			if (elements[i] == null) {
				i++;
			} else {
				return true;
			}
		}
		return false;
	}

	@Override
	public M next() {
		return elements[i++];
	}

	@Override
	public void remove() {
		elements[i - 1] = null;
		size--;
	}

	////////////////////////////////////////////////////////////////////////////////

	/** 奇数流编号转换为存储索引 */
	static int oddIndex(int odd) {
		// 奇数转换为索引 (odd-1)/2=index
		return (odd - 1) / 2;
	}

	/** 存储索引转换为奇数流编号 */
	static int indexOdd(int index) {
		return index * 2 + 1;
	}

	/** 偶数流编号转换为存储索引 */
	static int evenIndex(int even) {
		// 偶数转换为索引 (even-2)/2=index
		return (even - 2) / 2;
	}

	/** 存储索引转换为偶数流编号 */
	static int indexEven(int index) {
		return index * 2 + 2;
	}

	static class MessageOddIndex<M> extends MessageIndex<M> {

		public M get(int i) {
			return super.get(oddIndex(i));
		}

		public void add(M m, int i) {
			super.add(m, oddIndex(i));
		}

		public int add(M m) {
			return indexOdd(super.add(m));
		}

		public M remove(int i) {
			return super.remove(oddIndex(i));
		}
	}

	static class MessageEvenIndex<M> extends MessageIndex<M> {

		public M get(int i) {
			return super.get(evenIndex(i));
		}

		public void add(M m, int i) {
			super.add(m, evenIndex(i));
		}

		public int add(M m) {
			return indexEven(super.add(m));
		}

		public M remove(int i) {
			return super.remove(evenIndex(i));
		}
	}
}