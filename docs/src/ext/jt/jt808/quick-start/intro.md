---
icon: circle-info
article: false
---

# 介绍

## 部标 808 协议格式

### 消息结构

| 标识位             | 消息头       | 消息体       | 校验码    | 标识位             |
|-----------------|-----------|-----------|--------|-----------------|
| 1 byte(固定为0x7E) | `H` bytes | `B` bytes | 1 byte | 1 byte(固定为0x7E) |

- `H` 的取值情况如下：
    - **V2011 / V2013**
        - 非分包消息: **12** bytes
        - 分包消息: **16** bytes
    - **V2019**
        - 非分包消息: **17** bytes
        - 分包消息: **21** bytes
- `B` 的取值 `>= 0`, 和具体消息类型相关
    - 部分消息无消息体，比如：终端心跳消息

### 消息头结构

不同版本的消息头结构如下：

::: tabs

@tab V2011#1

~~**JT/T 808 V2011**~~ 已经废弃，消息体整体结构和 **JT/T 808 V2013** 一致。

@tab V2013#2

```text {12}
byte[0-2) 	消息ID word(16)
byte[2-4) 	消息体属性 word(16)
		bit[0-10)	消息体长度
		bit[10-13)	数据加密方式
						此三位都为 0,表示消息体不加密
						第 10 位为 1,表示消息体经过 RSA 算法加密
						其它保留
		bit[13]		分包
						1: 消息体卫长消息,进行分包发送处理,具体分包信息由消息包封装项决定
						0: 则消息头中无消息包封装项字段
		bit[14-15]	保留
byte[4-10) 	终端手机号或设备ID bcd[6]
        根据安装后终端自身的手机号转换
        手机号不足12 位,则在前面补 0
byte[10-12) 消息流水号 word(16)
        按发送顺序从 0 开始循环累加
byte[12-16) 	消息包封装项
        如果消息体属性中相关标识位确定消息分包处理,则该项有内容
        否则无该项
        byte[0-2)	消息包总数(word(16)) 该消息分包后得总包数
        byte[2-4)	包序号(word(16))  从 1 开始
```

@tab:active V2019#3

```text {11,13,14}
byte[0-2) 	消息ID word(16)
byte[2-4) 	消息体属性 word(16)
		bit[0-10)	消息体长度
		bit[10-13)	数据加密方式
						此三位都为 0,表示消息体不加密
						第 10 位为 1,表示消息体经过 RSA 算法加密
						其它保留
		bit[13]		分包
						1: 消息体卫长消息,进行分包发送处理,具体分包信息由消息包封装项决定
						0: 则消息头中无消息包封装项字段
		bit[14]	    版本标识
		bit[15]	保留
byte[4]     协议版本号
byte[5-15) 	终端手机号或设备ID bcd[10]
		根据安装后终端自身的手机号转换
		手机号不足12 位,则在前面补 0
byte[15-17) 	消息流水号 word(16)
		按发送顺序从 0 开始循环累加
byte[17-21) 	消息包封装项
        如果消息体属性中相关标识位确定消息分包处理,则该项有内容
		否则无该项
		byte[0-2)	消息包总数(word(16)) 该消息分包后得总包数
		byte[2-4)	包序号(word(16))  从 1 开始
```

:::
