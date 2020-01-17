package com.chavessumer.fileserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import com.chavessumer.fileserver.domain.ResourceFile;
import com.chavessumer.fileserver.handler.DefaultHandler;
import com.chavessumer.fileserver.handler.FileHandler;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class FileServer {

	private static Server server = null;
	private static Map<String, ResourceFile> resources = new HashMap<>();
	
	private static final String SSL_ENABLED = "SSL_ENABLED";
	private static final String PROXY_SERVICE_CONFIG = "PROXY_SERVICE_CONFIG";
	private static final String HTTP_PORT = "HTTP_PORT";
	private static final String HTTP_HOSTNAME = "HTTP_HOSTNAME";
	private static final String HTTPS_PORT = "HTTPS_PORT";
	private static final String HTTPS_HOSTNAME = "HTTPS_HOSTNAME";
	private static final String HTTP_PROTOCOL = "HTTP_PROTOCOL";
	private static final String KEYSTORE_FILE_PATH = "KEYSTORE_FILE_PATH";
	private static final String KEYSTORE_PASS = "KEYSTORE_PASS";
	private static final String WELCOME_FILES = "WELCOME_FILES";
	private static final String FILE_PATH_SERVER = "FILE_PATH_SERVER";
	private static final String FILES_PATH_SERVER = "FILES_PATH_SERVER";
	private static final String PASS_ENCODED = "PASS_ENCODED";
	
	private static final String HTTP_PROXY = "http_proxy";
	private static final String HTTPS_PROXY = "https_proxy";
	
	private static final String HTTP_PROXY_HOST = "http.proxyHost";
	private static final String HTTP_PROXY_PORT = "http.proxyPort";
	private static final String HTTP_PROXY_USER = "http.proxyUser";
	private static final String HTTP_PROXY_PASSWORD = "http.proxyPassword";
	
	private static final String HTTPS_PROXY_HOST = "https.proxyHost";
	private static final String HTTPS_PROXY_PORT = "https.proxyPort";
	private static final String HTTPS_PROXY_USER = "https.proxyUser";
	private static final String HTTPS_PROXY_PASSWORD = "https.proxyPassword";
	
	private static String filePathServer = "";
	
	private static String httpProxyHost;
	private static String httpProxyPort;
	private static String httpProxyUser;
	private static String httpProxyPassword;
	
	private static String httpsProxyHost;
	private static String httpsProxyPort;
	private static String httpsProxyUser;
	private static String httpsProxyPassword;
	
	public static void main(String[] args) throws Exception
    {
		
		System.out.println("starting File Server - 1.3");
		
		if (args.length>0 && args[0].equalsIgnoreCase("-ep")) {
			
			System.out.println("Please, insert the clear password and press <ENTER>");
			//Console con = System.console();
	        //String passwordKey = new String(con.readPassword());
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String passwordKey = br.readLine();
			
			String passEncoded = new String(Base64.getEncoder().encode(passwordKey.getBytes()));
			System.out.println("password encoded: [" + passEncoded+"]");
			System.out.println("password decoed: ["+new String(Base64.getDecoder().decode(passEncoded))+"]");
			return;
		}
		
		Boolean passEncoded = Boolean.valueOf(System.getProperty(PASS_ENCODED, "true"));
		
		Integer sslEnabled = 0;
		if (System.getProperty(SSL_ENABLED)!=null) {
			sslEnabled = Integer.valueOf(System.getProperty(SSL_ENABLED));
			System.out.println("ssl enabled => "+sslEnabled);
		}
		
		if (System.getProperty(PROXY_SERVICE_CONFIG)!=null) {
			List<ResourceFile> list = new Gson().fromJson(System.getProperty(PROXY_SERVICE_CONFIG), new TypeToken<List<ResourceFile>>(){}.getType() );
			for(ResourceFile r : list) {
				System.out.println("resource configuration => context: " + r.getContext() + ", url: " + r.getUrl() + ", cache: " + r.isCache());
				resources.put(r.getContext(), r);
			}
		}
		
		if (System.getProperty(HTTP_PROXY)!=null) {
			
			String proxy = System.getProperty(HTTP_PROXY);
			
			String[] proxyT =  proxy.replaceAll("http://", "").replaceAll("https://", "").split("@");
			
			if (proxyT.length==1) {
				httpProxyHost = proxyT[0].split(":")[0];
				httpProxyPort = proxyT[0].split(":")[1];
			} else {
				httpProxyHost = proxyT[1].split(":")[0];
				httpProxyPort = proxyT[1].split(":")[1];
				httpProxyUser = URLDecoder.decode(proxyT[0].split(":")[0], "utf-8");
				httpProxyPassword = URLDecoder.decode(proxyT[0].split(":")[1], "utf-8");
			}
		} else {
			
			if (System.getProperty(HTTP_PROXY_HOST)!=null) {
				httpProxyHost = System.getProperty(HTTP_PROXY_HOST);
			}
			if (System.getProperty(HTTP_PROXY_PORT)!=null) {
				httpProxyPort = System.getProperty(HTTP_PROXY_PORT);
			}
			if (System.getProperty(HTTP_PROXY_USER)!=null) {
				httpProxyUser = System.getProperty(HTTP_PROXY_USER);
			}
			if (System.getProperty(HTTP_PROXY_PASSWORD)!=null) {
				httpProxyPassword = System.getProperty(HTTP_PROXY_PASSWORD);
			}
		}
		
		if (System.getProperty(HTTPS_PROXY)!=null) {
			
			String proxy = System.getProperty(HTTPS_PROXY);
			String[] proxyT =  proxy.replaceAll("http://", "").replaceAll("https://", "").split("@");
			
			if (proxyT.length==1) {
				httpsProxyHost = proxyT[0].split(":")[0];
				httpsProxyPort = proxyT[0].split(":")[1];
			} else {
				httpsProxyHost = proxyT[1].split(":")[0];
				httpsProxyPort = proxyT[1].split(":")[1];
				httpsProxyUser = URLDecoder.decode(proxyT[0].split(":")[0], "utf-8");
				httpsProxyPassword = URLDecoder.decode(proxyT[0].split(":")[1], "utf-8");
			}
		} else {
			
			if (System.getProperty(HTTPS_PROXY_HOST)!=null) {
				httpsProxyHost = System.getProperty(HTTPS_PROXY_HOST);
			}
			if (System.getProperty(HTTPS_PROXY_PORT)!=null) {
				httpsProxyPort = System.getProperty(HTTPS_PROXY_PORT);
			}
			if (System.getProperty(HTTPS_PROXY_USER)!=null) {
				httpsProxyUser = System.getProperty(HTTPS_PROXY_USER);
			}
			if (System.getProperty(HTTPS_PROXY_PASSWORD)!=null) {
				httpsProxyPassword = System.getProperty(HTTPS_PROXY_PASSWORD);
			}
			
		}
	
		if (passEncoded) {
			System.out.println("decoding password ...");
			if (httpsProxyPassword!=null) {
				httpsProxyPassword = new String(Base64.getDecoder().decode(httpsProxyPassword));
			}
			if (httpProxyPassword!=null) {
				httpProxyPassword = new String(Base64.getDecoder().decode(httpProxyPassword));
			}
		}
		
		System.out.println("has http proxy: " + hasHttpProxy());
		System.out.println("has https proxy: " + hasHttpsProxy());
				
		if (sslEnabled>0) {
			https(sslEnabled);
		} else {		
			http();
		}
    }
	
	public static Boolean serverDown() {
		
		Boolean ret = Boolean.FALSE;
		System.out.println("server is stopping ...");
		if (server.isStarted()) {
			try {
				server.stop();
				System.out.println("server stopped!");
			} catch (Exception e) {
				e.printStackTrace();
			}
			ret = Boolean.TRUE;
		} else {
			System.out.println("server not started!");
		}
		return ret;
	}
	
	@SuppressWarnings("resource")
	public static void https(Integer sslEnabled) throws Exception {

		server = new Server();
		ServerConnector connector = new ServerConnector(server);

		Integer port = 8080;
		String protocol = "http/1.1";
		String hostname = "localhost";
		
		if (sslEnabled == 1) {
			if (System.getProperty(HTTP_PORT) != null) {
				port = Integer.valueOf(System.getProperty(HTTP_PORT));
			}
			connector.setPort(port);
			if (System.getProperty(HTTP_HOSTNAME) != null) {
				hostname = System.getProperty(HTTP_HOSTNAME);
			}
			connector.setHost(hostname);
			System.out.println("http addres: " + hostname +":" +  port);
		}
		
		if (System.getProperty(HTTP_PROTOCOL)!=null) {
			protocol = System.getProperty(HTTP_PROTOCOL);
		}
		System.out.println("http protocol: " + protocol);
		
		HttpConfiguration https = new HttpConfiguration();
		https.addCustomizer(new SecureRequestCustomizer());
		SslContextFactory sslContextFactory = new SslContextFactory();
		
		String keyStorePath = "";
		if (System.getProperty(KEYSTORE_FILE_PATH)!=null) {
			keyStorePath = System.getProperty(KEYSTORE_FILE_PATH);
		} else {
			throw new Exception("Key store is required!");
		}
		System.out.println("keystore path: " + keyStorePath);
		
		String keyStorePass = "";
		if (System.getProperty(KEYSTORE_PASS)!=null) {
			
			if (!System.getProperty(KEYSTORE_PASS).equals(StringUtils.EMPTY)) {
				keyStorePass = new String(Base64.getDecoder().decode(System.getProperty(KEYSTORE_PASS)));
			} else {
				keyStorePass = System.getProperty(KEYSTORE_PASS);
			}
		}
		
		sslContextFactory.setKeyStorePath(keyStorePath);
		sslContextFactory.setKeyStorePassword(keyStorePass);
		sslContextFactory.setKeyManagerPassword(keyStorePass);

		ServerConnector sslConnector = new ServerConnector(server, 
				new SslConnectionFactory(sslContextFactory, protocol),		
				new HttpConnectionFactory(https));

		Integer sslPort = 443;
		if (System.getProperty(HTTPS_PORT) != null) {
			sslPort = Integer.valueOf(System.getProperty(HTTPS_PORT));
		}
		sslConnector.setPort(sslPort);
		
		hostname = "localhost";
		if (System.getProperty(HTTPS_HOSTNAME)!=null) {
			try {
				hostname = String.valueOf(System.getProperty(HTTPS_HOSTNAME));
			} catch(Exception e) {
			}
		}
		sslConnector.setHost(hostname);
		System.out.println("http addres: " + hostname +":" +  sslPort);

		if (sslEnabled==1) {
			server.setConnectors(new Connector[] { connector, sslConnector });	
		} else {
			server.setConnectors(new Connector[] { sslConnector });
		}
		
		serverUp();
	}
	
	public static void http() throws Exception {
		// Create a basic Jetty server object that will listen on port 8080.  Note that if you set this to port 0
        // then a randomly available port will be assigned that you can either look in the logs for the port,
        // or programmatically obtain it for use in test cases.
		Integer port = 8080; 
		if (System.getProperty(HTTP_PORT)!=null) {
			try {
				port = Integer.valueOf(System.getProperty(HTTP_PORT));
			} catch(Exception e) {
			}
		}
		String hostname = "localhost";
		if (System.getProperty(HTTP_HOSTNAME)!=null) {
			try {
				hostname = String.valueOf(System.getProperty(HTTP_HOSTNAME));
			} catch(Exception e) {
			}
		}
		System.out.println("http addres: " + hostname +":" +  port);
		InetSocketAddress add = new InetSocketAddress(hostname, port);
		
		server = new Server(add);
		
        serverUp();
	}
	
	private static ResourceHandler createResourceHandler(String[] attrs)
	{
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(Boolean.valueOf(attrs[1]));
        
        String[] welcomeFiles = new String[]{ "index.html" };
        if (attrs.length>2) {
        	welcomeFiles = attrs[2].split(",");
        }
        System.out.println("welcome files: " + ArrayUtils.toString(welcomeFiles));
        resourceHandler.setWelcomeFiles(welcomeFiles);
        String filePathServerLocal = "";
       	filePathServerLocal = attrs[0];
        System.out.println("root path domain: " + filePathServerLocal);
        resourceHandler.setResourceBase(filePathServerLocal);
        return resourceHandler;
	}
	
	public static void serverUp() throws Exception {
		
		System.out.println("server is starting ...");
		
		// Add the ResourceHandler to the server.
        GzipHandler gzip = new GzipHandler();
        server.setHandler(gzip);
        HandlerList handlers = new HandlerList();
        
        List<Handler> lhandlers = new ArrayList<Handler>();
        lhandlers.add(new FileHandler());
		
        if (System.getProperty(WELCOME_FILES)!=null || System.getProperty(FILE_PATH_SERVER)!=null)
        {
            ResourceHandler resourceHandler = new ResourceHandler();
            resourceHandler.setDirectoriesListed(Boolean.valueOf(System.getProperty("DIRECTORIES_LISTED", "true")));
            
	        String[] welcomeFiles = new String[]{ "index.html" };
	        if (System.getProperty(WELCOME_FILES)!=null) {
	        	welcomeFiles = System.getProperty(WELCOME_FILES).split(",");
	        }
	        System.out.println("welcome files: " + ArrayUtils.toString(welcomeFiles));
	        resourceHandler.setWelcomeFiles(welcomeFiles);
	
	        if (System.getProperty(FILE_PATH_SERVER)!=null) {
				try {
					filePathServer = System.getProperty(FILE_PATH_SERVER);
				} catch(Exception e) {
	
				}
			} else {
				throw new Exception("Parameter FILE_PATH_SERVER is required!");
			}
	        System.out.println("root path domain: " + filePathServer);
	        resourceHandler.setResourceBase(filePathServer);
	        lhandlers.add(resourceHandler);
        }
        
        if (System.getProperty(FILES_PATH_SERVER)!=null) {
        	//<PATH>;<LIST DIRECTORY - TRUE OR FALSE>;<FILE1.EXT,FILE2.EXT>|
        	String[] sHandlers = System.getProperty(FILES_PATH_SERVER).split("\\|");
        	for (String sHandler : sHandlers) {

        		String[] attrs = sHandler.split(";");
        		lhandlers.add(createResourceHandler(attrs));
        	}
        }
        
        lhandlers.add(new DefaultHandler()); 
 
        //aHandlers
        Handler[] ahandlers = new Handler[lhandlers.size()];
        for(int i=0; i<ahandlers.length; i++) {
        	ahandlers[i] = lhandlers.get(i);
        }
        handlers.setHandlers(ahandlers);
        
        
        gzip.setHandler(handlers);
 
        new Thread(new Runnable() {
			
			public void run() {
				try {
					server.start();
					System.out.println("server started!");
					server.join();	
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("error in server join!");
					serverDown();
				}
			}
		}).start();
        
        final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while(Boolean.TRUE) {
        	Thread.sleep(1000);
        	System.out.println("> ");
        	String cmd = br.readLine();
        	
        	if (cmd.equalsIgnoreCase("quit")||cmd.equalsIgnoreCase("exit")) {
        		serverDown();
        		System.exit(0);
        	}
        }
        
	}

	public static Map<String, ResourceFile> getResources() {
		return resources;
	}


	public static String getFilePathServer() {
		return filePathServer;
	}
	
	public static final Boolean hasHttpProxy() {
		return httpProxyHost!=null;
	}
	public static final Boolean hasHttpsProxy() {
		return httpsProxyHost!=null;
	}
	
	public static final Boolean hasProxy(String protocol) {
		return protocol.equalsIgnoreCase("HTTPS")?hasHttpsProxy():hasHttpProxy();
	}
	
	@SuppressWarnings("restriction")
	public static final URLConnection getProxy(URL url) throws IOException {
		URLConnection conn = null;
		InetSocketAddress sa = null;
		String user = StringUtils.EMPTY;
		String password = StringUtils.EMPTY;
		Authenticator authenticator = null;
		Proxy.Type proxyType = Proxy.Type.HTTP;
		
		try {
			if (url.getProtocol().equalsIgnoreCase("HTTPS")) {
				if (System.getProperty("HTTPS_PROXY_TYPE")!=null) {
					if (System.getProperty("HTTPS_PROXY_TYPE").equalsIgnoreCase("SOCKS")) {
						proxyType = Proxy.Type.SOCKS;
					}
				}
				sa = new InetSocketAddress(httpsProxyHost, Integer.valueOf(httpsProxyPort));
				if(httpsProxyUser!=null) {
					user = httpsProxyUser;
				}
				if (httpsProxyPassword!=null) {
					password = httpsProxyPassword;
				}
				
				if (httpsProxyUser!=null && httpsProxyPassword!=null) {
					authenticator = new Authenticator() {
					    public PasswordAuthentication getPasswordAuthentication() {
					        return (new PasswordAuthentication(httpsProxyUser, httpsProxyPassword.toCharArray()));
					    }
					};
				} else {
					authenticator =null;
				}
				System.setProperty("https.proxyHost", httpsProxyHost);
	            System.setProperty("https.proxyPort", httpsProxyPort);
	            
			} else {
				if (System.getProperty("HTTP_PROXY_TYPE")!=null) {
					if (System.getProperty("HTTP_PROXY_TYPE").equalsIgnoreCase("SOCKS")) {
						proxyType = Proxy.Type.SOCKS;
					}
				}
				sa = new InetSocketAddress(httpProxyHost, Integer.valueOf(httpProxyPort));
				if(httpProxyUser!=null) {
					user = httpProxyUser;
				}
				if (httpProxyPassword!=null) {
					password = httpProxyPassword;
				}
				
				if (httpProxyUser!=null && httpProxyPassword!=null) {
					authenticator = new Authenticator() {
					    public PasswordAuthentication getPasswordAuthentication() {
					        return (new PasswordAuthentication(httpProxyUser, httpProxyPassword.toCharArray()));
					    }
					};
				} else {
					authenticator =null;
				}
				System.setProperty("http.proxyHost", httpProxyHost);
	            System.setProperty("http.proxyPort", httpProxyPort);
			}

			String encoded = new sun.misc.BASE64Encoder().encode((user + ":" + password).getBytes());
			Proxy proxy = new Proxy(proxyType, sa);
			System.out.println("open connection with proxy ...");
			conn = url.openConnection(proxy);
				
			conn.setRequestProperty("Proxy-Authorization", "Basic "+encoded);
			Authenticator.setDefault(authenticator);
			
		} catch(Throwable t) {
			t.printStackTrace();
		}
		
		return conn;
	}
	
}
