**react-native-gprinter2**

根据 Mr.阿拉丁 所提供的 佳博打印机 文档(http://www.cnblogs.com/MrDing/p/4337317.html) 改写的react-native插件。

**Installation：**

*Install with NPM
```shell
npm install BenLam7878/react-native-gprinter2 --save
```
or you need to install with the clone URL
```shell
npm install https://github.com/BenLam7878/react-native-gprinter2.git --save
```

*Links the react-native
```shell
react-native link react-native-gprinter2
```
or you may need to install manually 

1 modify settings.gradle

```shell
include ':react-native-gprinter2'
project(':react-native-gprinter2').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-gprinter2/android')
```

2 modify app/build.gradle, adds following code into the dependenceie：
```shell
compile project(':react-native-gprinter2')
```

3 modify app/src/main/java/com/MainApplication.java

```java
import com.gprinter.GPrinterPackage;
...
protected List<ReactPackage> getPackages() {
  return Arrays.<ReactPackage>asList(
      new MainReactPackage(),
      new GPrinterPackage()
  );
}
```

**Demos**
you can checks the demos in the samples folder of source code.

**Related documents**
you can find the documents in the docs folder of the source code.

**Caution:**
 printReceipt and Bitmap related feature still under development.