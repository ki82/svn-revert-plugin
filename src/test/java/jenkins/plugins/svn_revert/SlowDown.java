package jenkins.plugins.svn_revert;

import static org.junit.Assert.fail;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;

import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@SuppressWarnings({ "rawtypes" })
public class SlowDown extends BuildWrapper {


    private final Semaphore semaphore;
    private final boolean waitFailed = false;

    public SlowDown(final Semaphore semaphore) {
        this.semaphore = semaphore;
    }

    @Override
    public SlowDownDescriptorImpl getDescriptor() {
        return (SlowDownDescriptorImpl) super.getDescriptor();
    }

    @Override
    public Environment setUp(final AbstractBuild build, final Launcher launcher,
            final BuildListener listener) throws IOException, InterruptedException {
        try {
            if (!semaphore.tryAcquire(10L, TimeUnit.SECONDS)) {
                fail("Unable to complete build. Blocked because test semaphore never released.");
            }
        } catch (final InterruptedException e) {
        }
        return new Environment() {
        };
    }

    @Extension
    public static final class SlowDownDescriptorImpl extends BuildWrapperDescriptor {

        @Override
        public String getDisplayName() {
            return "Slowdown";
        }

        @Override
        public boolean isApplicable(final AbstractProject<?, ?> item) {
            return true;
        }

    }

}