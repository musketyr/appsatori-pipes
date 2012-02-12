package eu.appsatori.pipes;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eu.appsatori.pipes.Node;
import eu.appsatori.pipes.Pipes;

public class PipesServlet extends HttpServlet {
	
	private static final long serialVersionUID = 5680664857046934589L;
	private static final Logger log = Logger.getLogger(PipesServlet.class.getName());
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String nodeName = req.getParameter("node");
		if(nodeName == null){
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			resp.getWriter().append("Use 'node' parameter to specify node you want to start!");
			return;
		}
		try {
			Class<?> cls = Class.forName(nodeName);
			if(!Node.class.isAssignableFrom(cls)){
				String msg = "The specified class " + nodeName + " isn't node!";
				log.warning(msg);
				resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				resp.getWriter().append(msg);
				return;
			}
			Pipes.run((Class)cls);
			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
			resp.getWriter().append("Node '"+ nodeName + "' started");
		} catch (ClassNotFoundException e) {
			String msg = "The specified class " + nodeName + " does not exist!";
			log.warning(msg);
			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
			resp.getWriter().append(msg);
		}
	}
}
