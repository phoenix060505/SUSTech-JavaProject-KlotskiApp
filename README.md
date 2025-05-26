# Klotski (华容道) 游戏

## 简介

本项目是一个基于 JavaFX 实现的经典华容道益智游戏。玩家需要通过移动不同形状的滑块，帮助主要滑块（曹操）从初始位置移动到棋盘底部的出口。游戏包含用户注册登录、多个预设关卡、游戏计时、步数统计、保存和加载游戏进度、自动演示解谜步骤以及提示功能。

## 项目结构

SUSTech-JavaProject-KlotskiApp/

├── .idea/                     
├── src/                       
│  ├── Main.java             
│  ├── css/                   
│  │  ├── WarmTheme.css      
│  │  └── wave.css         
│  ├── game/                  
│  │  ├── AboutGame.java     
│  │  ├── AutoSolve.java      
│  │  ├── Direction.java     
│  │  ├── GameLogic.java     
│  │  ├── LevelManager.java  
│  │  ├── SoundManager.java  
│  │  └── Solver.java        
│  ├── model/                 
│  │  ├── Block.java         
│  │  ├── Board.java         
│  │  ├── GameState.java     
│  │  └── Level.java         
│  ├── resources/             
│  │  ├── fonts/            
│  │  ├── images/            
│  │  └── sounds/     
│  ├── ui/                   
│  │  ├── KlotskiApp.java    
│  │  └── controls/          
│  │      ├── WavePasswordConfirm.java 
│  │      ├── WavePasswordField.java 
│  │      └── WaveTextField.java     
│  ├── user/                  
│  │  ├── User.java          
│  │  └── UserManager.java   
│  └── util/                  
│      └── GameFileManager.java 
└── KlotskiPuzzle.iml            

## 主要功能

* **用户系统**:
    * 支持用户注册和登录。
    * 支持游客模式游玩。
    * 用户数据通过 `users.dat` 文件存储，并包含校验和机制以检测文件损坏。
* **游戏核心**:
    * 经典的华容道棋盘布局 (4x5 网格)。
    * 包含多种不同大小和类型的滑块 (曹操, 关羽, 将军, 士兵)。
    * 通过鼠标点击选择滑块，通过方向键或界面按钮移动滑块。
    * 记录游戏步数和所用时间。
* **关卡管理**:
    * 预设多个不同难度的关卡 (例如："Classic", "Advanced", "Expert")。
    * 玩家可以从主菜单选择不同关卡开始新游戏。
* **存档与读档**:
    * 注册用户可以保存当前游戏进度。
    * 可以加载之前保存的游戏状态继续游戏。
    * 游戏状态保存为 `<username>.save` 文件。
    * 支持自动保存功能，默认为每5秒保存一次。
* **游戏辅助**:
    * **提示 (Hint)**: 为当前棋局提供一步最佳移动建议。
    * **自动求解 (Auto-Solve)**: 自动演示从当前状态到游戏胜利的完整步骤。
    * **撤销 (Undo)**: 回退到上一步操作。
    * **重玩 (Restart)**: 重新开始当前关卡。
* **用户界面**:
    * 使用 JavaFX 构建图形用户界面。
    * 包含登录/注册界面、主菜单界面、游戏界面和胜利界面。
    * 自定义的文本输入框和密码框，具有波浪动画效果。
    * “关于游戏”介绍页面，提供游戏背景和玩法说明，文字采用打字机动画效果。
    * 界面元素应用了自定义的暖色调 CSS 主题 (`WarmTheme.css`)。
* **音效**:
    * 包含背景音乐 (登录界面BGM、游戏界面BGM)。
    * 包含滑块移动、游戏胜利等操作的音效。

## 如何运行

1.  确保已安装 Java 开发环境 (JDK，推荐版本 17 或更高，支持 JavaFX)。
2.  使用 IntelliJ IDEA 或其他支持 JavaFX 的 IDE 打开项目。
3.  配置好项目的 JDK 和 JavaFX 模块。
4.  运行 `src/Main.java` 文件中的 `main` 方法。

## 主要技术栈

* **Java**:核心编程语言。
* **JavaFX**:用于构建图形用户界面。
* **CSS**:用于美化界面元素。

## 文件管理 (`GameFileManager.java`)

* **用户数据**:
    * `users.dat`: 存储所有用户账户信息 (用户名和密码)。
    * 采用序列化方式存储 `Map<String, User>` 对象。
    * 增加了 `UserDataWrapper` 类，在保存用户数据时会计算并存储 CRC32 校验和。
    * 加载时会重新计算并对比校验和，若不匹配或文件损坏，则加载空用户数据，保证程序稳定运行。
* **游戏存档**:
    * `saves/` 目录: 存储每个用户的游戏存档。
    * 存档文件名为 `<username>.save`。
    * 采用序列化方式存储 `GameState` 对象，包含棋盘布局、当前关卡、已用时间、移动历史和游戏是否胜利的状态。
* **自动保存**:
    * 为登录用户提供自动保存功能，默认间隔为5秒。
    * 通过 `ScheduledExecutorService` 实现定时任务。
    * `startAutoSave()` 和 `stopAutoSave()` 方法控制自动保存的启动与停止。
    * `shutdown()` 方法用于在程序退出时优雅关闭定时器服务。

## 自定义 UI 控件 (`ui.controls.*`)

为了提升用户体验，项目包含以下自定义的 JavaFX 控件，它们都带有标签浮动和下划线动画效果，灵感来源于 Vue Wave Group：

* `WaveTextField.java`: 自定义文本输入框。
* `WavePasswordField.java`: 自定义密码输入框。
* `WavePasswordConfirm.java`: 自定义密码确认输入框 (通常用于注册时确认密码)。

这些控件的样式通过 `wave.css` 文件定义。

## 未来可扩展方向

* 增加更多预设关卡或引入关卡编辑器。
* 排行榜功能。
* 更复杂的用户统计数据。
* 更完善的错误处理和日志记录。
* 皮肤或主题切换功能。

---

希望这份 README 对您有所帮助！
