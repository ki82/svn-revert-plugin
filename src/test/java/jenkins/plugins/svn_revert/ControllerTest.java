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

import java.io.PrintStream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class ControllerTest extends AbstractMockitoTestCase {

    private static final Result NOT_SUCCESS = Result.UNSTABLE;
    private static final Result NOT_UNSTABLE = Result.SUCCESS;
    @Mock
    private AbstractBuild<?, FreeStyleBuild> build;
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


    @Before
    public void setUp() {
        when(listener.getLogger()).thenReturn(logger);
        when(build.getPreviousBuiltBuild()).thenReturn(previousBuild);
    }

    @Test
    public void shouldReturnTrueWhenBuildResultIsNotUnstable() throws Exception {
        when(build.getResult()).thenReturn(NOT_UNSTABLE);

        assertThat(Controller.perform(build, launcher, reverter, messenger), is(true));
    }

    @Test
    public void shouldLogWhenBuildResultIsNotUnstable() throws Exception {
        when(build.getResult()).thenReturn(NOT_UNSTABLE);

        Controller.perform(build, launcher, reverter, messenger);

        verify(messenger).informBuildStatusNotUnstable();
    }

    @Test
    public void shouldReturnTrueWhenPreviousBuildResultIsNotSuccess() throws Exception {
        when(build.getResult()).thenReturn(Result.UNSTABLE);
        when(previousBuild.getResult()).thenReturn(NOT_SUCCESS);

        assertThat(Controller.perform(build, launcher, reverter, messenger), is(true));
    }

    @Test
    public void shouldLogWhenPreviousBuildResultIsNotSuccess() throws Exception {
        when(build.getResult()).thenReturn(NOT_SUCCESS);

        Controller.perform(build, launcher, reverter, messenger);

        verify(messenger).informPreviousBuildStatusNotSuccess();
    }

    @Test
    public void shouldNotRevertWhenBuildResultIsSuccess() throws Exception {
        when(build.getResult()).thenReturn(Result.SUCCESS);

        Controller.perform(build, launcher, reverter, messenger);

        verify(reverter, never()).revert();
    }

    @Test
    public void shouldNotRevertWhenBuildResultIsFailure() throws Exception {
        when(build.getResult()).thenReturn(Result.FAILURE);

        Controller.perform(build, launcher, reverter, messenger);

        verify(reverter, never()).revert();
    }

    @Test
    public void shouldRevertWhenBuildResultIsUnstableAndPreviousResultIsSuccess() throws Exception {
        when(build.getResult()).thenReturn(Result.UNSTABLE);
        when(previousBuild.getResult()).thenReturn(Result.SUCCESS);

        Controller.perform(build, launcher, reverter, messenger);

        verify(reverter).revert();
    }

    @Test
    public void shouldNotRevertIfPreviousBuildWasNotSuccess() throws Exception {
        when(build.getResult()).thenReturn(Result.UNSTABLE);
        when(previousBuild.getResult()).thenReturn(NOT_SUCCESS);

        Controller.perform(build, launcher, reverter, messenger);

        verify(reverter, never()).revert();
    }

    @Test
    public void shouldFailBuildIfRevertFails() throws Exception {
        givenWillRevert();

        when(reverter.revert()).thenReturn(false);

        assertThat(Controller.perform(build, launcher, reverter, messenger), is(false));
    }

    @Test
    public void shouldNotFailBuildIfRevertSucceeds() throws Exception {
        givenWillRevert();

        when(reverter.revert()).thenReturn(true);

        assertThat(Controller.perform(build, launcher, reverter, messenger), is(true));
    }

    @Test
    public void shouldNotFailWhenFirstBuildIsUnstable() throws Exception {
        when(build.getResult()).thenReturn(Result.UNSTABLE);
        when(build.getPreviousBuiltBuild()).thenReturn(null);

        assertThat(Controller.perform(build, launcher, reverter, messenger), is(true));
    }

    void givenWillRevert() {
        when(build.getResult()).thenReturn(Result.UNSTABLE);
        when(previousBuild.getResult()).thenReturn(Result.SUCCESS);
    }

}
