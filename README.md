---
title: Android 动态调用外部jar/dex
date: 2018-07-25 20:42:54
tags: [Android,jar,dex]
categories: Android
comments: true
toc: true
---

# Android 动态调用外部jar/dex

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/00f8230c8beb4f5cb7f7e600a8084760)](https://www.codacy.com/app/Sogrey/LoadJar?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=Sogrey/LoadJar&amp;utm_campaign=Badge_Grade)

## 需求分析

现有需求，需要做一个生成外部jar，去验证已发布App有效性，这个外部jar可更新，而App不用重新发布之需要重新发布这个jar包即可。此次记录这种需求开发，jar包中以MD5加密为例。

## 创建项目

跟往常一样，创建android studio 项目，其中包含两个app Module和两个library Module，目前都是空项目。如下图：

![](https://github.com/Sogrey/LoadJar/blob/master/screenShot/TIM-20180813151634.jpg?raw=true)

其中：

- `app`:发布App，需要验证的App项目。
- `app2`:用于直接依赖测试打包jar项目。
- `lib_interface`:这个项目里只有一个接口`interface`，提供了一个或多个可供调用的方法,所有用到验证jar包的项目包括发布jar项目本身都要依赖于它，比如此项目中另外三个项目都要依赖于此library Module。
- `lib_md5`:用于发包jar包的项目。

## 创建接口类

首先处理`lib_interface`,在`lib_interface`中新建一个`interface`接口类[Md5JarInterface.java](https://github.com/Sogrey/LoadJar/blob/master/lib_interface/src/main/java/org/sogrey/jarinterface/Md5JarInterface.java)

![](https://github.com/Sogrey/LoadJar/blob/master/screenShot/TIM-20180813152011.jpg?raw=true)

里面只有一个方法:
``` java
    /**
     * 获取Md5值
     * @param content 原字符串
     * @return
     */
    String getMd5(String content);
```

![](https://github.com/Sogrey/LoadJar/blob/master/screenShot/TIM-20180813152128.jpg?raw=true)


## 实现接口类方法

要实现上面接口类方法，转到`lib_md5` module,首先需要先依赖`lib_interface`:

![](https://github.com/Sogrey/LoadJar/blob/master/screenShot/TIM-20180813152251.jpg?raw=true)

在`lib_md5`的[build.gradle](https://github.com/Sogrey/LoadJar/blob/master/lib_md5/build.gradle)多了句：
```
    implementation project(':lib_interface')
```

![](https://github.com/Sogrey/LoadJar/blob/master/screenShot/TIM-20180813152320.jpg?raw=true)


依赖好之后，在`lib_md5`新建一个[Md5Utils.java](https://github.com/Sogrey/LoadJar/blob/master/lib_md5/src/main/java/org/sogrey/md5/impl/Md5Utils.java)实现[Md5JarInterface](https://github.com/Sogrey/LoadJar/blob/master/lib_interface/src/main/java/org/sogrey/jarinterface/Md5JarInterface.java)接口:

![](https://github.com/Sogrey/LoadJar/blob/master/screenShot/TIM-20180813152416.jpg?raw=true)

![](https://github.com/Sogrey/LoadJar/blob/master/screenShot/TIM-20180813152504.jpg?raw=true)


下面引进[MD5.java](https://github.com/Sogrey/LoadJar/blob/master/lib_md5/src/main/java/org/sogrey/md5/MD5.java)(md5算法网上多得是)，并实现`getMd5()`方法：
``` java
    @Override
    public String getMd5(String content) {
        return MD5.MD5(content);
    }
```
此时一个简单的库项目功能基本完成，测试通过后就能发包jar包了。

## 依赖测试

完成了库项目功能开发，先直接依赖测试下结果。让`app2` module依赖于`lib_interface`和`lib_md5`：

![](https://github.com/Sogrey/LoadJar/blob/master/screenShot/TIM-20180813152617.jpg?raw=true)

简单修改[activity_main.xml](https://github.com/Sogrey/LoadJar/blob/master/app2/src/main/res/layout/activity_main.xml):

![](https://github.com/Sogrey/LoadJar/blob/master/screenShot/TIM-20180813152751.jpg?raw=true)

在[MainActivity.java](https://github.com/Sogrey/LoadJar/blob/master/app2/src/main/java/org/sogrey/app2/MainActivity.java)添加下面代码：
``` java
        TextView txtResult = findViewById(R.id.txt_result);
        txtResult.setText(new Md5Utils().getMd5("123456"));
```
![](https://github.com/Sogrey/LoadJar/blob/master/screenShot/TIM-20180813153202.jpg?raw=true)

编译运行测试：

![](https://github.com/Sogrey/LoadJar/blob/master/screenShot/TIM-20180813153401.jpg?raw=true)

测试正常。

## 混淆打包jar

经过测试`lib_md5`项目功能正常，下面准备混淆打包。

混淆模板参考[这里](https://sogrey.github.io/notes/%E6%B7%B7%E6%B7%86%E6%A8%A1%E6%9D%BF)。

但须注意，对外调用的接口方法是不能被混淆，否则后找不到,修改[proguard-rules.pro](https://github.com/Sogrey/LoadJar/blob/master/lib_md5/proguard-rules.pro)添加如下：
```
-keep public class org.sogrey.md5.impl.Md5Utils
-keepclasseswithmembers public class org.sogrey.md5.impl.Md5Utils{
   public String getMd5();
}
-keep class org.sogrey.md5.impl.Md5Utils{
   public <methods>;
}
```
![](https://github.com/Sogrey/LoadJar/blob/master/screenShot/TIM-20180813153930.jpg?raw=true)

编辑`lib_md5`的[build.gradle](https://github.com/Sogrey/LoadJar/blob/master/lib_md5/build.gradle),修改buildTypes.release.minifyEnabled 为 true.

![](https://github.com/Sogrey/LoadJar/blob/master/screenShot/TIM-20180813155019.jpg?raw=true)


添加task：
```
def SDK_BASENAME = "MD5" //名称
def SDK_VERSION = "_V1.0" //版本
def sdkDestinationPath = "build" //生成保存位置
def zipFile = file('build/intermediates/bundles/release/classes.jar') //打包源文件

task deleteBuild(type: Delete) {
    delete sdkDestinationPath + SDK_BASENAME + SDK_VERSION + ".jar"
}
task makeJar(type: Jar) {
    from zipTree(zipFile)
    from fileTree(dir: 'src/main',includes: ['assets/**'])//将assets目录打入jar包
    baseName = SDK_BASENAME + SDK_VERSION
    destinationDir = file(sdkDestinationPath)
}

makeJar.dependsOn(deleteBuild, build)
```

![](https://github.com/Sogrey/LoadJar/blob/master/screenShot/TIM-20180813154519.jpg?raw=true)

`makeJar` task作用是打包生成jar，但是生成的jar是没有混淆的，再添加task:
```
task proguardJar(dependsOn: ['makeJar'], type: proguard.gradle.ProGuardTask) {
    //Android 默认的 proguard 文件
    configuration android.getDefaultProguardFile('proguard-android.txt')
    //manifest 注册的组件对应的 proguard 文件
    configuration 'proguard-rules.pro'
    String inJar = makeJar.archivePath.getAbsolutePath()
    //输入 jar
    injars inJar
    //输出 jar
    outjars inJar.substring(0, inJar.lastIndexOf(File.separator)) + "/proguard-${makeJar.archiveName}"
    //设置不删除未引用的资源(类，方法等)
    dontshrink
    Plugin plugin = getPlugins().hasPlugin("AppPlugin") ?
            getPlugins().findPlugin("AppPlugin") :
            getPlugins().findPlugin("LibraryPlugin")
    if (plugin != null) {
        List<String> runtimeJarList
        if (plugin.getMetaClass().getMetaMethod("getRuntimeJarList")) {
            runtimeJarList = plugin.getRuntimeJarList()
        } else if (android.getMetaClass().getMetaMethod("getBootClasspath")) {
            runtimeJarList = android.getBootClasspath()
        } else {
            runtimeJarList = plugin.getBootClasspath()
        }
        for (String runtimeJar : runtimeJarList) {
            //给 proguard 添加 runtime
            libraryjars(runtimeJar)
        }
    }
}
```
![](https://github.com/Sogrey/LoadJar/blob/master/screenShot/TIM-20180813155807.jpg?raw=true)

`proguardJar` task 用于混淆打包。可以看到`proguardJar`里调用了`makeJar`
![](https://github.com/Sogrey/LoadJar/blob/master/screenShot/TIM-20180813155854.jpg?raw=true)

执行task,点击android studio 右上角`Gradle`展开找到`:lib_md5`,在`task`>`other`里找到我们刚定义的task：`makeJar`和`proguardJar`，直接双击执行，我们需要混淆的直接双击`proguardJar` task,等待编译完成，会在`build`里生成了两个jar包：`MD5_V1.0.jar`和`proguard-MD5_V1.0.jar`，从文件名就能看出`proguard-MD5_V1.0.jar`是混淆过的。
![](https://github.com/Sogrey/LoadJar/blob/master/screenShot/TIM-20180813155922.jpg?raw=true)

直接zip解压可直接看到包结构可class文件:
![](https://github.com/Sogrey/LoadJar/blob/master/screenShot/TIM-20180813155642.jpg?raw=true)

## jar包dx处理
jar包生成好之后，下面就要进行dx处理，把生成的jar拷贝到Android SDK目录下`build-tools\28.0.1`，后面的版本根据你自己的版本：
![](https://github.com/Sogrey/LoadJar/blob/master/screenShot/TIM-20180813160050.jpg?raw=true)

执行下面命令：
> dx --dex --output=proguard-MD5-dex_V1.0.jar proguard-MD5_V1.0.jar

![](https://github.com/Sogrey/LoadJar/blob/master/screenShot/TIM-20180813160501.jpg?raw=true)

将会生成目标jar包：`proguard-MD5-dex_V1.0.jar`
![](https://github.com/Sogrey/LoadJar/blob/master/screenShot/TIM-20180813160542.jpg?raw=true)

同样我们zip解压后看到的是一个dex文件。
![](https://github.com/Sogrey/LoadJar/blob/master/screenShot/TIM-20180813160636.jpg?raw=true)


## 引入外部jar测试

jar包dx处理完毕后就可以使用`app` module加载外部jar测试了，当然首先`app`须依赖于`lib_interface`。

为方便安装测试，我们把dx处理好的jar放在assets文件夹下，app安装后拷贝到sd卡再加载。
![](https://github.com/Sogrey/LoadJar/blob/master/screenShot/TIM-20180813160836.jpg?raw=true)

[MainActivity.java](https://github.com/Sogrey/LoadJar/blob/master/app/src/main/java/org/sogrey/loadjar/MainActivity.java)中代码实现：
``` java
        File cacheFile = FileUtils.getCacheDir(getApplicationContext());
        File libFile = new File(cacheFile, "lib");
        if (!libFile.exists()) libFile.mkdirs();
        String internalPath = cacheFile.getAbsolutePath() + File.separator + "lib" + File.separator + jarName;
        File desFile = new File(internalPath);
        try {
            if (!desFile.exists()) {
                desFile.createNewFile();
                FileUtils.copyFiles(this, jarName, desFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //以上是将jar拷贝到sd卡，是为测试方便，实际应用中应该是下载保存到sd卡.
        //下面开始加载dex class
        DexClassLoader dexClassLoader = new DexClassLoader(internalPath, libFile.getAbsolutePath(), null, getClassLoader());
        try {
            //加载的类名为jar文件里面完整类名，写错会找不到此类hh
            Class libClazz = dexClassLoader.loadClass(className);
            final Md5JarInterface md5JarInterface = (Md5JarInterface) libClazz.newInstance();
            if (md5JarInterface != null) {
                txtResult.post(new Runnable() {
                    @Override
                    public void run() {
                        txtResult.setText(md5JarInterface.getMd5("123456"));
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
```
以上代码通过`DexClassLoader`类加载器找到对应的类，该类实现了`Md5JarInterface`接口方法，调用该方法得到结果。

最后因为有SD卡文件读写，别忘了添加权限：
```xml
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
```
android 6.0动态权限申请请自行百度。

测试之：
![](https://github.com/Sogrey/LoadJar/blob/master/screenShot/TIM-20180813162747.jpg?raw=true)

项目地址：[github](https://github.com/Sogrey/LoadJar)
