package jenkins.plugins.svn_revert;

import hudson.model.AbstractBuild;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.Entry;

import java.util.List;

import com.google.common.collect.Lists;

class ChangedRevisions {

    Revisions getFor(final AbstractBuild<?, ?> build) {
        final ChangeLogSet<? extends Entry> cs = build.getChangeSet();
        final List<Integer> revisions = Lists.newArrayList();
        for (final Entry entry : cs) {
            revisions.add(Integer.parseInt(entry.getCommitId(), 10));
        }
        return Revisions.create(revisions);
    }
}