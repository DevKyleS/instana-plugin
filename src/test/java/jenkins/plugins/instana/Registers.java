package jenkins.plugins.instana;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import net.sf.json.JSONObject;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.entity.ContentType;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.util.MultiException;
import org.eclipse.jetty.util.MultiPartInputStreamParser;

import com.google.common.collect.Iterables;

import jenkins.plugins.instana.ReleaseEventTestBase.SimpleHandler;

/**
 * @author Janario Oliveira
 */
public class Registers {

	static void registerReleaseEndpointChecker(final String name, final String timestamp, final String apiToken)
	{
		registerHandler("/api/releases", HttpMode.POST,new SimpleHandler() {
			@Override
			void doHandle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
				final String body = requestBody(request);
				final JSONObject jsonObject = JSONObject.fromObject(body);
				assertEquals(jsonObject.getString("name"),name);
				assertEquals(jsonObject.getString("start"),timestamp);

				Enumeration<String> authHeaders = request.getHeaders("Authorization");
				String authHeaderValue = authHeaders.nextElement();
				assertFalse(authHeaders.hasMoreElements());
				assertEquals("apiToken "+apiToken, authHeaderValue);

				Enumeration<String> contentTypeHeader = request.getHeaders("Content-Type");
				String contentTypeValue = contentTypeHeader.nextElement();
				assertFalse(contentTypeHeader.hasMoreElements());
				assertEquals("application/json", contentTypeValue);

				body(response,200,ContentType.APPLICATION_JSON,jsonObject.toString());
			}
		});
	}

	static void registerAlways200()
	{
		registerHandler("/api/releases", HttpMode.POST,new SimpleHandler() {
			@Override
			void doHandle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
				body(response,200,ContentType.APPLICATION_JSON,"");
			}
		});
	}

	static void registerFailedAuthEndpoint(final String name, final String timestamp, final String apiToken)
	{
		registerHandler("/api/releases", HttpMode.POST,new SimpleHandler() {
			@Override
			void doHandle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
				final JSONObject jsonObject = JSONObject.fromObject("{\n" +
						"  \"errors\": [\n" +
						"    \"Unauthorized request\"\n" +
						"  ]\n" +
						"}");

				body(response,401,ContentType.APPLICATION_JSON,jsonObject.toString());
			}
		});
	}


	static void registerTimeout() {
		// Timeout, do not respond!
		registerHandler("/timeout", HttpMode.GET, new SimpleHandler() {
			@Override
			void doHandle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
				try {
					Thread.sleep(10000);
				} catch (InterruptedException ex) {
					// do nothing the sleep will be interrupted when the test ends
				}
			}
		});
	}


	private static void registerHandler(String target, HttpMode method, SimpleHandler handler) {
		ReleaseEventTestBase.registerHandler(target, method, handler);
	}
}