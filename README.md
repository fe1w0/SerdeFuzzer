# SerdeFuzzer

该工具主要依赖JQF和ASM使用，支持基于属性树的复制对象生成算法和基于利用链覆盖率的定向灰盒模糊测试，相关输入文件可以参考DataSet目录下的参考文件。

> author: fe1w0

## Setup

按照以下命令进行环境部署。

### mvn
```bash
# 编译
mvn clean package -DskipTests

# fuzz - 默认使用 Zest 算法
mvn jqf:fuzz -Djqf.failOnDeclaredExceptions=true -Dclass=xyz.xzaslxr.fuzzing.SerdeFuzzerTest -Dmethod=fuzz -Dtime=5s

# repro
mvn jqf:repro -Djqf.failOnDeclaredExceptions=true -Dclass=xyz.xzaslxr.fuzzing.SerdeFuzzerTest -Dmethod=reportFuzz -Dtime=5s -Dinput=/Users/fe1w0/Project/SoftWareAnalysis/Dynamic/SerdeFuzzer/target/fuzz-results/xyz.xzaslxr.fuzzing.SerdeFuzzerTest/fuzz/failures/id_000000
```

## Usage

## Prepare the needed configuration files

参考 `DataSet/[利用链别名]/conf` 下的函数调用图、属性树文件、危险函数签名文件（ASM格式）。

### Fuzzing Java libraries

## Todo:

- [x] 1 期目标:
  - [x] 支持以下功能:
    - [x] 提供基本的Fuzzing功能
      - [x] JQF
    - [x] 支持 根据 Property Trees 构造Fuzzing 种子
      - [x] propertyTreeNode
    - [x] 实现 Chains-Coverage Directed Fuzzing
      - 根据 example.jar 手工编写 paths.csv (1.0 版)
      - 编写新的 Guidance，从而实现 Chains-Coverage Directed Fuzzing
    - [x] 可以测试DataSet中的example
- [x] 2 期目标:
  - [x] 完善 Generator，补充  和 Reference Types
  - [x] 测试真实软件


