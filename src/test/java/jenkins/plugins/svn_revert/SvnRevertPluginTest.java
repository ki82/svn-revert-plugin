package jenkins.plugins.svn_revert;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import hudson.model.FreeStyleBuild;
import hudson.model.Result;
import hudson.model.Cause;
import hudson.model.FreeStyleProject;
import hudson.scm.NullSCM;

import java.util.List;

import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.MockBuilder;

public class SvnRevertPluginTest extends HudsonTestCase {

    private static final int LOG_LIMIT = 1000;

    public void testShouldNotRevertWhenBuildStatusIsSuccess() throws Exception {
        final FreeStyleProject job = createFreeStyleProject();
        job.setScm(new NullSCM());
        job.getPublishersList().add(new JenkinsGlue(""));

        final FreeStyleBuild build = job.scheduleBuild2(0, new Cause.UserCause()).get();

        final List<String> log = build.getLog(LOG_LIMIT);
        System.out.println(log.toString());
        assertEquals(log.toString(), "[Started by user SYSTEM, Will not revert since build status is not UNSTABLE., Finished: SUCCESS]");
        assertBuildStatus(Result.SUCCESS, build);
    }

    public void testShouldNotRevertWhenNotSubversionSCM() throws Exception {
        final FreeStyleProject job = createFreeStyleProject("no-scm-job");
        job.setScm(new NullSCM());
        job.getPublishersList().add(new JenkinsGlue(""));

        job.scheduleBuild2(0, new Cause.UserCause()).get();

        job.getBuildersList().add(new MockBuilder(Result.UNSTABLE));
        final FreeStyleBuild build = job.scheduleBuild2(0, new Cause.UserCause()).get();

        final List<String> log = build.getLog(LOG_LIMIT);
        System.out.println(log.toString());
        assertThat(log.toString(), containsString("The Subversion Revert Plugin can only be used with Subversion SCM"));
        assertBuildStatus(Result.UNSTABLE, build);

    }

}
