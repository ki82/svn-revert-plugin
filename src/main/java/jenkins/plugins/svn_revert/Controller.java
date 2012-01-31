package jenkins.plugins.svn_revert;

import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;

class Controller {

    static boolean perform(final AbstractBuild<?, ?> abstractBuild, final Launcher launcher,
            final BuildListener buildListener, final SvnReverter svnReverter,
            final Messenger messenger) {

        if (abstractBuild.getResult() != Result.UNSTABLE) {
            messenger.informBuildStatusNotUnstable();
            return true;
        }
        if (previousBuildStatus(abstractBuild) != Result.SUCCESS) {
            messenger.informPreviousBuildStatusNotSuccess();
            return true;
        }

        return svnReverter.revert();
    }

    private static Result previousBuildStatus(final AbstractBuild<?, ?> abstractBuild) {
        return abstractBuild.getPreviousBuiltBuild().getResult();
    }
}
