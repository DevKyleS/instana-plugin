package jenkins.plugins.instana;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;

import hudson.model.FreeStyleProject;
import hudson.tasks.Builder;

import jenkins.plugins.instana.auth.BasicDigestAuthentication;
import jenkins.plugins.instana.auth.FormAuthentication;
import jenkins.plugins.instana.util.HttpRequestNameValuePair;
import jenkins.plugins.instana.util.RequestAction;

/**
 * @author Martin d'Anjou
 */
public class HttpRequestBackwardCompatibilityTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @LocalData
    @Test
    public void defaultGlobalConfig() {
        // Test that config from 1.8.6 can be loaded
        HttpRequestGlobalConfig cfg = HttpRequestGlobalConfig.get();
        assertEquals(Collections.emptyList(), cfg.getBasicDigestAuthentications());
        assertEquals(Collections.emptyList(), cfg.getFormAuthentications());
        assertEquals("HttpRequest.xml", cfg.getConfigFile().getFile().getName());
    }

    @LocalData
    @Test
    public void populatedGlobalConfig() {
        // Test that global config from 1.8.6 can be loaded
        // Specifically tests the HttpRequestGlobalConfig.xStreamCompatibility() method
        // and the HttpRequestGlobalConfig.getConfigFile() method
        HttpRequestGlobalConfig cfg = HttpRequestGlobalConfig.get();

        List<BasicDigestAuthentication> bdas = cfg.getBasicDigestAuthentications();
        assertEquals(2,bdas.size());
		Iterator<BasicDigestAuthentication> itr = bdas.iterator();
		BasicDigestAuthentication bda = itr.next();
		assertEquals("k1",bda.getKeyName());
        assertEquals("u1",bda.getUserName());
        assertEquals("p1",bda.getPassword());
		bda = itr.next();
		assertEquals("k2",bda.getKeyName());
        assertEquals("u2",bda.getUserName());
        assertEquals("p2",bda.getPassword());

        List<FormAuthentication> fas = cfg.getFormAuthentications();
        assertEquals(1,fas.size());

        FormAuthentication fa = fas.iterator().next();
		assertEquals("k3", fa.getKeyName());
        List<RequestAction> ras = fa.getActions();
        assertEquals(1,ras.size());

		RequestAction ra = ras.iterator().next();
		assertEquals("http://localhost1",ra.getUrl().toString());
        assertEquals("GET",ra.getMode().toString());
        List<HttpRequestNameValuePair> nvps = ra.getParams();
        assertEquals(1,nvps.size());

		HttpRequestNameValuePair nvp = nvps.iterator().next();
		assertEquals("name1",nvp.getName());
        assertEquals("value1",nvp.getValue());
    }

    @LocalData
    @Test
    public void oldConfigWithoutCustomHeadersShouldLoad() {
        // Test that a job config from 1.8.6 can be loaded
        // Specifically tests the HttpRequest.readResolve() method
		FreeStyleProject p = (FreeStyleProject) j.getInstance().getItem("old");

        List<Builder> builders = p.getBuilders();

        HttpRequest httpRequest = (HttpRequest) builders.get(0);
        assertEquals("url", httpRequest.getUrl());
        assertNotNull(httpRequest.getCustomHeaders());
        assertNotNull(httpRequest.getValidResponseCodes());
        assertEquals("100:399", httpRequest.getValidResponseCodes());
    }

    @LocalData
    @Test
    public void oldConfigWithCustomHeadersShouldLoad() {
        // Test that a job config from 1.8.8 can be loaded
        // Specifically tests the HttpRequest.xStreamCompatibility() method
		FreeStyleProject p = (FreeStyleProject) j.getInstance().getItem("old");

        List<Builder> builders = p.getBuilders();

        HttpRequest httpRequest = (HttpRequest) builders.get(0);
        assertEquals("url", httpRequest.getUrl());

        assertNotNull(httpRequest.getCustomHeaders());
        List<HttpRequestNameValuePair> customHeaders = httpRequest.getCustomHeaders();
        assertEquals(1,customHeaders.size());
		Iterator<HttpRequestNameValuePair> itr = customHeaders.iterator();
		HttpRequestNameValuePair nvp = itr.next();
		assertEquals("h1",nvp.getName());
        assertEquals("v1",nvp.getValue());

        assertNotNull(httpRequest.getValidResponseCodes());
        assertEquals("100:399", httpRequest.getValidResponseCodes());
    }
}
