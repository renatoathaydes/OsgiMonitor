package com.athaydes.osgimonitor.impl.manage;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * User: Renato
 */
public class FilesHelper {

	private static final int MAX_DEPTH_TO_VISIT = 6;

	public static List<Path> findAllFilesWithExtension(
			final String extension, final Path start )
			throws IOException {
		final String dotExt = "." + extension;
		final List<Path> result = new ArrayList<>();

		Files.walkFileTree(
				start,
				EnumSet.noneOf( FileVisitOption.class ),
				MAX_DEPTH_TO_VISIT,
				new SimpleFileVisitor<Path>() {
					@Override
					public FileVisitResult visitFile( Path file, BasicFileAttributes attrs ) throws IOException {
						if ( file.toString().endsWith( dotExt ) ) {
							result.add( file );
						}
						return FileVisitResult.CONTINUE;
					}
				} );

		return result;
	}

}
