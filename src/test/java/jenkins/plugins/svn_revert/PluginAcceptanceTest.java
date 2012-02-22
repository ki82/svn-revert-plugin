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
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.jvnet.hudson.test.HudsonHomeLoader.CopyExisting;
import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.MockBuilder;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNStatus;

import com.google.common.collect.Lists;

@SuppressWarnings({ "rawtypes", "deprecation" })
public class PluginAcceptanceTest extends HudsonTestCase {

    private static final String EMAIL_SENT = "An attempt to send an e-mail to empty list of recipients, ignored.";
    private static final long NO_COMMITS = 1;
    private static final long ONE_COMMIT = 2;
    private static final long TWO_COMMITS = 3;
    private static final String ONE_REVERTED_REVISION =
            String.format(" %s:%s ", NO_COMMITS, ONE_COMMIT);
    private static final String TWO_REVERTED_REVISIONS =
            String.format(" %s:%s ", NO_COMMITS, TWO_COMMITS);
    private static final String MODULE_1 = "module1";
    private static final String MODULE_2 = "module2";
    private static final String MODIFIED_FILE = "modified_file.txt";
    private static final String MODIFIED_FILE_IN_MODULE_1 =
            MODULE_1 + File.separator + MODIFIED_FILE;
    private static final String MODIFIED_FILE_IN_MODULE_2 =
            MODULE_2 + File.separator + MODIFIED_FILE;
    private static final int LOG_LIMIT = 1000;
    private FreeStyleProject job;
    private String svnUrl;
    private SubversionSCM scm;
    private SubversionSCM rootScm;
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
        givenJobWithOneModule();
        givenChangesInSubversionIn(MODIFIED_FILE_IN_MODULE_1);

        currentBuild = whenBuilding();

