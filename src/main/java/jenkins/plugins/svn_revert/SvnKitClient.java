package jenkins.plugins.svn_revert;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNCommitClient;
import org.tmatesoft.svn.core.wc.SVNCommitPacket;
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

    boolean commit(final String revertMessage, final File... moduleDirectories) throws IOException, SVNException {
        final SVNCommitClient commitClient = clientManager.getCommitClient();
        final SVNCommitPacket[] commitPackets = getCommitPackets(commitClient, moduleDirectories);
        final SVNCommitInfo[] commitInfo = commitClient.doCommit(commitPackets, true, revertMessage);
        for (final SVNCommitInfo svnCommitInfo : commitInfo) {
            final SVNErrorMessage errorMessage = svnCommitInfo.getErrorMessage();
            if (errorMessage != null && errorMessage.getErrorCode() == SVNErrorCode.FS_TXN_OUT_OF_DATE) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("deprecation")
    private SVNCommitPacket[] getCommitPackets(final SVNCommitClient commitClient,
            final File... moduleDirectories) throws SVNException, IOException {
        final List<File> files = new LinkedList<File>();

        for (final File file : moduleDirectories) {
            files.add(file.getCanonicalFile());
        }
        return commitClient.doCollectCommitItems(files.toArray(new File[0]), true, false, true, true);
    }

}
