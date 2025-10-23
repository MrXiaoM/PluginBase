# Libraries Resolver Lite

这个项目主要是 Libraries Resolver 的简化版，只支持以下功能
+ 如果依赖在本地仓库存在，就不会去访问远程仓库
+ 将依赖下载到指定的本地仓库中
+ 通过 `.sha1` 文件，校验依赖是否完整

不会去支持以下功能
+ 通过下载 pom 抓取所有依赖的信息
+ 支持 SNAPSHOT 快照版本

## 使用

这个模块的原理是借助 Gradle 的 maven resolver 提前将需要下载的依赖处理好，就不需要在运行时获取依赖关系了。

即在编译时提前处理好自身依赖，在运行时直接下载即可，无需繁琐地获取依赖关系。

详细用法请阅读 [MCIO Plugins](https://plugins.mcio.dev/elopers/base/resolver-lite) 上的文档。
