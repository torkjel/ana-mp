/*
 * Created on Aug 28, 2004
 */
package anakata.modplay.utils;

import java.io.File;

import anakata.modplay.loader.ModuleLoader;
import anakata.modplay.module.Instrument;
import anakata.modplay.module.Module;
import anakata.modplay.module.Sample;
import anakata.sound.output.Output;
import anakata.sound.output.SoundDataFormat;
import anakata.sound.output.WavOutput;
import anakata.util.Logger;

/**
 * Extract all samles from a module. 
 * @author torkjel
 */
public class SampleExtractor {
    public static void main(String[] args) {
        try {
            ModuleLoader loader = ModuleLoader.getModuleLoader(new File(args[0]));
            Module module = loader.getModule();
            for (int n = 0; n < module.getNumberOfInstruments(); n++) {
                Instrument instrument = module.getInstrument(n);
                for (int m = 0; m < instrument.getNumberOfSamples(); m++) {
                    Sample sample = instrument.getSampleByNum(m);
                    if (sample == null) {
                        Logger.warning("Insturment " + n + ", sample " + m + " is empty");
                        continue;
                    }
                    Output out = new WavOutput(
                        module.getName().trim() + "-" + n + "-" + m + ".wav",
                        new SoundDataFormat(16,44100,2));
                    byte[] sampleData2 = new byte[sample.getLength()*4];
                    for (int p = 0; p < sample.getLength(); p++) {
                        short data = sample.getData()[p];
                        sampleData2[p*4+0] = (byte)(data & 0x0ff);
                        sampleData2[p*4+1] = (byte)(data >>> 8);
                        sampleData2[p*4+2] = (byte)(data & 0x0ff);
                        sampleData2[p*4+3] = (byte)(data >>> 8);
                    }
                    out.open();
                    out.write(sampleData2,0,sampleData2.length);
                    out.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        
    }
}
