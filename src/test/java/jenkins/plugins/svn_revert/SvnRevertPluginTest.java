package jenkins.plugins.svn_revert;

import static hudson.model.Result.SUCCESS;
import static hudson.model.Result.UNSTABLE;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import hudson.FilePath;
import hudson.model.FreeStyleBuild;
import hudson.model.Result;
import hudson.model.AbstractProject;
import hudson.model.FreeStyleProject;
import hudson.scm.NullSCM;
import hudson.scm.SubversionSCM;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.jvnet.hudson.test.HudsonHomeLoader.CopyExisting;
import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.MockBuilder;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNCommitClient;

@SuppressWarnings("deprecation")
public class SvnRevertPluginTest extends HudsonTestCase {

    private static final String CHANGED_FILE = "random_file.txt";
    private static final String CHANGED_FILE_IN_MODULE_1 = "module1" + File.separator + CHANGED_FILE;
    private static final String NO_COMMITS = "1";
    private static final String ONE_COMMIT = "2";
    private static final String TWO_COMMITS = "3";
    private static final String ONE_REVERTED_REVISION =
            String.format(" %s:%s ", NO_COMMITS, ONE_COMMIT);
    private static final String TWO_REVERTED_REVISIONS =
            String.format(" %s:%s ", NO_COMMITS, TWO_COMMITS);
    private static final int LOG_LIMIT = 1000;
    private FreeStyleProject job;
    private String svnUrl;
    private SubversionSCM scm;
    private FreeStyleBuild currentBuild;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        givenSubversionScmWithOneModule();
    }

    public void testShouldNotRevertWhenNotSubversionSCM() throws Exception {
        givenJobWithNullScm();

        currentBuild = whenPreviousJobSuccessfulAndCurrentUnstable();

        assertThat(logFor(currentBuild), containsString(Messenger.NOT_SUBVERSION_SCM));
        assertBuildStatus(UNSTABLE, currentBuild);
    }

    public void testShouldNotRevertWhenBuildStatusIsSuccess() throws Exception {
        givenJobWithSubversionScm();
        givenChangesInSubversion(CHANGED_FILE);

        currentBuild = scheduleBuild();

        assertThat(logFor(currentBuild), containsString(Messenger.BUILD_STATUS_NOT_UNSTABLE));
        assertBuildStatus(SUCCESS, currentBuild);
        assertNothingReverted();
    }

    public void testShouldLogAndRevertWhenBuildStatusChangesToUnstable() throws Exception {
        givenJobWithSubversionScm();

        currentBuild = whenPreviousJobSuccessfulAndCurrentUnstable();

        final String buildLog = logFor(currentBuild);
        assertThat(buildLog, containsString(svnUrl));
        assertThat(buildLog, containsString(ONE_REVERTED_REVISION));
        assertBuildStatus(UNSTABLE, currentBuild);
        assertFileReverted(CHANGED_FILE);
    }

    public void testCanRevertMultipleModulesInSameRepository() throws Exception {
        givenJobWithTwoModulesInSameRepository();
        givenPreviousBuildSuccessful();
        givenChangesInSubversion(CHANGED_FILE_IN_MODULE_1);
        givenNextBuildWillBe(UNSTABLE);

        currentBuild = scheduleBuild();

        final String log = logFor(currentBuild);
        assertThat(log, containsString("module1"));
        assertThat(log, containsString("module2"));
        assertThatStringContainsTimes(log, ONE_REVERTED_REVISION, 2);
        assertFileReverted(CHANGED_FILE_IN_MODULE_1);
    }

    public void testCanRevertMultipleRevisions() throws Exception {
        givenJobWithSubversionScm();

        currentBuild = whenPreviousJobSuccesfulAndCurrentUnstableWithTwoChanges();

        final String log = logFor(currentBuild);
        assertThat(log, containsString(svnUrl));
        assertThat(log, containsString(TWO_REVERTED_REVISIONS));
        assertFileReverted(CHANGED_FILE);
    }

    private FreeStyleBuild whenPreviousJobSuccesfulAndCurrentUnstableWithTwoChanges()
            throws Exception {
        givenPreviousBuildSuccessful();
        givenTwoChangesInSubversionIn(CHANGED_FILE);
        givenNextBuildWillBe(UNSTABLE);

        return scheduleBuild();
    }

    private void givenSubversionScmWithOneModule() throws Exception {
        final File repo = getRepoWithTwoModules();
        svnUrl = "file://" + repo.getPath() + "/module1";
        scm = new SubversionSCM(svnUrl);
    }

    private void givenChangesInSubversion(final String file) throws Exception {
        createCommit(file);
    }

    private void givenTwoChangesInSubversionIn(final String file) throws Exception {
        createCommit(file);
        createCommit(file);
    }

    private void givenJobWithTwoModulesInSameRepository() throws Exception, IOException {
        givenJobWithSubversionScm();
        final File repo = getRepoWithTwoModules();
        svnUrl = "file://" + repo.getPath();
        final String[] svnUrls = new String[]{ svnUrl + "/module1", svnUrl + "/module2" };
        final String[] repoLocations= new String[]{ "module1", "module1" };
        scm = new SubversionSCM(svnUrls, repoLocations, true, null);
        job.setScm(scm);
    }

    /**
     * Repo at revision 1 with structure
     * module1/file1
     * module2/file2
     */
    private File getRepoWithTwoModules() throws Exception {
        return new CopyExisting(getClass().getResource("repoWithTwoModules.zip")).allocate();
    }

    private void givenPreviousBuildSuccessful() throws Exception {
        assertBuildStatusSuccess(scheduleBuild());
    }

    private void givenNextBuildWillBe(final Result result) throws Exception {
        job.getBuildersList().add(new MockBuilder(result));
    }

    private void givenJobWithNullScm() throws Exception {
        job = createFreeStyleProject("no-scm-job");
        job.getPublishersList().add(new JenkinsGlue(""));
        job.setScm(new NullSCM());
    }

    private void givenJobWithSubversionScm() throws Exception {
        job = createFreeStyleProject("subversion-scm-job");
        job.getPublishersList().add(new JenkinsGlue(""));
        job.setScm(scm);
    }

    private FreeStyleBuild whenPreviousJobSuccessfulAndCurrentUnstable() throws Exception,
            InterruptedException, ExecutionException {
        givenPreviousBuildSuccessful();
        givenChangesInSubversion(CHANGED_FILE);
        givenNextBuildWillBe(UNSTABLE);
        return scheduleBuild();
    }

    private String logFor(final FreeStyleBuild build) throws IOException {
        final List<String> logLines = build.getLog(LOG_LIMIT);
        System.out.println("Build log: ");
        printLog(logLines);
        final String log = logLines.toString();
        return log;
    }

    private void printLog(final List<String> logLines) {
        for (final String logLine : logLines) {
            System.out.println("   " + logLine);
        }
    }

    private FreeStyleBuild scheduleBuild() throws Exception {
        return job.scheduleBuild2(0).get();
    }

    private void createCommit(final String... paths) throws Exception {
        final FreeStyleBuild b = getIndependentSubversionJob(scm);
        final SVNClientManager svnm = SubversionSCM.createSvnClientManager((AbstractProject)null);

        final List<File> added = new ArrayList<File>();
        for (final String path : paths) {
            final FilePath newFile = b.getWorkspace().child(path);
            added.add(new File(newFile.getRemote()));
            if (!newFile.exists()) {
                newFile.touch(System.currentTimeMillis());
                svnm.getWCClient().doAdd(new File(newFile.getRemote()),false,false,false, SVNDepth.INFINITY, false,false);
            } else {
                newFile.write("random content","UTF-8");
            }
        }
        final SVNCommitClient cc = svnm.getCommitClient();
        cc.doCommit(added.toArray(new File[added.size()]),false,"added",null,null,false,false,SVNDepth.EMPTY);
    }

    private void assertNothingReverted() throws Exception, IOException, InterruptedException {
        assertEquals(ONE_COMMIT, revisionAfterCurrentBuild());
    }

    private void assertFileReverted(final String path)
            throws IOException, InterruptedException, ExecutionException, Exception {

        final FreeStyleBuild job = getIndependentSubversionJob(scm);
        final FilePath file = job.getWorkspace().child(path);
        assertFalse("File '" + path + "' is not reverted (because it exists)", file.exists());
    }

    private FreeStyleBuild getIndependentSubversionJob(final SubversionSCM scm) throws IOException,
            Exception, InterruptedException, ExecutionException {
        final FreeStyleProject forCommit = createFreeStyleProject();
        forCommit.setScm(scm);
        forCommit.setAssignedLabel(hudson.getSelfLabel());
        final FreeStyleBuild b = assertBuildStatusSuccess(forCommit.scheduleBuild2(0).get());
        return b;
    }

    private String revisionAfterCurrentBuild() throws IOException, InterruptedException, Exception {
        return scheduleBuild().getEnvironment().get("SVN_REVISION");
    }

    private void assertThatStringContainsTimes(
            final String log, final String string, final int times) {
        assertThat(log.split(string).length, is(times + 1));

    }

}
