package org.kframework.parser.generator;

import org.kframework.kil.*;
import org.kframework.kil.loader.Context;
import org.kframework.kil.loader.JavaClassesFactory;
import org.kframework.kil.visitors.exceptions.TransformerException;
import org.kframework.parser.basic.KParser;
import org.kframework.utils.XmlLoader;
import org.kframework.utils.errorsystem.KException;
import org.kframework.utils.errorsystem.KException.ExceptionType;
import org.kframework.utils.errorsystem.KException.KExceptionGroup;
import org.kframework.utils.errorsystem.KMessages;
import org.kframework.utils.file.FileUtil;
import org.kframework.utils.file.KPaths;
import org.kframework.utils.general.GlobalSettings;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.Map;

public class BasicParser {
	private List<DefinitionItem> moduleItems;
	private Map<String, Module> modulesMap;
	private List<String> filePaths;
	private File mainFile;
	private String mainModule;
	private boolean autoinclude;

	public BasicParser(boolean autoinclude) {
		this.autoinclude = autoinclude;
	}

	/**
	 * Given a file, this method parses it and creates a list of modules from all of the included files.
	 * 
	 * @param filepath
	 */
	public void slurp(String fileName, Context context) {
		moduleItems = new ArrayList<DefinitionItem>();
		modulesMap = new HashMap<String, Module>();
		filePaths = new ArrayList<String>();

		try {
			// parse first the file given at console for fast failure in case of error
			File file = new File(fileName);
			if (!file.exists())
				GlobalSettings.kem.register(new KException(ExceptionType.ERROR, KExceptionGroup.CRITICAL, KMessages.ERR1004 + fileName + " given at console.", "", ""));

			slurp2(file, context);

			if (autoinclude) {
				// parse the autoinclude.k file but remember what I parsed to give the correct order at the end
				List<DefinitionItem> tempmi = moduleItems;
				moduleItems = new ArrayList<DefinitionItem>();

				file = buildCanonicalPath("autoinclude.k", new File(fileName));
				if (file == null)
					GlobalSettings.kem.register(new KException(ExceptionType.ERROR, KExceptionGroup.CRITICAL, KMessages.ERR1004 + fileName
							+ " autoimporeted for every definition ", fileName, ""));

				slurp2(file, context);
				moduleItems.addAll(tempmi);
			}

			setMainFile(file);
			context.finalizeRequirements();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private File buildCanonicalPath(String fileName, File parentFile) throws IOException {
		File file = new File(parentFile.getCanonicalFile().getParent() + "/" + fileName);
		if (file.exists())
			return file;
		file = new File(KPaths.getKBase(false) + "/include/" + fileName);
		if (file.exists())
			return file;

		return null;
	}

	private void slurp2(File file, Context context) throws IOException {
		String cannonicalPath = file.getCanonicalPath();
		if (!filePaths.contains(cannonicalPath)) {
			filePaths.add(cannonicalPath);

			List<DefinitionItem> defItemList = parseFile(file, context);

			// go through every required file
			for (ASTNode di : defItemList) {
				if (di instanceof Require) {
					Require req = (Require) di;

					File newFile = buildCanonicalPath(req.getValue(), file);

					if (newFile == null)
						GlobalSettings.kem.register(new KException(ExceptionType.ERROR, KExceptionGroup.CRITICAL, KMessages.ERR1004 + req.getValue(), req.getFilename(), req
								.getLocation()));

					slurp2(newFile, context);
					context.addFileRequirement(newFile.getCanonicalPath(), file.getCanonicalPath());
				}
			}

			boolean predefined = file.getCanonicalPath().startsWith(KPaths.getKBase(false) + File.separator + "include");
			if (!predefined)
				context.addFileRequirement(buildCanonicalPath("autoinclude.k", file).getCanonicalPath(), file.getCanonicalPath());

			// add the modules to the modules list and to the map for easy access
			for (DefinitionItem di : defItemList) {
				if (predefined)
					di.setPredefined(true);

				this.moduleItems.add(di);
				if (di instanceof Module) {
					Module m = (Module) di;
					this.modulesMap.put(m.getName(), m);
				}
			}
		}
	}

	public static List<DefinitionItem> parseFile(File file, Context context) {
		if (GlobalSettings.verbose)
			System.out.println("Including file: " + file.getAbsolutePath());
		String content = FileUtil.getFileContent(file.getAbsolutePath());
		return parseString(content, file.getAbsolutePath(), context);
	}

	/**
	 * Parses a string representing a file with modules in it.
	 * Returns only the basic parsing AST which contain bubbles instead of rules.
	 * @param content - the input string.
	 * @param filename - only for error reporting purposes. Can be empty string.
	 * @param context - the context for disambiguation purposes.
	 * @return - a list of DefinitionItems
	 */
	@SuppressWarnings("unchecked")
	public static List<DefinitionItem> parseString(String content, String filename, Context context) {
		String parsed = KParser.ParseKString(content);
		Document doc = XmlLoader.getXMLDoc(parsed);
		XmlLoader.addFilename(doc.getFirstChild(), filename);
		try {
			XmlLoader.reportErrors(doc);
		} catch (TransformerException e) {
			e.report();
		}

		NodeList nl = doc.getFirstChild().getChildNodes();
		List<DefinitionItem> defItemList = new ArrayList<DefinitionItem>();

		JavaClassesFactory.startConstruction(context);
		for (int i = 0; i < nl.getLength(); i++) {
			if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
				Element elm = (Element) nl.item(i);
				defItemList.add((DefinitionItem) JavaClassesFactory.getTerm(elm));
			}
		}
		JavaClassesFactory.endConstruction();
		defItemList = (List<DefinitionItem>) BasicParser.relocateComments(defItemList);
		return defItemList;
	}

	public void setMainFile(File mainFile) {
		this.mainFile = mainFile;
	}

	public File getMainFile() {
		return mainFile;
	}

	public void setMainModule(String mainModule) {
		this.mainModule = mainModule;
	}

	public String getMainModule() {
		return mainModule;
	}

	public List<DefinitionItem> getModuleItems() {
		return moduleItems;
	}

	public void setModuleItems(List<DefinitionItem> moduleItems) {
		this.moduleItems = moduleItems;
	}

	public Map<String, Module> getModulesMap() {
		return modulesMap;
	}

	public void setModulesMap(Map<String, Module> modulesMap) {
		this.modulesMap = modulesMap;
	}

	private static List<? extends ASTNode> sort(List<? extends ASTNode> nodes) {
		Collections.sort(nodes, new Comparator<ASTNode>() {
			@Override
			public int compare(ASTNode n1, ASTNode n2) {
				String[] loc1 = n1.getLocation().split("\\(|,|\\)");
				int loc11 = Integer.parseInt(loc1[1]);
				int loc12 = Integer.parseInt(loc1[2]);
				String[] loc2 = n2.getLocation().split("\\(|,|\\)");
				int loc21 = Integer.parseInt(loc2[1]);
				int loc22 = Integer.parseInt(loc2[2]);
				if (loc11 > loc21)
					return 1;
				if (loc11 == loc21 && loc12 > loc22)
					return 1;

				return 0;
			}
		});

		return nodes;
	}

	/**
	 * All comments are returned at the end of the DefinitionItem list so they need to be sorted and relocated into modules and between modules.
	 * 
	 * @param nodes
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static List<? extends ASTNode> relocateComments(List<? extends ASTNode> nodes) {

		Properties p = System.getProperties();
		p.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
		System.setProperties(p);

		nodes = BasicParser.sort(nodes);
		List<ASTNode> defItemListTemp = new ArrayList<ASTNode>();

		List<ASTNode> commentsToRemove = new ArrayList<ASTNode>();

		for (int i = 0; i < nodes.size(); i++) {
			ASTNode current = nodes.get(i);
			if (current instanceof Module) {
				Module m = (Module) current;
				for (; i + 1 < nodes.size(); i++) {
					ASTNode next = nodes.get(i + 1);
					if (next instanceof LiterateDefinitionComment && isInside(m, next)) {
						m.getItems().add(new LiterateModuleComment((LiterateDefinitionComment) next));
						commentsToRemove.add(next);
					} else
						break;
				}
				m.setItems((List<ModuleItem>) sort(m.getItems()));
			}

			defItemListTemp.add(current);
		}

		for (ASTNode anode : commentsToRemove)
			nodes.remove(anode);

		return nodes;
	}

	private static boolean isInside(ASTNode n1, ASTNode n2) {
		String[] loc1 = n1.getLocation().split("\\(|,|\\)");
		int loc11 = Integer.parseInt(loc1[3]);
		int loc12 = Integer.parseInt(loc1[4]);
		String[] loc2 = n2.getLocation().split("\\(|,|\\)");
		int loc21 = Integer.parseInt(loc2[1]);
		int loc22 = Integer.parseInt(loc2[2]);
		if (loc11 > loc21)
			return true;
		if (loc11 == loc21 && loc12 > loc22)
			return true;

		return false;
	}
}
