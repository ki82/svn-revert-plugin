package jenkins.plugins.svn_revert;

import hudson.model.AbstractBuild;
import hudson.scm.ChangeLogSet.AffectedFile;
import hudson.scm.ChangeLogSet.Entry;

import java.util.List;

import com.google.common.collect.Lists;

class ChangedFiles {

    private final AbstractBuild<?, ?> build;

    ChangedFiles(final AbstractBuild<?, ?> build) {
        this.build = build;
    }

    List<String> getRepositoryPathsFor() {
        final List<String> filePaths = Lists.newLinkedList();
        for (final Entry change : build.getChangeSet()) {
            for (final AffectedFile affectedFile : change.getAffectedFiles()) {
                filePaths.add(affectedFile.getPath());
            }
        }
        return filePaths;
    }

}
