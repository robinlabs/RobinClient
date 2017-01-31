package com.magnifis.parking.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeMap;

/**
 * Created by oded on 1/20/14.
 */
public class ValueSortedMap<K, V> {

    HashMap<K, V> hashMap;
    TreeMap<V, K> treeMap;
    TreeMap<V, K> reversedTreeMap;

    public ValueSortedMap() {

        hashMap = new HashMap<K, V>();
        treeMap = new TreeMap<V, K>();
        reversedTreeMap = new TreeMap<V, K>(Collections.reverseOrder());
    }

    public void put(K key, V value) {

        if (hashMap.get(key) != null) return;

        hashMap.put(key, value);
        treeMap.put(value, key);
        reversedTreeMap.put(value, key);
    }

    public V get(K key) {
        return hashMap.get(key);
    }

    public Collection<K> keySetAscending() {
        return treeMap.values();
    }

    public Collection<K> keySetDescending() {
        return reversedTreeMap.values();
    }

    public int size() {
        return hashMap.size();
    }
}
