# git 个人使用版本说明
 ## ubuntu系统下
1. 安装git

   sudo apt update

   sudo apt install git
2. 配置git用户信息

   git config --global user.name "你的用户名"

   git config --global user.email "你的邮箱"
3. 生成 SSH 密钥 

    ssh-keygen -t rsa -b 4096 -C "你的邮箱"

    (此时按照提示一路回车，密钥文件会存储在 ~/.ssh/id_rsa 路径)
4. 将 SSH 密钥添加到 GitHub

    cat ~/.ssh/id_rsa.pub

    复制好公钥（输出的所有内容都进行复制），打开github->右上角setting->SSH and GPG keys->new SSH key->title起一个便于识别的名字，key中粘贴复制好的公钥
5. 创建github仓库（直接在github主页面进行创建）
6. 编译软件关联github仓库

    在编译软件中打开终端， **git init** 初始化git仓库， **git remote add origin ~~git@github.com:username/repo.git~~** 将远程仓库添加为远程源
7. 克隆仓库

    **git clone ~~git@github.com:username/repo.git~~ <项目名称>**

根据文件的用途和特性，选择需要上传到git的文件：

| 文件类型 | 是否应提交 | 原因 |
|----------|------------|------|
| 源代码文件（`.cpp`, `.h`） | ✅ 是 | 项目的核心内容 |
| 配置文件（`CMakeLists.txt`, `.clang-format`） | ✅ 是 | 项目构建或格式规范必需 |
| 构建目录（`build/`） | ❌ 否 | 是自动生成的，应加入 `.gitignore` |
| CLion 工程文件（`.idea/`） | ⚠️ 通常不 | 属于用户 IDE 的个人设置，不通用 |
| 编译缓存（`.o`, `.so`, `cmake-build-debug/`） | ❌ 否 | 是编译中间产物，不应该提交 |
| 临时/调试文件（`temp.txt`, `log.txt`） | ⚠️ 一般不 | 通常是一次性数据 |
| `README.md`, `LICENSE`, `package.xml` | ✅ 是 | 项目的文档和元数据，应该跟随版本管理 |

## windows系统下
同ubuntu类似，更加可视化，更加便捷。