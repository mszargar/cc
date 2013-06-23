package ch.unibe.scg.cc;

import java.security.MessageDigest;
import java.util.Comparator;

import javax.inject.Singleton;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.util.Bytes;

import ch.unibe.scg.cc.Protos.SnippetMatch;
import ch.unibe.scg.cc.activerecord.CodeFileFactory;
import ch.unibe.scg.cc.activerecord.ConfigurationProvider;
import ch.unibe.scg.cc.activerecord.Function.FunctionFactory;
import ch.unibe.scg.cc.activerecord.ProjectFactory;
import ch.unibe.scg.cc.activerecord.VersionFactory;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;

//@formatter:off
public class CCModule extends AbstractModule {
	@Override
	protected void configure() {
		bind(MessageDigest.class).toProvider(MessageDigestProvider.class).in(Singleton.class);
		bind(Configuration.class).toProvider(ConfigurationProvider.class).in(Singleton.class);
		bind(Backend.class).to(Backend.RegisterClonesBackend.class).in(Singleton.class);
		bind(new TypeLiteral<Comparator<byte[]>>() {}).toInstance(Bytes.BYTES_COMPARATOR);
		bind(new TypeLiteral<Comparator<SnippetMatch>>() {}).to(SnippetMatchComparator.class);

		// factories
		install(new FactoryModuleBuilder().build(ProjectFactory.class));
		install(new FactoryModuleBuilder().build(VersionFactory.class));
		install(new FactoryModuleBuilder().build(CodeFileFactory.class));
		install(new FactoryModuleBuilder().build(FunctionFactory.class));

		bind(Boolean.class).annotatedWith(Names.named("writeToWalEnabled")).toInstance(false);
	}
}