package jenkins.plugins.svn_revert;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

final class Revisions {

    private final int last;
    private final int first;
    private final List<Integer> listOfRevisions;

    private Revisions(final int first, final int last, final List<Integer> listOfRevisions) {
        this.last = last;
        this.first = first;
        this.listOfRevisions = listOfRevisions;
    }

    static Revisions create(final Integer... revisions) {
        return create(Lists.newArrayList(revisions));
    }

    static Revisions create(final List<Integer> listOfRevisions) {
        Collections.sort(listOfRevisions);
        return new Revisions(
                listOfRevisions.get(0),
                listOfRevisions.get(listOfRevisions.size() - 1), listOfRevisions);
    }

    int getLast() {
        return last;
    }

    int getFirst() {
        return first;
    }

    int getBefore() {
        return first - 1;
    }

    List<Integer> getAllInOrder() {
        return listOfRevisions;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Revisions other = (Revisions) obj;
        if (first != other.first) {
            return false;
        }
        if (last != other.last) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Revisions, first="+first+", last="+last;
    }

}
