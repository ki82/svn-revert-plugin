package jenkins.plugins.svn_revert;


import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import hudson.model.AbstractBuild;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.AffectedFile;

import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.FakeChangeLogSCM.EntryImpl;
import org.jvnet.hudson.test.FakeChangeLogSCM.FakeChangeLogSet;
import org.mockito.Mock;

import com.google.common.collect.Lists;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class ChangedFilesTest extends AbstractMockitoTestCase {

    private static final String FILE_1 = "module1/changed_file";
    private static final String FILE_2 = "module1/changed_file";

    private ChangedFiles changedFiles;

    @Mock
    private AbstractBuild<?, ?> build;
    @Mock
    private EntryImpl entry;
    @Mock
    private AffectedFile affectedFile1;
    @Mock
    private AffectedFile affectedFile2;

    private final List<EntryImpl> entries = Lists.newLinkedList();
    private final Collection affectedFiles = Lists.newLinkedList();
    private final ChangeLogSet changeLogSet = new FakeChangeLogSet(build, entries);


    @Before
    public void setUp() throws Exception {
        entries.add(entry);
        when(entry.getAffectedFiles()).thenReturn(affectedFiles);
        affectedFiles.add(affectedFile1);
        when(affectedFile1.getPath()).thenReturn(FILE_1);
        affectedFiles.add(affectedFile1);
        when(affectedFile2.getPath()).thenReturn(FILE_2);
        when(build.getChangeSet()).thenReturn(changeLogSet);
        changedFiles = new ChangedFiles();
    }

    @Test
    public void testGetFilenamesFor() throws Exception {
        final List<String> actualFilenames = changedFiles.getFilenamesFor(build);
        final List<String> expectedFilenames = Lists.newArrayList(FILE_1, FILE_2);

        when(build.getChangeSet()).thenReturn(changeLogSet);

        assertThat(actualFilenames, equalTo(expectedFilenames));
    }

}
