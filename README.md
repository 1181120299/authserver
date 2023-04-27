# 一、概述

Authserver 是一个实现了OAuth2.1和OpenID Connect 1.0规范的认证授权框架。它是在[Spring Authorization Server](https://spring.io/projects/spring-authorization-server)之上完成的，安全、无侵入、支持自定义配置。client应用接入简单方便。授权服务、资源服务、client应用可以部署在不同的服务器，只要网络互通就行。

你可以在这里下载到完整的demo代码：[OAuth2-authorization-demo](https://github.com/1181120299/OAuth2-authorization-demo)

## 1.1 框架特性

- **统一登录认证：**所有client都会重定向到authserver的登录页面完成用户认证。在用户认证授权通过后，再重定向回client应用的页面。
- **单点登录：**用户登录任意一个client之后，无需登录即可访问在authserver注册的任何一个client。
- **自定义用户字段：**你可以在resource server中定义任何你需要的用户字段，在client中都可以拿到用户信息。
- **自定义权限点：**可以在client应用的方法上定义权限点，要求登录的用户拥有此权限点才可以访问该方法。
- **提供管理页面：**可以通过页面完成client应用的注册、用户授权等操作。
- **支持第三方授权**：可以使用github授权登录。

## 1.2 框架组成

这个认证授权框架由三个部分组成：

- 授权服务器：肩负着client、用户认证授权的责任。为client之间互相调用接口提供、解析JWT令牌。实现单点登录功能。开发人员不需要显式调用授权服务器的接口（当然也调用不了，因为access_token是不透明的，没有提供endpoint获取。没有令牌，就不能通过Authorization请求头进行认证访问）。
- [资源服务器：](https://github.com/1181120299/resource-server)提供自定义用户信息的功能。你可以在资源服务器上开发自己的用户信息、组织架构等任意资源。提供接口给client调用获取数据。
- [client客户端：](https://github.com/1181120299/client-authority)需要在授权服务器页面完成client的注册，才可以接入认证授权等功能。换句话说，要接入一个应用非常简单。你只需要在授权服务的管理页面注册一个client应用，然后将client信息配置在你的Spring boot应用即可完成应用接入。

![image-20230426173506408](https://jack-image.oss-cn-shenzhen.aliyuncs.com/image/image-20230426173506408.png)

# 二、开始使用

**环境要求：**

- JDK17 要求JDK版本至少是17，或者更高。
- Spring boot 3.0.0 要求spring-boot-starter-parent 版本至少是3.0.0，或者更高。
- RabbitMQ Server 3.9.11
- Redis（可选）
- mysql 8.0.30

## 2.1 安装授权服务

### 2.1.1 创建Spring boot应用

你可以使用[start.spring.io](https://start.spring.io/)创建一个基础应用，或者使用提供的授权服务示例[default authorization server sample](https://github.com/1181120299/OAuth2-authorization-demo/tree/main/default-authorizationserver)作为引导。然后添加authserver的maven依赖，如下所示：

```xml
<dependency>
    <groupId>com.jack</groupId>
    <artifactId>authserver</artifactId>
    <version>1.0.0</version>
</dependency>
```

> 提示：你需要将authserver项目install到你的本地仓库，才可以添加maven依赖。

### 2.1.2 导入authserver配置

在应用的配置文件application.yml中，导入authserver的配置。如下所示：

```yaml
spring:
  config:
    import: classpath:authserver.yml
```

### 2.1.3 连接数据库

授权服务器所需要的表结构，请查看：[附录1 授权服务器表结构](#附录1 授权服务器表结构)。将表结构加入到你的数据库。并配置数据库信息。如下所示：

```yaml
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    driverClassName: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/demo_auth_server?useUnicode=true&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=GMT%2B8
    username: root
    password: test123456
```

> 提示：请将示例中的数据库信息，替换为你本地实际的连接信息。

### 2.1.4 Github第三方授权

如果需要Github授权登录，请通过配置项提供在Github上注册的OAuth client信息。如下所示：

```yaml
jack:
  oauth2:
    github:
      client-id: foo
      client-secret: bar
```

> 提示：当你不确定有哪些配置项时，输入jack会有所帮助。

### 2.1.5 启动应用

假设你的server.port = 9000，并且没有配置server.servlet.context-path。应用启动后，你可以通过如下示例的链接访问授权服务提供的client管理页面：

http://localhost:9000

![image-20230426132132561](https://jack-image.oss-cn-shenzhen.aliyuncs.com/image/image-20230426132132561.png)

> 提示：（1）系统提供了一个内置的账号。用户名：**jack**。密码：**123456**。此账号不能修改、删除。而且只应该用于登录授权服务器管理client应用，不要在日常的业务系统中使用此账号（因为资源服务器没有jack账号的信息，也不允许创建、修改、删除jack账号。即不能用于权限管理）。
>
> （2）授权服务器会默认创建一个client应用（resource-server），给资源服务器使用。回调应用地址，请根据实际地址进行修改。

## 2.2 安装资源服务

### 2.2.1 下载资源服务代码

下载好资源服务代码[resource-server](https://github.com/1181120299/resource-server)，配置授权服务器地址以及client应用信息。如下所示：

```yaml
jack:
  oauth2:
    auth-server-uri: http://localhost:9000	①
    client-id: resource-server				②
    client-secret: secret					③
    client-ip: 192.168.1.101				④
```

① 授权服务的地址。注意：后面不不不要加斜杠/。

② client应用id（应用名称），前面在授权服务器上注册的client信息。

③ client应用秘钥，前面在授权服务器上注册的client信息。

④ 当前应用部署的服务器ip。需要和前面在授权服务器上注册的client回调应用地址中的ip保持一致。

> 提示：当你不确定有哪些配置项时，输入jack会有所帮助。

### 2.2.2 连接数据库

资源服务器所需要的表结构，请查看：[附录2 资源服务器表结构](#附录2 资源服务器表结构)。将表结构加入到你的数据库。并配置数据库信息。如下所示：

```yaml
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    driverClassName: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/resource_server?useUnicode=true&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=GMT%2B8
    username: root
    password: test123456
```

> 提示：请将示例中的数据库信息，替换为你本地实际的连接信息。

## 2.3 开发client应用

### 2.3.1 创建Spring boot应用

你可以使用[start.spring.io](https://start.spring.io/)创建一个基础应用，或者使用提供的client示例[default client sample](https://github.com/1181120299/OAuth2-authorization-demo/tree/main/default-client-first)作为引导。然后添加client-authority的maven依赖，如下所示：

```xml
<dependency>
    <groupId>com.jack</groupId>
    <artifactId>client-authority</artifactId>
    <version>1.0.0</version>
</dependency>
```

> 提示：你需要将[client-authority](https://github.com/1181120299/client-authority)项目install到你的本地仓库，才可以添加maven依赖。

### 2.3.2 导入client-authority配置

在应用的配置文件application.yml中，导入client-authority的配置。如下所示：

```yaml
spring:
  config:
    import: classpath:client-authority.yml
```

### 2.3.3 注册client

假设你的Spring boot应用有以下的配置：

```yaml
server:
  port: 8080
  servlet:
    context-path: /client-first
```

需要在授权服务器的[管理页面](#2.1.5 启动应用)注册client应用。假设client应用名称为client-first，部署在192.168.1.101服务器。注册完成的信息看起来会像下边的示例这样：

![image-20230426183903743](https://jack-image.oss-cn-shenzhen.aliyuncs.com/image/image-20230426183903743.png)

然后，就可以在你的Spring boot应用中添加client信息了。如下所示：

```yaml
jack:
  oauth2:
    auth-server-uri: http://localhost:9000			①
    resource-server-uri: http://localhost:9001		②
    client-id: client-first							③
    client-secret: secret							④
    client-ip: 192.168.1.101						⑤
```

① 授权服务的地址。注意：后面不不不要加斜杠/。

② 资源服务的地址。注意：后面不不不要加斜杠/。

③ client应用id（应用名称），前面在授权服务器上注册的client信息。

④ client应用秘钥，前面在授权服务器上注册的client信息。

⑤ 当前应用部署的服务器ip。需要和前面在授权服务器上注册的client回调应用地址中的ip保持一致。

### 2.3.4 连接数据库

client-authority所需要的表结构，请查看：[附录3 client应用表结构](#附录3 client应用表结构)。将表结构加入到你的数据库。并配置数据库信息。如下所示：

```yaml
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    driverClassName: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/client-first?useUnicode=true&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=GMT%2B8
    username: root
    password: test123456
```

> 提示：请将示例中的数据库信息，替换为你本地实际的连接信息。

### 2.3.5 启动应用

启动client应用。并且访问http://localhost:8080/client-first将会重定向到授权服务器登录页面：

![image-20230426191011916](https://jack-image.oss-cn-shenzhen.aliyuncs.com/image/image-20230426191011916.png)

登录成功后，回调client应用主页：

![image-20230426191247526](https://jack-image.oss-cn-shenzhen.aliyuncs.com/image/image-20230426191247526.png)

到这里，client应用接入完成。

## 2.4 小结

当安装好授权服务、资源服务后。要接入新的应用还是非常简单的。你只需要在授权服务管理页面上注册好client，将client的信息、授权服务地址、资源服务地址填入到你的应用配置文件中即可。不需要任何其他的设置。对你的应用而言，几乎没有侵入感。

# 三、配置模型

这一小节主要介绍一些默认的配置项，以及如何自定义配置。

## 3.1 登录页

授权服务器默认提供了登录页。入口类`com.jack.authserver.controller.LoginController`。该类只有一个`login()`方法，方法返回登录页模块的名称，使用Spring thymeleaf模板引擎进行渲染。

如果你需要自定义登录页，你可以通过实现`com.jack.authserver.annotation.LoginEntryProvider`接口，并且将你的实现类注册成Controller。例如：

```java
@Controller
public class CustomLoginController implements LoginEntryProvider {
    @Override
    public String login(HttpServletRequest request, HttpServletResponse response) {
        return "customLoginPage";
    }
}
```

> 提示：登录页是授权服务提供的。如果要自定义，请在你的[授权应用](#2.1 安装授权服务)中提供LoginEntryProvider接口的实现类

## 3.2 接口返回字段

授权服务、资源服务、client应用提供的JSON数据接口，返回的字段有三个，示例如下：

```json
{
    "data": {},				// 业务数据
    "retCode": 2000,		// 正常状态码：2000，错误状态码：2999
    "retMsg": "请求成功"	 // 提示信息
}
```

如果字段的名称，或者状态码和你的系统定义的不一样。你可以通过配置项进行修改。例如：

```yaml
jack:
  mapper:
    response-code-field: customCode
    response-message-field: customMsg
    response-data-field: customData
    response-correct-code: 200
    response-error-code: 500
```

> 注意：如果你要修改返回的字段名称或者状态码。那么授权服务、资源服务、client应用都要增加上面的配置项一起修改。

## 3.3 应用间接口调用策略

假设A应用调用B应用的接口，会有以下的步骤：

1. 首先A应用会拿自身的client信息去授权服务器获取JWT令牌。
2. 然后携带令牌访问B应用的接口。
3. B应用拿JWT令牌到授权服务器校验合法性。如果合法，允许访问接口。否则拒绝访问。

从上面的步骤可以知道，JWT令牌中，只包含了client应用的信息。这也是OAuth2协议的定义：用户同意授权A应用访问用户在B应用中的资源。

但是，我要实现跨应用的权限控制，就需要将用户信息编码进JWT令牌中，或者通过请求头传递用户信息。

默认的实现，是当A应用在请求B应用接口时，将用户信息写入请求头。然后会替换B应用解析JWT令牌后的Authentication对象的name属性。这是由`com.jack.clientauthority.annotation.defaultImpl.ClientAccessUsernameStrategyImpl`类进行控制的。

如果你不需要这个特性，你可以通过提供`com.jack.clientauthority.annotation.ClientAccessUsernameStrategy`接口的实现类，然后提供一个不做任何操作的策略。例如：

```java
@Component
public class NoopClientAccessUserNameStrategy implements ClientAccessUsernameStrategy {
    @Override
    public Strategy strategy() {
        return Strategy.NOOP;
    }
}
```

当然，如果你这么做了，那么应用之间将不能调用自定义权限点（@NeedPermission）的接口，因为获取不到用户信息。

## 3.4 client应用主页

在用户完成登录后，会重定向会应用主页。默认由`com.jack.clientauthority.annotation.defaultImpl.DefaultHomePageController`类提供实现。它会输出一句话：请提供HomePageProvider的实现类，定义应用主页。

你可以提供`HomePageProvider`接口的实现类，返回主页的模板，或者重定向到你的主页中。

> 注意：你的`HomePageProvider`接口的实现类，需要注册成Spring bean。例如在类上标注@Component注解。

# 四、主要的组件接口

## 4.1 获取用户信息

你可以在资源服务（resource-server）中自定义任何你需要的用户字段。然后在client应用中，通过`com.jack.clientauthority.utils.UserHelper`类获取用户信息。UserHelper主要有两个接口，功能是一样的。其中一个需要传入自定义用户的Class对象，方法签名如下所示：

```java
public static <T> T getUserinfo(Class<T> userClass)
```

如果你觉得每次获取用户信息，都要传递一个用户类的Class对象，是一件很麻烦的事。没关系，还有另外一个无参的方法。方法签名如下：

```java
public static CustomUserType getUserinfo()
```

在调用这个接口之前，请确保你的用户类实现了`com.jack.clientauthority.annotation.CustomUserType`接口。实现此接口的用户类，可能长这样：

```java
@Component
public class User implements CustomUserType {
   private String username;
   private Integer gender;
   private String hobby;
   // any other field
}
```

用户信息因为需要从资源服务获取，提供了Redis缓存实现（如果你有提供Redis相关配置，就会激活缓存）。当修改、删除用户时，会清空缓存。

如果你需要缓存用户信息，可以提供Redis配置。例如：

```yaml
jack:
  redis:
    time-to-live: 60
spring:
  cache:
    redis:
      use-key-prefix: true
  data:
    redis:
      database: 9
      host: localhost
      port: 6379
      jedis:
        pool:
          max-active: 8
          max-wait: -1ms
          max-idle: 8
          min-idle: 0
      timeout: 2000ms
```

## 4.2 应用间接口调用

client应用之间互相调用接口，通过`com.jack.clientauthority.utils.WebClientHelper`类提供的方法完成。

`WebClientHelper`在发送请求时，会获取JWT令牌，并且传递用户信息。下面是一个接口调用的例子：

```java
@GetMapping("/requestOtherClient")
public R requestOtherClient() {
    List<User> userList = Collections.emptyList();

     // Fake uri, you can provide a client who`s name is client-second
    String uri = "http://localhost:9090/client-second/foo";    
    R resp = WebClientHelper.get(uri, R.class);
    if (resp.getCode() == R.getCodeOk()) {
        userList = resp.getDataList(User.class);
    }

    return R.ok().setData(userList);
}
```

## 4.3 应用间跳转

从A应用跳转到B应用，非常的简单，前端只需要`window.open('B应用地址')`就可以了。不需要任何其他的操作。因为实现了单点登录。

当然，基于可配置的观点，建议由后端进行转发或者重定向，将B应用地址写在配置文件中。

# 五、自定义权限点

基于OAuth2协议，假设B应用有一个foo()方法，如下所示：

```java
@PreAuthorize("hasAuthority('write')")
public void foo() {}
```

A应用在调用B应用的foo()方法时，就会要求用户同意授权（当然，我把授权页面关了）。如果用户同意授予"SCOPE_write"的权限给A应用，那么A应用就可以访问B应用的foo()方法。换句话说，所有的用户，只要同意授权，就可以访问foo()方法。

但是，在日常的开发中，会存在这样的业务场景：有一些接口以及页面，只允许一部分人（例如：领导）访问，底下的卡拉米没有资格。

所以，作为OAuth2协议的补充，提供了`@NeedPermission`注解，用于自定义权限点。使用示例如下：

```java
@NeedPermission("查看设备清单")
public void foo() {}
```

加上`@NeedPermission`注解后，只有当用户拥有"查看设备清单"的权限，才会调用接口。否则抛出AccessDeniedException拒绝访问。

这里需要介绍几个概念：

- 用户：即通过用户名密码登录系统的用户。
- 角色：即某种类型的用户。比如说：管理员、测试员。
- 权限：即使用`@NeedPermission`注解定义的权限点。例如上面提到的"查看设备清单"。
- 菜单：即某种类型的用户能够访问的菜单。比如管理员能够看到"设备管理"等菜单。

出于方便操作以及复用性目的，权限、菜单是根据角色来关联的。然后给用户分配角色。

Client应用添加了client-authority的maven依赖后，默认提供了管理角色、权限、菜单的功能。

在应用的访问链接后面加**appConfig**后缀，即可访问管理页面。例如：

http://192.168.1.101:8080/client-first/appConfig

![image-20230426235319898](https://jack-image.oss-cn-shenzhen.aliyuncs.com/image/image-20230426235319898.png)

## 5.1 配置应用菜单

配置应用的菜单，主要是配置菜单编码，然后给到前端，用于控制菜单项是否显示。页面如下所示：

![image-20230426235929522](https://jack-image.oss-cn-shenzhen.aliyuncs.com/image/image-20230426235929522.png)

操作说明：

- 选择左侧树节点，然后鼠标右键弹出操作选项。
- 选择左侧树节点，然后ctrl + c可以复制菜单信息。例如：桌面称=/deviceManage/touchScale

## 5.2 角色配置

![image-20230427000329619](https://jack-image.oss-cn-shenzhen.aliyuncs.com/image/image-20230427000329619.png)

### 5.2.1 关联用户

由你来决定，哪些人是"领导"（拥有角色：领导）。页面如下：

![image-20230427001848553](https://jack-image.oss-cn-shenzhen.aliyuncs.com/image/image-20230427001848553.png)

> 提示：从选择框可以看到，并没有授权服务器内置的账号：jack

### 5.2.2 关联菜单

由你来决定，领导能看到哪些菜单。页面如下：

![image-20230427002338302](https://jack-image.oss-cn-shenzhen.aliyuncs.com/image/image-20230427002338302.png)

### 5.2.3 关联权限

由你来决定，领导拥有哪些权限。页面如下：

![image-20230427002816395](https://jack-image.oss-cn-shenzhen.aliyuncs.com/image/image-20230427002816395.png)

这里会扫描应用中定义的所有权限点。可以看到DeviceController还贴心的加上了注释：设备管理相关接口。这是通过swagger的注解`@Api`实现的。代码如下：

```java
@RestController
@Api(tags = "设备管理相关接口")
@RequestMapping("/device")
public class DeviceController {

    @NeedPermission("查看设备列表")
    @GetMapping("/page")
    public R page() {
        return R.ok();
    }

    @NeedPermission("删除设备")
    @GetMapping("/delete")
    public R delete(String id) {
        return R.ok();
    }
}
```

我们刚刚给"chen"这个账号分配了领导角色，然后领导角色拥有"删除设备"的权限。那么用户chen就可以访问DeviceController的delete()方法。

> 提示：这里说的chen账号可以访问delete()方法，不只是可以通过DeviceController所在的应用页面进行调用。而且chen登录了别的应用，依然有权限访问delete()方法。换句话说，支持client应用之间接口相互调用的权限控制。

## 5.3 权限点主要接口

主要有两个接口，由`com.jack.clientauthority.controller.RoleController`类提供。

**（1）获取用户可以访问的菜单**，接口示例：http://192.168.1.101:8080/client-first/role/getUserMenus?username=chen

数据格式看起来像这样：

```json
{
    "data": [
        {
            "id": "e86d21c00bd4a2ab0f3c6aa287c05c59",
            "parentId": "dfc7efc2dcfb48cb5dead20d0b0cd058",
            "name": "桌面称",
            "code": "/touchScale",
            "completeCode": "/deviceManage/touchScale",
            "sort": 0,
            "description": "智能桌面称重终端"
        }
    ],
    "retCode": 2000,
    "retMsg": "请求成功"
}
```

前端通过返回的菜单数据（例如菜单的完整编码completeCode），来控制显示哪些菜单。

**（2）获取用户可以访问的权限点**，接口示例：http://192.168.1.101:8080/client-first/role/getUserPermissions?username=chen

数据格式看起来像这样：

```json
{
    "data": [
        {
            "code": "c.j.d.C.DeviceController.delete",
            "description": "删除设备",
            "fromClassName": "com.jack.defaultclientfirst.Controller.DeviceController",
            "fromClassSimpleName": "DeviceController",
            "fromClassDesc": "设备管理相关接口"
        }
    ],
    "retCode": 2000,
    "retMsg": "请求成功"
}
```

前端可以通过返回的权限数据（例如权限编码code），来控制某些组件是否显示（例如"删除设备"的按钮）。

# 六、补充说明

（1）完整的demo代码，你可以在这里下载：[demo链接](https://github.com/1181120299/OAuth2-authorization-demo)

（2）你的client应用，由于单点登录的需要，必须配置server.servlet.context-path。

（3）session过期，前端jquery请求302重定向的问题。你可以提供一个全局异常处理器，直接捕获Exception或者Throwable。这样你的接口确保返回http状态码200。然后在你的jquery请求中，增加请求错误时的回调，可以刷新页面，这样就会重定向到登录页面。授权成功后再重定向回到当前页面。示例代码：

```javascript
$.ajax({
    type: "GET",
    url: "/book/jump/doSomething",
    success: function (data) {},
    error: function () {
        alert("登录已过期");
        window.location.reload();
    }
})
```

（4）最后一点，目前不支持用户登出功能。虽然Spring Authorization Server在1.0.2版本中，官方提供的demo有登出的配置，但是demo是使用gradle管理依赖的。使用maven从阿里云仓库拉下来的依赖，并没有登出的配置，留待后续完善。

# 七、贡献代码

欢迎[Pull requests](https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/proposing-changes-to-your-work-with-pull-requests/creating-a-pull-request)，或者通过Issues报告bug，提出你的优化建议或者分享你的idea。

# 八、许可

Authserver以及配套的client-authority是在[Apache2.0许可](https://www.apache.org/licenses/LICENSE-2.0.html)下发布的开源软件。

# 九、联系方法

你可以通过qq邮箱找到我：1181120299@qq.com

# 附录1 授权服务器表结构

```sql
CREATE TABLE `users`  (
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '加密后的密码',
  `enabled` tinyint(1) NOT NULL COMMENT '是否启用。1：启用，0：禁用',
  PRIMARY KEY (`username`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户（spring security用户）' ROW_FORMAT = Dynamic;

CREATE TABLE `authorities`  (
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `authority` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '权限',
  UNIQUE INDEX `ix_auth_username`(`username` ASC, `authority` ASC) USING BTREE,
  CONSTRAINT `fk_authorities_users` FOREIGN KEY (`username`) REFERENCES `users` (`username`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户-权限' ROW_FORMAT = Dynamic;

CREATE TABLE `groups`  (
  `id` bigint NOT NULL,
  `group_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

CREATE TABLE `group_authorities`  (
  `group_id` bigint NOT NULL,
  `authority` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  INDEX `fk_group_authorities_group`(`group_id` ASC) USING BTREE,
  CONSTRAINT `fk_group_authorities_group` FOREIGN KEY (`group_id`) REFERENCES `groups` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

CREATE TABLE `group_members`  (
  `id` bigint NOT NULL,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `group_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_group_members_group`(`group_id` ASC) USING BTREE,
  CONSTRAINT `fk_group_members_group` FOREIGN KEY (`group_id`) REFERENCES `groups` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

CREATE TABLE `oauth2_authorization`  (
  `id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `registered_client_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `principal_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `authorization_grant_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `authorized_scopes` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `attributes` blob NULL,
  `state` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `authorization_code_value` blob NULL,
  `authorization_code_issued_at` timestamp NULL DEFAULT NULL,
  `authorization_code_expires_at` timestamp NULL DEFAULT NULL,
  `authorization_code_metadata` blob NULL,
  `access_token_value` blob NULL,
  `access_token_issued_at` timestamp NULL DEFAULT NULL,
  `access_token_expires_at` timestamp NULL DEFAULT NULL,
  `access_token_metadata` blob NULL,
  `access_token_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `access_token_scopes` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `oidc_id_token_value` blob NULL,
  `oidc_id_token_issued_at` timestamp NULL DEFAULT NULL,
  `oidc_id_token_expires_at` timestamp NULL DEFAULT NULL,
  `oidc_id_token_metadata` blob NULL,
  `refresh_token_value` blob NULL,
  `refresh_token_issued_at` timestamp NULL DEFAULT NULL,
  `refresh_token_expires_at` timestamp NULL DEFAULT NULL,
  `refresh_token_metadata` blob NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

CREATE TABLE `oauth2_authorization_consent`  (
  `registered_client_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `principal_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `authorities` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`registered_client_id`, `principal_name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

CREATE TABLE `oauth2_registered_client`  (
  `id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键id',
  `client_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '客户端id，唯一不可重复。作为应用名称',
  `client_id_issued_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '此客户端发布时间',
  `client_secret` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '客户端秘钥',
  `client_secret_expires_at` timestamp NULL DEFAULT NULL COMMENT '此客户端秘钥过期时间',
  `client_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'uuid名称',
  `client_authentication_methods` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '客户端认证方法',
  `authorization_grant_types` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '授权类型',
  `redirect_uris` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '重定向地址，多个以英文逗号分割',
  `scopes` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '授予的作用域',
  `client_settings` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '客户端设置',
  `token_settings` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '访问令牌设置',
  `description` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '客户端描述',
  `redirect_uri_simple` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '重定向应用地址。例如http://localhost:8080/application',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_clientid`(`client_id` ASC) USING BTREE COMMENT '应用名称唯一'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'OAuth2已注册客户端' ROW_FORMAT = Dynamic;

```

# 附录2 资源服务器表结构

```sql
-- 自定义用户信息，加入任何你需要的字段。
CREATE TABLE `t_user`  (
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '加密后的密码',
  `gender` tinyint NULL DEFAULT NULL COMMENT '性别。1：男，0：女',
  `hobby` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '爱好',
  `enabled` tinyint(1) NOT NULL COMMENT '是否启用。1：启用，0：禁用',
  PRIMARY KEY (`username`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE COMMENT '用户名唯一'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户信息（定义业务系统需要的用户信息）' ROW_FORMAT = Dynamic;
```

# 附录3 client应用表结构

```sql

CREATE TABLE `t_menu`  (
  `id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键id',
  `parent_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '父级菜单id，根节点id = 0',
  `name` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '菜单名称',
  `code` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '菜单编码，以/开头，英文字母',
  `complete_code` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '完整的菜单编码，即包含了父级菜单的编码。以/开头，英文字母',
  `sort` int NOT NULL DEFAULT 0 COMMENT '顺序，值越小排越前面',
  `description` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '应用菜单项' ROW_FORMAT = Dynamic;

CREATE TABLE `t_role`  (
  `id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键',
  `name` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色名称，不可重复',
  `code` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色编码，不可重复',
  `description` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_name`(`name` ASC) USING BTREE COMMENT '角色名称，不可重复',
  UNIQUE INDEX `uk_code`(`code` ASC) USING BTREE COMMENT '角色编码，不可重复'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '应用角色' ROW_FORMAT = Dynamic;

CREATE TABLE `t_role_menu`  (
  `role_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色id',
  `menu_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '菜单项id',
  PRIMARY KEY (`role_id`, `menu_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色-菜单 对应关系' ROW_FORMAT = Dynamic;

CREATE TABLE `t_role_permission`  (
  `role_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色id',
  `permission_code` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '权限代码',
  PRIMARY KEY (`role_id`, `permission_code`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色-权限 对应关系' ROW_FORMAT = Dynamic;

CREATE TABLE `t_role_user`  (
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `role_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色id',
  PRIMARY KEY (`username`, `role_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户-角色 对应关系' ROW_FORMAT = Dynamic;
```

