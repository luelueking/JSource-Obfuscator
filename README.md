# JSource-Obfuscator

> Why I write this?
>
> - 学习混淆加密的相关姿势
> - 市面上大多都是混淆class文件，配置繁琐，并且极易跑不起来
> - 混淆java文件，即可以重修打包，打包能过，项目十有八九也可以运行，并且混淆java文件，自由度更高，可以适配修改甚至二次混淆等等
> - java文件层的混淆，即使反编译后也难以阅读，确实可以极大增加审计难度
> - 以后出题ex选手🐶
> - 做自用打算

### TODO-List

- [x] 类名、方法名、字段命名混淆
- [x] 字符串混淆
- [ ] 包名混淆
- [ ] 各种项目测试，兼容
- [ ] ...

### Quick Start

--path 指定java文件目录，会直接替换掉原来的java文件，请**注意备份⚠️！！！**

```
java -jar JSource-Obfuscator-1.0-SNAPSHOT.jar --path /Users/1ue/Downloads/obf-test/src/main/java
```

使用JSource-Obfuscator加密如下示例代码

![](images/001.png)

```
% java -jar JSource-Obfuscator-1.0-SNAPSHOT.jar --path /Users/zhchen/Downloads/obf-test/src/main/java
 ╦╔═╗┌─┐┬ ┬┬─┐┌─┐┌─┐  ╔═╗┌┐ ┌─┐
 ║╚═╗│ ││ │├┬┘│  ├┤───║ ║├┴┐├┤ 
╚╝╚═╝└─┘└─┘┴└─└─┘└─┘  ╚═╝└─┘└  

usage: --path 指定混淆的java目录 --obfMethod=true 开启方法名混淆

begin obfucate strings...
finished!!!
```

混淆后的代码大致如下

![](images/002.png)

并且正常运行

![](images/003.png)


