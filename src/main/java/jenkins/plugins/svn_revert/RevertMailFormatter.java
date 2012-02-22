package jenkins.plugins.svn_revert;

import hudson.model.AbstractBuild;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

class RevertMailFormatter {

    private final ChangedRevisions changedRevisions;

    RevertMailFormatter(final ChangedRevisions changedRevisions) {
        this.changedRevisions = changedRevisions;
    }

    public MimeMessage format(final MimeMessage mail, final AbstractBuild<?, ?> build,
            final String jenkinsUrl)
            throws MessagingException {
        final String revisions = changedRevisions.getRevisions().getAllInOrderAsString();
        mail.setSubject(String.format("Reverted revision(s): %s", revisions));
        final String jobName = build.getProject().getRootProject().getName();
        mail.setText(String.format(
                "Revision(s) %s was reverted since they made %s UNSTABLE.\n\n" +
                        "See: %s",
                revisions, jobName,  jenkinsUrl + build.getUrl()));
        return mail;
    }

}
