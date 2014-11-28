package com.favccxx.favsoft.deltarelease;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PublishService {

	private static Logger logger = LogManager.getLogger(PublishService.class
			.getName());

	public static void getDeltaReleaseFiles() {

		String patchFolder = SystemConfig.getInstance().getString(
				"apps[@outpatchFolder]");
		try {
			File file = new File(patchFolder);
			// if(file.exists()){
			// 清理输出文件目录
			// FileUtils.cleanDirectory(file); //太危险了,不小心设置错,把源码删了.......
			// }
			FileUtils.forceMkdir(file);
		} catch (IOException e) {
			logger.error("Error get File, The wrong file path is:"
					+ patchFolder);
			logger.error("Error Message is:" + e.getMessage());
		}

		// 取出最新的class列表
		List<HierarchicalConfiguration> appList = SystemConfig.getInstance()
				.configurationsAt("apps.app");
		for (Iterator<HierarchicalConfiguration> iterator = appList.iterator(); iterator
				.hasNext();) {
			HierarchicalConfiguration hierarchicalConfiguration = iterator
					.next();
			String appName = hierarchicalConfiguration.getString("[@appName]");
			String patchFilePath = hierarchicalConfiguration
					.getString("[@patchFile]");
			String patch_source_dir = hierarchicalConfiguration
					.getString("patch_source_dir");
			String patch_class_dir = hierarchicalConfiguration
					.getString("patch_class_dir");
			String web_dir = hierarchicalConfiguration.getString("web_dir");
			String patch_dest_dir = hierarchicalConfiguration
					.getString("patch_dest_dir");
			String destDir = patchFolder + patch_dest_dir;
			// 执行拷贝补丁文件
			copyPatchFile(appName, patch_source_dir, patchFilePath, destDir,
					web_dir, patch_class_dir);
		}

	}

	private static void copyPatchFile(String appName, String sourceDir,
			String patchFilePath, String destDir, String webDir,
			String patchClassDir) {
		logger.info("******************************开始拷贝" + appName
				+ "工程的补丁文件**************************");
		try {
			// 创建生成补丁文件的目录
			FileUtils.forceMkdir(new File(destDir));
		} catch (IOException e) {
			logger.error("Error get destDir,The wrong path is:" + destDir);
			logger.error("Error Message is:" + e.getMessage());
		}

		File patchFile = new File(
				Thread.currentThread()
						.getContextClassLoader()
						.getResource(
								PublishConstants.SYSTEM_CONFIG_DIR
										+ patchFilePath).getFile()); // 读取补丁文件--deltaReleaseFile.txt
		try {
			List fileLines = FileUtils.readLines(patchFile);
			int count = 0;
			for (Iterator iterator = fileLines.iterator(); iterator.hasNext();) {
				String fileName = (String) iterator.next();
				if (StringUtils.isNotEmpty(fileName)) {
					if (fileName.startsWith(webDir)) {
						// JSP、CSS、JS等格式的文件直接copy到工程的目录
						String sourceFilePath = sourceDir + "\\" + fileName;
						String destFilePath = destDir
								+ fileName.substring(webDir.length());
						FileUtils.copyFile(new File(sourceFilePath), new File(
								destFilePath));
						logger.info("-------" + ++count + "成功拷贝补丁文件到："
								+ destFilePath);
					} else {
						String classFilePath = null;
						
						//截去src和其同级的目录
						fileName = cutJavaPackagePath(fileName);

						List classFileNameList = null; // 针对内部类匿名类

						if (fileName.endsWith("java")) {
							// java补丁文件,需找到对应的class
							fileName = fileName.replaceAll(".java", ".class");

							//获取内部类或匿名类
							String tmpClassFilePath = patchClassDir + fileName;
							File tmpCompileFile = new File(tmpClassFilePath);
							if (tmpCompileFile.exists()) {
								classFileNameList = new ArrayList();
								String tmpClassFileFolderPath = tmpCompileFile.getParent();
								File tmpCompileFileFolder = new File(tmpClassFileFolderPath);
								File[] tmpFiles =tmpCompileFileFolder.listFiles();
								classFileNameList.add(fileName);
								for(int i=0;i<tmpFiles.length;i++)
								{
									if(tmpFiles[i].getName().startsWith(tmpCompileFile.getName().substring(0, tmpCompileFile.getName().lastIndexOf("."))+"$"))
									{
										classFileNameList.add(fileName.replace(tmpCompileFile.getName(), tmpFiles[i].getName()));
									}
								}
							}
							else {
								logger.error("未找到该补丁文件，错误的路径：" + tmpClassFilePath);
								continue;
							}
							
						}

						if (classFileNameList != null
								&& classFileNameList.size() > 0) {
							for(int j=0;j<classFileNameList.size();j++)
							{
								fileName = String.valueOf(classFileNameList.get(j));
								classFilePath = patchClassDir + fileName;

								File compileFile = new File(classFilePath);
								if (compileFile.exists()) {
									String destFilePath = destDir
											+ "\\WEB-INF\\classes" + fileName;
									FileUtils.copyFile(compileFile, new File(
											destFilePath));
									logger.info("-------" + ++count + "成功拷贝补丁文件到："
											+ destFilePath);
								} else {
									logger.error("未找到该补丁文件，错误的路径：" + classFilePath);
								}
							}							
						} else {
							classFilePath = patchClassDir + fileName;

							File compileFile = new File(classFilePath);
							if (compileFile.exists()) {
								String destFilePath = destDir
										+ "\\WEB-INF\\classes" + fileName;
								FileUtils.copyFile(compileFile, new File(
										destFilePath));
								logger.info("-------" + ++count + "成功拷贝补丁文件到："
										+ destFilePath);
							} else {
								logger.error("未找到该补丁文件，错误的路径：" + classFilePath);
							}
						}
					}
				}
			}
			logger.info("******************************" + appName
					+ "工程补丁文件获取成功，共拷贝" + count
					+ "个补丁文件**************************");
		} catch (IOException e) {
			logger.error("Error in execute copyPatchFile. The wrong message is:"
					+ e.getMessage());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	/**
	 * 截去src和其同级的目录
	 * @param fileName 包含src级目录的路径
	 * <br/>例:
	 * <br/>src\com\AppConstant.java 
	 * <br/>war\index.jsp
	 * @return 截去后的路径 
	 * <br/>例:
	 * <br/>\com\AppConstant.java 
	 * <br/>\index.jsp
	 * @throws Exception
	 */
	private static String cutJavaPackagePath(String fileName) throws Exception
	{
		if (fileName.indexOf("/") < 0
				&& fileName.indexOf("\\") >= 0) {
			fileName = fileName.substring(fileName
					.indexOf("\\"));
		} else if (fileName.indexOf("\\") < 0
				&& fileName.indexOf("/") >= 0) {
			fileName = fileName
					.substring(fileName.indexOf("/"));
		} else if (fileName.indexOf("/") >= 0
				&& fileName.indexOf("\\") >= 0) {
			if (fileName.indexOf("/") < fileName.indexOf("\\")) {
				fileName = fileName.substring(fileName
						.indexOf("/"));
			} else {
				fileName = fileName.substring(fileName
						.indexOf("\\"));
			}
		} else {
			logger.error("文件路径不正确：" + fileName);
			throw new Exception();
		}
		return fileName;
	}
	

}
