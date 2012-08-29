package jenkins.plugins.svn_revert;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import hudson.model.AbstractBuild;
import hudson.scm.ChangeLogSet;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.FakeChangeLogSCM.EntryImpl;
import org.jvnet.hudson.test.FakeChangeLogSCM.FakeChangeLogSet;
import org.mockito.Mock;

import com.google.common.collect.Lists;


@SuppressWarnings({"rawtypes", "unchecked"})
public class CommitMessagesTest extends AbstractMockitoTestCase {

    private CommitMessages commitMessages;

    @Mock
    private AbstractBuild<?, ?> build;
    @Mock
    private EntryImpl entry;

    private final List<EntryImpl> entries = Lists.newLinkedList();
    private final ChangeLogSet changeLogSet = new FakeChangeLogSet(build, entries);

    @Before
    public void setUp() throws Exception {
        when(build.getChangeSet()).thenReturn(changeLogSet);
        commitMessages = new CommitMessages(build);
    }

    @Test
    public void doesNotContainAnythingWhenNoCommits() throws Exception {
        assertFalse(commitMessages.anyMessageContains(""));
    }

    @Test
    public void substringInEntry() throws Exception {
        when(entry.getMsg()).thenReturn("Reverted");
        entries.add(entry);
        assertTrue(commitMessages.anyMessageContains("Reverted"));
    }

    @Test
    public void ignoresCasing() throws Exception {
        when(entry.getMsg()).thenReturn("REVert");
        entries.add(entry);
        assertTrue(commitMessages.anyMessageContains("REveRT"));
    }

}
