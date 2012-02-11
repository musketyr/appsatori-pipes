package eu.appsatori.pipes;

import java.util.Collection;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import eu.appsatori.pipes.NodeDatastore;
import eu.appsatori.pipes.NodeDescriptor;
import eu.appsatori.pipes.Pipes;

public class PipesServlet extends HttpServlet {
	
	private static final long serialVersionUID = 5680664857046934589L;
	private static final Logger log = Logger.getLogger(PipesServlet.class.getName());

	@Override
	public void init(ServletConfig config) throws ServletException {
		initNodeDatastore(config.getInitParameter("nodes-datastore"));
		initPipeDatastore(config.getInitParameter("pipes-datastore"));
		initNodes(config.getInitParameter("nodes-packages"));
	}

	private void initNodeDatastore(String initParameter) {
		if(initParameter == null){
			return;
		}
		try {
			Class<?> cls = Class.forName(initParameter);
			if(!NodeDatastore.class.isAssignableFrom(cls)){
				log.warning("The class specified in 'nodes-datastore' is not instance of NodeDatastore! Using default datastore instead!");
			}
			Pipes.setNodeDatastore((NodeDatastore) cls.newInstance());
		} catch (ClassNotFoundException e) {
			log.warning("The class specified in 'nodes-datastore' does not exist! Using default datastore instead!");
		} catch (InstantiationException e) {
			log.warning("The class specified in 'nodes-datastore' cannot be initiated because it does not have parameterless constructor! Using default datastore instead!");
		} catch (IllegalAccessException e) {
			log.warning("The class specified in 'nodes-datastore' cannot be initiated because it does not have parameterless constructor! Using default datastore instead!");
		}
	}
	
	private void initPipeDatastore(String initParameter) {
		if(initParameter == null){
			return;
		}
		try {
			Class<?> cls = Class.forName(initParameter);
			if(!PipeDatastore.class.isAssignableFrom(cls)){
				log.warning("The class specified in 'pipes-datastore' is not instance of PipeDatastore! Using default datastore instead!");
			}
			Pipes.setPipeDatastore((PipeDatastore) cls.newInstance());
		} catch (ClassNotFoundException e) {
			log.warning("The class specified in 'pipes-datastore' does not exist! Using default datastore instead!");
		} catch (InstantiationException e) {
			log.warning("The class specified in 'pipes-datastore' cannot be initiated because it does not have parameterless constructor! Using default datastore instead!");
		} catch (IllegalAccessException e) {
			log.warning("The class specified in 'pipes-datastore' cannot be initiated because it does not have parameterless constructor! Using default datastore instead!");
		}
	}

	private void initNodes(String nodePackages) {
		if(nodePackages == null){
			log.warning("The parameter 'nodes-packages' wasn't set. There will be no nodes added automatically!");
			return;
		}
		NodeDatastore nds = Pipes.getNodeDatastore();
		for(String pkg : nodePackages.split(",")){
			NodeFinder finder = new NodeFinder(pkg);
			Collection<NodeDescriptor<?>> nodes = finder.find();
			for(NodeDescriptor<?> node : nodes){
				log.info("Adding " + node);
				nds.add(node);
			}
		}
	}
}
