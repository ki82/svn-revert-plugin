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

        if (currentBuildNotUnstable(abstractBuild)) {
            messenger.informBuildStatusNotUnstable();
            return true;
        }
        if (previousBuildNotSuccessful(abstractBuild)) {
            messenger.informPreviousBuildStatusNotSuccess();
            return true;
        }

        return svnReverter.revert();
    }

    private static boolean currentBuildNotUnstable(final AbstractBuild<?, ?> abstractBuild) {
        return abstractBuild.getResult() != Result.UNSTABLE;
    }

    private static boolean previousBuildNotSuccessful(final AbstractBuild<?, ?> abstractBuild) {
        return !previousBuildSuccessful(abstractBuild);
    }

    private static boolean previousBuildSuccessful(final AbstractBuild<?, ?> abstractBuild) {
        final Run<?, ?> previousBuiltBuild = abstractBuild.getPreviousBuiltBuild();
        if (previousBuiltBuild != null) {
            return previousBuiltBuild.getResult() == Result.SUCCESS;
        }
        return false;
    }
}
