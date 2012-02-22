package jenkins.plugins.svn_revert;

import java.io.PrintStream;

class Messenger {

    static final String BUILD_STATUS_NOT_UNSTABLE =
            "Will not revert since build status is not UNSTABLE.";
    static final String PREVIOUS_BUILD_STATUS_NOT_SUCCESS =
            "Will not revert since previous build status is not SUCCESS.";
    static final String NOT_SUBVERSION_SCM =
            "The Subversion Revert Plugin can only be used with Subversion SCM.";
    static final String NO_SVN_AUTH_PROVIDER = "No Subversion credentials available.";
    static final String REVERTED_CHANGES =
            "Reverted changes between %d:%d in %s since build became UNSTABLE.\n";
    static final String NO_CHANGES =
            "Will not revert since there are no changes in current build.";
    static final String FILES_TO_REVERT_OUT_OF_DATE =
            "Tried to revert since build status became UNSTABLE, " +
            "but failed since files to revert are out of date.";
    static final String CHANGES_OUTSIDE_WORKSPACE =
            "Will not revert since some changes in commit(s) outside workspace detected.";
    private final PrintStream logger;

    Messenger(final PrintStream logger) {
        this.logger = logger;
    }

    void informBuildStatusNotUnstable() {
        logger.println(BUILD_STATUS_NOT_UNSTABLE);
    }

    void informPreviousBuildStatusNotSuccess() {
        logger.println(PREVIOUS_BUILD_STATUS_NOT_SUCCESS);
    }

    void informNotSubversionSCM() {
        logger.println(NOT_SUBVERSION_SCM);
    }

    void informNoSvnAuthProvider() {
        logger.println(NO_SVN_AUTH_PROVIDER);
    }

    void informReverted(final Revisions revisions, final String repository) {
        logger.format(REVERTED_CHANGES, revisions.getBefore(), revisions.getLast(), repository);
    }

    void informNoChanges() {
        logger.println(NO_CHANGES);
    }

    void informFilesToRevertOutOfDate() {
        logger.println(FILES_TO_REVERT_OUT_OF_DATE);
    }

    void informChangesOutsideWorkspace() {
        logger.println(CHANGES_OUTSIDE_WORKSPACE);
    }

    void printStackTraceFor(final Exception exception) {
        exception.printStackTrace(logger);
    }

    void log(final String string) {
        logger.println(string);
    }

}
