package jenkins.plugins.svn_revert;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import hudson.model.FreeStyleBuild;
import hudson.model.Result;
import hudson.model.Cause;
import hudson.model.FreeStyleProject;
import hudson.scm.NullSCM;

import java.io.IOException;
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
                containsString("Will not revert since build status is not UNSTABLE"));
        assertBuildStatus(Result.SUCCESS, build);
    }

    public void testShouldNotRevertWhenNotSubversionSCM() throws Exception {
        givenJobWithNullScm();

        scheduleBuild();

        givenJobStatusIsUnstable();

        final FreeStyleBuild build = scheduleBuild();

        assertThat(build.getLog(LOG_LIMIT).toString(),
                containsString("The Subversion Revert Plugin can only be used with Subversion SCM"));
        assertBuildStatus(Result.UNSTABLE, build);

    }

    private void givenJobStatusIsUnstable() throws IOException {
        job.getBuildersList().add(new MockBuilder(Result.UNSTABLE));
    }

    private void givenJobWithNullScm() throws IOException {
        job = createFreeStyleProject("no-scm-job");
        job.setScm(new NullSCM());
        job.getPublishersList().add(new JenkinsGlue(""));
    }

    private FreeStyleBuild scheduleBuild() throws InterruptedException,
    ExecutionException {
        return job.scheduleBuild2(0, new Cause.UserCause()).get();
    }

}
