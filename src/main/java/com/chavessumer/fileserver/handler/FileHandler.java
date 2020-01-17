package com.chavessumer.fileserver.handler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.handler.AbstractHandler;

import com.chavessumer.fileserver.FileServer;
import com.chavessumer.fileserver.domain.ResourceFile;

public class FileHandler extends AbstractHandler {
	
	@Override
	public void handle(String target,
			org.eclipse.jetty.server.Request baseRequest,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		
		System.out.println("target: " + target);
		
		String[] targets = target.split("/");
		Boolean found = Boolean.FALSE;
		ResourceFile rf = null;
		
		for (String t : targets) {
			found = FileServer.getResources().containsKey(t);
			if (found) {
				rf = FileServer.getResources().get(t);	
				break;
			}
		}
		
		if (found)
		{
			String fileName = FileServer.getFilePathServer()+System.getProperty("file.separator")+target;
			File file = new File(fileName);
			file.getParentFile().mkdirs();
			if (file.exists() && rf.isCache()) {
				System.out.println("found file "+target+" in chache ");
			} else {
				
				String nameUrl = rf.getUrl() + target.replaceAll(" ", "%20");
				System.out.println("downloading file: " + nameUrl);
				file.delete();
				URL url = new URL(nameUrl);
				URLConnection conn = null;
				
				if (FileServer.hasProxy(url.getProtocol())) {
					System.out.println("getting proxy ...");
					conn = FileServer.getProxy(url);
				}
				
				InputStream in = null;
				OutputStream outF = null;
				try {
					System.out.println("getting input stream ...");
					if (conn!= null) {
						in = new BufferedInputStream(conn.getInputStream());
					} else {
						in = new BufferedInputStream(url.openStream());
					}
					System.out.println(nameUrl+" downloaded! Saving file ...");
					outF = new BufferedOutputStream(new FileOutputStream(file));
	
					for ( int i; (i = in.read()) != -1; ) {
						outF.write(i);
					}
					System.out.println("file "+fileName+" saved!");
				} catch(Throwable ex) {
					ex.printStackTrace();
				} finally {
					if (in!=null) {
						in.close();
					}
					if (outF!=null) {
						outF.close();
					}
				}
			}
		}		
	}
}
