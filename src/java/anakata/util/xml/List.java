package anakata.util.xml;

import java.io.IOException;
import java.util.ArrayList;

import anakata.util.io.IndentingWriter;

public class List extends Element {

	private java.util.List<String> items = new ArrayList<String>();

	public List(String name) {
		super(name);
	}

	public List item(double value) {
		return item(String.valueOf(value));
	}

	public List item(int value) {
		return item(String.valueOf(value));
	}

	public List item(String value) {
		items.add(value);
		return this;
	}

	public void write(IndentingWriter writer) throws IOException {
		writer.writeln("<list name=\"" + getName() + "\">");
		writer.indent();
		for (String item : items) {
			writer.writeln("<item value=\"" + item + "\"/>");
		}
		writer.unindent();
		writer.writeln("</list>");
	}

}
