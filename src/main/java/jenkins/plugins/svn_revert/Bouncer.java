package jenkins.plugins.svn_revert;

import hudson.Launcher;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.Run;
import hudson.scm.SubversionSCM;

import java.io.IOException;

class Bouncer {

    static boolean throwOutIfUnstable(final AbstractBuild<?, ?> build, final Launcher launcher,
            final Messenger messenger, final SvnReverter svnReverter, final Claimer claimer,
            final ChangeLocator changeLocator, final RevertMailSender mailer) throws InterruptedException, IOException {

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
        if (noChangesIn(build)) {
            messenger.informNoChanges();
            return true;
        }
        final SubversionSCM subversionScm = getSubversionScm(build);
        if (changeLocator.changesOutsideWorkspace(subversionScm)) {
            messenger.informChangesOutsideWorkspace();
            return true;
        }

        if (svnReverter.revert(subversionScm)) {
            claimer.claim(build);
            mailer.sendRevertMail(build);
            return true;
        }
        return false;
    }

    private static boolean isNotSubversionJob(final AbstractBuild<?, ?> abstractBuild) {
        return !(abstractBuild.getProject().getRootProject().getScm() instanceof SubversionSCM);
    }

    private static boolean currentBuildNotUnstable(final AbstractBuild<?, ?> abstractBuild) {
        return abstractBuild.getResult() != Result.UNSTABLE;
    }

    private static boolean previousBuildNotSuccessful(final AbstractBuild<?, ?> abstractBuild) {
        return !previousBuildSuccessful(abstractBuild);
    }

    private static boolean noChangesIn(final AbstractBuild<?, ?> build) {
        return build.getChangeSet().isEmptySet();
    }

    private static SubversionSCM getSubversionScm(final AbstractBuild<?, ?> abstractBuild) {
        return SubversionSCM.class.cast(abstractBuild.getProject().getRootProject().getScm());
    }

    private static boolean previousBuildSuccessful(final AbstractBuild<?, ?> abstractBuild) {
        final Run<?, ?> previousBuild = abstractBuild.getPreviousBuild();
        if (previousBuild != null) {
            if (previousBuild.isBuilding()) {
                return false;
            }
            return previousBuild.getResult() == Result.SUCCESS;
        }
        return false;
    }
}
