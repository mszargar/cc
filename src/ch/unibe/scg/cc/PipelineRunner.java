package ch.unibe.scg.cc;

import java.io.IOException;
import java.io.Serializable;

import javax.inject.Inject;

import ch.unibe.scg.cc.GitInputFormat.GitRepoCodec;
import ch.unibe.scg.cc.Protos.Clone;
import ch.unibe.scg.cc.Protos.GitRepo;
import ch.unibe.scg.cc.Protos.Snippet;
import ch.unibe.scg.cells.Codec;
import ch.unibe.scg.cells.Pipeline;

/** Run the clone detector. */
public class PipelineRunner implements Serializable {
	final private static long serialVersionUID = 1L;

	final private GitPopulator gitPopulator;
	final private Function2RoughCloner function2RoughCloner;
	final private Function2FineCloner function2FineCloner;
	final private Codec<GitRepo> repoCodec;
	final private Codec<Snippet> snippet2FunctionsCodec;
	final private Codec<Clone> function2RoughClonesCodec;

	@Inject
	PipelineRunner(GitPopulator gitPopulator, Function2RoughCloner function2RoughCloner,
			Function2FineCloner function2FineCloner,
			GitRepoCodec repoCodec,
			Snippet2FunctionsCodec snippet2FunctionsCodec,
			Function2RoughClonesCodec function2RoughClonesCodec) {
		this.gitPopulator = gitPopulator;
		this.function2RoughCloner = function2RoughCloner;
		this.function2FineCloner = function2FineCloner;
		this.repoCodec = repoCodec;
		this.snippet2FunctionsCodec = snippet2FunctionsCodec;
		this.function2RoughClonesCodec = function2RoughClonesCodec;
	}

	/** Run the clone detector. */
	public void run(Pipeline<GitRepo, Clone> pipe) throws IOException, InterruptedException {
		pipe
			.influx(repoCodec)
			.map(gitPopulator)
			.shuffle(snippet2FunctionsCodec)
			.map(function2RoughCloner)
			.shuffle(function2RoughClonesCodec)
			.mapAndEfflux(function2FineCloner, function2RoughClonesCodec);
	}
}
