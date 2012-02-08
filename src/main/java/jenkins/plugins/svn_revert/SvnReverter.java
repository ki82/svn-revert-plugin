package jenkins.plugins.svn_revert;

import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.scm.SubversionSCM;

import org.tmatesoft.svn.core.wc.SVNClientManager;

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
        } catch (final NoSvnAuthException e) {
            messenger.informNoSvnAuthProvider();
            return false;
        }
    }

    public boolean revertAndCommit(final AbstractProject<?, ?> rootProject) throws NoSvnAuthException {
        if (!(rootProject.getScm() instanceof SubversionSCM)) {
            messenger.informNotSubversionSCM();
            return true;
        }

        final SubversionSCM subversionScm = SubversionSCM.class.cast(rootProject.getScm());
        svnClientManager = svnFactory.create(rootProject, subversionScm);
        return true;
    }


}
