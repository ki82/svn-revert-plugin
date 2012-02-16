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
    public boolean perform(final AbstractBuild<?, ?> abstractBuild,
            final Launcher launcher, final BuildListener listener)
    throws InterruptedException, IOException {
        final Messenger messenger = new Messenger(listener.getLogger());
        final ChangedRevisions changedRevisions = new ChangedRevisions();
        final SvnReverter svnReverter = new SvnReverter(abstractBuild, listener, messenger,
                new SvnKitClientFactory(), new ModuleResolver(), changedRevisions);
        return Bouncer.throwOutIfUnstable(abstractBuild, launcher, messenger, svnReverter,
                new Claimer(changedRevisions));
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
