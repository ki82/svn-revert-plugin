Jenkins SVN Revert Plugin
=========================

A plugin for Jenkins CI that automatically reverts SVN commits for a build if build status is changed from successful to unstable.

Known Limitations
-----------------
It currently only works for jobs with a **single module**, or with multiple modules from the **same repository**.

ChangeLog
---------
### v0.3

- Integration with Claim Plugin, making non-sticky claims when bad commits have been reverted.

### v0.2

- Support for reverting multiple commits.
- Support for reverting commits in multiple modules (but still in the same repository).
- Not reverting if files are out of date.

### v0.1

- Initial version. Support for single modules.
