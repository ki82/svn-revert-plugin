package jenkins.plugins.svn_revert;

import hudson.model.AbstractBuild;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

class RevertMailFormatter {

    private final ChangedRevisions changedRevisions;

    RevertMailFormatter(final ChangedRevisions changedRevisions) {
        this.changedRevisions = changedRevisions;
    }

    public MimeMessage format(final MimeMessage mail, final AbstractBuild<?, ?> build)
            throws MessagingException {
        final String revisions = changedRevisions.getFor(build).getAllInOrderAsString();
        mail.setSubject(String.format("Reverted revision(s): %s", revisions));
        mail.setText(String.format("Revision(s) %s was reverted since they made the build became UNSTABLE.", revisions));
        return mail;
    }

}
