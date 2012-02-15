Jenkins SVN Revert Plugin
=========================

A plugin for Jenkins CI that automatically reverts SVN commits for a build if build status is changed from successful to unstable.

Known limitations
-----------------
It currently only works for jobs with a single module, or with multiple modules from the same repository.
