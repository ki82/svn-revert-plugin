package jenkins.plugins.svn_revert;

import hudson.EnvVars;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.scm.SubversionSCM;
import hudson.scm.SubversionSCM.ModuleLocation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

class ModuleFinder {

    private final AbstractBuild<?, ?> build;
    private final BuildListener listener;

    ModuleFinder(final AbstractBuild<?, ?> build, final BuildListener listener) {
        this.build = build;
        this.listener = listener;
    }

    List<Module> getModules(final SubversionSCM subversionScm)
            throws IOException, InterruptedException {
        final EnvVars envVars = build.getEnvironment(listener);
        final ArrayList<Module> modules = Lists.newArrayList();
        for (final ModuleLocation moduleLocation : subversionScm.getLocations(envVars, build)) {
            modules.add(new Module(moduleLocation));
        }
        return modules;
    }

}
