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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.CompilerControl;
import org.openjdk.jmh.annotations.GenerateMicroBenchmark;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import com.clearspring.analytics.stream.cardinality.HyperLogLogPlus;
import com.clearspring.analytics.stream.cardinality.ICardinality;

/** Test how fast we can merge a stream of estimators.
 * 
 * We create a large file full of serialized estimators 
 * then try to merge it as fast as possible. It gives us a 
 * rough estimate of the real speed of the operation. 
 * 
 * It tests both the merge and deserialization speed. It should not
 * be IO bound since the data will be in the operating system VFS cache 
 * but no in CPU cache.
 *
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
public class HllpMergeMacro {
	
	// FIXME: JMH does not yet support inheritance so this code is duplicated from the
	// FIXME: HLL implementation. This should be refactored as soon as JMH evolve
	// FIXME: Meanwhile take care to synchronize the HLL and HLL++ versions
	
	static final int ESTIMATOR_COUNT = 20_000;
	static final int FILE_SIZE = 1_000_000_000;
	static final int MAX_CARD  = 80_000;
	static final Random rand = new Random();

	@State(Scope.Benchmark)
	public static class BenchmarkState {
		File fixedSizeFile;
		
		@Setup
		public void prepare() throws IOException {
			fixedSizeFile = File.createTempFile("stream-bench", ".dat");
			fixedSizeFile.deleteOnExit();
			try (FileOutputStream fos = new FileOutputStream(fixedSizeFile)) {
				int bytes = 0;
				while (bytes < FILE_SIZE) {
					byte [] buf = createHllp(rand.nextInt(MAX_CARD)).getBytes(); // Modified
					int len = buf.length;
					fos.write(intToByteArray(len));
					fos.write(buf);
					bytes += len;
				}
			}
		}
	}
	
	/** Can be used to compare two algorithm or two implementation
	 * 
	 * They must have the same expected precision to be fair.
	 */
	@GenerateMicroBenchmark
	public ICardinality mergeFixedCount(BenchmarkState benchmarkState) throws Exception {
		return readFile(benchmarkState.fixedSizeFile, ESTIMATOR_COUNT);
	}
	
	/** Test how long does it takes to merge 1GB of estimator
	 * 
	 * The raw value can be used to estimate if the merge is IO bound or not.
	 * Fastest devices can sustain 300-500MB/s, it make no sense optimizing 
	 * further if the benchmark complete in less than 2-3 seconds. 
	 */
	@GenerateMicroBenchmark
	public ICardinality merge1GB(BenchmarkState benchmarkState) throws Exception {
		return readFile(benchmarkState.fixedSizeFile, Integer.MAX_VALUE);
	}
	
	@CompilerControl(CompilerControl.Mode.INLINE)
	private ICardinality readFile(File file, int maxIterCount) throws Exception {
		ICardinality agg = createEmptyHllp(); // Modified
		
		try (FileInputStream fis = new FileInputStream(file)) {
			byte [] size = new byte[4];
			int iterCount = 0;
			while (fis.read(size) != -1 && iterCount++ < maxIterCount) {
				int len = byteArrayToInt(size);
				byte [] buf = new byte[len];
				fis.read(buf, 0, len);
				agg = agg.merge(HyperLogLogPlus.Builder.build(buf)); // Modified
			}
		}
		
		return agg;
	}
}
