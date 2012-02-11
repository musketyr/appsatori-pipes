package eu.appsatori.pipes;

import java.io.File;
import java.io.FileFilter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


class NodeFinder {
	
	private static enum ClassAndDirFilter implements FileFilter {
		INSTANCE;
		
		public boolean accept(File file) {
			if(file.isDirectory()){
				return true;
			}
			return file.getName().endsWith(".class");
		}
	}

	private final String pkg;
	
	public NodeFinder(String pkg) {
		this.pkg = pkg;
	}
	
	public Collection<NodeDescriptor<?>> find(){
		if(pkg == null){
			return Collections.emptyList();
		}
		try {
			String pkgPath =  File.separator + pkg.replaceAll("\\.", File.separator);
			URL url = getClass().getResource(pkgPath); 
			if(url == null){
				return Collections.emptyList();
			}
			File dir = new File(url.toURI());
			if(!dir.exists()){
				return Collections.emptyList();
			}
			return Collections.unmodifiableList(findInDir(dir, "."));
		} catch (URISyntaxException e) {
			return Collections.emptyList();
		}
	}

	@SuppressWarnings("unchecked")
	private List<NodeDescriptor<?>> findInDir(File dir, String packageSuffix) {
		List<NodeDescriptor<?>> ret = new ArrayList<NodeDescriptor<?>> ();
		for(File f : dir.listFiles(ClassAndDirFilter.INSTANCE)){
			if(f.isDirectory()){
				ret.addAll(findInDir(f, packageSuffix + f.getName() + "."));
				continue;
			}
			try {
				Class<?> cls = Class.forName(pkg + packageSuffix + f.getName().replaceAll(".class$", ""));
				if(Node.class.isAssignableFrom(cls)){
					try {
						ret.add(NodeDescriptor.from((Class<Node<?>>)cls));
					} catch (IllegalArgumentException e){
						// something wrong with the class
					}
				}
			} catch (ClassNotFoundException e) {
				throw new IllegalStateException("Class " + pkg + "." + f.getName().replaceAll(".class$", "") + " should exist but it doesn't");
			}
		}
		return ret;
	}

}
