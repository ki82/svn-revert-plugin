Jenkins SVN Revert Plugin
=========================

A plugin for Jenkins CI that automatically reverts SVN commits for a build if build status is changed from successful to unstable.

Known Limitations
-----------------
It currently only works for jobs with a **single module**, or with multiple modules from the **same repository**.

Change Log
----------

### 1.3

- Made Claim plugin integration/dependency optional

### 1.2 (September 5, 2012)

- Option to revert changes only if a broken build contains exactly one commit.

### 1.1 (August 29, 2012)

- Builds with a commit having "revert" somewhere in the commit message will no longer revert anything.
- Plurilization in messages.

### 1.0 (May 11, 2012)

- Job name is now included in revert commit message.
- Fixed an issue where builds would fail when revert failed.

### 0.7 (Feb 28, 2012)

- ~~No longer failing build if revert failed.~~

### v0.6 (Feb 22, 2012)

- Only reverting changes if all changes in the commit are in the workspace checked out by Jenkins.
- No longer claiming/sending mail when files to revert are out of date.

### v0.5 (Feb 20, 2012)

- Fixed multi-module support, which has been broken.

### v0.4 (Feb 17, 2012)

- Support for e-mailing to committers when reverting.

### v0.3 (Feb 16, 2012)

- Integration with Claim Plugin, making non-sticky claims when bad commits have been reverted.
- Removed ability to configure commit message when reverting.

### v0.2 (Feb 16, 2012)

- Support for reverting multiple commits.
- ~~Support for reverting commits in multiple modules (but still in the same repository).~~
- Not reverting if files are out of date.

### v0.1 (Feb 10, 2012)

- Initial version. Support for single modules.
