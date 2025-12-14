package kahoot.game;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class ConcurrentHashMap<K, V> {

    private final Map<K, V> map;

    public ConcurrentHashMap() {
        this.map = new HashMap<>();
    }


    public synchronized void put(K key, V value) {
        map.put(key, value);
    }

    public synchronized V get(K key) {
        return map.get(key);
    }

    public synchronized V getOrDefault(K key, V defaultValue) {
        V value = map.get(key);
        return (value != null) ? value : defaultValue;
    }

    public synchronized boolean containsKey(K key) {
        return map.containsKey(key);
    }

    public synchronized V remove(K key) {
        return map.remove(key);
    }

    public synchronized int size() {
        return map.size();
    }

    public synchronized void clear() {
        map.clear();
    }


    public synchronized void merge(K key, V value, java.util.function.BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        map.merge(key, value, remappingFunction);
    }

    public synchronized Map<K, V> snapshot() {
        return new HashMap<>(map);
    }


    public synchronized Set<K> keySetSnapshot() {
        return new HashMap<>(map).keySet();
    }
}