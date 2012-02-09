package jenkins.plugins.svn_revert;

import hudson.EnvVars;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.scm.SubversionSCM;
import hudson.scm.SubversionSCM.ModuleLocation;

import java.io.File;
import java.io.IOException;

import org.tmatesoft.svn.core.SVNException;

class SvnReverter {

    private final Messenger messenger;
    private final AbstractBuild<?, ?> build;
    private final BuildListener listener;
    private SvnKitClient svnKitClient;
    private final SvnKitClientFactory svnFactory;

    SvnReverter(final AbstractBuild<?,?> build, final BuildListener listener,
            final Messenger messenger, final SvnKitClientFactory svnFactory) {
        this.build = build;
        this.listener = listener;
        this.messenger = messenger;
        this.svnFactory = svnFactory;
    }

    boolean revert(final SubversionSCM subversionScm) {
        final AbstractProject<?, ?> rootProject = build.getProject().getRootProject();

        try {
            return revertAndCommit(rootProject, subversionScm);
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

    private boolean revertAndCommit(final AbstractProject<?, ?> rootProject,
            final SubversionSCM subversionScm)
    throws NoSvnAuthException, IOException, InterruptedException, SVNException {
        svnKitClient = svnFactory.create(rootProject, subversionScm);

        final EnvVars envVars = build.getEnvironment(listener);
        final int revisionNumber = Integer.parseInt(envVars.get("SVN_REVISION"));

        final ModuleLocation moduleLocation = subversionScm.getLocations(envVars, build)[0];

        final File moduleDir = new File(build.getWorkspace() + File.separator + moduleLocation.getLocalDir());
        svnKitClient.merge(revisionNumber, revisionNumber - 1, moduleLocation.getSVNURL(), moduleDir);

        svnKitClient.commit(moduleDir);

        messenger.informReverted(revisionNumber, revisionNumber - 1, moduleLocation.getURL());

        return true;
    }


}
