package jenkins.plugins.svn_revert;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

public class RevisionsTest {

    @Test
    public void createFromListsGetsFirstAndLast() throws Exception {
        final List<Integer> listOfRevisions = Lists.newArrayList(5, 2, 6, 3);
        final Revisions revisions = Revisions.create(listOfRevisions);
        assertThat(revisions.getFirst(), is(2));
        assertThat(revisions.getLast(), is(6));
        assertThat(revisions.getBefore(), is(1));
    }
}
