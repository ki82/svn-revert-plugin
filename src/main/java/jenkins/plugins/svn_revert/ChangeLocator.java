package jenkins.plugins.svn_revert;

import hudson.EnvVars;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.scm.ChangeLogSet.AffectedFile;
import hudson.scm.ChangeLogSet.Entry;
import hudson.scm.SubversionSCM;
import hudson.scm.SubversionSCM.ModuleLocation;

import java.io.IOException;
import java.util.List;

import org.tmatesoft.svn.core.SVNException;

import com.google.common.collect.Lists;

class ChangeLocator {

    private final AbstractBuild<?, ?> build;
    private final BuildListener listener;

    ChangeLocator(final AbstractBuild<?, ?> build, final BuildListener listener) {
        this.build = build;
        this.listener = listener;
    }

    boolean changesOutsideWorkspace(final SubversionSCM subversionScm) throws IOException, InterruptedException {
        final List<String> modulePaths = Lists.newArrayList();
        try {
            for (final ModuleLocation moduleLocation : getModuleLocations(subversionScm)) {
                final String fullUrl = moduleLocation.getURL();
                final String repositoryUrl = moduleLocation.getRepositoryRoot(build.getProject().getRootProject()).toString();
                String moduleRepoPath;
                if (fullUrl.startsWith(repositoryUrl)) {
                    moduleRepoPath = fullUrl.substring(repositoryUrl.length());
                    modulePaths.add(moduleRepoPath);
                } else {
                    throw new IllegalStateException("Module not in repo root (?)");
                }
            }
        } catch (final SVNException e) {
            e.printStackTrace();
            return true;
        }
        for (final Entry change : build.getChangeSet()) {
            for (final AffectedFile affectedFile : change.getAffectedFiles()) {
                if (!fileInWorkspace(modulePaths, affectedFile)) {
                    return true;
                }
            }
        }
        return false;
    }


    private List<ModuleLocation> getModuleLocations(final SubversionSCM subversionScm)
            throws IOException, InterruptedException {
        final EnvVars envVars = build.getEnvironment(listener);
        return Lists.newArrayList(subversionScm.getLocations(envVars, build));
    }

    private boolean fileInWorkspace(final List<String> modulePaths, final AffectedFile affectedFile) {
        for (final String modulePath : modulePaths) {
            if (affectedFile.getPath().startsWith(modulePath)) {
                return true;
            }
        }
        return false;
    }


}
