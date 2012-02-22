package jenkins.plugins.svn_revert;

import hudson.model.AbstractBuild;
import hudson.scm.SubversionSCM;

import java.io.IOException;
import java.util.List;

import org.tmatesoft.svn.core.SVNException;

import com.google.common.collect.Lists;

class ChangeLocator {

    private final AbstractBuild<?, ?> build;
    private final ModuleFinder locationFinder;
    private final ChangedFiles changedFiles;

    ChangeLocator(final AbstractBuild<?, ?> build, final ModuleFinder locationFinder,
            final ChangedFiles changedFiles) {
        this.build = build;
        this.locationFinder = locationFinder;
        this.changedFiles = changedFiles;
    }

    boolean changesOutsideWorkspace(final SubversionSCM subversionScm) throws IOException, InterruptedException {
        final List<String> modulePaths = Lists.newArrayList();
        try {
            return changedFilesMatchesModules(subversionScm, modulePaths);
        } catch (final SVNException e) {
            return true;
        }
    }

    private boolean changedFilesMatchesModules(final SubversionSCM subversionScm,
            final List<String> modulePaths) throws IOException, InterruptedException, SVNException {
        for (final Module module : locationFinder.getModules(subversionScm)) {
            modulePaths.add(module.getRepositoryPath(build));
        }
        for (final String filePath : changedFiles.getRepositoryPathsFor(build)) {
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
