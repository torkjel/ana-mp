package anakata.util.xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import anakata.util.io.IndentingWriter;

public class Element {

	private String name;

	private List<Attribute> attributes = new ArrayList<Attribute>();
	private List<Property> properties = new ArrayList<Property>();
	private List<Element> children = new ArrayList<Element>();

	public Element(String name) {
		this.name = name;
	}

	public Element property(String name, String value) {
		properties.add(new Property(name, value));
		return this;
	}

	public Element property(String name, int value) {
		return property(name, String.valueOf(value));
	}

	public Element property(String name, double value) {
		return property(name, String.valueOf(value));
	}

	public Element attribute(String name, int value) {
		return attribute(name, String.valueOf(value));
	}

	public Element attribute(String name, double value) {
		return attribute(name, String.valueOf(value));
	}

	public Element attribute(String name, String value) {
		attributes.add(new Attribute(name, value));
		return this;
	}

	public Element element(Element elem) {
		children.add(elem);
		return this;
	}

	protected String getName() {
		return name;
	}

	public void write(IndentingWriter iw) throws IOException {
		iw.write("<" + name);
		for (Attribute a : attributes)
			a.write(iw);
		if (children.size() == 0 && properties.size() == 0)
			iw.writeln("/>");
		else {
			iw.writeln(">");
			iw.indent();
			for (Property p : properties)
				p.write(iw);
			for (Element e : children) {
				e.write(iw);
			}
			iw.unindent();
			iw.writeln("</" + name + ">");
		}
	}
}
