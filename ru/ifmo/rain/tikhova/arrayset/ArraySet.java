package ru.ifmo.rain.tikhova.arrayset;

import java.util.*;

import static java.lang.Integer.max;


public class ArraySet<E extends Comparable> extends AbstractSet<E> implements NavigableSet<E> {
    private final List<E> data;
    private final Comparator<? super E> comparator;

    // Constructors

    public ArraySet() {
        this(Collections.emptyList(), null);
    }

    public ArraySet(Comparator<? super E> cmp) {
        data = Collections.emptyList();
        comparator = cmp;
    }

    public ArraySet(Collection<? extends E> collection) {
        this(collection, null);
    }

    public ArraySet(Collection<? extends E> collection, Comparator<? super E> cmp) {
        Set<E> treeSet = new TreeSet<>(cmp);
        treeSet.addAll(collection);
        data = new ArrayList<>(treeSet);
        comparator = cmp;
    }

    private ArraySet(List<E> list, Comparator<? super E> cmp) {
        data = list;
        comparator = cmp;
    }

    // AbstractSet

    @Override
    public int size() {
        return data == null ? 0 : data.size();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        return Collections.binarySearch(data, (E) o, comparator) >= 0;
    }

    @Override
    public Iterator<E> iterator() {
        return Collections.unmodifiableList(data).iterator();
    }

    // Methods inherited from SortedSet

    @Override
    public Comparator<? super E> comparator() {
        return comparator;
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        return headSet(toElement, false);
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        return tailSet(fromElement, true);
    }

    private void assertDataNotEmpty() {
        if (data.isEmpty())
            throw new NoSuchElementException();
    }

    @Override
    public E first() {
        assertDataNotEmpty();
        return data.get(0);
    }


    @Override
    public E last() {
        assertDataNotEmpty();
        return data.get(size() - 1);
    }

    // Methods inherited from NavigableSet

    private int getIndexEOL(E t, boolean inclusive) {
        int index = Collections.binarySearch(data, t, comparator);
        if (index < 0) {
            index = ~index;
        } else if (!inclusive) {
            ++index;
        }
        return index;
    }

    private int getIndexEOG(E t, boolean inclusive, List<E> list) {
        int index = Collections.binarySearch(list, t, comparator);
        if (index < 0) {
            index = ~index - 1;
        } else if (!inclusive) {
            --index;
        }
        return index;
    }

    private int getIndexEOG(E t, boolean inclusive) {
        return getIndexEOG(t, inclusive, data);
    }

    @Override
    public E lower(E t) {
        int index = getIndexEOG(t, false);
        return index >= 0 ? data.get(index) : null;
    }

    @Override
    public E floor(E t) {
        int index = getIndexEOG(t, true);
        return index >= 0 ? data.get(index) : null;
    }

    @Override
    public E ceiling(E t) {
        int index = getIndexEOL(t, true);
        return index < size() ? data.get(index) : null;
    }

    @Override
    public E higher(E t) {
        int index = getIndexEOL(t, false);
        return index < size() ? data.get(index) : null;
    }

    @Override
    public E pollFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public E pollLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("unchecked")
    public NavigableSet<E> descendingSet() {
        return new ArraySet<>(data instanceof DescendingList ?
                ((DescendingList<E>) data).data :
                new DescendingList<>(data), Collections.reverseOrder(comparator));
    }

    @Override
    public Iterator<E> descendingIterator() {
        return descendingSet().iterator();
    }

    @Override
    @SuppressWarnings("unchecked")
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        if (comparator != null ? comparator.compare(fromElement, toElement) > 0 : fromElement.compareTo(toElement) > 0)
            throw new IllegalArgumentException();
        int fromIndex = getIndexEOL(fromElement, fromInclusive);
        int toIndex = max(getIndexEOG(toElement, toInclusive) + 1, fromIndex);
        return new ArraySet<>(data.subList(fromIndex, toIndex), comparator);
    }

    @Override
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        int toIndex = getIndexEOG(toElement, inclusive) + 1;
        return new ArraySet<>(data.subList(0, toIndex), comparator);
    }

    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        int fromIndex = getIndexEOL(fromElement, inclusive);
        int size = size();
        if (fromIndex > size) return new ArraySet<>();
        return new ArraySet<>(data.subList(fromIndex, size()), comparator);
    }

    private static class DescendingList<T> extends AbstractList<T> implements RandomAccess {
        public List<T> data;

        public DescendingList(List<T> objects) {
            this.data = objects;
        }

        @Override
        public T get(int index) {
            return data.get(data.size() - index - 1);
        }

        @Override
        public int size() {
            return data.size();
        }
    }
}