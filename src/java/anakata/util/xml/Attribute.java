package anakata.util.xml;

import java.io.IOException;

import anakata.util.io.IndentingWriter;

public class Attribute {

	protected String name;
	protected String value;

	public Attribute(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public void write(IndentingWriter iw)  throws IOException {
		iw.write(" " + name + "=\"" + value + "\"");
	}
}
