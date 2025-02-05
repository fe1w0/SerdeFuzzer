# SerdeFuzzer

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

使用 DeSerVulnAnalysis(基于doop修改) 扫描，得到 PropertyTree 和 CoveragePath， 这两类文件。
### Fuzzing Java libraries

## Todo:

- [ ] 1 期目标:
  - [ ] 支持以下功能:
    - [x] 提供基本的Fuzzing功能
      - [x] JQF
    - [x] 支持 根据 Property Trees 构造Fuzzing 种子
      - [x] propertyTreeNode
    - [x] 实现 Chains-Coverage Directed Fuzzing
      - 根据 example.jar 手工编写 paths.csv (1.0 版)
      - 编写新的 Guidance，从而实现 Chains-Coverage Directed Fuzzing
    - [x] 可以测试DataSet中的example
- [ ] 2 期目标:
  - [ ] 完善 Generator，补充  和 Reference Types
  - [ ] 测试真实软件


