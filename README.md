# Spark-Alarm  [![Build Status](https://travis-ci.com/yaooqinn/spark-alarm.svg?branch=master)](https://travis-ci.com/yaooqinn/spark-alarm)[![HitCount](http://hits.dwyl.io/yaooqinn/spark-alarm.svg)](http://hits.dwyl.io/yaooqinn/spark-alarm)


## 项目简介

提供一些基本的报警手段，并可以通过SparkListener实现对Spark内部执行逻辑进行监控报警

| 报警模式 | 使用限制 | 简介 |
| ------ | ------ | ------ |
| 邮件 | 通用，无限制 | 通过 SMTP 协议发送告警 |
| 哨兵 | 网易内部使用 | 通过 HTTP 协议发送告警|
| Smilodon | 网易内部使用 | 通过 HTTP 协议发送告警 |

## 使用方法

### 编译

```bash
# 克隆本项目
git https://github.com/yaooqinn/spark-alarm.git
# cd spark-alarm
# mvn clean package
```

可以得到内置示例项目jar包：`streaming-alarmer/target/streaming-alarmer-1.0-SNAPSHOT.jar`，该构件实现了对Streaming程序"异常退出"和""任务堆积"等相关关键事件进行简单的告警服务

## 配置

配置工作分为三个过程：     
- 配置报警方式相关参数     
- 配置报警规则参数     
- 配置spark.extraListeners   
所有的参数都完全等价Spark内置的各项参数，可以通过代码/--conf/spark-default.conf等方式加入

### 配置邮件报警

| 配置 | 默认值 | 说明 |
| ------ | ------ | ------ |
| spark.alarm.email.smtp.host | <空值> | 发信邮件服务器的地址, 比如 smtp-mail.outlook.com |
| spark.alarm.email.smtp.port | 465(ssl)/25(no ssl) | 发信邮件服务器的端口， 587|
| spark.alarm.email.username | <空值> | 发信邮箱的用户名 |
| spark.alarm.email.password | <空值> | 发信邮箱的密码 |
| spark.alarm.email.ssl.on.connect | false | 是否ssl加密连接 |
| spark.alarm.email.from | <空值> | 发信邮箱 |
| spark.alarm.email.to | <空值> | 收信邮箱列表，用逗号分隔 |

参见: https://github.com/yaooqinn/spark-alarm/blob/master/alarm-email/src/test/scala/com/netease/spark/alarm/email/EmailAlarmistTest.scala

### 配置哨兵报警

| 配置 | 默认值 | 说明 |
| ------ | ------ | ------ |
| spark.alarm.sentry.url | <空值> | 哨兵服务地址 |
| spark.alarm.sentry.api.type | Stone | 哨兵服务通知到的终端类型 /** 泡泡 */ POPO, /** 易信 */YiXin, /** 短信 */ SMS, /** 语音 */ Voice, Stone, /** 邮件 */ Email|
| spark.alarm.sentry.app.name | <空值> | 哨兵服务的应用名称，若没有可以去哨兵官网添加 |
| spark.alarm.sentry.app.secret | <空值> | 哨兵服务应用对应的密钥 |
| spark.alarm.sentry.to | <空值> | 收信邮箱、或者ID、电话列表，用逗号分隔，根据具体的终端类型决定 |

参见: alarm-sentry/src/test/scala/com/netease/spark/alarm/sentry/SentryAlarmistTest.scala

### 配置Smilodon报警

| 配置 | 默认值 | 说明 |
| ------ | ------ | ------ |
| spark.alarm.smilodon.url | http://10.165.139.76:8765/v1/alerts | Smilodon服务地址 |
| spark.alarm.smilodon.channels | POPO,STONE | 通知中心终端类型，目前只支持|
| spark.alarm.smilodon.users | <空值> | 收信者的邮箱账号列表，逗号分隔 |
| spark.alarm.smilodon.groups | <空值> | 暂时不用管 |

参见: alarm-smilodon/src/test/scala/com/netease/spark/alarm/smilodon/SmilodonAlarmistTest.scala

注意：Smilodon服务有白名单控制，所以需要用户Spark作业的Driver进程所在的服务器在该列表中，对于Cluster模式则需要所有的节点（如NodeManager）在该列表中

### 配置报警规则参数

该参数列表适用于本项目内置示例项目streaming-alarmer的报警规则，用户可以自己定义相关的SparkListener来定制报警器

| 配置 | 默认值 | 说明 |
| ------ | ------ | ------ |
| spark.alarm.streaming.application.force.exit | false | 出现Application级别错误的时候，是否直接exit |
| spark.alarm.streaming.batch.error.enable | true | 是否对Batch出错的信息进行告警 |
| spark.alarm.streaming.batch.delay.ratio | 1 | Schedule delay时间 / Processing 时间的比值，大于该值，则作为判定为需要告警的必要条件之一  |
| spark.alarm.streaming.batch.process.time.threshold | 1s | 处理时间的阈值，实际处理时间大于该值的批次，才进行报警 |

参见：streaming-alarmer/src/test/scala/com/netease/spark/alarm/alarmer/streaming/EmailStreamingAlarmListenerTest.scala


### 配置spark.extraListeners

开启邮件报警，也可以设置其他实现，或者多个逗号分隔的报警实现，以及自己拓展的实现
```scala
spark.extraListeners=com.netease.spark.alarm.alarmer.streaming.EmailStreamingAlarmListener
```
