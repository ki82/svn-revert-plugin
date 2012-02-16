package jenkins.plugins.svn_revert;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Lists;

final class Revisions {

    private final List<Integer> listOfRevisions;

    private Revisions(final List<Integer> listOfRevisions) {
        this.listOfRevisions = listOfRevisions;
    }

    static Revisions create(final Integer... revisions) {
        return create(Lists.newArrayList(revisions));
    }

    static Revisions create(final List<Integer> listOfRevisions) {
        Collections.sort(listOfRevisions);
        return new Revisions(listOfRevisions);
    }

    int getLast() {
        return listOfRevisions.get(listOfRevisions.size() - 1);
    }

    int getFirst() {
        return listOfRevisions.get(0);
    }

    int getBefore() {
        return getFirst() - 1;
    }

    List<Integer> getAllInOrder() {
        return listOfRevisions;
    }

    String getAllInOrderAsString() {
        return StringUtils.join(getAllInOrder(), ", ");
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
        if (listOfRevisions == null) {
            if (other.listOfRevisions != null) {
                return false;
            }
        } else if (!listOfRevisions.equals(other.listOfRevisions)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Revisions, <" + getAllInOrderAsString() + ">";
    }

}
