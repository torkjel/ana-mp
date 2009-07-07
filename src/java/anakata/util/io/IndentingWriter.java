package anakata.util.io;

import java.io.IOException;
import java.io.Writer;

/**
 * a writer that support indentation
 * @author torkjel
 */
public class IndentingWriter extends Writer {

	private static final String INDENT = "  ";
	private String indent = "";

	private Writer writer;

	public IndentingWriter(Writer writer) {
		super();
		this.writer = writer;
	}

	private char lastChar = ' ';

	public void write(char[] cbuf, int off, int len) throws IOException {
		char c;
		for (int i = off; i < off+len; i++) {
			c = cbuf[i];
			if (lastChar == '\n')
				writer.write(indent);
			writer.write(c);
			lastChar = c;
		}
	}

	public void writeln() throws IOException {
		write("\n");
	}

	public void writeln(String str) throws IOException {
		write(str);
		write("\n");
	}

	public void writeRaw(String data) throws IOException {
		writer.write(data);
	}

	public void flush() throws IOException {
		writer.flush();
	}

	public void close() throws IOException {
		writer.close();
	}

	public void indent() {
		indent += INDENT;
	}

	public void unindent() {
		if (indent.length() >= INDENT.length())
			indent = indent.substring(2);
	}
}
