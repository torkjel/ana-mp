/*
 * Created on Apr 6, 2005
 */
package anakata.modplay.utils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import anakata.modplay.loader.InvalidFormatException;
import anakata.modplay.loader.ModUnits;
import anakata.modplay.loader.ModuleLoader;
import anakata.modplay.module.Instrument;
import anakata.modplay.module.Module;
import anakata.modplay.module.ModuleUnits;
import anakata.modplay.module.Sample;
import anakata.util.io.IndentingWriter;
import anakata.util.xml.Element;
import anakata.util.xml.List;
import anakata.util.xml.TextNode;

/**
 * Convert a module to XML format.  This is meant mostly as a debugging/development aid.
 * @author torkjel
 */
public class XMLConvert {
    public static void main(String[] args) throws InvalidFormatException, IOException {

		File file = args.length > 0 ?
			new File(args[0]) :
			new File("/mnt/data/data/media/mods/eye.mod");

		ModuleLoader loader = ModuleLoader.getModuleLoader(file);
		Module mod = loader.getModule();

		XMLConvert conv = new XMLConvert(mod);
		Writer w = new OutputStreamWriter(System.out);
		conv.write(w);
		w.flush();
	}

	private Module module;

	public XMLConvert(Module module) {
		this.module = module;
	}

	public void write(Writer writer) throws IOException {
		IndentingWriter iw = new IndentingWriter(writer);
		Element elem = new Element("module")
			.attribute("type", "ana-mp")
			.attribute("version", 1);
		writeHeader(elem);
		writeInstruments(elem);
		elem.write(iw);
	}

	private void writeHeader(Element root) {
		Element header = new Element("header")
			.property("name", module.getName())
			.property("id", module.getId())
			.property("tracker", module.getTracker())
			.property("bmp", module.getInitialBpm())
			.property("speed", module.getInitialSpeed())
			.property("volume", module.getInitialVolume())
			.property("panning-type", module.getPanningType())
			.property("restart-pos", module.getRestartPos())
			.property("track-count", module.getTrackCount())
			.property("instrument-count", module.getNumberOfInstruments())
			.property("pattern-count", module.getNumberOfPatterns())
			.property("position-count", module.getNumberOfPositions());

		for (String key : module.getPropertyNames())
			header.property(key, module.getProperty(key));

		printDescription(header);
		printPatternOrder(header);
		printInitialVolume(header);
		printInitialPanning(header);
		root.element(header);
	}

	private void printDescription(Element header) {
		if (module.getDescription() != null)
			header.element(new Element("description").element(new TextNode(module.getDescription())));
		else
			header.element(new Element("description"));
	}

	private void printPatternOrder(Element header) {
		List order = new List("pattern-order");
		for (int n = 0; n < module.getNumberOfPositions(); n++)
			order.item(module.getPatternIndexAtPos(n));
		header.element(order);
	}

	private void printInitialVolume(Element header) {
		List volume = new List("volume");
		for (int n = 0; n < module.getTrackCount(); n++)
			volume.item(module.getInitialVolume(n));
		header.element(volume);
	}

	private void printInitialPanning(Element header) {
		List panning = new List("panning");
		for (int n = 0; n < module.getTrackCount(); n++)
			panning.item(module.getInitialPanning(n));
		header.element(panning);
	}

	private void writeInstruments(Element root) {
		Element samples = new Element("samples");;
		Set<Sample> allSamples = new HashSet<Sample>();
		for (int n = 0; n < module.getNumberOfInstruments(); n++) {
			Instrument instrument = module.getInstrument(n);
			for (int m = 0; m < instrument.getNumberOfSamples(); m++) {
				Sample sample = instrument.getSampleByNum(m);
				if (!allSamples.contains(sample)) {
					allSamples.add(sample);
					writeSample(samples, sample);
				}
			}
		}
		root.element(samples);
	}

	private void writeSample(Element samples, Sample sample) {
		Element s = new Element("sample").attribute("id", sample.getId());
		s.element(
			new Element("header")
				.property("name", sample.getName())
				.property("volume", sample.getVolume())
				.property("relative-note", sample.getRelativeNote())
				.property("finetune", sample.getFineTune())
				.property("panning", sample.getPanning())
				.property("length", sample.getLength())
				.property("loop-type", sample.getLoopType())
				.property("loop-length", sample.getLoopLength())
				.property("loop-start", sample.getLoopStart()));
		writeUnits(s, sample.getUnits());
		samples.element(s);
	}

	private void writeUnits(Element sample, ModuleUnits units) {
		Element u = new Element("units").attribute("type", units.getName());
		if (units instanceof ModUnits)
			u.property("clock", ((ModUnits)units).getAmigaClock());
		sample.element(u);
	}
}
