package ar.edu.itba.client;

import ar.edu.itba.combiner.WordcountCombinerFactory;
import ar.edu.itba.mapper.TokenizerMapper;
import ar.edu.itba.reducer.WordcountReducerFactory;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ICompletableFuture;
import com.hazelcast.core.IMap;
import com.hazelcast.mapreduce.Job;
import com.hazelcast.mapreduce.JobTracker;
import com.hazelcast.mapreduce.KeyValueSource;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Map;


public class Cluster {
	
	private static final String[] DATA_RESOURCES_TO_LOAD = {"dracula.txt", "mobydick.txt", "2city10.txt"};
	
	private static final String MAP_NAME = "books";
	
	public static void main(String[] args) {
		
		HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance();
		
		try {
			// read data
			fillMapWithData(hazelcastInstance);
			
			JobTracker tracker = hazelcastInstance.getJobTracker("word-to-books");
			
			IMap<String, String> map = hazelcastInstance.getMap(MAP_NAME);
			
			KeyValueSource<String, String> source = KeyValueSource.fromMap(map);
			
			Job<String, String> job = tracker.newJob(source);
			
			ICompletableFuture<Map<String, Integer>> future = job
					.mapper(new TokenizerMapper())
					// activate Combiner to add combining phase!
					.combiner(new WordcountCombinerFactory())
					.reducer(new WordcountReducerFactory())
					.submit();
			
			System.out.println(ToStringPrettyfier.toString(future.get()));
		} catch (Exception e) {
			e.printStackTrace();
			
		} finally {
			// shutdown cluster
			Hazelcast.shutdownAll();
		}
	}
	
	private static void fillMapWithData(HazelcastInstance hazelcastInstance) throws Exception {
		
		IMap<String, String> map = hazelcastInstance.getMap(MAP_NAME);
		
		for (String file : DATA_RESOURCES_TO_LOAD) {
			InputStream is = Cluster.class.getClassLoader().getResourceAsStream(file);
			LineNumberReader reader = new LineNumberReader(new InputStreamReader(is));
			
			String line;
			StringBuilder sb = new StringBuilder();
			while ((line = reader.readLine()) != null) {
				sb.append(line).append("\n");
			}
			map.put(file, sb.toString());
			
			is.close();
			reader.close();
		}
	}
	
	private static final class ToStringPrettyfier {
		
		private ToStringPrettyfier() {
		}
		
		@SuppressWarnings("checkstyle:cyclomaticcomplexity")
		private static String prettify(String toStringValue) {
			int depth = 0;
			
			char[] chars = toStringValue.toCharArray();
			
			boolean openQuote = false;
			boolean openDoubleQuote = false;
			
			StringBuilder sb = new StringBuilder(chars.length);
			for (char c : chars) {
				if (c == ',' && !openQuote && !openDoubleQuote) {
					sb.append(',').append('\n');
					indent(sb, depth);
				} else if (c == '{' || c == '[') {
					depth++;
					sb.append(c).append('\n');
					indent(sb, depth);
				} else if (c == '}' || c == ']') {
					depth--;
					sb.append('\n');
					indent(sb, depth);
					sb.append(c);
				} else if (c == '\'') {
					if (!openDoubleQuote) {
						openQuote = !openQuote;
					}
					sb.append(c);
				} else if (c == '"') {
					if (!openQuote) {
						openDoubleQuote = !openDoubleQuote;
					}
					sb.append(c);
				} else {
					sb.append(c);
				}
			}
			return sb.toString();
		}
		
		private static void indent(StringBuilder sb, int depth) {
			for (int o = 0; o < depth; o++) {
				sb.append("  ");
			}
		}
		
		static String toString(Object value) {
			return prettify(value.toString());
		}
	}
}