        assertThat(logFor(currentBuild), containsString(Messenger.BUILD_STATUS_NOT_UNSTABLE));
        assertBuildStatus(SUCCESS, currentBuild);
        assertNothingRevertedSince(ONE_COMMIT);
    }

    public void testShouldLogAndRevertWhenBuildStatusChangesToUnstable() throws Exception {
        givenJobWithOneModule();

        currentBuild = whenPreviousJobSuccessfulAndCurrentUnstable();

        final String log = logFor(currentBuild);

        assertBuildStatus(UNSTABLE, currentBuild);
        assertFileReverted(MODIFIED_FILE_IN_MODULE_1);
        assertThat(log, containsString(svnUrl));
        assertThat(log, containsString(ONE_REVERTED_REVISION));
    }

    public void testCanRevertMultipleModulesInSameRepository() throws Exception {
        givenJobWithTwoModulesInSameRepository();
        givenPreviousBuildSuccessful();
        givenChangesInSubversionIn(MODIFIED_FILE_IN_MODULE_1);
        givenNextBuildWillBe(UNSTABLE);

        currentBuild = whenBuilding();

        final String log = logFor(currentBuild);

        assertBuildStatus(UNSTABLE, currentBuild);
        assertFileReverted(MODIFIED_FILE_IN_MODULE_1);
        assertThat(log, containsString(MODULE_1));
        assertThat(log, containsString(MODULE_2));
        assertThatStringContainsTimes(log, ONE_REVERTED_REVISION, 2);
    }

    public void testCanRevertMultipleRevisions() throws Exception {
        givenJobWithOneModule();

        currentBuild = whenPreviousJobSuccesfulAndCurrentUnstableWithTwoChanges();

        final String log = logFor(currentBuild);

        assertBuildStatus(UNSTABLE, currentBuild);
        assertFileReverted(MODIFIED_FILE_IN_MODULE_1);
        assertThat(log, containsString(svnUrl));
        assertThat(log, containsString(TWO_REVERTED_REVISIONS));
    }

    public void testWillNotRevertIfFileHasChangedSinceBuildStarted() throws Exception {
        givenJobWithOneModule();
        givenPreviousBuildSuccessful();
        givenChangesInSubversionIn(MODIFIED_FILE_IN_MODULE_1);
        givenNextBuildWillBe(UNSTABLE);

        currentBuild = whenFileChangedDuringBuilding(MODIFIED_FILE_IN_MODULE_1);

        assertBuildStatus(UNSTABLE, currentBuild);
        assertNothingRevertedSince(TWO_COMMITS);
        assertThatStringContainsTimes(logFor(currentBuild), ONE_REVERTED_REVISION, 0);
        assertLogNotContains(EMAIL_SENT, currentBuild);
    }

    public void testShouldNotRevertAnythingWhenFileToRevertHasChanged() throws Exception {
        givenJobWithTwoModulesInSameRepository();
        givenPreviousBuildSuccessful();
        givenChangesInSubversionIn(MODIFIED_FILE_IN_MODULE_1, MODIFIED_FILE_IN_MODULE_2);
        givenNextBuildWillBe(UNSTABLE);

        currentBuild = whenFileChangedDuringBuilding(MODIFIED_FILE_IN_MODULE_1);

        assertNothingRevertedSince(TWO_COMMITS);
        assertBuildStatus(UNSTABLE, currentBuild);
        assertThatStringContainsTimes(logFor(currentBuild), TWO_REVERTED_REVISIONS, 0);
    }

    public void testShouldNotRevertAnythingWhenWorkspaceOnlyContainsPartsOfCommit()
    throws Exception {
        givenJobWithOneModule();
        givenPreviousBuildSuccessful();
        givenChangesInSubversionIn(MODIFIED_FILE_IN_MODULE_1, MODIFIED_FILE_IN_MODULE_2);
        givenNextBuildWillBe(UNSTABLE);

        currentBuild = whenBuilding();

        final String log = logFor(currentBuild);
        assertNothingRevertedSince(ONE_COMMIT);
        assertBuildStatus(UNSTABLE, currentBuild);
        assertThatStringContainsTimes(log, ONE_REVERTED_REVISION, 0);
    }

    private FreeStyleBuild whenFileChangedDuringBuilding(final String file) throws Exception, InterruptedException,
            ExecutionException {
        final Future<FreeStyleBuild> future = job.scheduleBuild2(1);
        givenChangesInSubversionIn(file);
        return future.get();
    }

    private void givenSubversionScmWithOneModule() throws Exception {
        final File repo = getRepoWithTwoModules();
        final String repoUrl = "file://" + repo.getPath();
        svnUrl = repoUrl + "/" + MODULE_1;
        scm = new SubversionSCM(svnUrl);
        rootScm = new SubversionSCM(repoUrl);
    }

    private void givenChangesInSubversionIn(final String... file) throws Exception {
        modifyAndCommit(file);
    }

    private void givenTwoChangesInSubversionIn(final String file) throws Exception {
        modifyAndCommit(file);
        modifyAndCommit(file);
    }

    private void givenJobWithTwoModulesInSameRepository() throws Exception, IOException {
        givenJobWithOneModule();
        final File repo = getRepoWithTwoModules();
        svnUrl = "file://" + repo.getPath();
        final String[] svnUrls = new String[]{ svnUrl + "/" + MODULE_1, svnUrl + "/" + MODULE_2 };
        final String[] repoLocations= new String[]{ MODULE_1, MODULE_2 };
        scm = new SubversionSCM(svnUrls, repoLocations, true, null);
        rootScm = new SubversionSCM(svnUrl);
        job.setScm(scm);
    }

    private void givenPreviousBuildSuccessful() throws Exception {
        assertBuildStatusSuccess(whenBuilding());
    }

    private void givenNextBuildWillBe(final Result result) throws Exception {
        job.getBuildersList().add(new MockBuilder(result));
    }

    private void givenJobWithNullScm() throws Exception {
        job = createFreeStyleProject("no-scm-job");
        job.getPublishersList().add(new JenkinsGlue());
        job.setAssignedLabel(hudson.getSelfLabel());
        job.setScm(new NullSCM());
    }

    private void givenJobWithOneModule() throws Exception {
        job = createFreeStyleProject("subversion-scm-job");
        job.getPublishersList().add(new JenkinsGlue());
        job.setAssignedLabel(hudson.getSelfLabel());
        job.setScm(scm);
    }

    private FreeStyleBuild whenPreviousJobSuccessfulAndCurrentUnstable() throws Exception,
            InterruptedException, ExecutionException {
        givenPreviousBuildSuccessful();
        givenChangesInSubversionIn(MODIFIED_FILE_IN_MODULE_1);
        givenNextBuildWillBe(UNSTABLE);
        return whenBuilding();
    }

    private FreeStyleBuild whenPreviousJobSuccesfulAndCurrentUnstableWithTwoChanges()
            throws Exception {
        givenPreviousBuildSuccessful();
        givenTwoChangesInSubversionIn(MODIFIED_FILE_IN_MODULE_1);
        givenNextBuildWillBe(UNSTABLE);
        return whenBuilding();
    }

    private void assertNothingRevertedSince(final long revisionNumber) throws Exception {
        assertEquals("HEAD revision", revisionNumber, getHeadSvnRevision());
    }

    private void assertFileReverted(final String path)
            throws IOException, InterruptedException, ExecutionException, Exception {

        final FreeStyleBuild build = getIndependentSubversionBuild(rootScm);
        final FilePath file = build.getWorkspace().child(path);
        assertFalse("File '" + path + "' is not reverted (because it exists)", file.exists());
    }

    private void assertThatStringContainsTimes(
            final String log, final String string, final int times) {
        final int actualTimes = log.split(string).length - 1;
        assertThat(actualTimes, is(times));

    }

    /**
     * Repo at revision 1 with structure
     *   module1/
     *           file1
     *   module2/
     *           file2
     */
    private File getRepoWithTwoModules() throws Exception {
        return new CopyExisting(getClass().getResource("repoWithTwoModules.zip")).allocate();
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

    private FreeStyleBuild whenBuilding() throws Exception {
        return job.scheduleBuild2(0).get();
    }

    private void modifyAndCommit(final String... paths) throws Exception {
        final FreeStyleBuild build = getIndependentSubversionBuild(rootScm);
        final SVNClientManager svnm = SubversionSCM.createSvnClientManager((AbstractProject) null);

        final List<File> filesToCommit = Lists.newArrayList();
        for (final String path : paths) {
            final FilePath file = build.getWorkspace().child(path);
            if (!file.exists()) {
                file.touch(System.currentTimeMillis());
                svnm.getWCClient().doAdd(new File(file.getRemote()), false, false, false,
                        SVNDepth.INFINITY, false, false);
            } else {
                file.write("random content", "UTF-8");
            }
            filesToCommit.add(new File(file.getRemote()));
        }

        svnm.getCommitClient().doCommit(filesToCommit.toArray(new File[0]), false,
                "test changes", null, null, false, false, SVNDepth.EMPTY);
    }

    private FreeStyleBuild getIndependentSubversionBuild(final SubversionSCM scm) throws IOException,
            Exception, InterruptedException, ExecutionException {
        final FreeStyleProject forCommit = createFreeStyleProject();
        forCommit.setScm(scm);
        forCommit.setAssignedLabel(hudson.getSelfLabel());
        final FreeStyleBuild build = assertBuildStatusSuccess(forCommit.scheduleBuild2(0).get());
        return build;
    }

    private long getHeadSvnRevision() throws Exception {
        final SVNClientManager svnm = SubversionSCM.createSvnClientManager((AbstractProject) null);
        final FreeStyleBuild build = getIndependentSubversionBuild(rootScm);
        final File workspace = new File(build.getWorkspace().getRemote());
        final SVNStatus status = svnm.getStatusClient().doStatus(workspace, true);
        return status.getRevision().getNumber();
    }

}
