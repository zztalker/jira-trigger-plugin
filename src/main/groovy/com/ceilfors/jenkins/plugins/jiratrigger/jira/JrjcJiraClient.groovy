package com.ceilfors.jenkins.plugins.jiratrigger.jira

import com.atlassian.jira.rest.client.api.domain.Comment
import com.atlassian.jira.rest.client.auth.BasicHttpAuthenticationHandler
import com.atlassian.jira.rest.client.internal.async.AsynchronousHttpClientFactory
import com.atlassian.jira.rest.client.internal.async.DisposableHttpClient
import com.ceilfors.jenkins.plugins.jiratrigger.JiraTriggerGlobalConfiguration
import com.google.inject.Singleton
import groovy.util.logging.Log
import hudson.Extension
import hudson.XmlFile
import hudson.model.Saveable
import hudson.model.listeners.SaveableListener

import javax.inject.Inject
import java.util.concurrent.TimeUnit

/**
 * @author ceilfors
 */
@Singleton
@Log
class JrjcJiraClient implements JiraClient {

    long timeout = 10
    TimeUnit timeoutUnit = TimeUnit.SECONDS

    JiraTriggerGlobalConfiguration jiraTriggerGlobalConfiguration

    private ExtendedJiraRestClient extendedJiraRestClient

    @Inject
    public JrjcJiraClient(JiraTriggerGlobalConfiguration jiraTriggerGlobalConfiguration) {
        this.jiraTriggerGlobalConfiguration = jiraTriggerGlobalConfiguration
    }

    protected URI getServerUri() {
        jiraTriggerGlobalConfiguration.validateConfiguration()
        return jiraTriggerGlobalConfiguration.jiraRootUrl.toURI()
    }

    protected DisposableHttpClient getHttpClient() {
        return new AsynchronousHttpClientFactory()
                .createClient(serverUri,
                new BasicHttpAuthenticationHandler(jiraTriggerGlobalConfiguration.jiraUsername, jiraTriggerGlobalConfiguration.jiraPassword.plainText));
    }

    ExtendedJiraRestClient getJiraRestClient() {
        if (extendedJiraRestClient == null) {
            this.extendedJiraRestClient = new ExtendedJiraRestClient(serverUri, httpClient)
        }
        return extendedJiraRestClient
    }

    @Override
    boolean validateIssueKey(String issueKey, String jqlFilter) {
        def searchResult = jiraRestClient.searchClient.searchJql("key=$issueKey and ($jqlFilter)").get(timeout, timeoutUnit)
        searchResult.total != 0
    }

    @Override
    void addComment(String issueKey, String comment) {
        def issue = jiraRestClient.issueClient.getIssue(issueKey).get(timeout, timeoutUnit)
        jiraRestClient.issueClient.addComment(issue.commentsUri, Comment.valueOf(comment)).get(timeout, timeoutUnit)
    }

    @Extension
    static class ResourceCleaner extends SaveableListener {

        @Inject
        JrjcJiraClient jiraClient

        @Override
        void onChange(Saveable o, XmlFile file) {
            if (o instanceof JiraTriggerGlobalConfiguration) {
                if (jiraClient.extendedJiraRestClient != null) {
                    jiraClient.extendedJiraRestClient.close()
                    jiraClient.extendedJiraRestClient = null
                }
            }
        }
    }
}
