package anakata.sound.output;

import java.io.IOException;

public class NullOutput implements Output {

	boolean open = false;
	
	public boolean close() {
		open = false;
		return true;
	}

	public boolean isOpen() {
		return open;
	}

	public boolean open() {
		open = true;
		return true;
	}

	public int write(byte[] data, int ofs, int len) throws IOException {
		return len;
	}
}
