package jenkins.plugins.svn_revert;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

public class RevisionsTest {

    private Revisions revisions;

    @Before
    public void setUp() throws Exception {
        final List<Integer> listOfRevisions = Lists.newArrayList(5, 2, 3, 3);
        revisions = Revisions.create(listOfRevisions);
    }

    @Test
    public void getsRevisionBeforeLowestRevision() throws Exception {
        assertThat(revisions.getBefore(), is(1));
    }

    @Test
    public void getsLowestRevision() throws Exception {
        assertThat(revisions.getFirst(), is(2));
    }

    @Test
    public void getsHighestRevision() throws Exception {
        assertThat(revisions.getLast(), is(5));
    }

    @Test
    public void removesDuplicatesWhenConvertingToString() throws Exception {
        assertThat(revisions.getAllInOrderAsString(), equalTo("2, 3, 5"));
    }
}
