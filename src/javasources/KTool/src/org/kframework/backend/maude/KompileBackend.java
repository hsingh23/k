package org.kframework.backend.maude;

import org.kframework.backend.BasicBackend;
import org.kframework.compile.transformers.DeleteFunctionRules;
import org.kframework.kil.Definition;
import org.kframework.kil.loader.Context;
import org.kframework.kil.visitors.exceptions.TransformerException;
import org.kframework.utils.Stopwatch;
import org.kframework.utils.file.FileUtil;
import org.kframework.utils.file.KPaths;
import org.kframework.utils.general.GlobalSettings;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class KompileBackend extends BasicBackend {

	public KompileBackend(Stopwatch sw, Context context) {
		super(sw, context);
	}

	@Override
	public Definition firstStep(Definition javaDef) {
		String fileSep = System.getProperty("file.separator");
		String propPath = KPaths.getKBase(false) + fileSep + "lib" + fileSep + "maude" + fileSep;
		Properties specialMaudeHooks = new Properties();
		Properties maudeHooks = new Properties();
		try {
            FileUtil.loadProperties(maudeHooks, propPath + "MaudeHooksMap.properties");
            FileUtil.loadProperties(specialMaudeHooks, propPath + "SpecialMaudeHooks.properties");
		} catch (IOException e) {
			e.printStackTrace();
		}
		MaudeBuiltinsFilter builtinsFilter = new MaudeBuiltinsFilter(maudeHooks, specialMaudeHooks, context);
		javaDef.accept(builtinsFilter);
		final String mainModule = javaDef.getMainModule();
        StringBuilder builtins = new StringBuilder()
            .append("mod ").append(mainModule).append("-BUILTINS is\n").append(" including ")
            .append(mainModule).append("-BASE .\n")
            .append(builtinsFilter.getResult()).append("endm\n");
		FileUtil.save(context.dotk.getAbsolutePath() + "/builtins.maude", builtins);
		sw.printIntermediate("Generating equations for hooks");
		try {
			javaDef = (Definition) javaDef.accept(new DeleteFunctionRules(maudeHooks
					.stringPropertyNames(), context));
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		return super.firstStep(javaDef);
	}

	@Override
	public void run(Definition javaDef) throws IOException {
		MaudeBackend maude = new MaudeBackend(sw, context);
		maude.run(javaDef);

		String load = "load \"" + KPaths.getKBase(true) + KPaths.MAUDE_LIB_DIR + "/k-prelude\"\n";

		// load libraries if any
		String maudeLib = GlobalSettings.lib.equals("") ? "" : "load " + KPaths.windowfyPath(new File(GlobalSettings.lib).getAbsolutePath()) + "\n";
		load += maudeLib;

		final String mainModule = javaDef.getMainModule();
		//String defFile = javaDef.getMainFile().replaceFirst("\\.[a-zA-Z]+$", "");

        StringBuilder main = new StringBuilder().append(load).append("load \"base.maude\"\n")
            .append("load \"builtins.maude\"\n").append("mod ").append(mainModule).append(" is \n")
            .append("  including ").append(mainModule).append("-BASE .\n")
            .append("  including ").append(mainModule).append("-BUILTINS .\n")
            .append("eq mainModule = '").append(mainModule).append(" .\nendm\n");
        FileUtil.save(context.dotk.getAbsolutePath() + "/" + "main.maude", main);
	}

	@Override
	public String getDefaultStep() {
		return "LastStep";
	}

}
