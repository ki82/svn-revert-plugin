package jenkins.plugins.svn_revert;

import hudson.EnvVars;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.scm.SubversionSCM;
import hudson.scm.SubversionSCM.ModuleLocation;

import java.io.IOException;
import java.util.List;

import com.google.common.collect.Lists;

class ModuleLocationFinder {

    private final AbstractBuild<?, ?> build;
    private final BuildListener listener;

    ModuleLocationFinder(final AbstractBuild<?, ?> build, final BuildListener listener) {
        this.build = build;
        this.listener = listener;
    }

    List<ModuleLocation> getModuleLocations(final SubversionSCM subversionScm)
            throws IOException, InterruptedException {
        final EnvVars envVars = build.getEnvironment(listener);
        return Lists.newArrayList(subversionScm.getLocations(envVars, build));
    }

}
