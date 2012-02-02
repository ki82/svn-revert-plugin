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

    private final String revertMessage;

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    public String getRevertMessage() {
        return revertMessage;
    }

    @DataBoundConstructor
    public JenkinsGlue(final String revertMessage) {
        this.revertMessage = revertMessage;
    }

    @Override
    public SvnRevertDescriptorImpl getDescriptor() {
        return (SvnRevertDescriptorImpl)super.getDescriptor();
    }

    @Override
    public boolean perform(final AbstractBuild<?, ?> abstractBuild,
            final Launcher launcher,
            final BuildListener buildListener)
                    throws InterruptedException, IOException {
        final Messenger messenger = new Messenger(buildListener.getLogger());
        return Controller.perform(abstractBuild, launcher, buildListener,
                new SvnReverter(abstractBuild, messenger), messenger);
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

        public String getRevertMessage() {
            return "Revert message 2";
        }

    }

}
