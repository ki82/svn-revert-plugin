package jenkins.plugins.svn_revert;

import java.io.File;
import java.util.Collections;

import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNCommitClient;
import org.tmatesoft.svn.core.wc.SVNDiffClient;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNRevisionRange;

class SvnKitClient {

    private final SVNClientManager clientManager;

    SvnKitClient(final SVNClientManager clientManager) {
        this.clientManager = clientManager;
    }

    void merge(final int fromRevision, final int toRevision, final SVNURL svnurl, final File file)
            throws SVNException {
        final SVNRevisionRange range = new SVNRevisionRange(
                SVNRevision.create(fromRevision), SVNRevision.create(toRevision));
        final SVNDiffClient diffClient = clientManager.getDiffClient();
        diffClient.doMerge(svnurl, SVNRevision.create(fromRevision),
                Collections.singleton(range), file, SVNDepth.INFINITY,
                true, false, false, false);
    }

    @SuppressWarnings("deprecation")
    void commit(final File workspace) throws SVNException {
        final SVNCommitClient commitClient = clientManager.getCommitClient();
        commitClient.doCommit(new File[] { workspace }, true, "Reverted", false, true);

    }


}
