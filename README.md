DeltaRelease
============

Delta Release Project
Java Web工程增量发布工具

DeltaRelease Project一款Java Web项目增量发布工具，能够快速的帮您取到最新的补丁文件，并发布到对应的工程目录下。

DeltaRelease Project适用于在Eclipse、MyEclipse、NetBeans等Java IDE下开发的工程。

***********************
**修改 : xrdtt
**version : V2.0
**date : 2014.11.28
***********************
上面是原作者的说明，具体说明以下面的为主，
原作者代码地址:
https://github.com/favccxx/DeltaRelease

相对原作的变更:
1。增加对匿名类，内部类的支持
2。增加对编译文件目录的自定义
3。增加对多source根目录的支持
4。增加对web根目录的指定

使用说明:
1。修改/conf/publish_config.xml
	outpatchFolder：输出主文件夹(绝对路径),末尾不要带正反斜杠，例：C:\patch_dest_dir
	appName：项目名
	patchFile：修改列表存放文件
	patch_dest_dir：输出子文件夹(相对路径),末尾不要带正反斜杠，例：\myapp
	patch_source_dir：源码根目录(绝对路径),末尾不要带正反斜杠，例：C:\SVN\myapp
	web_dir：web文件夹名，例：war
	patch_class_dir：编译根目录(绝对路径),末尾不要带正反斜杠，例：C:\SVN\myapp\war\WEB-INF\classes

2。将修改列表写入/conf/releaseFile.txt，注意：修改列表必须是从文件根目录开始的相对路径，第一位不要加正反斜杠。
例：
	war/js/test.js
	war/jsp/test.jsp
	src\com\util\AppConstant.java
	config/log4j.properties
	config/spring/applicationContext-app.xml
	war\WEB-INF\web.xml
	war/index.jsp
修改列表批量获取小技巧：使用TortoiseSVN，右键项目文件夹，选择check for modifications.
就会出来一个列表，然后选择中你需要的列表，右键Copy path to clipboard。这样就能批量获取修改列表。

3。运行Publisher。java
就可以在你配置的outpatchFolder+patch_dest_dir目录下找到输出的增量文件。

4。日志查看：
/logs
