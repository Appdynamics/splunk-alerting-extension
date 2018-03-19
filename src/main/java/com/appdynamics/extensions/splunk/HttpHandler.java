/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.splunk;

import com.appdynamics.TaskInputArgs;
import com.appdynamics.extensions.crypto.CryptoUtil;
import com.appdynamics.extensions.http.Http4ClientBuilder;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Satish Muddam
 */
public class HttpHandler {

    private static final Logger LOGGER = Logger.getLogger(HttpHandler.class);

    private static final String REQUEST_PARAM_SEPARATOR = "&";

    private Configuration config;
    private CloseableHttpClient httpClient;
    private HttpClientContext httpContext;

    public HttpHandler(Configuration configuration) {
        this.config = configuration;

        setupHttpClient();
    }

    private String buildTargetUrl() {
        StringBuilder sb = new StringBuilder("https://");

        sb.append(config.getHost()).append(":").append(config.getPort()).append("/services/receivers/stream?");

        sb.append("source=http-simple").append(REQUEST_PARAM_SEPARATOR).append("sourcetype=")
                .append(config.getSourceType()).append(REQUEST_PARAM_SEPARATOR).append("index=").append(config.getIndex());


        return sb.toString();
    }

    private void setupHttpClient() {

        Map map = createHttpConfigMap();

        //Workaround to ignore the certificate mismatch issue.
        SSLContext sslContext = null;
        try {
            sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                    return true;
                }
            }).build();
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("Unable to create SSL context", e);
            throw new RuntimeException("Unable to create SSL context", e);
        } catch (KeyManagementException e) {
            LOGGER.error("Unable to create SSL context", e);
            throw new RuntimeException("Unable to create SSL context", e);
        } catch (KeyStoreException e) {
            LOGGER.error("Unable to create SSL context", e);
            throw new RuntimeException("Unable to create SSL context", e);
        }
        HostnameVerifier hostnameVerifier = SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, (X509HostnameVerifier) hostnameVerifier);
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", sslSocketFactory)
                .build();

        PoolingHttpClientConnectionManager connMgr = new PoolingHttpClientConnectionManager(socketFactoryRegistry);

        HttpClientBuilder builder = Http4ClientBuilder.getBuilder(map);
        builder.setConnectionManager(connMgr);


        httpContext = HttpClientContext.create();

        HttpClientBuilder httpClientBuilder = builder.setSSLSocketFactory(sslSocketFactory);

        if(config.getUsername() != null && config.getUsername().length() > 0) {

            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(config.getUsername(), getPassword()));

            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);

            httpContext.setCredentialsProvider(credentialsProvider);
        }

        httpClient = httpClientBuilder.build();
    }

    private Map<String, String> createHttpConfigMap() {

        Map map = new HashMap();
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        map.put("servers", list);
        HashMap<String, String> server = new HashMap<String, String>();
        server.put("uri", "https://" + config.getHost());
        if(config.getUsername() != null && config.getUsername().length() > 0) {
            server.put("username", config.getUsername());
            server.put("password", getPassword());
        }
        list.add(server);

        HashMap<String, String> proxyProps = new HashMap<String, String>();
        map.put("proxy", proxyProps);
        proxyProps.put("uri", config.getProxyUri());
        proxyProps.put("username", config.getProxyUser());
        proxyProps.put("password", config.getProxyPassword());

        return map;
    }

    private String getPassword() {

        String password = config.getPassword();

        Map<String, String> map = new HashMap<String, String>();

        if (password != null) {
            LOGGER.debug("Using provided password");
            map.put(TaskInputArgs.PASSWORD, password);
        }

        String passwordEncrypted = config.getPasswordEncrypted();
        if (passwordEncrypted != null) {
            LOGGER.debug("Using provided passwordEncrypted");
            map.put(TaskInputArgs.PASSWORD_ENCRYPTED, passwordEncrypted);
            map.put(TaskInputArgs.ENCRYPTION_KEY, config.getEncryptionKey());
        }

        String plainPassword = CryptoUtil.getPassword(map);

        return plainPassword;
    }

    public void sendEventsToSplunk(List<LogMessage> logMessages) throws IOException {

        try {

            HttpPost httpPost = new HttpPost(buildTargetUrl());

            for (LogMessage logMessage : logMessages) {
                String message = logMessage.getMessage();
                LOGGER.debug("Alert sending to Splunk { " + message + " }");
                httpPost.setEntity(new StringEntity(message));
                try {
                    CloseableHttpResponse response = httpClient.execute(httpPost, httpContext);

                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_NO_CONTENT) {
                        LOGGER.info("Alert posted to Splunk ");
                    } else {
                        LOGGER.info("Received error response [" + statusCode + "] while executing [" + httpPost.getMethod() + "] on [" + httpPost.getURI() + "]");
                        LOGGER.debug(EntityUtils.toString(response.getEntity()));
                    }

                } catch (IOException e) {
                    LOGGER.error("Error executing [" + httpPost.getMethod() + "] on [" + httpPost.getURI() + "]", e);
                }
            }
        } finally {
            if (httpClient != null) {
                httpClient.close();
            }
        }
    }
}