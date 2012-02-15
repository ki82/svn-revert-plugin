package jenkins.plugins.svn_revert;

import java.io.File;
import java.io.IOException;
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

    void merge(final Revisions revisions, final SVNURL svnurl,
            final File moduleDirectory)
    throws SVNException, IOException {
        final SVNRevisionRange range = new SVNRevisionRange(
                SVNRevision.create(revisions.getLast()), SVNRevision.create(revisions.getBefore()));
        final SVNDiffClient diffClient = clientManager.getDiffClient();
        diffClient.doMerge(svnurl, SVNRevision.create(revisions.getLast()),
                Collections.singleton(range), moduleDirectory.getCanonicalFile(), SVNDepth.INFINITY,
                true, false, false, false);
    }

    @SuppressWarnings("deprecation")
    void commit(final String revertMessage, final File... moduleDirectories) throws SVNException, IOException {
        final SVNCommitClient commitClient = clientManager.getCommitClient();
        commitClient.doCommit(getCanonicalFiles(moduleDirectories), true,
                revertMessage, false, true);
    }

    private File[] getCanonicalFiles(final File... moduleDirectories) throws IOException {
        final File[] canonicalDirectories = new File[moduleDirectories.length];
        for (int i = 0; i < moduleDirectories.length; i++) {
            canonicalDirectories[i] = moduleDirectories[i].getCanonicalFile();
        }
        return canonicalDirectories;
    }

}
