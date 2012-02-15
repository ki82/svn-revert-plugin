package jenkins.plugins.svn_revert;

import java.util.Collections;
import java.util.List;

final class Revisions {

    private final int last;
    private final int first;

    private Revisions(final int first, final int last) {
        this.last = last;
        this.first = first;
    }

    static Revisions create(final int revision) {
        return new Revisions(revision, revision);
    }

    static Revisions create(final int first, final int last) {
        return new Revisions(first, last);
    }

    static Revisions create(final List<Integer> listOfRevisions) {
        Collections.sort(listOfRevisions);
        return new Revisions(
                listOfRevisions.get(0),
                listOfRevisions.get(listOfRevisions.size() - 1));
    }

    public int getLast() {
        return last;
    }

    public int getFirst() {
        return first;
    }

    public int getBefore() {
        return first - 1;
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
