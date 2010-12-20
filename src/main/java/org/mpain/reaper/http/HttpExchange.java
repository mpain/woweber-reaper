package org.mpain.reaper.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpExchange implements IHttpExchange {
	private static final Pattern CONTENT_TYPE_PATTERN = Pattern.compile("\\s*(.+);\\s+charset\\s*=\\s*(.*)");
	
	private static final String CONTEXT_TYPE_SSL = "SSL";
	private static final String PROTO_TYPE_HTTPS = "https";
	private static final String PROTO_TYPE_HTTP = "http";
	
	private static final String SEND_ENCODING = "utf-8";
    
	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	private static HttpClient client = null;

	private ThreadLocal<HttpExchangeData> exchangeData = new ThreadLocal<HttpExchangeData>() {
		public HttpExchangeData initialValue() {
			return new HttpExchangeData();
		}
	};
	
	@Override
	public void executeGet(String address) throws HttpExchangeException {
		try {
			executeGetRequest(address);
		} catch (IOException e) {
			throw new HttpExchangeException(e);
		}
	}
	
	@Override
	public void executePost(String address, String xmlData) throws HttpExchangeException {
		try {
			executePostRequest(address, xmlData);
		} catch (IOException e) {
			throw new HttpExchangeException(e);
		}
	}

	private void executeGetRequest(String urlString) throws IOException {
		HttpGet method = new HttpGet(urlString);
		exchangeData.get().setRequest(method.getRequestLine().toString());
		executeRequest(method);
	}
	
	private void executePostRequest(String urlString, String data) throws IOException {
		HttpPost method = new HttpPost(urlString);
		method.setHeader("Content-Type", "application/x-www-form-urlencoded");
		StringEntity entity = new StringEntity(data, SEND_ENCODING);
		method.setEntity(entity);
		
		exchangeData.get().setRequest(String.format("%s\r\n\r\n%s", method.getRequestLine().toString(), data));
		executeRequest(method);
	}
	
	private void executeRequest(HttpRequestBase method) throws IOException {
		HttpClient http = HttpExchange.getClient();

		log.debug(exchangeData.get().getRequest());
		
		HttpResponse httpResponse = http.execute(method);
		
		exchangeData.get().setCode(httpResponse.getStatusLine().getStatusCode());
		exchangeData.get().setResponse(httpReadResponse(httpResponse.getEntity()));
		
		log.debug("RESPONSE: " + exchangeData.get().getResponse());
	}

	private String httpReadResponse(HttpEntity resp) throws IOException {
		String result = null;
		try {
			if (resp != null) {
				Header encodingHeader = resp.getContentEncoding();
				
				String encoding = (encodingHeader != null && encodingHeader.getValue() != null) ? 
						encodingHeader.getValue() : 
						detectEncodingByContentType(resp);
						
				log.debug("RESPONSE ENCODING: " + encoding);
				BufferedReader br = new BufferedReader(new InputStreamReader(resp.getContent(), encoding));

				StringBuilder sb = new StringBuilder();
				for (String line; (line = br.readLine()) != null;) {
					sb.append(line);
					sb.append("\r\n");
				}
				result = sb.toString();
			}
		} finally {
			if (resp != null) {
				resp.consumeContent();
			}
		}

		return result;
	}
	
	public void close() {
		exchangeData.remove();
	}
	
	public static HttpClient getClient() {
		if (client == null) {
			HttpParams params = new BasicHttpParams();
			
			ConnManagerParams.setMaxTotalConnections(params, 200);
			ConnManagerParams.setMaxConnectionsPerRoute(params, new ConnPerRouteBean(50));
			ConnManagerParams.setTimeout(params, 30000);
			
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			
			SchemeRegistry schemeRegistry = new SchemeRegistry();
			schemeRegistry.register(new Scheme(PROTO_TYPE_HTTP, PlainSocketFactory.getSocketFactory(), 80));

			HttpConnectionParams.setSoTimeout(params, 30000);
	        HttpConnectionParams.setConnectionTimeout(params, 10000);
	        
			try {
				SSLContext context = SSLContext.getInstance(CONTEXT_TYPE_SSL);
				context.init(null, new TrustManager[] { new X509TrustManager() {

					@Override
					public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
					}

					@Override
					public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
					}

					@Override
					public X509Certificate[] getAcceptedIssuers() {
						return null;
					}
					
				} }, null);
				SSLSocketFactory factory = new SSLSocketFactory(context);
				factory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
				schemeRegistry.register(new Scheme(PROTO_TYPE_HTTPS, factory, 443));

			} catch (NoSuchAlgorithmException e) {
			} catch (KeyManagementException e) {
			}
			
			ClientConnectionManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
			client = new DefaultHttpClient(cm, params);
		}

		return client;
	}

	public HttpExchangeData getExchangeData() {
		return exchangeData.get();
	}
	
	private String detectEncodingByContentType(HttpEntity resp) {
		String source = resp.getContentType().getValue();
		Matcher matcher = CONTENT_TYPE_PATTERN.matcher(source);
		if (matcher.find()) {
			return matcher.group(2);
		}
		
		return SEND_ENCODING;
	}

}
