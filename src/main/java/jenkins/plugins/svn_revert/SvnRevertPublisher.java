package jenkins.plugins.svn_revert;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;

import org.kohsuke.stapler.DataBoundConstructor;

public class SvnRevertPublisher extends Notifier {

    private final String revertMessage;

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        System.out.println("REVERTING");
        return null;
    }

    public String getRevertMessage() {
        return "Revert message";
    }

    @DataBoundConstructor
    public SvnRevertPublisher(final String revertMessage) {
        this.revertMessage = revertMessage;
    }

    @Override
    public SvnRevertDescriptorImpl getDescriptor() {
        return (SvnRevertDescriptorImpl)super.getDescriptor();
    }


    @Extension
    public static final class SvnRevertDescriptorImpl extends BuildStepDescriptor<Publisher> {

        @Override
        public boolean isApplicable(final Class<? extends AbstractProject> arg0) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Display name";
        }

        public String getRevertMessage() {
            return "Revert message 2";
        }

    }
}
