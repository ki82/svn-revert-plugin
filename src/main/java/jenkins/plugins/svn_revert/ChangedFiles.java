package jenkins.plugins.svn_revert;

import hudson.model.AbstractBuild;
import hudson.scm.ChangeLogSet.AffectedFile;
import hudson.scm.ChangeLogSet.Entry;

import java.util.List;

import com.google.common.collect.Lists;

class ChangedFiles {

    List<String> getRepositoryPathsFor(final AbstractBuild<?, ?> build) {
        final List<String> filePaths = Lists.newLinkedList();
        for (final Entry change : build.getChangeSet()) {
            for (final AffectedFile affectedFile : change.getAffectedFiles()) {
                filePaths.add(affectedFile.getPath());
            }
        }
        return filePaths;
    }

}
