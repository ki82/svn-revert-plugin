package jenkins.plugins.svn_revert;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.scm.SubversionSCM;
import hudson.scm.SubversionSCM.ModuleLocation;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.tmatesoft.svn.core.wc.SVNCommitClient;
import org.tmatesoft.svn.core.wc.SVNDiffClient;

@SuppressWarnings("rawtypes")
public class SvnReverterTest extends AbstractMockitoTestCase {

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
    private SVNDiffClient diffClient;
    @Mock
    private SVNCommitClient commitClient;
    @Mock
    private SvnKitClient svnKitClient;

    @Mock
    private File file;

    private final IOException ioException = new IOException();



    @Before
    public void setup() {
        when(build.getRootBuild()).thenReturn(rootBuild);
        when(build.getProject()).thenReturn(project);
        when(project.getRootProject()).thenReturn(rootProject);
        reverter = new SvnReverter(build, listener, messenger, svnFactory);
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

    @Ignore
    @Test
    public void shouldLogWhenRevertSuccessful() throws Exception {
        givenScmWithAuth();

        when(build.getEnvironment(listener)).thenReturn(environmentVariables);
        when(environmentVariables.get("SVN_REVISION")).thenReturn("911");
        when(subversionScm.getLocations(environmentVariables, build)).thenReturn(new ModuleLocation[] {
            new ModuleLocation("remote", "local")
        });
        when(file.getPath()).thenReturn("/module-root-path");
        when(file.getAbsolutePath()).thenReturn("/module-root-path");
        final FilePath moduleRoot = new FilePath(file);
        when(build.getModuleRoot()).thenReturn(moduleRoot);
        reverter.revert(subversionScm);

        verify(messenger).informReverted(911, 910, "remote");
    }

    private void givenScmWithAuth() throws Exception {
        when(svnFactory.create(rootProject, subversionScm)).thenReturn(svnKitClient);
    }

    private void givenScmWithNoAuth() throws Exception {
        when(svnFactory.create(Matchers.<AbstractProject>any(), Matchers.<SubversionSCM>any()))
        .thenThrow(new NoSvnAuthException());
    }

}
