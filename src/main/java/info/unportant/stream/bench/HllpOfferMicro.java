/*
 * Copyright (C) 2013 Clément MATHIEU.
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

package info.unportant.stream.bench;

import static info.unportant.stream.bench.HllUtils.createEmptyHllp;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.GenerateMicroBenchmark;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

import com.clearspring.analytics.stream.cardinality.HyperLogLogPlus;

/**
 * The goal of theses micro-benchmarks is to check how fast 
 * offer can be and to compare different implementations.
 *
 * It does not expect to exhibit real world performances since data
 * will always be in the CPU cache.
 * 
 * We also check the scalability of the offer operation. It is not thread safe 
 * in upstream (as for 2.4.0) so the resulting register set is obviously broken.
 * But the performance data can be used to compare an alternative thread safe 
 * implementation to the maximum achievable performance.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
public class HllpOfferMicro {

	// FIXME: JMH does not yet support inheritance so this code is duplicated from the
	// FIXME: HLL implementation. This should be refactored as soon as JMH evolve
	// FIXME: Meanwhile take care to synchronize the HLL and HLL++ versions
	
	@State(Scope.Benchmark)
	public static class BenchmarkState {
		final HyperLogLogPlus hll = createEmptyHllp(); // Modified
	}
	
	@State(Scope.Thread)
	public static class ThreadState {
		final Random rand = new Random();
		final int nextInt() {
			return rand.nextInt();
		}
	}
	
	/* This one fails since HLL++ does not yet support offerHashed  // Modified
	 * But AFAIK it could be supported                              // Modified
	 */
	@GenerateMicroBenchmark()
	public boolean simpleHashed(ThreadState threadState, BenchmarkState benchState) {
		return benchState.hll.offerHashed(threadState.rand.nextLong());
	}
	
	@GenerateMicroBenchmark()
	public boolean simple(ThreadState threadState, BenchmarkState benchState) {
		return benchState.hll.offer(threadState.rand.nextLong());
	}

	// Fails with the default implementation (not thread safe) // Modified
	@Threads(4)
	@GenerateMicroBenchmark()
	public boolean scalability4(ThreadState threadState, BenchmarkState benchState) {
		return benchState.hll.offer(threadState.rand.nextLong());
	}

	// Fails with the default implementation (not thread safe) // Modified
	@Threads(0)
	@GenerateMicroBenchmark()
	public boolean scalabilityMax(ThreadState threadState, BenchmarkState benchState) {
		return benchState.hll.offer(threadState.rand.nextLong());
	}
}
