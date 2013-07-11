package info.unportant.stream.bench;

import java.util.Random;

import com.clearspring.analytics.stream.cardinality.HyperLogLog;
import com.clearspring.analytics.stream.cardinality.HyperLogLogPlus;
import com.clearspring.analytics.stream.cardinality.ICardinality;

public class HllUtils {
	static final Random rand = new Random();
	static final int LOG2M = 14;
	
	private  HllUtils() {
	}
	
	public static HyperLogLog createEmptyHll() {
		return new HyperLogLog(LOG2M);
	}
	
	public static HyperLogLog createHll(int cardinality) {
		HyperLogLog hll = createEmptyHll();
		fill(hll, cardinality);
		return hll;
	}
	
	public static HyperLogLogPlus createEmptyHllp() {
		return new HyperLogLogPlus(LOG2M, LOG2M);
	}

	public static HyperLogLogPlus createHllp(int cardinality) {
		HyperLogLogPlus hllp = createEmptyHllp();
		fill(hllp, cardinality);
		return hllp;
	}

	
	public static void fill(ICardinality icard, int cardinality) {
		for (int j = 0; j < cardinality; j++) {
			icard.offer(rand.nextLong());
		}
	}
	

	public static int byteArrayToInt(final byte[] buf) {
		int ret = (buf[0] << 24);
		ret |= (buf[1] & 0xFF) << 16;
		ret |= (buf[2] & 0xFF) << 8;
		ret |= (buf[3] & 0xFF);
		return ret;
	}

	public static byte [] intToByteArray(final int val) {
		byte [] buf = new byte[4];
		buf[0] = (byte) (val >>> 24);
		buf[1] = (byte) (val >>> 16);
		buf[2] = (byte) (val >>> 8);
		buf[3] = (byte) (val);
		return buf;
		
	}
}
