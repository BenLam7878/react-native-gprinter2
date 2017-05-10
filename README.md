#react-native-gprinter2
#根据 Mr.阿拉丁 所提供的文档(http://www.cnblogs.com/MrDing/p/4337317.html) 改写的react-native插件。

安装：

1. 下载package到项目

npm install BenLam7878/react-native-gprinter2 --save


可以使用react-native-link 命令执行以下步骤。

2. 修改settings.gradle

include ':react-native-gprinter2'
project(':react-native-gprinter2').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-gprinter2/android')

3.修改app/build.gradle,在dependenceie里面加入：

compile project(':react-native-gprinter2')

4.修改app/src/main/java/com/MainApplication.java

导入:import com.gprinter.GPrinterPackage;

protected List<ReactPackage> getPackages() {
  return Arrays.<ReactPackage>asList(
      new MainReactPackage(),
      new GPrinterPackage()
  );
}