package com.ceilfors.jenkins.plugins.jiratrigger

import com.atlassian.jira.rest.client.api.domain.Issue
import hudson.EnvVars
import hudson.model.AbstractBuild
import hudson.model.EnvironmentContributingAction

/**
 * @author ceilfors
 */
class JiraIssueEnvironmentContributingAction implements EnvironmentContributingAction {

    Issue issue

    @SuppressWarnings('BuilderMethodWithSideEffects')
    @Override
    void buildEnvVars(AbstractBuild<?, ?> build, EnvVars env) {
        env.put('JIRA_ISSUE_KEY', issue.key)
    }

    @Override
    String getIconFileName() {
        null
    }

    @Override
    String getDisplayName() {
        null
    }

    @Override
    String getUrlName() {
        null
    }
}
