package ch.unibe.scg.cc;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;
import org.unibe.scg.cells.Codec;

import ch.unibe.scg.cc.Annotations.FunctionString;
import ch.unibe.scg.cc.Protos.Function;
import ch.unibe.scg.cc.javaFrontend.JavaModule;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

public class FunctionStringCodecTest {
	@Test
	public void testFunctionStringCodec() throws IOException {
		Injector i = Guice.createInjector(new CCModule(), new JavaModule(), new InMemoryModule());
		Codec<Function> fsCodec = i.getInstance(Key.get(new TypeLiteral<Codec<Function>>() {}, FunctionString.class));
		String input = "" +
				"public final class GitTablePopulatorTest {\n"
				+ "	@Test\n"
				+ "	\n"
				+ "	public void testProjnameRegex() {\n"
				+ "		GitTablePopulatorMapper gtpm = new GitTablePopulatorMapper(null, null, null, null, null, null, null, null,\n"
				+ "				null);\n"
				+ "		String fullPathString = \"har://hdfs-haddock.unibe.ch/projects/testdata.har\"\n"
				+ "				+ \"/apfel/.git/objects/pack/pack-b017c4f4e226868d8ccf4782b53dd56b5187738f.pack\";\n"
				+ "		String projName = gtpm.getProjName(fullPathString);\n"
				+ "		Assert.assertEquals(\"apfel\", projName);\n"
				+ "		\n"
				+ "		fullPathString = \"har://hdfs-haddock.unibe.ch/projects/dataset.har/dataset/sensei/objects/pack/pack-a33a3daca1573e82c6fbbc95846a47be4690bbe4.pack\";\n"
				+ "		projName = gtpm.getProjName(fullPathString);\n"
				+ "		Assert.assertEquals(\"sensei\", projName);\n"
				+ "	}\n"
				+ "};";
		Function f = Function.newBuilder().setContents(input).build();
		assertEquals(fsCodec.decode(fsCodec.encode(f)).getContents(), input);
	}
}
