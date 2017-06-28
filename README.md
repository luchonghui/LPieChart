# LPieChart
饼状图加可滑动筛选

手撸饼状图和滑动筛选联动，简单好用。
## 集成方式

方式一 compile引入

```
dependencies {
    compile 'com.github.luchonghui:LPieChart:1.1'
}

```

项目根目录build.gradle加入

```
allprojects {
   repositories {
      jcenter()
      maven { url 'https://jitpack.io' }
   }
}
```

方式二 maven引入

step 1.
```
<repositories>
       <repository>
       <id>jitpack.io</id>
	<url>https://jitpack.io</url>
       </repository>
 </repositories>
```
step 2.
```
	<dependency>
	    <groupId>com.github.luchonghui</groupId>
	    <artifactId>LPieChart</artifactId>
	    <version>1.1</version>
	</dependency>

## Screenshot

![image](https://github.com/luchonghui/LPieChart/blob/master/screenshot/demo.gif)
