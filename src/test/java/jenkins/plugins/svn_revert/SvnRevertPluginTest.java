package jenkins.plugins.svn_revert;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import hudson.model.FreeStyleBuild;
import hudson.model.Result;
import hudson.model.FreeStyleProject;
import hudson.scm.NullSCM;
import hudson.scm.SubversionSCM;

import java.util.concurrent.ExecutionException;

import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.MockBuilder;

public class SvnRevertPluginTest extends HudsonTestCase {

    private static final int LOG_LIMIT = 1000;
    private FreeStyleProject job;

    public void testShouldNotRevertWhenBuildStatusIsSuccess() throws Exception {
        givenJobWithNullScm();

        final FreeStyleBuild build = scheduleBuild();

        assertThat(build.getLog(LOG_LIMIT).toString(),
                containsString(Messenger.BUILD_STATUS_NOT_UNSTABLE));
        assertBuildStatus(Result.SUCCESS, build);
    }

    public void testShouldNotRevertWhenNotSubversionSCM() throws Exception {
        givenJobWithNullScm();
        final FreeStyleBuild currentBuild = givenPreviousJobSuccessfulAndCurrentUnstable();

        assertThat(currentBuild.getLog(LOG_LIMIT).toString(),
                containsString(Messenger.NOT_SUBVERSION_SCM));
        assertBuildStatus(Result.UNSTABLE, currentBuild);
    }

    public void DISABLEDtestShouldRevertWhenBuildStatusChangesToUnstable() throws Exception {
        givenJobWithSubversionScm();
        final FreeStyleBuild currentBuild = givenPreviousJobSuccessfulAndCurrentUnstable();

        System.out.println(currentBuild.getLog(LOG_LIMIT).toString());
        assertBuildStatus(Result.UNSTABLE, currentBuild);
    }

    private FreeStyleBuild givenPreviousJobSuccessfulAndCurrentUnstable() throws Exception,
            InterruptedException, ExecutionException {
        scheduleBuild();
        givenJobStatusIsUnstable();
        return scheduleBuild();
    }

    private void givenJobStatusIsUnstable() throws Exception {
        job.getBuildersList().add(new MockBuilder(Result.UNSTABLE));
    }

    private void givenJobWithNullScm() throws Exception {
        job = createFreeStyleProject("no-scm-job");
        job.getPublishersList().add(new JenkinsGlue(""));
        job.setScm(new NullSCM());
    }

    private void givenJobWithSubversionScm() throws Exception {
        job = createFreeStyleProject("subversion-scm-job");
        job.getPublishersList().add(new JenkinsGlue(""));
        System.setProperty("SVN_REVISION", "123");
        job.setScm(new SubversionSCM(""));
    }

    private FreeStyleBuild scheduleBuild() throws Exception {
        return job.scheduleBuild2(0).get();
    }

}
