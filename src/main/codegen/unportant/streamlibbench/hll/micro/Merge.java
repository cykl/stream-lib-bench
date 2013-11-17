<@pp.dropOutputFile />
<#list HyperLogLogImplementations as impl>
<@pp.changeOutputFile name=pp.pathTo(impl.className + "Merge.java") />

/*
 * Copyright (C) 2013 Clément MATHIEU
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package unportant.streamlibbench.hll.micro;

import static info.unportant.stream.bench.HllUtils.*;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.CompilerControl;
import org.openjdk.jmh.annotations.GenerateMicroBenchmark;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

import com.clearspring.analytics.stream.cardinality.CardinalityMergeException;
import com.clearspring.analytics.stream.cardinality.ICardinality;

/**
 * The goal of theses micro-benchmarks is to check how fast 
 * merge can be and to compare different implementations.
 *
 * It does not expect to exhibit real world performances since data
 * will always be in the CPU cache.
 * 
 * We also check the scalability of the merge operation. It is not thread safe 
 * in upstream (as for 2.4.0) so the resulting register set is obviously broken.
 * But the performance data can be used to compare an alternative thread safe 
 * implementation to the maximum achievable performance.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
public class ${impl.className}Merge {
	
	@State(Scope.Benchmark)
	public static class BenchmarkState {
		ICardinality hll1 = create${impl.className}(10_000);
		ICardinality hll2 = create${impl.className}(10_000);
	}

	@State(Scope.Thread)
	public static class ThreadState {
		Random rand = new Random();
	}
	
	
	@GenerateMicroBenchmark
	public ICardinality dumb(ThreadState threadState, BenchmarkState benchState) 
			throws CardinalityMergeException {
		return benchState.hll1.merge(benchState.hll2);
	}
	
	@GenerateMicroBenchmark()
	public ICardinality evolving(ThreadState threadState, BenchmarkState benchState) 
			throws CardinalityMergeException {
		return evolving0(threadState, benchState);
	}
	
	@CompilerControl(CompilerControl.Mode.INLINE)
	private ICardinality evolving0(ThreadState threadState, BenchmarkState benchState) 
			throws CardinalityMergeException {
		benchState.hll1.offer(threadState.rand.nextLong());
		benchState.hll2.offer(threadState.rand.nextLong());
		
		benchState.hll1 = benchState.hll1.merge(benchState.hll1);
		return benchState.hll1;
	}
}
</#list>