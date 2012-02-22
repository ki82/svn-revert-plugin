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

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;

import com.google.common.collect.Lists;

@SuppressWarnings("rawtypes")
public class SvnReverterTest extends AbstractMockitoTestCase {

    private static final String LOCAL_REPO = "local" + File.separator;
    private static final String LOCAL_REPO_2 = "local2" + File.separator;
    private static final String REMOTE_REPO = "remote";
    private static final String REMOTE_REPO_2 = "remote2";
    private static final int FIRST_CHANGE = 911;
    private static final int SECOND_CHANGE = FIRST_CHANGE + 1;

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
    private File moduleDir;
    @Mock
    private File moduleDir2;
    @Mock
    private SVNURL svnUrl;
    @Mock
    private SVNURL svnUrl2;
    @Mock
    private SVNException svnException;
    @Mock
    private ChangedRevisions changedRevisions;
    @Mock
    private ModuleFinder locationFinder;

    private final IOException ioException = new IOException();

    private final List<Module> modules = Lists.newLinkedList();

    @Before
    public void setup() throws Exception {
        when(build.getRootBuild()).thenReturn(rootBuild);
        when(build.getProject()).thenReturn(project);
        when(project.getRootProject()).thenReturn(rootProject);
        when(svnKitClient.commit(anyString(), any(File.class))).thenReturn(true);
        when(svnKitClient.commit(anyString(), any(File.class), any(File.class))).thenReturn(true);
        when(locationFinder.getModules(subversionScm)).thenReturn(modules);
        reverter = new SvnReverter(build, listener, messenger, svnFactory, locationFinder, changedRevisions);
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
        when(locationFinder.getModules(subversionScm)).thenThrow(ioException);
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

        verify(messenger).informReverted(Revisions.create(FIRST_CHANGE), REMOTE_REPO);
        verifyNoMoreInteractions(messenger);
    }

    @Test
    public void shouldUseConfiguredMessageWhenReverting() throws Exception {
        givenAllRevertConditionsMet();

        reverter.revert(subversionScm);

        verify(svnKitClient).commit(buildCommitMessage(), moduleDir);
    }

    @Test
    public void shouldRevertChangedRevision() throws Exception {
        givenAllRevertConditionsMet();

        reverter.revert(subversionScm);

        verify(svnKitClient).reverseMerge(Revisions.create(FIRST_CHANGE), svnUrl, moduleDir);
    }

    @Test
    public void shouldRevertChangedRevisionsInAllModulesWhenSameRevisionsChanged() throws Exception {
        givenAllRevertConditionsMetForTwoModulesInSameRepo();

        reverter.revert(subversionScm);

        verify(messenger).informReverted(Revisions.create(FIRST_CHANGE), REMOTE_REPO);
        verify(messenger).informReverted(Revisions.create(FIRST_CHANGE), REMOTE_REPO_2);
        verify(svnKitClient).reverseMerge(Revisions.create(FIRST_CHANGE), svnUrl, moduleDir);
        verify(svnKitClient).reverseMerge(Revisions.create(FIRST_CHANGE), svnUrl2, moduleDir2);
        verify(svnKitClient).commit(buildCommitMessage(), moduleDir, moduleDir2);
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
        when(changedRevisions.getFor(build)).thenReturn(Revisions.create(FIRST_CHANGE, SECOND_CHANGE));

        reverter.revert(subversionScm);

        verify(svnKitClient).reverseMerge(Revisions.create(FIRST_CHANGE, SECOND_CHANGE), svnUrl, moduleDir);
    }

    @Test
    public void shouldLogNotRevertedWhenFileIsOutOfDate() throws Exception {
        givenAllRevertConditionsMet();
        when(svnKitClient.commit(anyString(), any(File[].class))).thenReturn(false);

        reverter.revert(subversionScm);

        verify(messenger).informFilesToRevertOutOfDate();
        verifyNoMoreInteractions(messenger);
    }

    private void givenAllRevertConditionsMetForTwoModulesInSameRepo() throws Exception,
            IOException, InterruptedException {
        givenAllRevertConditionsMet();
        givenModuleLocations(moduleDir2, svnUrl2, REMOTE_REPO_2, LOCAL_REPO_2);
        when(changedRevisions.getFor(build)).thenReturn(Revisions.create(FIRST_CHANGE));
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

    private void givenChangesInFirstRepository() {
        when(changedRevisions.getFor(build)).thenReturn(Revisions.create(FIRST_CHANGE));
    }

    private void givenScmWithAuth() throws Exception {
        when(svnFactory.create(rootProject, subversionScm)).thenReturn(svnKitClient);
    }

    private void givenEnvironmentVariables() throws Exception {
        when(build.getEnvironment(listener)).thenReturn(environmentVariables);
    }

    private void givenModuleLocations(final File moduleDir, final SVNURL svnUrl,
            final String remoteLocation, final String localLocation) throws Exception {
        final Module module = mock(Module.class);
        modules.add(module);
        when(module.getModuleRoot(build)).thenReturn(moduleDir);
        when(module.getSvnUrl()).thenReturn(svnUrl);
        when(module.getURL()).thenReturn(remoteLocation);
    }

    private void givenScmWithNoAuth() throws Exception {
        when(svnFactory.create(Matchers.<AbstractProject>any(), Matchers.<SubversionSCM>any()))
        .thenThrow(new NoSvnAuthException());
    }

    private String buildCommitMessage() {
        return String.format(SvnReverter.REVERT_MESSAGE, Revisions.create(FIRST_CHANGE).getAllInOrderAsString());
    }

}
