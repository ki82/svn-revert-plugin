package jenkins.plugins.svn_revert;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.FreeStyleBuild;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.scm.NullSCM;
import hudson.scm.SubversionSCM;

import java.io.PrintStream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

@SuppressWarnings("rawtypes")
public class ControllerTest extends AbstractMockitoTestCase {

    private static final Result NOT_SUCCESS = Result.UNSTABLE;
    private static final Result NOT_UNSTABLE = Result.SUCCESS;
    @Mock
    private AbstractBuild build;
    @Mock
    private AbstractBuild rootBuild;
    @Mock
    private BuildListener listener;
    @Mock
    private Launcher launcher;
    @Mock
    private PrintStream logger;
    @Mock
    private FreeStyleBuild previousBuild;
    @Mock
    private SvnReverter reverter;
    @Mock
    private Messenger messenger;
    @Mock
    private SubversionSCM subversionScm;
    @Mock
    private AbstractProject project;
    @Mock
    private AbstractProject rootProject;
    @Mock
    private NullSCM nullScm;

    @Before
    public void setUp() {
        when(build.getRootBuild()).thenReturn(rootBuild);
        when(build.getProject()).thenReturn(project);
        when(project.getRootProject()).thenReturn(rootProject);
        when(listener.getLogger()).thenReturn(logger);
        when(build.getPreviousBuiltBuild()).thenReturn(previousBuild);
        when(rootProject.getScm()).thenReturn(subversionScm);
    }

    @Test
    public void shouldLogIfRepoIsNotSubversion() throws Exception {
        givenNotSubversionScm();
        Controller.perform(build, launcher, messenger, reverter);
        verify(messenger).informNotSubversionSCM();
    }

    @Test
    public void shouldReturnTrueIfRepoIsNotSubversion() throws Exception {
        givenNotSubversionScm();
        assertThat(Controller.perform(build, launcher, messenger, reverter), is(true));
    }

    private void givenNotSubversionScm() {
        when(rootProject.getScm()).thenReturn(nullScm);
    }

    @Test
    public void shouldReturnTrueWhenBuildResultIsNotUnstable() throws Exception {
        when(build.getResult()).thenReturn(NOT_UNSTABLE);

        assertThat(Controller.perform(build, launcher, messenger, reverter), is(true));
    }

    @Test
    public void shouldLogWhenBuildResultIsNotUnstable() throws Exception {
        when(build.getResult()).thenReturn(NOT_UNSTABLE);

        Controller.perform(build, launcher, messenger, reverter);

        verify(messenger).informBuildStatusNotUnstable();
    }

    @Test
    public void shouldReturnTrueWhenPreviousBuildResultIsNotSuccess() throws Exception {
        when(build.getResult()).thenReturn(Result.UNSTABLE);
        when(previousBuild.getResult()).thenReturn(NOT_SUCCESS);

        assertThat(Controller.perform(build, launcher, messenger, reverter), is(true));
    }

    @Test
    public void shouldLogWhenPreviousBuildResultIsNotSuccess() throws Exception {
        when(build.getResult()).thenReturn(NOT_SUCCESS);

        Controller.perform(build, launcher, messenger, reverter);

        verify(messenger).informPreviousBuildStatusNotSuccess();
    }

    @Test
    public void shouldNotRevertWhenBuildResultIsSuccess() throws Exception {
        when(build.getResult()).thenReturn(Result.SUCCESS);

        Controller.perform(build, launcher, messenger, reverter);

        verify(reverter, never()).revert(subversionScm);
    }

    @Test
    public void shouldNotRevertWhenBuildResultIsFailure() throws Exception {
        when(build.getResult()).thenReturn(Result.FAILURE);

        Controller.perform(build, launcher, messenger, reverter);

        verify(reverter, never()).revert(subversionScm);
    }

    @Test
    public void shouldRevertWhenBuildResultIsUnstableAndPreviousResultIsSuccess() throws Exception {
        when(build.getResult()).thenReturn(Result.UNSTABLE);
        when(previousBuild.getResult()).thenReturn(Result.SUCCESS);

        Controller.perform(build, launcher, messenger, reverter);

        verify(reverter).revert(subversionScm);
    }

    @Test
    public void shouldNotRevertIfPreviousBuildWasNotSuccess() throws Exception {
        when(build.getResult()).thenReturn(Result.UNSTABLE);
        when(previousBuild.getResult()).thenReturn(NOT_SUCCESS);

        Controller.perform(build, launcher, messenger, reverter);

        verify(reverter, never()).revert(subversionScm);
    }

    @Test
    public void shouldFailBuildIfRevertFails() throws Exception {
        givenWillRevert();

        when(reverter.revert(subversionScm)).thenReturn(false);

        assertThat(Controller.perform(build, launcher, messenger, reverter), is(false));
    }

    @Test
    public void shouldNotFailBuildIfRevertSucceeds() throws Exception {
        givenWillRevert();

        when(reverter.revert(subversionScm)).thenReturn(true);

        assertThat(Controller.perform(build, launcher, messenger, reverter), is(true));
    }

    @Test
    public void shouldNotFailWhenFirstBuildIsUnstable() throws Exception {
        when(build.getResult()).thenReturn(Result.UNSTABLE);
        when(build.getPreviousBuiltBuild()).thenReturn(null);

        assertThat(Controller.perform(build, launcher, messenger, reverter), is(true));
    }



    void givenWillRevert() {
        when(build.getResult()).thenReturn(Result.UNSTABLE);
        when(previousBuild.getResult()).thenReturn(Result.SUCCESS);
    }

}
