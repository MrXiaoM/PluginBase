# plugin.yml

与正常开发插件的 plugin.yml 格式一致

```yaml
name: 插件名自己改
# 版本号，构建脚本会帮你改，不要动它
version: '${version}'
# 主类路径
main: top.mrxiaom.example.PluginMain
# 支持的 MC 版本，需要同时支持 1.12 或以下和 1.13 或以上就填 1.13
api-version: 1.13
# 依赖前置插件
depend: [ Vault ]
# 软依赖(可选)前置插件
softdepend: [ PlaceholderAPI ]
# 作者列表
authors: [ MrXiaoM ]
# 注册命令
commands:
  # 主命令名，之后在代码中注册需要用到
  example:
    description: 命令描述
    # 命令别名
    aliases: [ ex, e ]

```
