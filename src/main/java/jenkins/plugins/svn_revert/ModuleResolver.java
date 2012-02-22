package jenkins.plugins.svn_revert;

import hudson.model.AbstractBuild;
import hudson.scm.SubversionSCM.ModuleLocation;

import java.io.File;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;

class ModuleResolver {

    File getModuleRoot(final AbstractBuild<?, ?> build, final ModuleLocation moduleLocation) {
        return new File(build.getWorkspace() + File.separator + moduleLocation.getLocalDir());
    }

    SVNURL getSvnUrl(final ModuleLocation moduleLocation) throws SVNException {
        return moduleLocation.getSVNURL();
    }

}
