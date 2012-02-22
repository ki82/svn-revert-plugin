package jenkins.plugins.svn_revert;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;

import java.io.IOException;

import org.kohsuke.stapler.DataBoundConstructor;

public class JenkinsGlue extends Notifier {

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    @DataBoundConstructor
    public JenkinsGlue() {
    }

    @Override
    public SvnRevertDescriptorImpl getDescriptor() {
        return (SvnRevertDescriptorImpl)super.getDescriptor();
    }

    @Override
    public boolean perform(final AbstractBuild<?, ?> build,
            final Launcher launcher, final BuildListener listener)
    throws InterruptedException, IOException {
        final Messenger messenger = new Messenger(listener.getLogger());
        final ChangedRevisions changedRevisions = new ChangedRevisions(build);
        final ChangedFiles changedFiles = new ChangedFiles();
        final ModuleFinder locationFinder = new ModuleFinder(build, listener);
        final SvnReverter svnReverter = new SvnReverter(build, listener, messenger,
                new SvnKitClientFactory(), locationFinder, changedRevisions);
        final Claimer claimer = new Claimer(changedRevisions);
        final RevertMailSender mailer = new RevertMailSender(new RevertMailFormatter(changedRevisions), listener);
        final ChangeLocator changeLocator = new ChangeLocator(build, locationFinder, changedFiles );
        return Bouncer.throwOutIfUnstable(build, launcher, messenger, svnReverter, claimer, changeLocator, mailer);
    }

    @Extension
    public static final class SvnRevertDescriptorImpl extends BuildStepDescriptor<Publisher> {

        @Override
        public boolean isApplicable(final Class<? extends AbstractProject> arg0) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Revert commits that breaks the build";
        }

    }

}
