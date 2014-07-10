package com.solace.cf4j.config;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Loads.... the {@link Caches} configuration
 * 
 * @author williamd1618
 *
 */
public class ConfigurationLoader {

	public static final String CONFIG_FILE = "caches.json";

	private static final ObjectMapper MAPPER = new ObjectMapper();

	public Caches load(String fileName) {
		InputStream in = this.getClass().getClassLoader()
				.getResourceAsStream(fileName);

		final char[] buffer = new char[1024];
		final StringBuilder out = new StringBuilder();

		Caches c;
		try {
			final Reader r = new InputStreamReader(in, "UTF-8");
			try {
				for (;;) {
					int rsz = r.read(buffer, 0, buffer.length);
					if (rsz < 0)
						break;
					out.append(buffer, 0, rsz);
				}
			} finally {
				in.close();
			}

			c = MAPPER.reader(Caches.class).readValue(out.toString());
		} catch (Exception ex) {
			throw new RuntimeException("Could not load config file", ex);
		}

		return c;

	}

	public Caches load() {
		return load(CONFIG_FILE);
	}
}
