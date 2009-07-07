package anakata.util.xml;

import java.io.IOException;

import anakata.util.io.IndentingWriter;

public class Property extends Attribute {

	public Property(String name, String value) {
		super(name, value);
	}

	public void write(IndentingWriter iw) throws IOException {
		iw.writeln("<property name=\"" + name + "\" value=\"" + value + "\"/>");
	}
}
