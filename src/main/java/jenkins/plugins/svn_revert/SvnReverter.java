package jenkins.plugins.svn_revert;

import hudson.EnvVars;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.scm.SubversionSCM;
import hudson.scm.SubversionSCM.ModuleLocation;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.tmatesoft.svn.core.SVNException;

import com.google.common.collect.Lists;

class SvnReverter {

    private final Messenger messenger;
    private final AbstractBuild<?, ?> build;
    private final BuildListener listener;
    private SvnKitClient svnKitClient;
    private final SvnKitClientFactory svnFactory;
    private final ModuleResolver moduleResolver;
    private final String revertMessage;
    private final ChangedRevisions changedRevisions;

    SvnReverter(final AbstractBuild<?,?> build, final BuildListener listener,
            final Messenger messenger, final SvnKitClientFactory svnFactory,
            final ModuleResolver moduleResolver, final String revertMessage,
            final ChangedRevisions changedRevisions) {
        this.build = build;
        this.listener = listener;
        this.messenger = messenger;
        this.svnFactory = svnFactory;
        this.moduleResolver = moduleResolver;
        this.revertMessage = revertMessage;
        this.changedRevisions = changedRevisions;
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
        final Revisions revisions = changedRevisions.getFor(build);

        final List<ModuleLocation> moduleLocations =
                Lists.newArrayList(subversionScm.getLocations(envVars, build));
        final List<File> moduleDirs = Lists.newArrayList();
        for (final ModuleLocation moduleLocation : moduleLocations) {
            final File moduleDir = moduleResolver.getModuleRoot(build, moduleLocation);

            svnKitClient.merge(revisions, moduleResolver.getSvnUrl(moduleLocation), moduleDir);

            moduleDirs.add(moduleDir);
        }

        if (svnKitClient.commit(revertMessage, moduleDirs.toArray(new File[0]))) {
            informReverted(revisions, moduleLocations);
        } else {
            messenger.informFilesToRevertOutOfDate();
        }


        return true;
    }

    private void informReverted(final Revisions revisions, final List<ModuleLocation> moduleLocations) {
        for (final ModuleLocation moduleLocation : moduleLocations) {
            messenger.informReverted(revisions, moduleLocation.getURL());
        }
    }

}
