/*
 * Copyright 2012 AppSatori s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.appsatori.pipes;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eu.appsatori.pipes.Node;
import eu.appsatori.pipes.Pipes;

/**
 * Servlet support for executing tasks. Once mapped to URL it will execute node by its class name.
 * This is particularly useful if you want to start your pipes using cron jobs.
 * Assuming you have following servlet configuration in your <code>web.xml</code> file
<pre>
{@code
</servlet>
	<servlet>
	<servlet-name>PipesServlet</servlet-name>
	<servlet-class>eu.appsatori.pipes.PipesServlet</servlet-class>
</servlet>
<servlet-mapping>
	<servlet-name>PipesServlet</servlet-name>
	<url-pattern>_ah/pipes/</url-pattern>
</servlet-mapping>
}
</pre>

 * it will start com.example.MyNode with parameter {@code xyz} using following url:
<pre>
{@code http://yourappid.appspot.com/_ah/pipes/?node=com.example.MyNode&arg=xyz}
</pre>

 * 
 * The parameter passed to the node is either <code>null</code> or {@link String} representing the given parameter {@code arg}
 * so if the node doesn't support {@link String} parameter just run it without the {@code arg} parameter.
 * 
 * @author <a href="mailto:vladimir.orany@appsatori.eu">Vladimir Orany</a>
 *
 */
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
			Pipes.run((Class)cls, req.getParameter("arg"));
			resp.getWriter().append("Node '"+ nodeName + "' started");
		} catch (ClassNotFoundException e) {
			String msg = "The specified class " + nodeName + " does not exist!";
			log.warning(msg);
			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
			resp.getWriter().append(msg);
		}
	}
}
