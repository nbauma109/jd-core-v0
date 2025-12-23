package jd.core.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.jd.core.v1.api.loader.Loader;
import org.jd.core.v1.loader.ClassPathLoader;
import org.jd.core.v1.util.ZipLoader;

public class CompositeLoader implements Loader {

	private ClassPathLoader classPathLoader;
	private ZipLoader zipLoader;

	public CompositeLoader(InputStream in) throws IOException {
		classPathLoader = new ClassPathLoader();
		zipLoader = new ZipLoader(in);
	}

	@Override
	public boolean canLoad(String internalName) {
		return zipLoader.canLoad(internalName) || classPathLoader.canLoad(internalName);
	}

	@Override
	public byte[] load(String internalName) throws IOException {
		return Optional.ofNullable(zipLoader.load(internalName)).orElse(classPathLoader.load(internalName));
	}

}
