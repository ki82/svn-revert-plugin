package jenkins.plugins.svn_revert;

import hudson.model.AbstractProject;
import hudson.scm.SubversionSCM;

import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationProvider;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

class SvnClientManagerFactory {

    SVNClientManager create(final AbstractProject<?, ?> project,
            final SubversionSCM scm) throws NoSvnAuthException {
        final ISVNAuthenticationProvider svnAuthProvider =
                scm.getDescriptor().createAuthenticationProvider(project);
        if (svnAuthProvider == null) {
            throw new NoSvnAuthException();
        }
        final ISVNAuthenticationManager svnAuthManager =
                SVNWCUtil.createDefaultAuthenticationManager();
        svnAuthManager.setAuthenticationProvider(svnAuthProvider);
        return SVNClientManager.newInstance(null, svnAuthManager);
    }
}
