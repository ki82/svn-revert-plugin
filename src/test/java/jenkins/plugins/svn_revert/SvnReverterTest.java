package jenkins.plugins.svn_revert;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.scm.SubversionSCM;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.tmatesoft.svn.core.wc.SVNClientManager;

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
    private SvnClientManagerFactory svnFactory;
    @Mock
    private SVNClientManager clientManager;

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

    private void givenScmWithAuth() throws Exception {
        when(svnFactory.create(project, subversionScm)).thenReturn(clientManager);
    }

    public void givenScmWithNoAuth() throws Exception {
        when(svnFactory.create(Matchers.<AbstractProject>any(), Matchers.<SubversionSCM>any()))
        .thenThrow(new NoSvnAuthException());
    }

}
