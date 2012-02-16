package jenkins.plugins.svn_revert;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.FreeStyleBuild;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.scm.ChangeLogSet;
import hudson.scm.NullSCM;
import hudson.scm.SubversionSCM;

import java.io.PrintStream;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.FakeChangeLogSCM.EntryImpl;
import org.jvnet.hudson.test.FakeChangeLogSCM.FakeChangeLogSet;
import org.mockito.Mock;

import com.google.common.collect.Lists;

@SuppressWarnings("rawtypes")
public class BouncerTest extends AbstractMockitoTestCase {

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
    private Claimer claimer;
    @Mock
    private SubversionSCM subversionScm;
    @Mock
    private AbstractProject project;
    @Mock
    private AbstractProject rootProject;
    @Mock
    private NullSCM nullScm;

    private final ChangeLogSet emptyChangeSet = ChangeLogSet.createEmpty(build);
    private EntryImpl entry;
    private LinkedList<EntryImpl> entryList;

    @Before
    public void setUp() {
        when(build.getRootBuild()).thenReturn(rootBuild);
        when(build.getProject()).thenReturn(project);
        when(project.getRootProject()).thenReturn(rootProject);
        when(listener.getLogger()).thenReturn(logger);
        when(build.getPreviousBuiltBuild()).thenReturn(previousBuild);
        when(rootProject.getScm()).thenReturn(subversionScm);
        givenMayRevert();
    }

    @Test
    public void shouldRevertWhenBuildResultIsUnstableAndPreviousResultIsSuccess() throws Exception {
        throwOutIfUnstable();

        verify(reverter).revert(subversionScm);
    }

    @Test
    public void shouldNotRevertIfPreviousBuildWasNotSuccess() throws Exception {
        when(previousBuild.getResult()).thenReturn(NOT_SUCCESS);

        throwOutIfUnstable();

        verifyNotReverted();
    }

    @Test
    public void shouldNotRevertWhenBuildResultIsSuccess() throws Exception {
        when(build.getResult()).thenReturn(Result.SUCCESS);

        throwOutIfUnstable();

        verifyNotReverted();
    }

    @Test
    public void shouldNotRevertWhenBuildResultIsFailure() throws Exception {
        when(build.getResult()).thenReturn(Result.FAILURE);

        throwOutIfUnstable();

        verifyNotReverted();
    }

    @Test
    public void shouldNotRevertWhenNoChanges() throws Exception {
        when(build.getChangeSet()).thenReturn(emptyChangeSet);

        throwOutIfUnstable();

        verifyNotReverted();
    }

    @Test
    public void shouldLogIfRepoIsNotSubversion() throws Exception {
        givenNotSubversionScm();
        throwOutIfUnstable();
        verify(messenger).informNotSubversionSCM();
    }

    @Test
    public void shouldLogWhenBuildResultIsNotUnstable() throws Exception {
        when(build.getResult()).thenReturn(NOT_UNSTABLE);

        throwOutIfUnstable();

        verify(messenger).informBuildStatusNotUnstable();
    }

    @Test
    public void shouldLogWhenPreviousBuildResultIsNotSuccess() throws Exception {
        when(previousBuild.getResult()).thenReturn(NOT_SUCCESS);

        throwOutIfUnstable();

        verify(messenger).informPreviousBuildStatusNotSuccess();
    }

    @Test
    public void shouldLogWhenNoChanges() throws Exception {
        when(build.getChangeSet()).thenReturn(emptyChangeSet);

        throwOutIfUnstable();

        verify(messenger).informNoChanges();
    }

    @Test
    public void shouldReturnTrueIfRepoIsNotSubversion() throws Exception {
        givenNotSubversionScm();
        assertThat(throwOutIfUnstable(), is(true));
    }

    @Test
    public void shouldReturnTrueWhenBuildResultIsNotUnstable() throws Exception {
        when(build.getResult()).thenReturn(NOT_UNSTABLE);

        assertThat(throwOutIfUnstable(), is(true));
    }

    @Test
    public void shouldReturnTrueWhenPreviousBuildResultIsNotSuccess() throws Exception {
        when(previousBuild.getResult()).thenReturn(NOT_SUCCESS);

        assertThat(throwOutIfUnstable(), is(true));
    }

    @Test
    public void shouldFailBuildWhenRevertFails() throws Exception {
        when(reverter.revert(subversionScm)).thenReturn(false);

        assertThat(throwOutIfUnstable(), is(false));
    }

    @Test
    public void shouldNotFailWhenFirstBuildIsUnstable() throws Exception {
        when(build.getPreviousBuiltBuild()).thenReturn(null);

        assertThat(throwOutIfUnstable(), is(true));
    }

    @Test
    public void shouldNotFailBuildWhenRevertSucceeds() throws Exception {
        when(reverter.revert(subversionScm)).thenReturn(true);

        assertThat(throwOutIfUnstable(), is(true));
    }

    @Test
    public void shouldClaimWhenRevertSucceeds() throws Exception {
        when(reverter.revert(subversionScm)).thenReturn(true);

        throwOutIfUnstable();

        verify(claimer).claim(build);
    }

    @Test
    public void shouldNotClaimWhenRevertFails() throws Exception {
        when(reverter.revert(subversionScm)).thenReturn(false);

        throwOutIfUnstable();

        verifyZeroInteractions(claimer);
    }

    private boolean throwOutIfUnstable() {
        return Bouncer.throwOutIfUnstable(build, launcher, messenger, reverter, claimer);
    }

    private void givenNotSubversionScm() {
        when(rootProject.getScm()).thenReturn(nullScm);
    }

    private void givenMayRevert() {
        when(build.getResult()).thenReturn(Result.UNSTABLE);
        when(previousBuild.getResult()).thenReturn(Result.SUCCESS);
        entryList = Lists.newLinkedList();
        entryList.add(entry);
        when(build.getChangeSet()).thenReturn(new FakeChangeLogSet(build, entryList));
    }

    private void verifyNotReverted() {
        verify(reverter, never()).revert(subversionScm);
    }

}
