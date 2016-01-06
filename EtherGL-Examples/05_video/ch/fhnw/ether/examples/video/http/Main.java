package ch.fhnw.ether.examples.video.http;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.file.Files;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import ch.fhnw.util.TextUtilities;

public class Main {
	static File file;

	public static void main(String[] args) throws Exception {
		file = new File(args[0]);
		if(!(file.exists())) throw new FileNotFoundException(file.getAbsolutePath());
		int port = 9000;
		if(args.length > 1) port = Integer.parseInt(args[1]);
		HttpServer server = HttpServer.create(new InetSocketAddress(9000), 0);
		System.out.println("HTTP Server: http://" + InetAddress.getLocalHost().getHostName() + ":" + port + "/");
		server.createContext("/", new WebCam());
		server.setExecutor(null);
		server.start();
	}

	static class WebCam implements HttpHandler {
		@Override
		public void handle(HttpExchange t) throws IOException {
			URI uri = t.getRequestURI();
			if(uri.getPath().equals("/")) {
				byte[] response = (
						"<!DOCTYPE html><html><head><title>[tvver] Simple Video Server</title></head>" +
								"<body>"+
								"<video id=\"video\">"+
								"<source src=\"video.webm\" type=\"video/webm\">"+
								"</video>"+
								"<div id=\"buttonbar\">" +
								"<button id=\"play\" onclick=\"video.play()\">Play</button>" +
								"<button id=\"restart\" onclick=\"video.pause()\">Pause</button>" +
								"</div>" +
								"</body>" +
						"</html>").getBytes();
				t.sendResponseHeaders(200, response.length);
				try(OutputStream os = t.getResponseBody()) {
					os.write(response);
				}
			} else if(uri.getPath().startsWith("/video.")) {
				String type = TextUtilities.getFileExtensionWithoutDot(uri.getPath());
				File video = new File(file.getParentFile(), TextUtilities.getFileNameWithoutExtension(file) + "." + type);
				t.sendResponseHeaders(200, (int)video.length());
				try(OutputStream os = t.getResponseBody()) {
					Files.copy(video.toPath(), os);
				}
			}
		}
	}
}
