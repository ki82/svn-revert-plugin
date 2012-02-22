package jenkins.plugins.svn_revert;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.scm.SubversionSCM.ModuleLocation;

import java.io.File;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;

class Module {

    private final ModuleLocation moduleLocation;


    Module(final ModuleLocation moduleLocation) {
        this.moduleLocation = moduleLocation;
    }

    File getModuleRoot(final AbstractBuild<?, ?> build) {
        return new File(build.getWorkspace() + File.separator + moduleLocation.getLocalDir());
    }

    SVNURL getSvnUrl() throws SVNException {
        return moduleLocation.getSVNURL();
    }

    String getURL() {
        return moduleLocation.getURL();
    }

    String getRepositoryRoot(final AbstractProject<?, ?> rootProject) throws SVNException {
        return moduleLocation.getRepositoryRoot(rootProject).toString();
    }

    String getRepositoryPath(final AbstractBuild<?, ?> build) throws SVNException {
        final String fullUrl = getURL();
        final String repositoryUrl = getRepositoryRoot(build.getProject().getRootProject());
        if (fullUrl.startsWith(repositoryUrl)) {
            return fullUrl.substring(repositoryUrl.length());
        } else {
            throw new IllegalStateException("Module not in repo root (?)");
        }
    }

    ModuleLocation getModuleLocation() {
        return moduleLocation;
    }
}
