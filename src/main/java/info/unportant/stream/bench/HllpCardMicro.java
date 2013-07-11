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

package info.unportant.stream.bench;

import static info.unportant.stream.bench.HllUtils.*;

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

import com.clearspring.analytics.stream.cardinality.CardinalityMergeException;
import com.clearspring.analytics.stream.cardinality.HyperLogLogPlus;

/**
 * The goal of theses micro-benchmarks is to check how fast 
 * cardinality can be and to compare different implementations.
 *
 * It does not expect to exhibit real world performances since data
 * will always be in the CPU cache.
 * 
 * Several cardinalities are tested to check if the number of elements 
 * change the performance behavior and to be able to compare HLL to HLL++.
 * 
 * We also check the scalability of the cardinality operation.
 */
@BenchmarkMode(Mode.Throughput)
@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
public class HllpCardMicro {

	// FIXME: JMH does not yet support inheritance so this code is duplicated from the
	// FIXME: HLL implementation. This should be refactored as soon as JMH evolve
	// FIXME: Meanwhile take care to synchronize the HLL and HLL++ versions
	
	HyperLogLogPlus hllEmpty  = createHllp(0);         // Modified
	HyperLogLogPlus hllMedium = createHllp(10_000);    // Modified
	HyperLogLogPlus hllLarge  = createHllp(1_000_000); // Modified

	@GenerateMicroBenchmark()
	public long empty() throws CardinalityMergeException {
		return hllEmpty.cardinality();
	}

	@GenerateMicroBenchmark()
	public long medium() throws CardinalityMergeException {
		return hllMedium.cardinality();
	}

	@GenerateMicroBenchmark()
	public long large() throws CardinalityMergeException {
		return hllLarge.cardinality();
	}

	@Threads(4)
	@GenerateMicroBenchmark()
	public long scalability4() throws CardinalityMergeException {
		return hllLarge.cardinality();
	}

	@Threads(0)
	@GenerateMicroBenchmark()
	public long scalabilityMax() throws CardinalityMergeException {
		return hllLarge.cardinality();
	}
}
