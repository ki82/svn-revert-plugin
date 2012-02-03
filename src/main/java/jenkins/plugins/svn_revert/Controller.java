package jenkins.plugins.svn_revert;

import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.Run;

class Controller {

    static boolean perform(final AbstractBuild<?, ?> abstractBuild, final Launcher launcher,
            final BuildListener buildListener, final SvnReverter svnReverter,
            final Messenger messenger) {

        if (!currentBuildUnstable(abstractBuild)) {
            messenger.informBuildStatusNotUnstable();
            return true;
        }
        if (!previousBuildSuccessful(abstractBuild)) {
            messenger.informPreviousBuildStatusNotSuccess();
            return true;
        }

        return svnReverter.revert();
    }

    private static boolean currentBuildUnstable(final AbstractBuild<?, ?> abstractBuild) {
        return abstractBuild.getResult() == Result.UNSTABLE;
    }

    private static boolean previousBuildSuccessful(final AbstractBuild<?, ?> abstractBuild) {
        final Run<?, ?> previousBuiltBuild = abstractBuild.getPreviousBuiltBuild();
        if (previousBuiltBuild != null) {
            return previousBuiltBuild.getResult() == Result.SUCCESS;
        }
        return false;
    }
}
