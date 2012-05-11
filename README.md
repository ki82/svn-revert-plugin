Jenkins SVN Revert Plugin
=========================

A plugin for Jenkins CI that automatically reverts SVN commits for a build if build status is changed from successful to unstable.

Known Limitations
-----------------
It currently only works for jobs with a **single module**, or with multiple modules from the **same repository**.

Change Log
----------
### 1.0 (11 May 2012)

- Job name is now included in revert commit message.
- Fixed an issue where builds would fail when revert failed.

### 0.7 (28 Feb 2012)

- ~~No longer failing build if revert failed.~~

### v0.6 (22 Feb 2012)

- Only reverting changes if all changes in the commit are in the workspace checked out by Jenkins.
- No longer claiming/sending mail when files to revert are out of date.

### v0.5 (20 Feb 2012)

- Fixed multi-module support, which has been broken.

### v0.4 (17 Feb 2012)

- Support for e-mailing to committers when reverting.

### v0.3 (16 Feb 2012)

- Integration with Claim Plugin, making non-sticky claims when bad commits have been reverted.
- Removed ability to configure commit message when reverting.

### v0.2 (16 Feb 2012)

- Support for reverting multiple commits.
- ~~Support for reverting commits in multiple modules (but still in the same repository).~~
- Not reverting if files are out of date.

### v0.1 (10 Feb 2012)

- Initial version. Support for single modules.
