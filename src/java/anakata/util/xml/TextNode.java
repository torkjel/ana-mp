package anakata.util.xml;

import java.io.IOException;

import anakata.util.io.IndentingWriter;

public class TextNode extends Element {

	public TextNode(String text) {
		super(text);
	}

	public void write(IndentingWriter iw) throws IOException {
		iw.writeRaw(getName());
	}

}
