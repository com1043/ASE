package ase.web.httpServer;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.NanoHTTPD;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import ase.ServerCore;
import ase.console.LogWriter;
import ase.fileIO.FileHandler;
import ase.web.webSocket.WebSession;
import ase.web.webSocket.WebSessionManager;

public class HTTPServer extends NanoHTTPD
{
	private static final Logger logger = LogWriter.createLogger(HTTPServer.class, "HTTPServer");
	public static final String PROP_DOCROOT = "WebServerDocRoot";
	
	public static final String rootDirectory = FileHandler.getExtResourceFile("www").toString();
	public static final String PROP_HTTP_DEFAULT_PAGE = "HttpDefaultPage";
	public final String docRoot;
	private String httpDefaultPage;
	
	private final WebSessionManager webSessionManager;

	public HTTPServer(int port, WebSessionManager webSessionManager)
	{
		super(port);
		this.webSessionManager = webSessionManager;
		this.docRoot = ServerCore.getProp(PROP_DOCROOT);
		logger.log(Level.INFO, "HTTP Document Root:"+this.docRoot);
	}

	private static Response serveImage(MIME_TYPE imageType, String dir)
	{
		String imageTypeStr = imageType.toString();
		
		return Response.newFixedLengthResponse(Status.OK, imageTypeStr, FileHandler.getResInputStream(dir), -1);
	}
	
	private static Response serveStrFile(MIME_TYPE mimeType, String dir)
	{
		return Response.newFixedLengthResponse(Status.OK, mimeType.toString(), FileHandler.getResInputStream(dir), -1);
	}
	
	private static Response serveError(Status status, String errorStr)
	{
		return Response.newFixedLengthResponse(status, errorStr, "");
	}
	
	private static Response serveRedirect(String dir)
	{
		Response r = Response.newFixedLengthResponse(Status.REDIRECT, MIME_HTML, "");
		r.addHeader("Location", dir);
		return r;
	}

	@Override
	public Response serve(IHTTPSession request)
	{
		String uri = request.getUri();

		String msg = "";
		Response response;
		
		if (uri.startsWith("/"))
		{
			String dir;
			if(uri.equals("/"))
			{
				response = HTTPServer.serveRedirect(this.httpDefaultPage);
				this.sessionService(request, response);
				return response;
			}
			else
			{
				dir = this.docRoot+uri;
			}
			
			if(!FileHandler.isExistResFile(dir))
			{
				response = HTTPServer.serveError(Status.NOT_FOUND, "Error 404: File not found");
				this.sessionService(request, response);
				return response;
			}
			
			int pos = dir.lastIndexOf( "." );
			
			if(pos == -1)
			{
				response = HTTPServer.serveError(Status.BAD_REQUEST, "Error 400: Bad Request");
				this.sessionService(request, response);
				return response;
			}
			String ext = dir.substring( pos + 1 );
			switch(ext)
			{
			case "html":
				response = serveStrFile(MIME_TYPE.MIME_HTML, dir); break;
			case "jpg":
				response = serveImage(MIME_TYPE.MIME_JPEG, dir); break;
			case "png":
				response = serveImage(MIME_TYPE.MIME_PNG, dir); break;
			case "js": case "mjs":
				response = serveStrFile(MIME_TYPE.MIME_JS,  dir); break;
			case "css":
				response = serveStrFile(MIME_TYPE.MIME_CSS,  dir); break;
			default:
				response = serveStrFile(MIME_TYPE.MIME_PLAINTEXT,  dir); break;
			}
		}
		else
		{
			response = Response.newFixedLengthResponse(msg);
		}
		this.sessionService(request, response);
		return response;
	}
	
	private void sessionService(IHTTPSession request, Response response)
	{
		String sessionUIDStr = getCookie(request, WebSessionManager.COOKIE_KEY_SESSION);
		UUID sessionUID;
		if(sessionUIDStr != null)
		{
			sessionUID = UUID.fromString(sessionUIDStr);
			WebSession s = this.webSessionManager.sessionMap.getOrDefault(sessionUID, null);
			if(s != null)
			{

				return;
			}
		}

	}
	
	public static String getCookie(IHTTPSession request, String key)
	{
		String value = request.getCookies().read(key);
		return value;
	}

	public static Response setCookie(Response response, String key, String value)
	{
		response.addCookieHeader(String.format("%s=%s; Path=/", key, value));
		return response;
	}
	
	public static Response setCookie(Response response, String key, String value, int timeSec)
	{
		response.addCookieHeader(String.format("%s=%s; max-age=%d; path=/", key, value, timeSec));
		return response;
	}
	
	public void startModule()
	{
		this.httpDefaultPage = ServerCore.getProp(PROP_HTTP_DEFAULT_PAGE);
		try
		{
			this.start(NanoHTTPD.SOCKET_READ_TIMEOUT, true);
		}
		catch (IOException e)
		{
			logger.log(Level.SEVERE,"http서버 시작중 오류", e);
		}
	}
}
