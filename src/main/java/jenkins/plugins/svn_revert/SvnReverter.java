package jenkins.plugins.svn_revert;

import hudson.EnvVars;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.Entry;
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

    SvnReverter(final AbstractBuild<?,?> build, final BuildListener listener,
            final Messenger messenger, final SvnKitClientFactory svnFactory,
            final ModuleResolver moduleResolver, final String revertMessage) {
        this.build = build;
        this.listener = listener;
        this.messenger = messenger;
        this.svnFactory = svnFactory;
        this.moduleResolver = moduleResolver;
        this.revertMessage = revertMessage;
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
        final int revisionNumber = getChangeRevisions().get(0);

        final ModuleLocation moduleLocation = subversionScm.getLocations(envVars, build)[0];
        final File moduleDir = moduleResolver.getModuleRoot(build, moduleLocation);

        svnKitClient.merge(revisionNumber, revisionNumber - 1, moduleResolver.getSvnUrl(moduleLocation), moduleDir);
        svnKitClient.commit(moduleDir, revertMessage);

        messenger.informReverted(revisionNumber, revisionNumber - 1, moduleLocation.getURL());

        return true;
    }

    private List<Integer> getChangeRevisions() {
        final ChangeLogSet<? extends Entry> cs = build.getChangeSet();
        final List<Integer> revisions = Lists.newArrayList();
        for (final Entry entry : cs) {
            revisions.add(Integer.parseInt(entry.getCommitId(), 10));
        }
        return revisions;
    }


}
