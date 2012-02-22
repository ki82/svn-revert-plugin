package jenkins.plugins.svn_revert;

import hudson.model.AbstractBuild;
import hudson.scm.SubversionSCM;
import hudson.scm.SubversionSCM.ModuleLocation;

import java.io.IOException;
import java.util.List;

import org.tmatesoft.svn.core.SVNException;

import com.google.common.collect.Lists;

class ChangeLocator {

    private final AbstractBuild<?, ?> build;
    private final ModuleLocationFinder locationFinder;
    private final ChangedFiles changedFiles;

    ChangeLocator(final AbstractBuild<?, ?> build, final ModuleLocationFinder locationFinder,
            final ChangedFiles changedFiles) {
        this.build = build;
        this.locationFinder = locationFinder;
        this.changedFiles = changedFiles;
    }

    boolean changesOutsideWorkspace(final SubversionSCM subversionScm) throws IOException, InterruptedException {
        final List<String> modulePaths = Lists.newArrayList();
        try {
            for (final ModuleLocation moduleLocation : locationFinder.getModuleLocations(subversionScm)) {
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
        for (final String filePath : changedFiles.getFilenamesFor(build)) {
            if (!fileInWorkspace(modulePaths, filePath)) {
                return true;
            }
        }
        return false;
    }

    private boolean fileInWorkspace(final List<String> modulePaths, final String filePath) {
        for (final String modulePath : modulePaths) {
            if (filePath.startsWith(modulePath)) {
                return true;
            }
        }
        return false;
    }

}
