package jenkins.plugins.svn_revert;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.inOrder;
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
import org.mockito.InOrder;
import org.mockito.Mock;

public class SvnRevertPublisherTest extends AbstractMockitoTestCase {

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

    private final SvnRevertPublisher publisher = new SvnRevertPublisher("");

    @Before
    public void setUp() {
        when(listener.getLogger()).thenReturn(logger);
        when(build.getPreviousBuiltBuild()).thenReturn(previousBuild);
        publisher.setReverter(reverter);
        publisher.setMessenger(messenger);
    }

    @Test
    public void loggerShouldBeSetToMessengerWhenPerformingABuild() throws Exception {
        when(build.getResult()).thenReturn(Result.SUCCESS);

        publisher.perform(build, launcher, listener);

        final InOrder inorder = inOrder(messenger);
        inorder.verify(messenger).setLogger(logger);
        inorder.verify(messenger).informBuildStatusNotUnstable();
    }

    @Test
    public void shouldReturnTrueWhenBuildResultIsSuccess() throws Exception {
        when(build.getResult()).thenReturn(Result.SUCCESS);

        assertThat(publisher.perform(build, launcher, listener), is(true));
    }

    @Test
    public void shouldLogWhenBuildResultIsNotUnstable() throws Exception {
        when(build.getResult()).thenReturn(NOT_UNSTABLE);

        publisher.perform(build, launcher, listener);

        verify(messenger).informBuildStatusNotUnstable();
    }

    @Test
    public void shouldLogWhenPreviousBuildResultIsNotSuccess() throws Exception {
        when(build.getResult()).thenReturn(NOT_SUCCESS);

        publisher.perform(build, launcher, listener);

        verify(messenger).informPreviousBuildStatusNotSuccess();
    }

    @Test
    public void shouldNotRevertWhenBuildResultIsSuccess() throws Exception {
        when(build.getResult()).thenReturn(Result.SUCCESS);

        publisher.perform(build, launcher, listener);

        verify(reverter, never()).revert();
    }

    @Test
    public void shouldNotRevertWhenBuildResultIsFailure() throws Exception {
        when(build.getResult()).thenReturn(Result.FAILURE);

        publisher.perform(build, launcher, listener);

        verify(reverter, never()).revert();
    }

    @Test
    public void shouldRevertWhenBuildResultIsUnstableAndPreviousResultIsSuccess() throws Exception {
        when(build.getResult()).thenReturn(Result.UNSTABLE);
        when(previousBuild.getResult()).thenReturn(Result.SUCCESS);

        publisher.perform(build, launcher, listener);

        verify(reverter).revert();
    }

    @Test
    public void shouldNotRevertIfPreviousBuildWasNotSuccess() throws Exception {
        when(build.getResult()).thenReturn(Result.UNSTABLE);
        when(previousBuild.getResult()).thenReturn(NOT_SUCCESS);

        publisher.perform(build, launcher, listener);

        verify(reverter, never()).revert();
    }
}
