package jenkins.plugins.svn_revert;

import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.scm.SubversionSCM;

class SvnReverter {

    private final Messenger messenger;
    private final AbstractBuild<?, ?> build;
    private final BuildListener listener;

    SvnReverter(final AbstractBuild<?,?> build, final BuildListener listener, final Messenger messenger) {
        this.build = build;
        this.listener = listener;
        this.messenger = messenger;
    }

    boolean revert() {
        final AbstractProject<?, ?> rootProject = build.getProject().getRootProject();

        if (!(rootProject.getScm() instanceof SubversionSCM)) {
            messenger.informNotSubversionSCM();
        }

        return true;
    }

}
