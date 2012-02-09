package jenkins.plugins.svn_revert;

import hudson.Launcher;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.Run;
import hudson.scm.SubversionSCM;

class Bouncer {

    static boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher,
            final Messenger messenger, final SvnReverter svnReverter) {

        if (isNotSubversionJob(build)) {
            messenger.informNotSubversionSCM();
            return true;
        }
        if (currentBuildNotUnstable(build)) {
            messenger.informBuildStatusNotUnstable();
            return true;
        }
        if (previousBuildNotSuccessful(build)) {
            messenger.informPreviousBuildStatusNotSuccess();
            return true;
        }

        return svnReverter.revert(getSubversionScm(build));
    }

    public static SubversionSCM getSubversionScm(final AbstractBuild<?, ?> abstractBuild) {
        return SubversionSCM.class.cast(abstractBuild.getProject().getRootProject().getScm());
    }

    public static boolean isNotSubversionJob(final AbstractBuild<?, ?> abstractBuild) {
        return !(abstractBuild.getProject().getRootProject().getScm() instanceof SubversionSCM);
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
