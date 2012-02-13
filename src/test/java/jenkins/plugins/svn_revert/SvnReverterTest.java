package jenkins.plugins.svn_revert;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import hudson.EnvVars;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.scm.ChangeLogSet;
import hudson.scm.SubversionSCM;
import hudson.scm.SubversionSCM.ModuleLocation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.FakeChangeLogSCM.EntryImpl;
import org.jvnet.hudson.test.FakeChangeLogSCM.FakeChangeLogSet;
import org.mockito.Matchers;
import org.mockito.Mock;

import com.google.common.collect.Lists;

@SuppressWarnings("rawtypes")
public class SvnReverterTest extends AbstractMockitoTestCase {

    private static final int FROM_REVISION = 911;
    private static final int TO_REVISION = FROM_REVISION - 1;

    private SvnReverter reverter;

    @Mock
    private Messenger messenger;
    @Mock
    private AbstractBuild build;
    @Mock
    private AbstractBuild rootBuild;
    @Mock
    private AbstractProject rootProject;
    @Mock
    private AbstractProject project;
    @Mock
    private BuildListener listener;
    @Mock
    private SubversionSCM subversionScm;
    @Mock
    private SvnKitClientFactory svnFactory;
    @Mock
    private EnvVars environmentVariables;
    @Mock
    private SvnKitClient svnKitClient;
    @Mock
    private ModuleResolver moduleResolver;
    @Mock
    private File moduleDir;

    private final ChangeLogSet emptyChangeSet = ChangeLogSet.createEmpty(build);

    private final IOException ioException = new IOException();

    private final String revertMessage = "Configurable message!!!";
    private FakeChangeLogSet changeLogSet;

    @Before
    public void setup() {
        when(build.getRootBuild()).thenReturn(rootBuild);
        when(build.getProject()).thenReturn(project);
        when(project.getRootProject()).thenReturn(rootProject);
        reverter = new SvnReverter(build, listener, messenger, svnFactory, moduleResolver, revertMessage);
    }

    @Test
    public void shouldLogIfNoSvnAuthAvailable() throws Exception {
        givenScmWithNoAuth();
        reverter.revert(subversionScm);
        verify(messenger).informNoSvnAuthProvider();
    }

    @Test
    public void shouldFailIfNoSvnAuthAvailable() throws Exception {
        givenScmWithNoAuth();
        assertThat(reverter.revert(subversionScm), is(false));
    }

    @Test
    public void shouldLogExceptionIfThrown() throws Exception {
        givenScmWithAuth();
        when(build.getEnvironment(listener)).thenThrow(ioException);
        reverter.revert(subversionScm);
        verify(messenger).printStackTraceFor(ioException);
    }

    @Test(expected=RuntimeException.class)
    public void shouldNotCatchRuntimeExceptionIfThrown() throws Exception {
        givenScmWithAuth();
        when(build.getEnvironment(listener)).thenThrow(new RuntimeException());
        reverter.revert(subversionScm);
    }

    @Test
    public void shouldLogWhenRevertSuccessful() throws Exception {
        givenAllRevertConditionsMet();

        reverter.revert(subversionScm);

        verify(messenger).informReverted(FROM_REVISION, TO_REVISION, "remote");
    }

    @Test
    public void shouldUseConfiguredMessageWhenReverting() throws Exception {
        givenAllRevertConditionsMet();

        reverter.revert(subversionScm);

        verify(svnKitClient).commit(moduleDir, revertMessage );
    }

    private void givenAllRevertConditionsMet() throws Exception, IOException, InterruptedException {
        givenScmWithAuth();
        givenEnvirontVariables();
        givenNewRevisions(FROM_REVISION);
        givenModuleLocations();
    }

    private void givenScmWithAuth() throws Exception {
        when(svnFactory.create(rootProject, subversionScm)).thenReturn(svnKitClient);
        when(build.getChangeSet()).thenReturn(emptyChangeSet);
    }

    private void givenEnvirontVariables() throws Exception {
        when(build.getEnvironment(listener)).thenReturn(environmentVariables);
    }

    private void givenNewRevisions(final int... revisions) {
        final ArrayList<EntryImpl> entries = Lists.newArrayList();
        for (final int revision : revisions) {
            final EntryImpl entry = mock(EntryImpl.class);
            when(entry.getCommitId()).thenReturn(Integer.toString(revision));
            entries.add(entry);
        }
        changeLogSet = new FakeChangeLogSet(build, entries);
        when(build.getChangeSet()).thenReturn(changeLogSet);
    }

    private void givenModuleLocations() {
        final ModuleLocation moduleLocation = new ModuleLocation("remote", "local");
        when(subversionScm.getLocations(environmentVariables, build)).thenReturn(new ModuleLocation[] {
            moduleLocation
        });
        when(moduleResolver.getModuleRoot(build, moduleLocation)).thenReturn(moduleDir);
    }

    private void givenScmWithNoAuth() throws Exception {
        when(svnFactory.create(Matchers.<AbstractProject>any(), Matchers.<SubversionSCM>any()))
        .thenThrow(new NoSvnAuthException());
    }

}
