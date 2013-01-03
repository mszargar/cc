package ch.unibe.scg.cc.javaFrontend;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import ch.unibe.scg.cc.Tokenizer;
import ch.unibe.scg.cc.activerecord.Function;
import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;

@Singleton
public class JavaTokenizer implements Tokenizer {

	final String splitterRegex = "([a-zA-Z \\[\\]<>,]*\\([a-zA-Z \\[\\]<>,]*\\)[a-zA-Z \\[\\]<>,]*\\{|([^\n]*[^.]|\\n)(class|interface)[^\n]*)[^\n]*";

	@Inject
	Provider<Function> functionProvider;

	final RunAutomaton splitter;

	public JavaTokenizer() {
		RegExp splitterRegexp = new RegExp(splitterRegex);
		splitter = new RunAutomaton(splitterRegexp.toAutomaton());
	}

	@Override
	public List<Function> tokenize(String file, String fileName) {
		int currentLineNumber = 0;
		int lineLength = 0;
		int lastStart = 0;

		List<Function> ret = new ArrayList<Function>();
		AutomatonMatcher m = splitter.newMatcher(file);

		while (m.find()) {
			String currentFunctionString = file.substring(lastStart, m.start());
			lineLength = countOccurrences(currentFunctionString, '\n');

			if (!currentFunctionString.equals("")) {
				Function function = makeFunction(currentLineNumber, fileName,
						currentFunctionString);
				ret.add(function);
			}

			lastStart = m.start();
			currentLineNumber += lineLength;
		}
		String currentFunctionString = file.substring(lastStart, file.length());
		// Location location = makeLocation(currentLineNumber,
		// countOccurrences(currentFunction, '\n'));
		if (!currentFunctionString.equals("")) {
			Function function = makeFunction(currentLineNumber, fileName,
					currentFunctionString);
			ret.add(function);
		}
		return ret;
	}

	Function makeFunction(int baseLine, String fname, String contents) {
		Function f = functionProvider.get();
		f.setBaseLine(baseLine);
		f.setContents(contents);
		return f;
	}

	public static int countOccurrences(String haystack, char needle) {

		int count = 0;
		for (int i = 0; i < haystack.length(); i++) {
			if (haystack.charAt(i) == needle) {
				count++;
			}
		}
		return count;
	}
}
