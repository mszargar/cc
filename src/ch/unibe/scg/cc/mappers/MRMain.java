package ch.unibe.scg.cc.mappers;

import java.io.IOException;
import java.util.Arrays;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

import ch.unibe.scg.cc.modules.CCModule;
import ch.unibe.scg.cc.modules.HBaseModule;
import ch.unibe.scg.cc.modules.JavaModule;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

/**
 * Executes MR-jobs. All Mappers/Reducers use this class as the Main-Class in
 * the manifest.
 * <p>
 * {@code 
 * <antcall target="jar">
 * 	<param name="mainClass" value="ch.unibe.scg.cc.mappers.MRMain" />
 * 	</antcall>
 * }
 * <p>
 * Finally, the command which gets called on the server looks somewhat like
 * this:
 * <p>
 * {@code 
 * hadoop jar /tmp/cc.jar ch.unibe.scg.cc.mappers.IndexFacts2Files
 * }
 * <p>
 * Provides generic Mappers and Reducers that will create the real
 * Mapper/Reducer by using dependency injection. Which Mapper/Reducer gets
 * injected is defined in the configuration with the attribute
 * "GuiceMapperAnnotation"/"GuiceReducerAnnotation" respectively.
 */
public class MRMain extends Configured implements Tool {
	static Logger logger = Logger.getLogger(MRMain.class);

	public static void main(String[] args) throws Throwable {
		logger.debug(Arrays.toString(args));
		ToolRunner.run(new MRMain(), args);
	}

	@Override
	public int run(String[] args) {
		logger.debug(Arrays.toString(args));
		assert args.length == 1;
		Class<?> c;
		try {
			c = Class.forName(args[0]);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		AbstractModule confModule = new AbstractModule() {
			@Override
			protected void configure() {
				// bind(Configuration.class).annotatedWith(Names.named("commandLine")).toInstance(getConf());
			}
		};
		Injector injector = Guice.createInjector(confModule, new CCModule(),
				new JavaModule(), new HBaseModule());
		Object instance = injector.getInstance(c);
		((Runnable) instance).run();
		return 0;
	}

	/**
	 * We can only pass the Mapper as a class, but we want to configure our
	 * mapper using Guice. We'd prefer to set the mapper as an object (already
	 * Guice configured), but Hadoop won't let us. So, this class bridges
	 * between Guice and Hadoop. In setup, we Guice configure the real reducer,
	 * and this class acts as a proxy to the guice-configured reducer.
	 * 
	 * <p>
	 * All methods except
	 * {@link #run(org.apache.hadoop.mapreduce.Mapper.Context)} are called on
	 * the guice-configured reducer.
	 */
	public static class MRMainMapper<KEYIN, VALUEIN, KEYOUT, VALUEOUT> extends
			Mapper<KEYIN, VALUEIN, KEYOUT, VALUEOUT> {
		GuiceMapper<KEYIN, VALUEIN, KEYOUT, VALUEOUT> guiceMapper;

		@SuppressWarnings("unchecked")
		@Override
		protected void setup(Context context) throws IOException,
				InterruptedException {
			String clazz = context.getConfiguration().get(
					"GuiceMapperAnnotation");
			Injector injector = Guice.createInjector(new CCModule(),
					new JavaModule(), new HBaseModule());
			guiceMapper = injector.getInstance(Key.get(GuiceMapper.class,
					Names.named(clazz)));
			guiceMapper.setup(context);
		}

		@Override
		protected void map(KEYIN key, VALUEIN value, Context context)
				throws IOException, InterruptedException {
			guiceMapper.map(key, value, context);
		}

		@Override
		protected void cleanup(Context context) throws IOException,
				InterruptedException {
			guiceMapper.cleanup(context);
		}

		/**
		 * We need to call {@link #run(Mapper.Context)} on the super class here
		 * in order to keep control over the {@link #setup(Mapper.Context)},
		 * {@link #map(Object, Object, Mapper.Context)} and
		 * {@link #cleanup(Mapper.Context)} methods.
		 */
		public void run(Context context) throws IOException,
				InterruptedException {
			super.run(context);
		}
	}

	/** see {@link MRMainMapper} */
	public static class MRMainTableMapper<KEYOUT, VALUEOUT> extends
			TableMapper<KEYOUT, VALUEOUT> {
		GuiceTableMapper<KEYOUT, VALUEOUT> guiceMapper;

		@SuppressWarnings("unchecked")
		@Override
		protected void setup(Context context) throws IOException,
				InterruptedException {
			String clazz = context.getConfiguration().get(
					"GuiceMapperAnnotation");
			Injector injector = Guice.createInjector(new CCModule(),
					new JavaModule(), new HBaseModule());
			guiceMapper = injector.getInstance(Key.get(GuiceTableMapper.class,
					Names.named(clazz)));
			guiceMapper.setup(context);
		}

		@Override
		protected void map(ImmutableBytesWritable key, Result value,
				Context context) throws IOException, InterruptedException {
			guiceMapper.map(key, value, context);
		}

		@Override
		protected void cleanup(Context context) throws IOException,
				InterruptedException {
			guiceMapper.cleanup(context);
		}

		public void run(Context context) throws IOException,
				InterruptedException {
			super.run(context);
		}
	}

	/**
	 * see {@link MRMainMapper}
	 */
	public static class MRMainTableReducer
			extends
			TableReducer<ImmutableBytesWritable, ImmutableBytesWritable, ImmutableBytesWritable> {
		GuiceTableReducer<ImmutableBytesWritable, ImmutableBytesWritable, ImmutableBytesWritable> reducer;

		@SuppressWarnings("unchecked")
		@Override
		protected void setup(Context context) throws IOException,
				InterruptedException {
			String clazz = context.getConfiguration().get(
					"GuiceReducerAnnotation");
			Injector injector = Guice.createInjector(new CCModule(),
					new JavaModule(), new HBaseModule());
			reducer = injector.getInstance(Key.get(GuiceTableReducer.class,
					Names.named(clazz)));
			reducer.setup(context);
		}

		@Override
		protected void cleanup(Context context) throws IOException,
				InterruptedException {
			reducer.cleanup(context);
		}

		public void run(Context context) throws IOException,
				InterruptedException {
			super.run(context);
		}

		@Override
		protected void reduce(ImmutableBytesWritable key,
				Iterable<ImmutableBytesWritable> values, Context context)
				throws IOException, InterruptedException {
			reducer.reduce(key, values, context);
		}
	}
}
