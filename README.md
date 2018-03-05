# Java SSE 客户端

​	Java 版本  Server-Sent Events（以下简称 SSE）的客户端，用户可以使用标准 SSE 格式解析使用，也可以根据自己扩展的 SSE 格式进行解析使用。



### 项目环境

java 1.7+

gradle 4.2.0+



### 常用命令

```shell
gradle javaDoc  # 生成 javaDoc
gradle test 	# 执行测试 （注意：目前需要先手动执行 `python src/test/resource/simpleWebInPython2.py`）
gradle java 	# 生成 Jar
```



### 项目结构

**SSE 规范：**

|- SSEParseNorm

​	|- SimpleSSEParse

|- SSECallback

​	|- SSECallbackDefaultAdapter



**SSE 客户端：**

|- SSEWithoutCookieTemplet



### SSE 解析的使用

对于标准 SSE 数据可以

1. 提供事件处理类，实现 `SSECallback` 全部方法 或继承 `SSECallbackDefaultAdapter` 重写需要的方法。
2. 直接使用 `SimpleSSEParse` 做数据解析

```java
// step 1
SSECallback callback = new SSECallback() {
  @Override
  void onStart() {
    System.out.println("It is start.");
  }
  ...
}

// or just want monitor onStart event
// SSECallback callback = new SSECallbackDefaultAdapter() {
//  @Override
//  void onStart() {
//    System.out.println("Just monitor start.");
//  }
// }

// step 2
SimpleSSEParse parse = new SimpleSSEParse();
parse.setInputStream(sseInputStream, sseCharset);
parse.parse(callback);
```

这样就可以执行了，是不是很简单？关于 `SSECallback` 及 `SimpleSSEParse` 的用法请参见相关 JavaDoc。



### SSE 客户端

目前提供不带 cookie 的客户端，底层采用 `OKHttp` 实现。

1. 提供事件处理类，实现 `SSECallback` 全部方法 或继承 `SSECallbackDefaultAdapter` 重写需要的方法。
2. 构建客户端。

注意：仅支持标准 SSE 格式。

```java
// step 1
SSECallback callback = new SSECallback() {
  @Override
  void onStart() {
    System.out.println("It is start.");
  }
  ...
}

// or just want monitor onStart event
// SSECallback callback = new SSECallbackDefaultAdapter() {
//  @Override
//  void onStart() {
//    System.out.println("Just monitor start.");
//  }
// }

// step 2
// 对于 GET 请求可以只提供 Url 地址
// SSEWithoutCookieTemplet templet = new SSEWithoutCookieTemplet(getMethodForUrl);

// 也可以自己准备 Request
SSEWithoutCookieTemplet templet = new SSEWithoutCookieTemplet(request);
templet.execute(callback);
```

更多用法请参见 JavaDoc。