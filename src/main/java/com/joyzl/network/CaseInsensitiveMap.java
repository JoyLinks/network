/*
 * www.joyzl.net
 * 重庆骄智科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network;

import java.io.Serializable;
import java.util.Map;

/**
 * A case-insensitive <code>Map</code>.
 * <p>
 * Before keys are added to the map or compared to other existing keys, they are
 * converted to all lowercase in a locale-independent fashion by using
 * information from the Unicode data file.
 * </p>
 * <p>
 * Null keys are supported.
 * </p>
 * <p>
 * The <code>keySet()</code> method returns all lowercase keys, or nulls.
 * </p>
 * <p>
 * Example:
 * </p>
 * 
 * <pre>
 * <code>
 *  Map&lt;String, String&gt; map = new CaseInsensitiveMap&lt;String, String&gt;();
 *  map.put("One", "One");
 *  map.put("Two", "Two");
 *  map.put(null, "Three");
 *  map.put("one", "Four");
 * </code>
 * </pre>
 * <p>
 * The example above creates a <code>CaseInsensitiveMap</code> with three
 * entries.
 * </p>
 * <p>
 * <code>map.get(null)</code> returns <code>"Three"</code> and
 * <code>map.get("ONE")</code> returns <code>"Four".</code> The <code>Set</code>
 * returned by <code>keySet()</code> equals <code>{"one", "two", null}.</code>
 * </p>
 * <p>
 * <strong>This map will violate the detail of various Map and map view
 * contracts.</strong> As a general rule, don't compare this map to other maps.
 * In particular, you can't use decorators like {@link ListOrderedMap} on it,
 * which silently assume that these contracts are fulfilled.
 * </p>
 * <p>
 * <strong>Note that CaseInsensitiveMap is not synchronized and is not
 * thread-safe.</strong> If you wish to use this map from multiple threads
 * concurrently, you must use appropriate synchronization. The simplest approach
 * is to wrap this map using {@link java.util.Collections#synchronizedMap(Map)}.
 * This class may throw exceptions when accessed by concurrent threads without
 * synchronization.
 * </p>
 *
 * @param <K> the type of the keys in this map
 * @param <V> the type of the values in this map
 * @since 3.0
 */
public class CaseInsensitiveMap<K, V> extends AbstractHashedMap<K, V> implements Serializable, Cloneable {

	/** Serialisation version */
	private static final long serialVersionUID = -7074655917369299456L;

	/**
	 * Constructs a new empty map with default size and load factor.
	 */
	public CaseInsensitiveMap() {
		super(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_THRESHOLD);
	}

	/**
	 * Constructs a new, empty map with the specified initial capacity.
	 *
	 * @param initialCapacity the initial capacity
	 * @throws IllegalArgumentException if the initial capacity is negative
	 */
	public CaseInsensitiveMap(final int initialCapacity) {
		super(initialCapacity);
	}

	/**
	 * Constructs a new, empty map with the specified initial capacity and load
	 * factor.
	 *
	 * @param initialCapacity the initial capacity
	 * @param loadFactor the load factor
	 * @throws IllegalArgumentException if the initial capacity is negative
	 * @throws IllegalArgumentException if the load factor is less than zero
	 */
	public CaseInsensitiveMap(final int initialCapacity, final float loadFactor) {
		super(initialCapacity, loadFactor);
	}

	/**
	 * Constructor copying elements from another map.
	 * <p>
	 * Keys will be converted to lower case strings, which may cause some
	 * entries to be removed (if string representation of keys differ only by
	 * character case).
	 *
	 * @param map the map to copy
	 * @throws NullPointerException if the map is null
	 */
	public CaseInsensitiveMap(final Map<? extends K, ? extends V> map) {
		super(map);
	}

	// -----------------------------------------------------------------------
	/**
	 * Overrides convertKey() from {@link AbstractHashedMap} to convert keys to
	 * lower case.
	 * <p>
	 * Returns {@link AbstractHashedMap#NULL} if key is null.
	 *
	 * @param key the key convert
	 * @return the converted key
	 */
	@Override
	protected Object convertKey(final Object key) {
		if (key != null) {
			final char[] chars = key.toString().toCharArray();
			for (int i = chars.length - 1; i >= 0; i--) {
				chars[i] = Character.toLowerCase(Character.toUpperCase(chars[i]));
			}
			return new String(chars);
		}
		return AbstractHashedMap.NULL;
	}

	// -----------------------------------------------------------------------
	/**
	 * Clones the map without cloning the keys or values.
	 *
	 * @return a shallow clone
	 */
	@Override
	public CaseInsensitiveMap<K, V> clone() {
		return (CaseInsensitiveMap<K, V>) super.clone();
	}
}
