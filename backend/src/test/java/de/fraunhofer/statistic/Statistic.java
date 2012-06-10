package de.fraunhofer.statistic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class Statistic {

	static int fileCount = 0;
	static int lineCount = 0;
	
	static String[] ignoreFiles = {
		".svn",
		".settings",
		"misc",
		"src_test",
		".classpath",
		".project",
		".springBeans",
		"certificates",
		".wkt",
		"apidoc",
		"calendar",
		"jquery",
		"openLayers",
		".jar",
		"target",
		"handbuch.html",
		"dokumentation.html"
	};
	
	static String[] resourceFiles = {
		".xls",
		".png",
		".jpg",
		".xcf",
		".psd",
		".svg",
		".ico",
	};

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			calculate(new File("."));
			System.out.println("fileCount: "+ fileCount);
			System.out.println("lineCount: "+ lineCount);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void calculate(File file) throws IOException {
		if (file.isDirectory()) {
			//Directory
			for (File child : file.listFiles()) {
				boolean acceptFile = true;
				for (String ignoreFile : ignoreFiles)
					if (child.getName().endsWith(ignoreFile)) {
						acceptFile = false;
						break;
					}
				if (acceptFile) {
					calculate(child);
				} else {
					System.out.println(StringUtils.rightPad("IGNORE",30)+StringUtils.substringAfter(child.getAbsolutePath(), "\\.\\"));
				}
			}
		
		} else {
			//File
			boolean sourceFile = true;
			for (String resourceFile : resourceFiles)
				if (file.getName().endsWith(resourceFile)) {
					sourceFile = false;
					break;
				}
			if (sourceFile) {
				//SourceFile
				fileCount++;
				
				int fileLines = 0;
				BufferedReader br = new BufferedReader(new FileReader(file));
				while (br.ready()) {
					if (StringUtils.isNotBlank(br.readLine()))
						fileLines++;
				}
				br.close();
				System.out.println(StringUtils.rightPad("SOURCEFILE LINESCOUNT "+StringUtils.leftPad(fileLines+"", 5), 30)+StringUtils.substringAfter(file.getAbsolutePath(), "\\.\\"));
				lineCount+= fileLines;
			} else {
				System.out.println(StringUtils.rightPad("RESOURCEFILE",30)+StringUtils.substringAfter(file.getAbsolutePath(), "\\.\\"));
				//ResourceFile
				fileCount++;
			}

		}
	}

}
