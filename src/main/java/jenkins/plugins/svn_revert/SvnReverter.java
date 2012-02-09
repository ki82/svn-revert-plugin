package jenkins.plugins.svn_revert;

import hudson.EnvVars;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.scm.SubversionSCM;
import hudson.scm.SubversionSCM.ModuleLocation;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNDiffClient;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNRevisionRange;

class SvnReverter {

    private final Messenger messenger;
    private final AbstractBuild<?, ?> build;
    private final BuildListener listener;
    private SVNClientManager svnClientManager;
    private final SvnClientManagerFactory svnFactory;

    SvnReverter(final AbstractBuild<?,?> build, final BuildListener listener,
            final Messenger messenger, final SvnClientManagerFactory svnFactory) {
        this.build = build;
        this.listener = listener;
        this.messenger = messenger;
        this.svnFactory = svnFactory;
    }

    boolean revert() {
        final AbstractProject<?, ?> rootProject = build.getProject().getRootProject();

        try {
            return revertAndCommit(rootProject);
        } catch (final RuntimeException e) {
            throw e;
        } catch (final NoSvnAuthException e) {
            messenger.informNoSvnAuthProvider();
            return false;
        } catch (final Exception e) {
            messenger.printStackTraceFor(e);
            return false;
        }
    }

    private boolean revertAndCommit(final AbstractProject<?, ?> rootProject)
    throws NoSvnAuthException, IOException, InterruptedException, SVNException {
        if (!(rootProject.getScm() instanceof SubversionSCM)) {
            messenger.informNotSubversionSCM();
            return true;
        }

        final SubversionSCM subversionScm = SubversionSCM.class.cast(rootProject.getScm());
        svnClientManager = svnFactory.create(rootProject, subversionScm);

        final EnvVars envVars = build.getEnvironment(listener);
        messenger.log(envVars.toString());
        final int revisionNumber = Integer.parseInt(envVars.get("SVN_REVISION"));
        final SVNRevisionRange range = new SVNRevisionRange(
                SVNRevision.create(revisionNumber), SVNRevision.create(revisionNumber - 1));

        final ModuleLocation moduleLocation = subversionScm.getLocations(envVars, build)[0];

        final SVNDiffClient diffClient = svnClientManager.getDiffClient();
        diffClient.doMerge(moduleLocation.getSVNURL(), SVNRevision.create(revisionNumber),
                Collections.singleton(range),
                new File(build.getModuleRoot().absolutize().toString()),
                SVNDepth.INFINITY, true, false, false, false);
        return true;
    }


}
