package anakata.modplay.loader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.ZipInputStream;

import anakata.modplay.module.Module;

public class ZippedModuleLoader extends ModuleLoader {

	private Module module;

	public ZippedModuleLoader(String name, byte[] data) throws InvalidFormatException, IOException {
		ZipInputStream zi = new ZipInputStream(new ByteArrayInputStream(data));
		zi.getNextEntry();
		module = ModuleLoader.getModuleLoader(
			name.substring(0, name.length() - EXT_ZIP.length()),
			getData(zi)).getModule();
	}

	public Module getModule() {
		return module;
	}

}
