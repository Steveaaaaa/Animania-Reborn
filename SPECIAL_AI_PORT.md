# 1.12 特殊 AI 对照记录

依据：本地 `.upstream-animania` 的 1.12 分支（32ae2b4c56cb84284e865dae0d3b78770992ba1d）。下表列出原实现与移植版的主要对应关系。

| 原实现 | NeoForge 实现 |
| --- | --- |
| EntityAIFindMud | LegacyFindMudGoal；娱乐计时、白天限制、搜索范围、泥坑容量、角色接近坐标 |
| 猪实体及泥浴渲染 | AnimaniaPig、AnimaniaPigRenderer、PigMudLayer；泥浴减速、侧躺、泥层、雨水清洗、娱乐恢复 |
| EntityAIPigSnuffle | LegacyPigSnuffleGoal；饥饿触发、森林/牵绳/成年条件、松露数量、觅食、粒子 |
| EntityAITemptItemStack | LegacyTemptGoal 的精确物品堆模式；泔水桶及原配置冷却 |
| EntityAIButtHeadsGoats / Sheep | LegacyHeadButtGoal；双方对手、战斗状态、接近和计时，不附加伤害与击退 |
| EntityAIGoatsLeapAtTarget | LegacyGoatLeapGoal；战斗条件、近距离窗口、跳跃概率和原速度公式 |
| EntityAIAttackMeleeBulls | LegacyBullAttackGoal；排除骷髅、唤醒、战斗状态、原攻击范围、寻路间隔、20 tick 攻击间隔 |
| GoatFainting / EntityAnimaniaGoat | 冲刺碰撞惊吓、停用 AI、原跳起窗口与恢复计时；停用现代山羊 Brain 的额外行为 |
| EntityHorseEatGrass | LegacyGrazeGoal；被骑乘及牵引时禁止吃草 |
| EntityAIWanderHorses / EntityAILookIdleHorses | LegacyGoalRegistration、LegacyIdleLookGoal；白天、睡眠、牵引限制 |
| EntityAIFollowMateHorses | LegacyFollowMateHorseGoal；配偶 UUID、距离条件、60 tick 路径更新、1.1 速度 |
| EntityAIFindNest / EntityAIFindPeacockNest | LegacyFindNestGoal；原网格搜索、巢种类、鸡巢占用检查、接近坐标、脚下产蛋 |
| 两个 EntityAIWatchClosestFromSide | LegacyBirdWatchGoal；视线目标位于目标眼睛上方 5 格 |
| 公鸡争斗 | 原近战与跳跃组合，80 次机会间隔的公鸡目标选择，roostersFight 配置 |
| EntityAICatAttack | LegacyCatAttackGoal；使用 OcelotAttack 的潜行接近及追击速度 |
| GenericAIPlay | LegacyPlayGoal；幼猫/幼犬配对、互换追逐角色、16×7 随机远离路径 |
| AnimalAIGetDogHerded | LegacyGetDogHerdedGoal；已驯服成年德国牧羊犬、坐下/睡眠排除、跟随路径最终节点 |
| EntityAIFerretFindNests / EntityAIHedgehogFindNests | LegacyRaidNestGoal；原 16×3 搜索、最近曼哈顿距离、脚下偷蛋、刺猬作物觅食 |
| EntityAIRodentEat | LegacyRodentGrazeGoal；草/泥土条件、80 tick、幼成年不同概率、移动/视线/跳跃互斥 |
| EntityAILookIdleRodent | LegacyIdleLookGoal；被携带时禁止闲看 |
| GenericAISwimmingSmallCreatures | LegacySmallCreatureFloatGoal；原预测位置的泥中浮起与水中游泳 |
| EntityAmphibian 跳跃控制 | AnimaniaAmphibian 的 MoveControl、JumpControl、落地等待、跳跃高度、动画事件；停用现代 FrogAi |
| Pepe / Killer | 原目标、优先级、生命值、伤害类型与玩家击退；独立 AI 重新注册 |
| GenericAISitIdle | LegacySitIdleGoal；保留代码但不注册，原版注册行处于注释状态 |
| GenericAISearchBlock | LegacySearchBlockGoal；主/次目标、搜索、避开失败位置、卡住重试 |
| GenericAIFindFood / Water / SaltLick | 对应 LegacyFind*Goal；需求、配置、骑乘限制与到达后的消费 |
| GenericAIEatGrass | LegacyGrazeGoal；草地/蘑菇牛菌丝、160 tick、吃草动画与方块消费；猫狗、雪貂、刺猬和兔子另注册不消耗草方块的动作分支 |
| GenericAISleep | LegacySleepGoal；床位、物种作息、天气与唤醒；睡眠期间固定实体朝向 |
| GenericAIMate / GenericAIFollowParents | LegacyMateGoal / LegacyFollowParentGoal；保留配偶、繁殖和母亲 UUID 与品种跟随 |
| GenericAIFollowOwner | LegacyFollowOwnerGoal；原路径失败后传送、5×5 外圈落点、距离、坐下和配置限制 |
| GenericAITempt | LegacyTemptGoal；最近玩家、双手物品、胆怯、互动状态；补花朵与胡萝卜钓竿 |
| GenericAINearestAttackableTarget / GenericAITargetNonTamed | LegacyNearestAttackableTargetGoal；睡眠/坐下及持续驯养状态检查 |
| GenericAIOwnerHurtByTarget / GenericAIOwnerHurtTarget | LegacyGoalRegistration 中原版目标 AI 的睡眠/坐下限制 |
| GenericAIPanic / GenericAIWanderAvoidWater / GenericAILookIdle / GenericAIWatchClosest / GenericAIAvoidEntity / GenericAISit | LegacyGoalRegistration、LegacyIdleLookGoal 与对应现代 Goal；按物种恢复优先级、速度、避敌、日照躲避和睡眠互斥 |

## 动画与状态同步

`GeneratedLegacyMotion` 按原模型分别保留进食、行走、顶头、坐下、站立和尾部动作的公式。
`LegacyMotionContext` 将现代实体状态接到这些方法。静态蛙类模型不附加摆腿动作，跳跃伸缩来自原实体和渲染器公式。

公鸡的打鸣时段、仓鼠的五级储粮和站立状态、猪的飞溅计时同步到客户端。
泥浴表现为侧躺、入泥初期的泥块粒子和随后出现的泥层；原版没有独立翻滚动作。
泥块的显示 tick 按原坐标条件生成八个粒子，泥层使用原来的 1.01 倍缩放。

`GeneratedLegacySleep` 保留农场动物睡眠分支中的计时公式，包括中途改变身体倾斜方向的条件。
普通步长为 0.01，弗里斯兰母羊为 0.0125，终点约为 -0.55；猫狗使用 10 tick 姿势插值。
客户端每 tick 推进一次，普通绘制、眨眼层和 Iris 阴影共用进度。

作息、着火和露天淋雨沿用睡眠 AI 的延迟检查：每三个 tick 累计一次 delay，超过
`ticksBetweenAIFirings` 加随机 0..99 后检查。牵绳、骑乘等即时唤醒条件单独处理。

孔雀睡眠时合拢扇面是本移植的改动，由 `PeacockSleepingFan` 调整原羽毛的支点和角度。
1.12 的实现只将整组尾羽向后放平，并没有合拢扇面。

## 验证范围

这些对应关系用于定位实现和追踪差异，不代表每项行为都已完成游戏测试。
日常修改编译打包后在客户端确认表现，不运行 GameTest 或单元测试。
