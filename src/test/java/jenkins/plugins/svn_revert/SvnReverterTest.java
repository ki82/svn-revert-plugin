package jenkins.plugins.svn_revert;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import hudson.EnvVars;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.scm.SubversionSCM;
import hudson.scm.SubversionSCM.ModuleLocation;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.FakeChangeLogSCM.EntryImpl;
import org.jvnet.hudson.test.FakeChangeLogSCM.FakeChangeLogSet;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;

import com.google.common.collect.Lists;

@SuppressWarnings("rawtypes")
public class SvnReverterTest extends AbstractMockitoTestCase {

    private static final String LOCAL_REPO = "local" + File.separator;
    private static final String LOCAL_REPO_2 = "local2" + File.separator;
    private static final String REMOTE_REPO = "remote";
    private static final String REMOTE_REPO_2 = "remote2";
    private static final int FROM_REVISION = 911;
    private static final int TO_REVISION = FROM_REVISION - 1;
    private static final int FROM_REVISION_2 = 112;
    private static final int TO_REVISION_2 = FROM_REVISION_2 - 1;

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
    @Mock
    private File moduleDir2;
    @Mock
    private SVNURL svnUrl;
    @Mock
    private SVNURL svnUrl2;
    @Mock
    private SVNException svnException;

    private final IOException ioException = new IOException();

    private final String revertMessage = "Configured commit message.";
    private FakeChangeLogSet changeLogSet;
    private final List<EntryImpl> entries = Lists.newLinkedList();
    private final List<ModuleLocation> modules = Lists.newLinkedList();

    @Before
    public void setup() {
        when(build.getRootBuild()).thenReturn(rootBuild);
        when(build.getProject()).thenReturn(project);
        when(project.getRootProject()).thenReturn(rootProject);
        changeLogSet = new FakeChangeLogSet(build, entries);
        when(build.getChangeSet()).thenReturn(changeLogSet);
        when(subversionScm.getLocations(environmentVariables, build)).thenAnswer(getModuleLocationAnswer());
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

        verify(messenger).informReverted(Revisions.create(FROM_REVISION), REMOTE_REPO);
    }

    @Test
    public void shouldUseConfiguredMessageWhenReverting() throws Exception {
        givenAllRevertConditionsMet();

        reverter.revert(subversionScm);

        verify(svnKitClient).commit(revertMessage, moduleDir);
    }

    @Test
    public void shouldRevertChangedRevision() throws Exception {
        givenAllRevertConditionsMet();

        reverter.revert(subversionScm);

        verify(svnKitClient).merge(Revisions.create(FROM_REVISION), svnUrl, moduleDir);
    }

    @Test
    public void shouldRevertChangedRevisionsInAllModulesWhenSameRevisionsChanged() throws Exception {
        givenAllRevertConditionsMetForTwoModulesInSameRepo();

        reverter.revert(subversionScm);

        verify(messenger).informReverted(Revisions.create(FROM_REVISION), REMOTE_REPO);
        verify(messenger).informReverted(Revisions.create(FROM_REVISION), REMOTE_REPO_2);
        verify(svnKitClient).merge(Revisions.create(FROM_REVISION), svnUrl, moduleDir);
        verify(svnKitClient).merge(Revisions.create(FROM_REVISION), svnUrl2, moduleDir2);
        verify(svnKitClient).commit(revertMessage, moduleDir, moduleDir2);
        verifyNoMoreInteractions(svnKitClient);
    }

    @Test
    public void shouldNotLogRevertedWhenCommitFails() throws Exception {
        givenAllRevertConditionsMet();
        doThrow(svnException).when(svnKitClient).commit(anyString(), any(File.class));

        reverter.revert(subversionScm);

        verify(messenger, never()).informReverted(any(Revisions.class), anyString());
    }

    @Test
    public void shouldRevertMultipleRevisionsWhenMultipleCommitsSinceLastBuild() throws Exception {
        givenAllRevertConditionsMet();
        givenChangedRevisionsIn(LOCAL_REPO, FROM_REVISION + 1);

        reverter.revert(subversionScm);

        verify(svnKitClient).merge(Revisions.create(FROM_REVISION, FROM_REVISION + 1), svnUrl, moduleDir);
    }

    private void givenAllRevertConditionsMetForTwoModulesInSameRepo() throws Exception,
            IOException, InterruptedException {
        givenAllRevertConditionsMet();
        givenModuleLocations(moduleDir2, svnUrl2, REMOTE_REPO_2, LOCAL_REPO_2);
        givenChangedRevisionsIn(LOCAL_REPO_2, FROM_REVISION);
    }

    private void givenRepositoryWithoutChanges() throws Exception {
        givenScmWithAuth();
        givenEnvironmentVariables();
        givenModuleLocations(moduleDir, svnUrl, REMOTE_REPO, LOCAL_REPO);
    }

    private void givenAllRevertConditionsMet() throws Exception, IOException, InterruptedException {
        givenRepositoryWithoutChanges();
        givenChangesInFirstRepository();
    }

    private void givenScmWithAuth() throws Exception {
        when(svnFactory.create(rootProject, subversionScm)).thenReturn(svnKitClient);
    }

    private void givenEnvironmentVariables() throws Exception {
        when(build.getEnvironment(listener)).thenReturn(environmentVariables);
    }

    private void givenChangesInFirstRepository() {
        givenChangedRevisionsIn(LOCAL_REPO, FROM_REVISION);
    }

    private void givenChangedRevisionsIn(final String path, final int revision) {
        final EntryImpl entry = mock(EntryImpl.class);
        when(entry.getCommitId()).thenReturn(Integer.toString(revision));
        when(entry.getAffectedPaths()).thenReturn(Collections.singleton(path + File.separator + "changed_file.txt"));
        entries.add(entry);
    }

    private void givenModuleLocations(final File moduleDir, final SVNURL svnUrl, final String remoteLocation, final String localLocation) throws Exception {
        final ModuleLocation moduleLocation = new ModuleLocation(remoteLocation, localLocation);
        modules.add(moduleLocation);
        when(moduleResolver.getModuleRoot(build, moduleLocation)).thenReturn(moduleDir);
        when(moduleResolver.getSvnUrl(moduleLocation)).thenReturn(svnUrl);
    }

    private void givenScmWithNoAuth() throws Exception {
        when(svnFactory.create(Matchers.<AbstractProject>any(), Matchers.<SubversionSCM>any()))
        .thenThrow(new NoSvnAuthException());
    }

    private Answer<ModuleLocation[]> getModuleLocationAnswer() {
        return new Answer<ModuleLocation[]>() {

            @Override
            public ModuleLocation[] answer(final InvocationOnMock invocation) throws Throwable {
                return modules.toArray(new ModuleLocation[0]);
            }

        };
    }

}
